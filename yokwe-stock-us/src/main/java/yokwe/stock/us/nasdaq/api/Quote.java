package yokwe.stock.us.nasdaq.api;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import yokwe.util.StringUtil;
import yokwe.util.http.HttpUtil;
import yokwe.util.json.JSON;

public class Quote {
	// https://api.nasdaq.com/api/quote/LMT/historical?assetclass=stocks&fromdate=2020-11-25&limit=9999&todate=2021-11-25
	// https://api.nasdaq.com/api/quote/YYY/historical?assetclass=etf&fromdate=2020-11-25&limit=18&todate=2021-11-25
	
	// https://api.nasdaq.com/api/quote/YYY/dividends?assetclass=etf
	// https://api.nasdaq.com/api/quote/LMT/dividends?assetclass=stocks
	
	// https://api.nasdaq.com/api/quote/LMT/realtime-trades?&limit=5
	
	// https://api.nasdaq.com/api/quote/LMT/extended-trading?assetclass=stocks&markettype=post
	
	// https://api.nasdaq.com/api/quote/LMT/extended-trading?assetclass=stocks&markettype=pre
	
	// https://api.nasdaq.com/api/quote/FR10UK/chart?assetclass=index
	// https://api.nasdaq.com/api/quote/FR10UK/info?assetclass=index

	public static class Status {
		public String bCodeMessage;
		public String developerMessage;
		public int    rCode;
	}

	public static class Info {
		// https://api.nasdaq.com/api/quote/LMT/info?assetclass=stocks
		// https://api.nasdaq.com/api/quote/YYY/info?assetclass=etf
		
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
		
		public static String getURL(AssetClass assetClass, String symbol) {
			return String.format("https://api.nasdaq.com/api/quote/%s/info?assetclass=%s", URLEncoder.encode(symbol, StandardCharsets.UTF_8), assetClass.toString());
		}
		
		public static Info getInstance(AssetClass assetClass, String symbol) {
			String url = getURL(assetClass, symbol);
			HttpUtil.Result result = HttpUtil.getInstance().download(url);
			return result == null ? null : JSON.unmarshal(Info.class, result.result);
		}
		public static Info getETF(String symbol) {
			return getInstance(AssetClass.ETF, symbol);
		}
		public static Info getStock(String symbol) {
			return getInstance(AssetClass.STOCK, symbol);
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

		public Data data;
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
