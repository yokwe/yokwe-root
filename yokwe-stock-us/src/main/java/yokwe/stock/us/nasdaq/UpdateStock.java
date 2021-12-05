package yokwe.stock.us.nasdaq;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import yokwe.stock.us.Storage;
import yokwe.stock.us.nasdaq.api.API;
import yokwe.stock.us.nasdaq.api.Info;
import yokwe.stock.us.nasdaq.api.Screener;
import yokwe.stock.us.nasdaq.api.Summary;
import yokwe.util.CSVUtil;
import yokwe.util.StringUtil;
import yokwe.util.UnexpectedException;

public class UpdateStock {
	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(UpdateStock.class);
	
	public static String normalizeSymbol(String symbol) {
		// TRTN^A => TRTN-A
		// BRK/A  => BRK.A
		return symbol.replace('^', '-').replace('/', '.');
	}
	
	public static class StockList implements CSVUtil.Detail {
		public static final String PATH_FILE = Storage.NASDAQ.getPath("stock-list.csv");

		public static void save(Collection<StockList> collection) {
			CSVUtil.save(StockList.class, collection);
		}
		public static List<StockList> getList() {
			return CSVUtil.getList(StockList.class);
		}
		public static Map<String, StockList> getMap() {
			return CSVUtil.getMap(StockList.class);
		}
		
		public static void update() {
			logger.info("StockList update");
			List<StockList> list = new ArrayList<>();
			
			// ETF
			{
				Screener.ETF instance = Screener.ETF.getInstance();
				logger.info("etf     {}", instance.data.data.rows.length);
				for(var e: instance.data.data.rows) {
					String symbol = normalizeSymbol(e.symbol.trim());
					list.add(new StockList(symbol, API.ETF));
				}
			}
			// Stock
			{
				Screener.Stock instance = Screener.Stock.getInstance();
				logger.info("stock   {}", instance.data.rows.length);
				for(var e: instance.data.rows) {
					String symbol = normalizeSymbol(e.symbol.trim());
					list.add(new StockList(symbol, API.STOCK));
				}
			}
			
			StockList.save(list);
			logger.info("save {} {}", list.size(), StockList.PATH_FILE);
		}
		
		public String symbol     = null;
		public String assetClass = null;
		
		public StockList(String symbol, String assetClass) {
			this.symbol     = symbol;
			this.assetClass = assetClass;
		}
		// Default constructor for newInstance
		public StockList() {}

		@Override
		public String getPath() {
			return PATH_FILE;
		}

		@Override
		public String getKey() {
			return symbol;
		}
	}
	
	public static class StockInfo implements CSVUtil.Detail {
		private static final String PATH_FILE = Storage.NASDAQ.getPath("stock-info.csv");
		
		public static void save(List<StockInfo> list) {
			CSVUtil.save(StockInfo.class, list);
		}
		public static List<StockInfo> getList() {
			return CSVUtil.getList(StockInfo.class);
		}
		public static Map<String, StockInfo> getMap() {
			return CSVUtil.getMap(StockInfo.class);
		}
		
		public static void update(List<StockList> stockList) {
			// download
			{
				List<StockList> downloadList = new ArrayList<>();
				for(var e: stockList) {
					File file = new File(Info.getPath(e.symbol));
					if (file.exists()) continue;
					downloadList.add(e);
				}
				logger.info("StockInfo download {}", downloadList.size());
				
				int countTotal = downloadList.size();
				int count = 0;
				for(var e: downloadList) {
					if ((count % 100) == 0) logger.info("{}", String.format("%5d / %5d  %s", count, countTotal, e.symbol));
					count++;
					
					Info.getInstance(e.symbol, e.assetClass);
				}
			}
			
			// build list
			{
				logger.info("StockInfo build list");
				List<StockInfo> list = new ArrayList<>(stockList.size());
				
				int countTotal = stockList.size();
				int count = 0;
				for(var e: stockList) {
					if ((count % 1000) == 0) logger.info("{}", String.format("%5d / %5d  %s", count, countTotal, e.symbol));
					count++;
					
					Info info = Info.getInstance(e.symbol, e.assetClass);
					if (info.data == null) {
						logger.warn("data is null {}", e.symbol);
						continue;
					}
					
					String symbol           = e.symbol; // use normalized symbol
					String companyName      = info.data.companyName;
					String complianceStatus = info.data.complianceStatus == null ? "" : info.data.complianceStatus.message;
					String exchange         = info.data.exchange;

					String lastSalePrice      = info.data.primaryData.lastSalePrice;
					String lastTradeTimestamp = info.data.primaryData.lastTradeTimestamp;

					StockInfo stockInfo = new StockInfo(
						symbol, companyName, complianceStatus, exchange,
						lastSalePrice, lastTradeTimestamp);
					list.add(stockInfo);
				}
				
				StockInfo.save(list);
				logger.info("save {} {}", list.size(), StockInfo.PATH_FILE);
			}
		}
				
