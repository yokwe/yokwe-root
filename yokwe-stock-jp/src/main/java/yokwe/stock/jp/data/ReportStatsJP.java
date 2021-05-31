package yokwe.stock.jp.data;

import java.io.File;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import yokwe.stock.jp.edinet.EDINETInfo;
import yokwe.stock.jp.edinet.FundInfo;
import yokwe.stock.jp.jpx.Price;
import yokwe.stock.jp.jpx.Stock;
import yokwe.stock.jp.jpx.StockInfo;
import yokwe.util.DoubleUtil;
import yokwe.util.JapanHoliday;
import yokwe.util.libreoffice.Sheet;
import yokwe.util.libreoffice.SpreadSheet;
import yokwe.util.stats.DoubleArray;
import yokwe.util.stats.DoubleStreamUtil;
import yokwe.util.stats.HV;
import yokwe.util.stats.MA;
import yokwe.util.stats.RSI;

public class ReportStatsJP {
	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(ReportStatsJP.class);
	
	public static final String TIMESTAMP = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss").format(LocalDateTime.now());

	public static final String URL_TEMPLATE      = "file:///home/hasegawa/Dropbox/Trade/TEMPLATE_STATS_JP.ods";
	public static final String URL_REPORT        = String.format("file:///home/hasegawa/Dropbox/Trade/Report/stats-jp-%s.ods", TIMESTAMP);

	private static final LocalDate DATE_LAST  = JapanHoliday.getLastTradingDate();

