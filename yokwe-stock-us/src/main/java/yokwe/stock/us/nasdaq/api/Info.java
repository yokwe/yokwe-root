package yokwe.stock.us.nasdaq.api;

import yokwe.stock.us.nasdaq.api.Quote.Status;
import yokwe.util.StringUtil;

public final class Info {
	// https://api.nasdaq.com/api/quote/LMT/info?assetclass=stocks
	// https://api.nasdaq.com/api/quote/YYY/info?assetclass=etf
	
	public static String getURL(String symbol, String assetClass) {
		return String.format("https://api.nasdaq.com/api/quote/%s/info?assetclass=%s",
			Quote.encodeSymbolForURL(symbol), assetClass);
	}
	
	public static Info getInstance(String symbol, String assetClass) {
		return Quote.getInstance(Info.class, Info::getURL, assetClass, symbol);
	}
	
	public static Info getETF(String symbol) {
		return getInstance(symbol, Quote.ETF);
	}
	public static Info getStock(String symbol) {
		return getInstance(symbol, Quote.STOCK);
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
		public ComplianceStatus.URL    url;
		
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