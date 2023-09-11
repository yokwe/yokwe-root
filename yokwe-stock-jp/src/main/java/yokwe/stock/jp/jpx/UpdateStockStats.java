package yokwe.stock.jp.jpx;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import yokwe.stock.jp.Storage;
import yokwe.stock.jp.japanreit.REIT;
import yokwe.stock.jp.japanreit.REITDiv;
import yokwe.stock.jp.moneybujpx.ETF;
import yokwe.stock.jp.moneybujpx.ETFDiv;
import yokwe.stock.jp.toushin.Fund;
import yokwe.stock.jp.xbrl.tdnet.report.Dividend;
import yokwe.util.DoubleUtil;
import yokwe.util.MarketHoliday;
import yokwe.util.StringUtil;
import yokwe.util.UnexpectedException;
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
	private static final String URL_TEMPLATE  = StringUtil.toURLString(Storage.JPX.getPath("TEMPLATE_JPX_STOCK_STATS.ods"));
	
	private static void generateReport(List<StockStats> statsList) {
		String urlReport;
		{
			String timestamp  = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss").format(LocalDateTime.now());
			String name       = String.format("stock-stats-%s.ods", timestamp);
			String pathReport = Storage.JPX.getPath(PREFIX_REPORT, name);
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

	private static List<StockStats> getStatsList() {
		List<StockStats> statsList = new ArrayList<>();
		
		final LocalDate dateLast  = MarketHoliday.JP.getLastTradingDate();
		final LocalDate dateFirst;
		{
			LocalDate date = dateLast.minusYears(1).plusDays(1);
			dateFirst = MarketHoliday.JP.isClosed(date) ? MarketHoliday.JP.getNextTradingDate(date) : date;
		}
		Set<String> dateSet = new TreeSet<>();
		//  date YYYY-MM-DD
		{
			LocalDate date = dateFirst;
			for(;;) {
				dateSet.add(date.toString());
				if (date.compareTo(dateLast) == 0) break;
				date = date.plusDays(1);
			}
		}
		
		Map<String, Map<String, Dividend>>  divMap = Dividend.getMap();
		//  stockCode   date
		
		Map<String, ETF> etfMap = ETF.getMap();
		//  stockCode
		
		Map<String, REIT> reitMap = REIT.getMap();
		//  stockCode
		
		Map<String, Fund> fundMap = Fund.getMap();
		//  isinCode
		
		for(var stock: Stock.getList()) {
			String      stockCode = stock.stockCode;
			StockInfo   info      = StockInfo.get(stockCode);
			if (info == null) {
				logger.warn("No stock info  {}  {}", stock.stockCode, stock.name);
				continue;
			}

			List<Price> priceList;
			{
				var list = Price.getList(stockCode);
				if (list == null) {
					logger.warn("No price  {}  {}", stock.stockCode, stock.name);
					continue;
				}
				priceList = list.stream().filter(o -> dateSet.contains(o.date)).collect(Collectors.toList());
				if (priceList.size() == 0) {
					logger.warn("Empty price list  {}  {}", stock.stockCode, stock.name);
					continue;
				}
				Collections.sort(priceList);
			}
			Price lastPrice = priceList.get(priceList.size() - 1);
			
			final ETF etf = etfMap.get(stockCode);
			if (stock.isETF() || stock.isETN()) {
				if (etf == null) {
					logger.warn("No etf info  {}  {}", stockCode, stock.name);
				}
			}
			
			final REIT reit = reitMap.get(stockCode);
			if (stock.isREIT() || stock.isInfraFund()) {
				if (reit == null) {
					logger.warn("No reit info  {}  {}", stockCode, stock.name);
				}
			}
			
			final Fund fund = fundMap.get(info.isinCode);
			
			
			StockStats stockStats = new StockStats();
			// stockCode name category unit issued
			stockStats.isinCode  = info.isinCode;
			stockStats.stockCode = stock.stockCode;
			stockStats.name      = stock.name;
			if (stock.isETF() || stock.isETN()) {
				if (etf != null) {
					stockStats.category = "ETF-" + etf.categoryName.replace("ETF", "");
				} else {
					stockStats.category = "ETF";
				}
			} else {
				if (stock.sector33.equals("-")) {
					stockStats.category = stock.stockKind.toString();
				} else {
					stockStats.category = stock.sector33;
				}
			}
			stockStats.unit      = info.tradeUnit;
			stockStats.issued    = BigDecimal.valueOf(info.issued);
			
			if (reit != null) {
				stockStats.category = "REIT-" + reit.category.replace(" ", "");
			}
			

			// priceDate pricec price marketCap
			stockStats.priceDate = lastPrice.date;
			stockStats.pricec    = priceList.size();
			stockStats.price     = DoubleUtil.toBigDecimal(lastPrice.close, 1);
			stockStats.marketCap = stockStats.issued.multiply(stockStats.price).movePointLeft(6).setScale(0, RoundingMode.HALF_UP); // unit in million
			// priceLastPCT priceMinPCT priceMaxPCT
			if (2 <= priceList.size()) {
				Price      previous      = priceList.get(priceList.size() - 2);
				BigDecimal previousPrice = DoubleUtil.toBigDecimal(previous.close, 1);
				stockStats.priceLastPCT = (previousPrice.subtract(stockStats.price)).divide(stockStats.price, 3, RoundingMode.HALF_UP);
			} else {
				stockStats.priceLastPCT = BigDecimal.ZERO;
			}
			{
				var priceMin = DoubleUtil.toBigDecimal(priceList.stream().map(m -> m.close).min(Comparator.naturalOrder()).get(), 1);
				var priceMax = DoubleUtil.toBigDecimal(priceList.stream().map(m -> m.close).max(Comparator.naturalOrder()).get(), 1);
				stockStats.priceMinPCT = stockStats.price.subtract(priceMin).divide(stockStats.price, 3, RoundingMode.HALF_UP);
				stockStats.priceMaxPCT = priceMax.subtract(stockStats.price).divide(stockStats.price, 3, RoundingMode.HALF_UP);
			}
			
			// sd hv rsi
			{
				double[] priceArray = priceList.stream().mapToDouble(o -> o.close).toArray();
				
				{
					double logReturn[] = DoubleArray.logReturn(priceArray);
					DoubleStreamUtil.Stats stats = new DoubleStreamUtil.Stats(logReturn);
					double value = stats.getStandardDeviation();
					
					stockStats.sd = Double.isNaN(value) ? BigDecimal.ONE.negate() : DoubleUtil.toBigDecimal(value, 4);
				}
				{
					double value = new HV(priceArray).getValue();
					
					stockStats.hv = Double.isNaN(value) ? BigDecimal.ONE.negate() : DoubleUtil.toBigDecimal(value, 4);
				}
				if (RSI.DEFAULT_PERIDO < priceArray.length) {
					RSI rsi = new RSI();
					Arrays.stream(priceArray).forEach(rsi);
					stockStats.rsi = DoubleUtil.toBigDecimal(rsi.getValue(), 1);
				} else {
					stockStats.rsi = BigDecimal.ONE.negate();
				}
			}
			
			
			// divc div div1Y yiedlLast yiedl1Y
			stockStats.divc      = 0;
			stockStats.divLast   = BigDecimal.ZERO;
			stockStats.div1Y     = BigDecimal.ZERO;
			stockStats.yieldLast = BigDecimal.ZERO;
			stockStats.yield1Y   = BigDecimal.ZERO;
			{
				double[] divArray = null;
				
				if (etf != null) {
					List<ETFDiv> list = ETFDiv.getList(stockCode);
					if (list != null && !list.isEmpty()) {
						Collections.sort(list);
						var last = list.get(list.size() - 1);
						var firstDate = LocalDate.parse(last.date).minusYears(1).plusDays(1).toString();
						
						divArray = list.stream().filter(o -> 0 <= o.date.compareTo(firstDate)).mapToDouble(o -> o.amount.doubleValue()).toArray();
						if (etf.divFreq != divArray.length) {
							logger.warn("divFreq not match  {}  divArray {}  ETF  difFreq {}  {}", stockCode, divArray.length, etf.divFreq, etf.listingDate);
						}
						if (fund != null && etf.divFreq != fund.divFreq) {
							logger.error("divFreq not match");
							logger.error("  fund     {}", fund.divFreq);
							logger.error("  etf      {}", etf.divFreq);
							logger.error("  divArray {}", divArray.length);
							throw new UnexpectedException("divFreq not match");
						}
						
						stockStats.divc      = etf.divFreq;
						stockStats.divLast   = DoubleUtil.toBigDecimal(divArray[divArray.length - 1], 3);
						// yiedlLast = divLast * divc / price
						stockStats.yieldLast = stockStats.divLast.multiply(BigDecimal.valueOf(stockStats.divc)).divide(stockStats.price, 3, RoundingMode.HALF_UP);
						stockStats.div1Y     = DoubleUtil.toBigDecimal(Arrays.stream(divArray).sum(), 3);
						// if divArray doesn't contain 1 year data
						if (etf.divFreq != divArray.length) {
							// adjust div1Y  div1Y = div1Y * divc / divArray.length
							stockStats.div1Y   = stockStats.div1Y.multiply(BigDecimal.valueOf(stockStats.divc)).divide(BigDecimal.valueOf(divArray.length), 15, RoundingMode.HALF_UP);
						}
						// yiedl1Y = div1Y / price
						stockStats.yield1Y   = stockStats.div1Y.divide(stockStats.price, 3, RoundingMode.HALF_UP);
					}
				} else if (reit != null) {
					List<REITDiv> list = REITDiv.getList(stockCode);
					if (list != null && !list.isEmpty()) {
						Collections.sort(list);

						var last = list.get(list.size() - 1);
						var firstDate = LocalDate.parse(last.date).minusYears(1).plusDays(1).toString();

						divArray = list.stream().filter(o -> 0 <= o.date.compareTo(firstDate)).mapToDouble(o -> o.actual).toArray();

						if (reit.divFreq != divArray.length) {
							logger.warn("divFreq not match  {}  difArray {}  REIT divFreq {}  {}", stockCode, divArray.length, reit.divFreq, reit.listingDate);
						}
						
						stockStats.divc      = reit.divFreq;
						stockStats.divLast   = DoubleUtil.toBigDecimal(divArray[divArray.length - 1], 3);
						// yiedlLast = divLast * divc / price
						stockStats.yieldLast = stockStats.divLast.multiply(BigDecimal.valueOf(stockStats.divc)).divide(stockStats.price, 3, RoundingMode.HALF_UP);
						stockStats.div1Y     = DoubleUtil.toBigDecimal(Arrays.stream(divArray).sum(), 3);
						// if divArray doesn't contain 1 year data
						if (reit.divFreq != divArray.length) {
							// adjust div1Y  div1Y = div1Y * divc / divArray.length
							stockStats.div1Y   = stockStats.div1Y.multiply(BigDecimal.valueOf(stockStats.divc)).divide(BigDecimal.valueOf(divArray.length), 15, RoundingMode.HALF_UP);
						}
						// yiedl1Y = div1Y / price
						stockStats.yield1Y   = stockStats.div1Y.divide(stockStats.price, 3, RoundingMode.HALF_UP);
					}
				} else {
					if (divMap.containsKey(stockCode)) {
						var list = divMap.get(stockCode).values().stream().collect(Collectors.toList());
						
						if (list != null && !list.isEmpty()) {
							Collections.sort(list);
							var last = list.get(list.size() - 1);
							var firstDate = LocalDate.parse(last.payDate).minusYears(1).plusDays(7).toString();
							
							divArray = list.stream().filter(o -> 0 <= o.payDate.compareTo(firstDate)).mapToDouble(o -> o.dividend).toArray();
						}
						
						stockStats.divc      = divArray.length;
						stockStats.divLast   = DoubleUtil.toBigDecimal(divArray[divArray.length - 1], 3);
						stockStats.div1Y     = DoubleUtil.toBigDecimal(Arrays.stream(divArray).sum(), 3);
						stockStats.yieldLast = stockStats.divLast.multiply(BigDecimal.valueOf(stockStats.divc)).divide(stockStats.price, 3, RoundingMode.HALF_UP);
						stockStats.yield1Y   = stockStats.div1Y.divide(stockStats.price, 3, RoundingMode.HALF_UP);
					}
				}
			}
			
			// vol vol5 vol21
			{
				double[] volArray = priceList.stream().mapToDouble(o -> o.volume).toArray();
				double   vol5     = MA.sma( 5, volArray).getValue();
				double   vol21    = MA.sma(21, volArray).getValue();
				
				stockStats.vol   = BigDecimal.valueOf(lastPrice.volume);
				stockStats.vol5  = DoubleUtil.toBigDecimal(vol5, 0);
				stockStats.vol21 = DoubleUtil.toBigDecimal(vol21, 0);
			}
			
			statsList.add(stockStats);
		}
		
		
		return statsList;
	}
	
	public static void main(String[] args) {
		try {
			logger.info("START");
			
			List<StockStats> statsList = getStatsList();
			logger.info("save {} {}", StockStats.getPath(), statsList.size());
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
