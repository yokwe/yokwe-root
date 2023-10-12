package yokwe.finance.report;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import yokwe.finance.Storage;
import yokwe.finance.stock.StockDivJP;
import yokwe.finance.stock.StockInfoJP;
import yokwe.finance.stock.StockPriceJP;
import yokwe.util.DoubleUtil;
import yokwe.util.MarketHoliday;
import yokwe.util.StringUtil;
import yokwe.util.finance.online.SimpleReturn;
import yokwe.util.libreoffice.LibreOffice;
import yokwe.util.libreoffice.Sheet;
import yokwe.util.libreoffice.SpreadSheet;
import yokwe.util.stats.DoubleArray;
import yokwe.util.stats.DoubleStreamUtil;
import yokwe.util.stats.HV;
import yokwe.util.stats.MA;
import yokwe.util.stats.RSI;

public class UpdateStockStatsJP {
	private static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();
	
	private static final String URL_TEMPLATE  = StringUtil.toURLString("data/form/STOCK_STATS_JP.ods");
	
	private static List<StockStatsJP> getStatsList() {
		var dateStop  = MarketHoliday.JP.getLastTradingDate();
		var dateStart = dateStop.minusYears(1).plusDays(1);
		
		logger.info("date range  {}  -  {}", dateStart, dateStop);
		
		var list = new ArrayList<StockStatsJP>();
		{
			for(var stockInfo: StockInfoJP.getList()) {
				var stockCode = stockInfo.stockCode;
				var priceList = StockPriceJP.getList(stockCode).stream().filter(o -> !o.date.isBefore(dateStart) && !o.date.isAfter(dateStop)).collect(Collectors.toList());
				var divArray  = StockDivJP.getList(stockCode).stream().filter(o -> !o.date.isBefore(dateStart) && !o.date.isAfter(dateStop)).mapToDouble(o -> o.value.doubleValue()).toArray();

				if (priceList.isEmpty()) {
					logger.info("no price info {}", stockCode);
					continue;
				}
				
				var priceLast = priceList.get(priceList.size() - 1);
				if (DoubleUtil.isAlmostZero(priceLast.close.doubleValue())) {
					logger.warn("Skip price is zero  {}", stockCode);
					continue;
				}
				
				
				StockStatsJP statsJP = new StockStatsJP();
				{
					
					int      pricec      = priceList.size();
					double[] closeArray  = priceList.stream().mapToDouble(o -> o.close.doubleValue()).toArray();
					double[] volumeArray = priceList.stream().mapToDouble(o -> o.volume).toArray();
					
					statsJP.stockCode = stockCode;
					
					statsJP.type      = stockInfo.type.simpleType.toString();
					statsJP.name      = stockInfo.name;
					statsJP.date      = priceLast.date.toString();
					
					statsJP.pricec    = priceList.size();
					statsJP.price     = priceLast.close.doubleValue();
					
					
					// last
					if (2 <= pricec) {
						var lastClose = priceList.get(priceList.size() - 2).close.doubleValue();
						if (DoubleUtil.isAlmostZero(lastClose)) {
							statsJP.last = -1;
						} else {
							statsJP.last = SimpleReturn.getValue(lastClose, statsJP.price);
						}
					} else {
						statsJP.last = -1;
					}
					
					// stats - sd hv rsi
					if (30 <= pricec) {
						double logReturn[] = DoubleArray.logReturn(closeArray);
						DoubleStreamUtil.Stats stats = new DoubleStreamUtil.Stats(logReturn);
						
						double sd = stats.getStandardDeviation();
						statsJP.sd = Double.isNaN(sd) ? -1 : DoubleUtil.round(sd, 4);

						HV hv = new HV(closeArray);
						statsJP.hv = Double.isNaN(hv.getValue()) ? -1 : DoubleUtil.round(hv.getValue(), 4);
					} else {
						statsJP.sd = -1;
						statsJP.hv = -1;
					}
					if (RSI.DEFAULT_PERIDO <= pricec) {
						RSI rsi = new RSI();
						Arrays.stream(closeArray).forEach(rsi);
						double rsiValue = rsi.getValue();
						if (Double.isFinite(rsiValue)) {
							statsJP.rsi = DoubleUtil.round(rsi.getValue(), 1);
						} else {
							statsJP.rsi = -1;
						}
					} else {
						statsJP.rsi = -1;
					}
					
					// min max
					{
						var min = priceList.stream().mapToDouble(o -> o.low.doubleValue()).min().getAsDouble();
						var max = priceList.stream().mapToDouble(o -> o.high.doubleValue()).max().getAsDouble();
						statsJP.min = DoubleUtil.round((statsJP.price - min) / statsJP.price, 3);
						statsJP.max = DoubleUtil.round((max - statsJP.price) / statsJP.price, 3);
					}
					
					// dividend
					{
						if (divArray.length == 0) {
							statsJP.divc  = 0;
							statsJP.yield = 0;
						} else {
							var div = Arrays.stream(divArray).sum();
							statsJP.divc  = divArray.length;
							statsJP.yield = DoubleUtil.round(div / statsJP.price, 3);
						}
					}
					
					// volume
					statsJP.vol       = priceLast.volume;
					if (5 <= pricec) {
						MA vol5 = MA.sma(5, volumeArray);
						statsJP.vol5 = (long)vol5.getValue();
					} else {
						statsJP.vol5 = -1;
					}
					if (20 <= pricec) {
						MA vol21 = MA.sma(21, volumeArray);
						statsJP.vol21 = (long)vol21.getValue();
					} else {
						statsJP.vol21 = -1;
					}
				}
				list.add(statsJP);
			}
		}

		return list;
	}
	
	
	private static void generateReport(List<StockStatsJP> statsList) {
		String urlReport;
		{
			String timestamp  = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss").format(LocalDateTime.now());
			String name       = String.format("stock-stats-jp-%s.ods", timestamp);
			String pathReport = Storage.report_stock_stats_jp.getPath(name);
			urlReport  = StringUtil.toURLString(pathReport);
		}

		logger.info("urlReport {}", urlReport);
		logger.info("docLoad   {}", URL_TEMPLATE);
		try {
			// start LibreOffice process
			LibreOffice.initialize();
			
			SpreadSheet docLoad = new SpreadSheet(URL_TEMPLATE, true);
			SpreadSheet docSave = new SpreadSheet();
			
			String sheetName = Sheet.getSheetName(StockStatsJP.class);
			logger.info("sheet     {}", sheetName);
			docSave.importSheet(docLoad, sheetName, docSave.getSheetCount());
			Sheet.fillSheet(docSave, statsList);
			
			// remove first sheet
			docSave.removeSheet(docSave.getSheetName(0));

			docSave.store(urlReport);
			logger.info("output    {}", urlReport);
			
			docLoad.close();
			logger.info("close     docLoad");
			docSave.close();
			logger.info("close     docSave");
		} finally {
			// stop LibreOffice process
			LibreOffice.terminate();
		}
	}
	
	
	private static void update() {
		var statsList = getStatsList();
		logger.info("save {} {}", statsList.size(), StockStatsJP.getPath());
		StockStatsJP.save(statsList);
		
		generateReport(statsList);
	}
	
	public static void main(String[] args) {
		logger.info("START");
		
		update();
				
		logger.info("STOP");
	}
}