	private static StatsJP getInstance(Stock stock, List<Price> priceList) {
		StatsJP ret = new StatsJP();
		
		StockInfo stockInfo = StockInfo.get(stock.stockCode);
		EDINETInfo    edinet    = EDINETInfo.getFromStockCode(stock.stockCode);
		FundInfo      fund      = FundInfo.getFromStockCodeFundName(stock.stockCode, stock.name);

//		this.exchange = stock.exchange;
		ret.stockCode = stock.stockCode;
		
		ret.name      = stock.name;
		ret.unit      = stockInfo.tradeUnit;
		if (stock.sector33.equals("-") && stock.sector17.equals("-")) {
			if (stock.isETF()) {
				ret.sector33 = "ETF";
				ret.sector17 = "ETF";
			} else if (stock.isREIT()) {
				ret.sector33 = "REIT";
				ret.sector17 = "REIT";
			} else {
				ret.sector33 = stock.market.toString();
				ret.sector17 = stock.market.toString();
			}
		} else {
			ret.sector33 = stock.sector33;
			ret.sector17 = stock.sector17;
		}
		
		{
			Price lastPrice = priceList.get(priceList.size() - 1);
			ret.date  = lastPrice.date;
			ret.price = DoubleUtil.round(lastPrice.close, 2);
			ret.vol   = lastPrice.volume;
		}

		{
			// Limit to 1 year
			double[] priceArray = priceList.stream().mapToDouble(o -> o.close).toArray();
			ret.pricec = priceArray.length;
			
			{
				double logReturn[] = DoubleArray.logReturn(priceArray);
				DoubleStreamUtil.Stats stats = new DoubleStreamUtil.Stats(logReturn);
				
				double sd = stats.getStandardDeviation();
				ret.sd = Double.isNaN(sd) ? -1 : DoubleUtil.round(sd, 4);
			}
			
			HV hv = new HV(priceArray);
			ret.hv = Double.isNaN(hv.getValue()) ? -1 : DoubleUtil.round(hv.getValue(), 4);
			
			if (RSI.DEFAULT_PERIDO < priceArray.length) {
				RSI rsi = new RSI();
				Arrays.stream(priceArray).forEach(rsi);
				ret.rsi = DoubleUtil.round(rsi.getValue(), 1);
			} else {
				ret.rsi = -1;
			}
			
			{
				double min = priceList.get(0).low;
				double max = priceList.get(0).high;
				for(Price price: priceList) {
					if (price.low < min)  min = price.low;
					if (max < price.high) max = price.high;
				}
				ret.min    = DoubleUtil.round(min, 2);
				ret.max    = DoubleUtil.round(max, 2);
				ret.minPCT = DoubleUtil.round((ret.price - ret.min) / ret.price, 3);
				ret.maxPCT = DoubleUtil.round((ret.max - ret.price) / ret.price, 3);
			}
			
			
			// price change detection
			ret.last    = (priceArray.length < 2) ? -1 : priceArray[priceArray.length - 2];
			ret.lastPCT = DoubleUtil.round((ret.price - ret.last) / ret.last, 3) ;
		}
		
		// dividend
		{
			DividendAnnual divAnn = DividendAnnual.getMap().get(ret.stockCode);
			if (divAnn == null) {
				ret.div   = 0;
				ret.divc  = 0;
				ret.yield = 0;
			} else {
				ret.div   = divAnn.dividend;
				ret.divc  = divAnn.count;
				ret.yield = DoubleUtil.round(divAnn.dividend / ret.price, 3);
			}
		}
		
		// volume
		{
			double[] volArray = priceList.stream().mapToDouble(o -> o.volume).toArray();

			MA vol5 = MA.sma(5, volArray);
			ret.vol5 = (long)vol5.getValue();

			MA vol21 = MA.sma(21, volArray);
			ret.vol21 = (long)vol21.getValue();
		}
		
		// endDate1 endDate2
		{
			if (fund != null) {
				ret.endDate1 = fund.specialDate1;
				ret.endDate2 = fund.specialDate2;
			} else {
				if (edinet != null) {
					ret.endDate1 = edinet.closingDate;
					ret.endDate2 = "";
				} else {
					ret.endDate1 = "";
					ret.endDate2 = "";
				}
			}
		}
		
		BigDecimal mllion = BigDecimal.valueOf(1, -6);
		BigDecimal kilo   = BigDecimal.valueOf(1, -3);

		// marketCap
		{
			BigDecimal numberOfIssued = BigDecimal.valueOf(stockInfo.issued);
			BigDecimal price          = BigDecimal.valueOf(ret.price);
			BigDecimal vol            = BigDecimal.valueOf(ret.vol);
			
			BigDecimal marketCap = numberOfIssued.multiply(price).divide(mllion, RoundingMode.HALF_UP);
			BigDecimal trade     = vol.multiply(price).divide(mllion, RoundingMode.HALF_UP);

			ret.numberOfIssuedK = numberOfIssued.divide(kilo).longValue();
			ret.marketCapM      = marketCap.longValue();
			ret.tradeCapM       = trade.longValue();
			if (stockInfo.issued == 0) {
				ret.volPCT          = 0;
			} else {
				ret.volPCT          = DoubleUtil.round((double)ret.vol / (double)stockInfo.issued, 3);
			}
		}
		
		// feb17pct
		{
			Price priceFeb17 = Price.getPrice(ret.stockCode, "2020-02-17");
			if (priceFeb17 == null) {
				ret.feb17PCT = -1;
			} else {
				ret.feb17PCT = DoubleUtil.round((ret.price - priceFeb17.close) / priceFeb17.close, 3);
			}
		}
		
		
		return ret;
	}
	