		public String symbol           = null;
		public String companyName      = null;
		public String complianceStatus = null;
		public String exchange         = null;

		public String lastSalePrice      = null;
		public String lastTradeTimestamp = null;
		
		public StockInfo(
				String symbol,
				String companyName,
				String complianceStatus,
				String exchange,

				String lastSalePrice,
				String lastTradeTimestamp
			) {
			this.symbol           = symbol;
			this.companyName      = companyName;
			this.complianceStatus = complianceStatus;
			this.exchange         = exchange;

			this.lastSalePrice      = lastSalePrice;
			this.lastTradeTimestamp = lastTradeTimestamp;
		}
		public StockInfo() {}

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
	
	public static class StockSummary implements CSVUtil.Detail {
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
		
		public static void update(List<StockList> stockList) {
			// download
			{
				List<StockList> downloadList = new ArrayList<>();
				for(var e: stockList) {
					File file = new File(Summary.getPath(e.symbol));
					if (file.exists()) continue;
					downloadList.add(e);
				}
				logger.info("StockSummary download {}", downloadList.size());

				int countTotal = downloadList.size();
				int count = 0;
				for(var e: downloadList) {
					if ((count % 100) == 0) logger.info("{}", String.format("%5d / %5d  %s", count, countTotal, e.symbol));
					count++;
					
					String symbol     = e.symbol;
					
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
			
			// build list
			{
				logger.info("StockSummary build list");
				List<StockSummary> list = new ArrayList<>(stockList.size());
				
				int countTotal = stockList.size();
				int count = 0;
				for(var e: stockList) {
					if ((count % 1000) == 0) logger.info("{}", String.format("%5d / %5d  %s", count, countTotal, e.symbol));
					count++;

					String symbol     = e.symbol;
					String assetClass = e.assetClass;
					
					String annualizedDividend;
//					String beta;
					String dividendPaymentDate;
					String exDividendDate;
//					String fiftTwoWeekHighLow;
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
//						beta                = summary.data.summaryData.beta.value;
						dividendPaymentDate = summary.data.summaryData.dividendPaymentDate.value.replace("N/A", "").replace(",", "").replace("$", "");
						exDividendDate      = summary.data.summaryData.exDividendDate.value.replace("N/A", "").replace(",", "").replace("$", "");
//						fiftTwoWeekHighLow  = summary.data.summaryData.fiftTwoWeekHighLow.value;
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
//						beta                = summary.data.summaryData.beta.value;
						dividendPaymentDate = summary.data.summaryData.dividendPaymentDate.value.replace("N/A", "").replace(",", "").replace("$", "");
						exDividendDate      = summary.data.summaryData.exDividendDate.value.replace("N/A", "").replace(",", "").replace("$", "");
//						fiftTwoWeekHighLow  = summary.data.summaryData.fiftTwoWeekHighLow.value;
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
//							beta,
						dividendPaymentDate,
						exDividendDate,
//							fiftTwoWeekHighLow,
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

	
	public static void main(String[] args) {
		logger.info("START");		
		
		StockList.update();
		
		List<StockList> stockListList = StockList.getList();
		
		Collections.shuffle(stockListList);
		StockInfo.update(stockListList);
		
		Collections.shuffle(stockListList);
		StockSummary.update(stockListList);
		
		// info
		logger.info("STOP");
	}
}
