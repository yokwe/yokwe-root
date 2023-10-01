package yokwe.stock.us.nasdaq;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import yokwe.stock.us.Storage;
import yokwe.stock.us.TradingStock;
import yokwe.util.DoubleUtil;
import yokwe.util.MarketHoliday;
import yokwe.util.StringUtil;
import yokwe.util.libreoffice.Sheet;
import yokwe.util.libreoffice.SpreadSheet;
import yokwe.util.stats.DoubleArray;
import yokwe.util.stats.DoubleStreamUtil;
import yokwe.util.stats.HV;
import yokwe.util.stats.MA;
import yokwe.util.stats.RSI;

public class UpdateStockStats {
	private static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();
	
	private static final String PREFIX_REPORT = "report";
	private static final String URL_TEMPLATE  = StringUtil.toURLString(Storage.NASDAQ.getPath("TEMPLATE_NASDAQ_STOCK_STATS.ods"));

	private static List<StockStats> getStatsList(Set<String> dateSet) {
		List<StockStats> statsList = new ArrayList<>();
		Map<String, StockPrice> map = StockPrice.getMap();
		
		Map<String, StockDividend> stockDividendMap = StockDividend.getMap();
		
		for(var e: TradingStock.getList()) {
			String     symbol     = e.symbol;
			
			List<Price> priceList = Price.getList(symbol).stream().filter(o -> dateSet.contains(o.date)).collect(Collectors.toList());
			// skip if priceList is empty
			if (priceList.isEmpty()) {
				logger.info("no price info {}", symbol);
				continue;
			}

			int pricec = priceList.size();
			double[] closeArray  = priceList.stream().mapToDouble(o -> o.close).toArray();
			double[] volumeArray = priceList.stream().mapToDouble(o -> o.volume).toArray();
			
			StockPrice stockPrice = map.get(symbol);
			
			Price price = priceList.get(pricec - 1);
			StockStats statsUS = new StockStats();
			
			statsUS.stockCode = e.symbol;
			
			statsUS.monex     = e.monex;
			statsUS.sbi       = e.sbi;
			statsUS.rakuten   = e.rakuten;
			statsUS.nikko     = e.nikko;
			statsUS.moomoo    = e.moomoo;
			
			statsUS.type      = e.type.toString();
			statsUS.name      = e.name;
			statsUS.date      = stockPrice.dateLast;
			
			statsUS.pricec    = pricec;
			statsUS.price     = price.close;
			
			if (DoubleUtil.isAlmostZero(statsUS.price)) {
				logger.warn("Skip price is zero  {}", symbol);
				continue;
			}
			
			// last
			if (2 <= pricec) {
				Price last = priceList.get(pricec - 2);
				if (DoubleUtil.isAlmostZero(last.close)) {
					statsUS.last = -1;
				} else {
					var lastClose = last.close;
					statsUS.last = DoubleUtil.round((statsUS.price - lastClose) / lastClose, 3) ;
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
				var min = priceList.stream().mapToDouble(o -> o.low).min().getAsDouble();
				var max = priceList.stream().mapToDouble(o -> o.high).max().getAsDouble();
				statsUS.min = DoubleUtil.round((statsUS.price - min) / statsUS.price, 3);
				statsUS.max = DoubleUtil.round((max - statsUS.price) / statsUS.price, 3);
			}
			
			// dividend
			{
				StockDividend stockDividend = stockDividendMap.get(symbol);
				if (stockDividend == null) {
					statsUS.divc  = 0;
					statsUS.yield = 0;
				} else {
					var div = stockDividend.annual;
					
					statsUS.divc  = stockDividend.count;
					statsUS.yield = DoubleUtil.round(div / statsUS.price, 3);
				}
			}
			
			// volume
			statsUS.vol       = price.volume;
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

			statsList.add(statsUS);
		}
		return statsList;
	}
	
	private static Set<String> getDateSet() {
		Set<String> dateSet = new TreeSet<>();
		{
			LocalDate lastDate = MarketHoliday.US.getLastTradingDate();
			LocalDate firstDate = lastDate.minusYears(1).plusDays(1);
			if (MarketHoliday.US.isClosed(firstDate)) {
				firstDate = MarketHoliday.US.getPreviousTradingDate(firstDate);
			}
			
			for(LocalDate date = firstDate; date.isEqual(lastDate) || date.isBefore(lastDate); date = MarketHoliday.US.getNextTradingDate(date)) {
				dateSet.add(date.toString());
			}
		}
		return dateSet;
	}
	
	private static void generateReport(List<StockStats> statsList) {
		String urlReport;
		{
			String timestamp  = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss").format(LocalDateTime.now());
			String name       = String.format("stock-stats-%s.ods", timestamp);
			String pathReport = Storage.NASDAQ.getPath(PREFIX_REPORT, name);
			urlReport  = StringUtil.toURLString(pathReport);
		}

		logger.info("urlReport {}", urlReport);
		logger.info("docLoad   {}", URL_TEMPLATE);
		try (
			SpreadSheet docLoad = new SpreadSheet(URL_TEMPLATE, true);
			SpreadSheet docSave = new SpreadSheet();) {				
			String sheetName = Sheet.getSheetName(StockStats.class);
			logger.info("sheet {}", sheetName);
			docSave.importSheet(docLoad, sheetName, docSave.getSheetCount());
			Sheet.fillSheet(docSave, statsList);
			
			// remove first sheet
			docSave.removeSheet(docSave.getSheetName(0));

			docSave.store(urlReport);
			logger.info("output {}", urlReport);
		}
	}

	
	public static void main(String[] args) {
		try {
			logger.info("START");
			
			Set<String> dateSet = getDateSet();
			logger.info("date {}  {} - {}", dateSet.size(), dateSet.stream().min(String::compareTo).get(), dateSet.stream().max(String::compareTo).get());
			
//			int dateCount = dateSet.size();
			List<StockStats> statsList = getStatsList(dateSet);
			
			logger.info("save {} {}", statsList.size(), StockStats.getPath());
			StockStats.save(statsList);
			
			generateReport(statsList);
			
			logger.info("STOP");
		} catch (Throwable e) {
			String exceptionName = e.getClass().getSimpleName();
			logger.error("{} {}", exceptionName, e);
		} finally {
			System.exit(0);
		}
		
	}
}