	private static List<StatsJP> getStatsList() {
		final LocalDate firstPriceDate;
		{
			LocalDate date = DATE_LAST.minusYears(1).plusDays(1);
			if (JapanHoliday.isClosed(date)) {
				date = JapanHoliday.getNextTradingDate(date);
			}
			firstPriceDate = date;
		}
		
		final Set<LocalDate> priceListDateSet = new TreeSet<>();
		{
			LocalDate date = firstPriceDate;
			for(;;) {
				if (date.isAfter(DATE_LAST)) break;
				
				if (JapanHoliday.isClosed(date)) {
					//
				} else {
					priceListDateSet.add(date);
				}
				date = date.plusDays(1);
			}
		}
		final int priceListCount = priceListDateSet.size();

		logger.info("date {} - {}  {}", firstPriceDate, DATE_LAST, priceListCount);

		List<StatsJP> statsList = new ArrayList<>();
		
		Collection<Stock> stockList = Stock.getList();
		
		int total = stockList.size();
		int count = 0;
		
		int showInterval = 1000;
		boolean showOutput;
		int lastOutputCount = -1;
		for(Stock stock: stockList) {
			String stockCode = stock.stockCode;

			int outputCount = count / showInterval;
			if (outputCount != lastOutputCount) {
				showOutput = true;
				lastOutputCount = outputCount;
			} else {
				showOutput = false;
			}

			count++;
			if (showOutput) logger.info("{}  update {}", String.format("%4d / %4d",  count, total), stockCode);
			
			{
				StockInfo stockInfo = StockInfo.get(stockCode);
				if (stockInfo == null) {
					logger.info("{}  no StockInfo {}", String.format("%4d / %4d",  count, total), stockCode);
					continue;
				}
			}
			
			File priceFile    = new File(Price.getPath(stockCode));
			
			List<Price> priceList;
			if (priceFile.exists()) {
				priceList = new ArrayList<>();
				
				for(Price e: Price.getList(stockCode)) {
					if (0 < e.open && 0 < e.high && 0 < e.low && 0 < e.close) {
						LocalDate date = LocalDate.parse(e.date);
						if (date.isEqual(firstPriceDate) || date.isAfter(firstPriceDate)) {
							priceList.add(e);
						}
					}
				}
				Collections.sort(priceList);
				
				{
					if (priceList.size() != priceListCount) {
						{
							Set<LocalDate> set = new TreeSet<>(priceListDateSet);
							priceList.stream().forEach(o -> set.remove(LocalDate.parse(o.date)));
							if (set.isEmpty()) {
								//
							} else {
								List<LocalDate> dateList = priceList.stream().map(o -> LocalDate.parse(o.date)).collect(Collectors.toList());
								Collections.sort(dateList);
								if (dateList.size() == 1) {
									logger.warn("{}  small  {}", String.format("%4d / %4d",  count, total), String.format("%5s %4d  %3d[%s]", stockCode, priceListCount, dateList.size(), dateList.get(0)));
								} else {
									logger.warn("{}  small  {}", String.format("%4d / %4d",  count, total), String.format("%5s %4d  %3d[%s .. %s]", stockCode, priceListCount, dateList.size(), dateList.get(0), dateList.get(dateList.size() - 1)));
								}
							}
						}
						{
							Set<LocalDate> set = priceList.stream().map(o -> LocalDate.parse(o.date)).collect(Collectors.toSet());
							priceListDateSet.stream().forEach(o -> set.remove(o));
							if (set.isEmpty()) {
								//
							} else {
								List<LocalDate> dateList = priceList.stream().map(o -> LocalDate.parse(o.date)).collect(Collectors.toList());
								Collections.sort(dateList);
								if (dateList.size() == 1) {
									logger.warn("{}  SMALL  {}", String.format("%4d / %4d",  count, total), String.format("%5s %4d  %3d[%s]", stockCode, priceListCount, dateList.size()), dateList.get(0));
								} else {
									logger.warn("{}  SMALL  {}", String.format("%4d / %4d",  count, total), String.format("%5s %4d  %3d[%s .. %s]", stockCode, priceListCount, dateList.size(), dateList.get(0), dateList.get(dateList.size() - 1)));
								}
							}
						}
					}
				}
			} else {
				continue;
			}
			
			
			StatsJP statsJP = getInstance(stock, priceList);
			if (statsJP != null) statsList.add(statsJP);
		}
		return statsList;
	}

	public static void main(String[] args) {
		logger.info("START");
		
		boolean onlyCSV = Boolean.getBoolean("onlyCSV");
		logger.info("onlyCSV {}", onlyCSV);

		List<StatsJP> statsList = getStatsList();
		logger.info("save {} {}", StatsJP.PATH_FILE, statsList.size());
		StatsJP.save(statsList);
		
		if (!onlyCSV) {
			logger.info("build report");
			try (
					SpreadSheet docLoad = new SpreadSheet(URL_TEMPLATE, true);
					SpreadSheet docSave = new SpreadSheet()) {

					String sheetName = Sheet.getSheetName(StatsJP.class);
					docSave.importSheet(docLoad, sheetName, docSave.getSheetCount());
					Sheet.fillSheet(docSave, statsList);
					
					String newSheetName = String.format("%s",  DATE_LAST.toString());
					logger.info("sheet {}", newSheetName);
					docSave.renameSheet(sheetName, newSheetName);

					// remove first sheet
					docSave.removeSheet(docSave.getSheetName(0));

					docSave.store(URL_REPORT);
					logger.info("output {}", URL_REPORT);
					docLoad.close();
				}
		}
		
		logger.info("STOP");
		System.exit(0);
	}
}
