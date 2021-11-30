package yokwe.stock.us.nasdaq.api;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

import yokwe.util.StringUtil;
import yokwe.util.UnexpectedException;
import yokwe.util.http.HttpUtil;
import yokwe.util.json.JSON;

public class Quote {	
	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(Quote.class);

	// https://api.nasdaq.com/api/quote/YYY/dividends?assetclass=etf
	// https://api.nasdaq.com/api/quote/LMT/dividends?assetclass=stocks
	
	// https://api.nasdaq.com/api/quote/LMT/realtime-trades?&limit=5
	
	// https://api.nasdaq.com/api/quote/LMT/extended-trading?assetclass=stocks&markettype=post
	
	// https://api.nasdaq.com/api/quote/LMT/extended-trading?assetclass=stocks&markettype=pre
	
	// https://api.nasdaq.com/api/quote/FR10UK/chart?assetclass=index
	// https://api.nasdaq.com/api/quote/FR10UK/info?assetclass=index

	public static String encodeSymbolForURL(String symbol) {
		// TRTN-A => TRTN%5EA
		return symbol.replace("-", "%5E");
	}
	
	public static String convertDate(String string) {
		string = string.trim().replace("N/A", "");
		if (string.isEmpty()) return ""; // FIXME
		
		// 12/27/2021 => 2021-12-07
		String[] mdy = string.split("\\/");
		if (mdy.length != 3) {
			logger.error("Unpexpected");
			logger.error("  date {}", string);
			throw new UnexpectedException("Unpexpected");
		}
		return mdy[2] + "-" + mdy[0] + "-" + mdy[1];
	}
	
	public static enum AssetClass {
		STOCK("stocks"),
		ETF  ("etf");
		
		public final String value;
		
		AssetClass(String value) {
			this.value = value;
		}
		@Override
		public String toString() {
			return value;
		}
	}
	
	public static class Dividends {
		// https://api.nasdaq.com/api/quote/YYY/dividends?assetclass=etf
		// https://api.nasdaq.com/api/quote/LMT/dividends?assetclass=stocks

		public static String getURL(String assetClass, String symbol, int limit) {
			return String.format("https://api.nasdaq.com/api/quote/%s/dividends?assetclass=%s&limit=%d",
					encodeSymbolForURL(symbol), assetClass.toString(), limit);
		}

		public static Dividends getInstance(String assetClass, String symbol, int limit) {
			String url = getURL(assetClass, symbol, limit);
			HttpUtil.Result result = HttpUtil.getInstance().download(url);
//			logger.debug("result {}!", result.result);
			return result == null ? null : JSON.unmarshal(Dividends.class, result.result);
		}
		public static Dividends getInstance(String assetClass, String symbol) {
			return getInstance(assetClass, symbol, 9999);
		}

		
		public static class Values {
			// "exOrEffDate":"11/27/2013","type":"CASH","amount":"$1.33","declarationDate":"09/26/2013","recordDate":"12/02/2013","paymentDate":"12/27/2013
			// "exOrEffDate":"10/27/2021","type":"CASH","amount":"$0.12","declarationDate":"01/18/2021","recordDate":"10/28/2021","paymentDate":"10/29/2021"
			public String exOrEffDate;
			public String type;
			public String amount;
			public String declarationDate;
			public String recordDate;
			public String paymentDate;
			
			public Values() {
				exOrEffDate     = null;
				type            = null;
				amount          = null;
				declarationDate = null;
				recordDate      = null;
				paymentDate     = null;
			}
            
			@Override
			public String toString() {
				return StringUtil.toString(this);
			}
		}

		public static class Table {
			public Values   headers;
			public Values[] rows;
			
			public Table() {
				headers = null;
				rows   =  null;
			}
            
			@Override
			public String toString() {
				return StringUtil.toString(this);
			}
		}
		public static class Data {
//		      "annualizedDividend" : "11.2",
//		      "dividendPaymentDate" : "12/27/2021",
//		      "exDividendDate" : "11/30/2021",
//		      "payoutRatio" : "15.75",
//		      "yield" : "3.03%"

			public String annualizedDividend;
			public String dividendPaymentDate;
			public String exDividendDate;
			public String payoutRatio;
			public String yield;
			public Table  dividends;
            
			public Data() {
				annualizedDividend  = null;
				dividendPaymentDate = null;
				exDividendDate      = null;
				payoutRatio         = null;
				yield               = null;
				dividends           = null;				
			}
			
			@Override
			public String toString() {
				return StringUtil.toString(this);
			}
		}
		
		public Data   data;
		public String message;
		public Status status;
		
		public Dividends() {
			data    = null;
			message = null;
			status  = null;
		}
		
		@Override
		public String toString() {
			return StringUtil.toString(this);
		}
	}
	
	public static class Historical {
		// https://api.nasdaq.com/api/quote/LMT/historical?assetclass=stocks&fromdate=2020-11-25&limit=9999&todate=2021-11-25
		// https://api.nasdaq.com/api/quote/YYY/historical?assetclass=etf&fromdate=2020-11-25&limit=18&todate=2021-11-25

