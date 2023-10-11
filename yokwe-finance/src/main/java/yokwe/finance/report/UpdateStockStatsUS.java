package yokwe.finance.report;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import yokwe.finance.Storage;
import yokwe.finance.provider.monex.TradingStockMonex;
import yokwe.finance.provider.moomoo.TradingStockMoomoo;
import yokwe.finance.provider.rakuten.TradingStockRakuten;
import yokwe.finance.provider.sbi.TradingStockSBI;
import yokwe.finance.stock.StockDivUS;
import yokwe.finance.stock.StockInfoUS;
import yokwe.finance.stock.StockPriceUS;
import yokwe.finance.type.TradingStockType;
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

public class UpdateStockStatsUS {
	private static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();
	
	private static final String URL_TEMPLATE  = StringUtil.toURLString("data/form/STOCK_STATS_US.ods");
	
	private static String tradingString(Map<String, TradingStockType> map, String stockCode) {
		return map.containsKey(stockCode) ? Integer.toString(map.get(stockCode).feeType.value): "";
	}
	
	
	private static List<StockStatsUS> getStatsList() {
		var dateStop  = MarketHoliday.US.getLastTradingDate();
		var dateStart = dateStop.minusYears(1).plusDays(1);
		
		logger.info("date range  {}  -  {}", dateStart, dateStop);
		
		var list = new ArrayList<StockStatsUS>();
		{
			var monexMap   = TradingStockMonex.getMap();
			var sbiMap     = TradingStockSBI.getMap();
			var rakutenMap = TradingStockRakuten.getMap();
			var moomooMap  = TradingStockMoomoo.getMap();
			
			for(var stockInfo: StockInfoUS.getList()) {
				var stockCode = stockInfo.stockCode;
				var priceList = StockPriceUS.getList(stockCode).stream().filter(o -> !o.date.isBefore(dateStart) && !o.date.isAfter(dateStop)).collect(Collectors.toList());
				var divArray  = StockDivUS.getList(stockCode).stream().filter(o -> !o.date.isBefore(dateStart) && !o.date.isAfter(dateStop)).mapToDouble(o -> o.value.doubleValue()).toArray();

				if (priceList.isEmpty()) {
					logger.info("no price info {}", stockCode);
					continue;
				}
				
				var priceLast = priceList.get(priceList.size() - 1);
				if (DoubleUtil.isAlmostZero(priceLast.close.doubleValue())) {
					logger.warn("Skip price is zero  {}", stockCode);
					continue;
				}
				
				
				StockStatsUS statsUS = new StockStatsUS();
				{
					
					int      pricec      = priceList.size();
					double[] closeArray  = priceList.stream().mapToDouble(o -> o.close.doubleValue()).toArray();
					double[] volumeArray = priceList.stream().mapToDouble(o -> o.volume).toArray();
					
					statsUS.stockCode = stockCode;
					
					statsUS.monex     = tradingString(monexMap, stockCode);
					statsUS.nikko     = ""; // FIXME
					statsUS.sbi       = tradingString(sbiMap, stockCode);
					statsUS.rakuten   = tradingString(rakutenMap, stockCode);
					statsUS.moomoo    = tradingString(moomooMap, stockCode);
									
					statsUS.type      = stockInfo.type.toString();
					statsUS.name      = stockInfo.name;
					statsUS.date      = priceLast.date.toString();
					
					statsUS.pricec    = priceList.size();
					statsUS.price     = priceLast.close.doubleValue();
					
					
					// last
					if (2 <= pricec) {
						var lastClose = priceList.get(priceList.size() - 2).close.doubleValue();
						if (DoubleUtil.isAlmostZero(lastClose)) {
							statsUS.last = -1;
						} else {
							statsUS.last = SimpleReturn.getValue(lastClose, statsUS.price);
						}
					} else {
						statsUS.last = -1;
					}
					
					// stats - sd hv rsi
					if (30 <= pricec) {
						double logReturn[] = DoubleArray.logReturn(closeArray);
						DoubleStreamUtil.Stats stats = new DoubleStreamUtil.Stats(logReturn);
						
						double sd = stats.getStandardDeviation();
						statsUS.sd = Double.isNaN(sd) ? -1 : DoubleUtil.round(sd, 4);

						HV hv = new HV(closeArray);
						statsUS.hv = Double.isNaN(hv.getValue()) ? -1 : DoubleUtil.round(hv.getValue(), 4);
					} else {
						statsUS.sd = -1;
						statsUS.hv = -1;
					}
					if (RSI.DEFAULT_PERIDO <= pricec) {
						RSI rsi = new RSI();
						Arrays.stream(closeArray).forEach(rsi);
						double rsiValue = rsi.getValue();
						if (Double.isFinite(rsiValue)) {
							statsUS.rsi = DoubleUtil.round(rsi.getValue(), 1);
						} else {
							statsUS.rsi = -1;
						}
					} else {
						statsUS.rsi = -1;
					}
					
					// min max
					{
						var min = priceList.stream().mapToDouble(o -> o.low.doubleValue()).min().getAsDouble();
						var max = priceList.stream().mapToDouble(o -> o.high.doubleValue()).max().getAsDouble();
						statsUS.min = DoubleUtil.round((statsUS.price - min) / statsUS.price, 3);
						statsUS.max = DoubleUtil.round((max - statsUS.price) / statsUS.price, 3);
					}
					
					// dividend
					{
						if (divArray.length == 0) {
							statsUS.divc  = 0;
							statsUS.yield = 0;
						} else {
							var div = Arrays.stream(divArray).sum();
							statsUS.divc  = divArray.length;
							statsUS.yield = DoubleUtil.round(div / statsUS.price, 3);
						}
					}
					
					// volume
					statsUS.vol       = priceLast.volume;
					if (5 <= pricec) {
						MA vol5 = MA.sma(5, volumeArray);
						statsUS.vol5 = (long)vol5.getValue();
					} else {
						statsUS.vol5 = -1;
					}
					if (20 <= pricec) {
						MA vol21 = MA.sma(21, volumeArray);
						statsUS.vol21 = (long)vol21.getValue();
					} else {
						statsUS.vol21 = -1;
					}
				}
				list.add(statsUS);
			}
		}

		return list;
	}
	
	
	private static void generateReport(List<StockStatsUS> statsList) {
		String urlReport;
		{
			String timestamp  = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss").format(LocalDateTime.now());
			String name       = String.format("stock-stats-us-%s.ods", timestamp);
			String pathReport = Storage.report_stock_stats_us.getPath(name);
			urlReport  = StringUtil.toURLString(pathReport);
		}

		logger.info("urlReport {}", urlReport);
		logger.info("docLoad   {}", URL_TEMPLATE);
		try {
			// start LibreOffice process
			LibreOffice.initialize();
			
			SpreadSheet docLoad = new SpreadSheet(URL_TEMPLATE, true);
			SpreadSheet docSave = new SpreadSheet();
			
			String sheetName = Sheet.getSheetName(StockStatsUS.class);
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
		logger.info("save {} {}", statsList.size(), StockStatsUS.getPath());
		StockStatsUS.save(statsList);
		
		generateReport(statsList);
	}
	
	public static void main(String[] args) {
		logger.info("START");
		
		update();
				
		logger.info("STOP");
	}
}
