package yokwe.stock.us.nasdaq.api;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

import yokwe.util.StringUtil;
import yokwe.util.http.HttpUtil;
import yokwe.util.json.JSON;

public class Quote {	
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
	
	public static class Historical {
		// https://api.nasdaq.com/api/quote/LMT/historical?assetclass=stocks&fromdate=2020-11-25&limit=9999&todate=2021-11-25
		// https://api.nasdaq.com/api/quote/YYY/historical?assetclass=etf&fromdate=2020-11-25&limit=18&todate=2021-11-25

		public static String getURL(Quote.AssetClass assetClass, String symbol, LocalDate fromDate, LocalDate toDate, long limit) {
			return String.format("api.nasdaq.com/api/quote/YYY/historical?assetclass=%s&fromdate=%s&todate=%s&limit=%d",
					encodeSymbolForURL(symbol), assetClass.toString(), fromDate.toString(), toDate.toString(), limit);
		}
		public static String getURL(Quote.AssetClass assetClass, String symbol, LocalDate fromDate, LocalDate toDate) {
			long days = ChronoUnit.DAYS.between(fromDate, toDate);
			return getURL(assetClass, symbol, fromDate, toDate, days + 1);
		}

		public static Historical getInstance(Quote.AssetClass assetClass, String symbol, LocalDate fromDate, LocalDate toDate) {
			String url = getURL(assetClass, symbol, fromDate, toDate);
			HttpUtil.Result result = HttpUtil.getInstance().download(url);
			return result == null ? null : JSON.unmarshal(Historical.class, result.result);
		}
		public static Historical getETF(String symbol, LocalDate fromDate, LocalDate toDate) {
			return getInstance(AssetClass.ETF, symbol, fromDate, toDate);
		}
		public static Historical getStock(String symbol, LocalDate fromDate, LocalDate toDate) {
			return getInstance(AssetClass.STOCK, symbol, fromDate, toDate);
		}
		
		public static class Values {
//          "close" : "16.81",
//          "date" : "11/24/2021",
//          "high" : "16.82",
//          "low" : "16.7",
//          "open" : "16.71",
//          "volume" : "136,621"

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
			public static class TradeTable {
				Values   headers;
				Values[] rows;
				
				public TradeTable() {
					headers = null;
					rows    = null;
				}
				
				@Override
				public String toString() {
					return StringUtil.toString(this);
				}
			}
			
			public String     symbol;
			public int        totalRecords;
			public TradeTable tradeTable;
			
			public Data() {
				symbol       = null;
				totalRecords = 0;
				tradeTable   = null;
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