		public static String getURL(String assetClass, String symbol, LocalDate fromDate, LocalDate toDate, long limit) {
			return String.format("https://api.nasdaq.com/api/quote/%s/historical?assetclass=%s&fromdate=%s&todate=%s&limit=%d",
					encodeSymbolForURL(symbol), assetClass.toString(), fromDate.toString(), toDate.toString(), limit);
		}
		public static String getURL(String assetClass, String symbol, LocalDate fromDate, LocalDate toDate) {
			long days = ChronoUnit.DAYS.between(fromDate, toDate);
			return getURL(assetClass, symbol, fromDate, toDate, days + 1);
		}

		public static Historical getInstance(String assetClass, String symbol, LocalDate fromDate, LocalDate toDate) {
			String url = getURL(assetClass, symbol, fromDate, toDate);
			HttpUtil.Result result = HttpUtil.getInstance().download(url);
			return result == null ? null : JSON.unmarshal(Historical.class, result.result);
		}
		
		public static class Values {
			// close: "194.39", date: "11/26/2021", high: "196.82", low: "194.19", open: "196.82", volume: "11,113"
			// close: "$17.86", date: "11/26/2021", high: "$18.155", low: "$17.765", open: "$18.03", volume: "1,645,865
			public String close;
			public String date;
			public String high;
			public String low;
			public String open;
			public String volume;
			
			public Values() {
				close  = null;
				date   = null;
				high   = null;
				low    = null;
				open   = null;
				volume = null;
			}
            
			@Override
			public String toString() {
				return StringUtil.toString(this);
			}
		}
		
		public static class Data {
			public static class TradesTable {
				public Values   headers;
				public Values[] rows;
				
				public TradesTable() {
					headers = null;
					rows    = null;
				}
				
				@Override
				public String toString() {
					return StringUtil.toString(this);
				}
			}
			
			public String      symbol;
			public int         totalRecords;
			public TradesTable tradesTable;
			
			public Data() {
				symbol       = null;
				totalRecords = 0;
				tradesTable  = null;
			}
			
			@Override
			public String toString() {
				return StringUtil.toString(this);
			}
		}
		
		public Data   data;
		public String message;
		public Status status;
		
		public Historical() {
			data    = null;
			message = null;
			status  = null;
		}
		
		@Override
		public String toString() {
			return StringUtil.toString(this);
		}
	}

	
	public static class Info {
		// https://api.nasdaq.com/api/quote/LMT/info?assetclass=stocks
		// https://api.nasdaq.com/api/quote/YYY/info?assetclass=etf
		
		public static String getURL(Quote.AssetClass assetClass, String symbol) {
			return String.format("https://api.nasdaq.com/api/quote/%s/info?assetclass=%s",
					encodeSymbolForURL(symbol), assetClass.toString());
		}
		
		public static Info getInstance(Quote.AssetClass assetClass, String symbol) {
			String url = getURL(assetClass, symbol);
			HttpUtil.Result result = HttpUtil.getInstance().download(url);
			return result == null ? null : JSON.unmarshal(Info.class, result.result);
		}
		public static Info getETF(String symbol) {
			return getInstance(Quote.AssetClass.ETF, symbol);
		}
		public static Info getStock(String symbol) {
			return getInstance(Quote.AssetClass.STOCK, symbol);
		}
		
		public static class PrimaryData {
			public String  deltaIndicator;
			public boolean isRealTime;
			public String  lastSalePrice;
			public String  lastTradeTimestamp;
			public String  netChange;
			public String  percentageChange;
			
			public PrimaryData() {
				deltaIndicator     = null;
				isRealTime         = false;
				lastSalePrice      = null;
				lastTradeTimestamp = null;
				netChange          = null;
				percentageChange   = null;
			}
			
			@Override
			public String toString() {
				return StringUtil.toString(this);
			}
		}
		public static class SecondaryData {
			//
			public SecondaryData() {}
			
			@Override
			public String toString() {
				return StringUtil.toString(this);
			}
		}

		public static class ComplianceStatus {
			public static class URL {
				public String label;
				public String value;
				
				public URL() {
					label = null;
					value = null;
				}
				
				@Override
				public String toString() {
					return StringUtil.toString(this);
				}				
			}
			public String header;
			public String message;
			public URL    url;
			
			public ComplianceStatus() {
				header  = null;
				message = null;
				url     = null;
			}
			
			@Override
			public String toString() {
				return StringUtil.toString(this);
			}
		}
		
		public static class Data {
			public String           assetClass;
			public String           companyName;
			public ComplianceStatus complianceStatus;
			public String           exchange;
			public boolean          isHeld;
			public boolean          isNasdaq100;
			public boolean          isNasdaqListed;
			public String           marketStatus;
			public PrimaryData      primaryData;
			public SecondaryData    secondaryData;
			public String           stockType;
			public String           symbol;
			public String           tradingHeld;
			
			public Data() {
				assetClass       = null;
				companyName      = null;
				complianceStatus = null;
				exchange         = null;
				isHeld           = false;
				isNasdaq100      = false;
				isNasdaqListed   = false;
				marketStatus     = null;
				primaryData      = null;
				secondaryData    = null;
				stockType        = null;
				symbol           = null;
				tradingHeld      = null;
			}
			
			@Override
			public String toString() {
				return StringUtil.toString(this);
			}
		}

		public Data   data;
		public String message;
		public Status status;

		public Info() {
			data    = null;
			message = null;
			status  = null;
		}
		
		@Override
		public String toString() {
			return StringUtil.toString(this);
		}
	}

}
