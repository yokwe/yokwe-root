package yokwe.stock.us.nasdaq;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import yokwe.stock.us.Storage;
import yokwe.stock.us.nasdaq.api.API;
import yokwe.stock.us.nasdaq.api.Summary;
import yokwe.util.CSVUtil;
import yokwe.util.StringUtil;
import yokwe.util.UnexpectedException;

public class StockSummary implements CSVUtil.Detail {
	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(StockSummary.class);

	private static final String PATH_FILE = Storage.NASDAQ.getPath("stock-summary.csv");
	
	public static void save(List<StockSummary> list) {
		CSVUtil.save(StockSummary.class, list);
	}
	public static List<StockSummary> getList() {
		return CSVUtil.getList(StockSummary.class);
	}
	public static Map<String, StockSummary> getMap() {
		return CSVUtil.getMap(StockSummary.class);
	}
	
	public static void download(List<StockList> stockList) {
		List<StockList> list = new ArrayList<>();
		for(var e: stockList) {
			File file = new File(Summary.getPath(e.symbol));
			if (file.exists()) continue;
			list.add(e);
		}
		logger.info("StockSummary download {}", list.size());

		int countTotal = list.size();
		int count = 0;
		for(var e: list) {
			if ((count % 100) == 0) logger.info("{}", String.format("%5d / %5d  %s", count, countTotal, e.symbol));
			count++;
			
			String symbol = e.symbol;
			
			if (e.assetClass.equals(API.ETF)) {
				Summary.ETF.getInstance(symbol);
			} else if (e.assetClass.equals(API.STOCK)) {
				Summary.Stock.getInstance(symbol);
			} else {
				logger.error("Unexpected");
				logger.error("  assetClass {}!", e.assetClass);
				throw new UnexpectedException("Unexpected");
			}
		}
	}
	public static void update(List<StockList> stockList) {
		logger.info("StockSummary update {}", stockList.size());
		List<StockSummary> list = new ArrayList<>(stockList.size());
		
		int countTotal = stockList.size();
		int count = 0;
		for(var e: stockList) {
			if ((count % 1000) == 0) logger.info("{}", String.format("%5d / %5d  %s", count, countTotal, e.symbol));
			count++;

			String symbol     = e.symbol;
			String assetClass = e.assetClass;
			
			String annualizedDividend;
//				String beta;
			String dividendPaymentDate;
			String exDividendDate;
//				String fiftTwoWeekHighLow;
			String marketCap;
			String previousClose;
			String shareVolume;
			String todayHighLow;
			String yield;

			if (e.assetClass.equals(API.ETF)) {
				Summary.ETF summary = Summary.ETF.getInstance(symbol);
				if (summary.data == null) {
					logger.warn("data is null {}", e.symbol);
					continue;
				}
				annualizedDividend  = summary.data.summaryData.annualizedDividend.value.replace("N/A", "").replace(",", "").replace("$", "");
//					beta                = summary.data.summaryData.beta.value;
				dividendPaymentDate = summary.data.summaryData.dividendPaymentDate.value.replace("N/A", "").replace(",", "").replace("$", "");
				exDividendDate      = summary.data.summaryData.exDividendDate.value.replace("N/A", "").replace(",", "").replace("$", "");
//					fiftTwoWeekHighLow  = summary.data.summaryData.fiftTwoWeekHighLow.value;
				marketCap           = summary.data.summaryData.marketCap.value.replace("N/A", "").replace(",", "").replace("$", "");
				previousClose       = summary.data.summaryData.previousClose.value.replace("N/A", "").replace(",", "").replace("$", "");
				shareVolume         = summary.data.summaryData.shareVolume.value.replace("N/A", "").replace(",", "").replace("$", "");
				todayHighLow        = summary.data.summaryData.todayHighLow.value.replace("N/A", "").replace(",", "").replace("$", "");
				yield               = summary.data.summaryData.yield.value.replace("N/A", "").replace(",", "").replace("$", "");
			} else if (e.assetClass.equals(API.STOCK)) {
				Summary.Stock summary = Summary.Stock.getInstance(symbol);
				if (summary.data == null) {
					logger.warn("data is null {}", e.symbol);
					continue;
				}
				annualizedDividend  = summary.data.summaryData.annualizedDividend.value.replace("N/A", "").replace(",", "").replace("$", "");
//					beta                = summary.data.summaryData.beta.value;
				dividendPaymentDate = summary.data.summaryData.dividendPaymentDate.value.replace("N/A", "").replace(",", "").replace("$", "");
				exDividendDate      = summary.data.summaryData.exDividendDate.value.replace("N/A", "").replace(",", "").replace("$", "");
//					fiftTwoWeekHighLow  = summary.data.summaryData.fiftTwoWeekHighLow.value;
				marketCap           = summary.data.summaryData.marketCap.value.replace("N/A", "").replace(",", "").replace("$", "");
				previousClose       = summary.data.summaryData.previousClose.value.replace("N/A", "").replace(",", "").replace("$", "");
				shareVolume         = summary.data.summaryData.shareVolume.value.replace("N/A", "").replace(",", "").replace("$", "");
				todayHighLow        = summary.data.summaryData.todayHighLow.value.replace("N/A", "").replace(",", "").replace("$", "");
				yield               = summary.data.summaryData.yield.value.replace("N/A", "").replace(",", "").replace("$", "");
			} else {
				logger.error("Unexpected");
				logger.error("  assetClass {}!", e.assetClass);
				throw new UnexpectedException("Unexpected");
			}

			StockSummary stockSummary = new StockSummary(
				symbol,
				assetClass,
				
				annualizedDividend,
//						beta,
				dividendPaymentDate,
				exDividendDate,
//						fiftTwoWeekHighLow,
				marketCap,
				previousClose,
				shareVolume,
				todayHighLow,
				yield
				);
			list.add(stockSummary);
		}
		
		StockSummary.save(list);
		logger.info("save {} {}", list.size(), StockSummary.PATH_FILE);				
	}
	
	public String symbol              = null;
	public String assetClass          = null;
	public String annualizedDividend  = null;
//		public String beta                = null;
	public String dividendPaymentDate = null;
	public String exDividendDate      = null;
//		public String fiftTwoWeekHighLow  = null;
	public String marketCap           = null;
	public String previousClose       = null;
	public String shareVolume         = null;
	public String todayHighLow        = null;
	public String yield               = null;
	
	public StockSummary(
			String symbol,
			String assetClass,
			String annualizedDividend,
//				String beta,
			String dividendPaymentDate,
			String exDividendDate,
//				String fiftTwoWeekHighLow,
			String marketCap,
			String previousClose,
			String shareVolume,
			String todayHighLow,
			String yield
		) {
		this.symbol              = symbol;
		this.assetClass          = assetClass;
		this.annualizedDividend  = annualizedDividend;
//			this.beta                = beta;
		this.dividendPaymentDate = dividendPaymentDate;
		this.exDividendDate      = exDividendDate;
//			this.fiftTwoWeekHighLow  = fiftTwoWeekHighLow;
		this.marketCap           = marketCap;
		this.previousClose       = previousClose;
		this.shareVolume         = shareVolume;
		this.todayHighLow        = todayHighLow;
		this.yield               = yield;
	}
	public StockSummary() {}

	@Override
	public String toString() {
		return StringUtil.toString(this);
	}
	@Override
	public String getPath() {
		return PATH_FILE;
	}
	@Override
	public String getKey() {
		return symbol;
	}
}