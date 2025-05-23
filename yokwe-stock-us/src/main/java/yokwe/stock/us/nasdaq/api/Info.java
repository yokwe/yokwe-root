package yokwe.stock.us.nasdaq.api;

import yokwe.stock.us.Storage;
import yokwe.util.ToString;

public final class Info {
	// https://api.nasdaq.com/api/quote/LMT/info?assetclass=stocks
	// https://api.nasdaq.com/api/quote/YYY/info?assetclass=etf
	
	public static final String PATH_DIR = Storage.NASDAQ.getPath("api/info");
	public static String getPath(String symbol) {
		return String.format("%s/%s.json", PATH_DIR, symbol);
	}
	
	public static String getURL(String symbol, AssetClass assetClass) {
		return String.format("https://api.nasdaq.com/api/quote/%s/info?assetclass=%s",
			API.encodeSymbolForURL(symbol), assetClass);
	}
	
	public static Info getInstance(String symbol, AssetClass assetClass) {
		String url  = getURL(symbol, assetClass);
		return API.getInstance(Info.class, url);
//		return API.getInstance(Info.class, url, getPath(symbol));
	}
	
	public static Info getETF(String symbol) {
		return getInstance(symbol, AssetClass.ETF);
	}
	public static Info getStock(String symbol) {
		return getInstance(symbol, AssetClass.STOCK);
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
			return ToString.withFieldName(this);
		}
	}
	public static class SecondaryData {
		//
		public SecondaryData() {}
		
		@Override
		public String toString() {
			return ToString.withFieldName(this);
		}
	}

	public static class ComplianceStatus {
		public String     header;
		public String     message;
		public LabelValue url;
		
		public ComplianceStatus() {
			header  = null;
			message = null;
			url     = null;
		}
		
		@Override
		public String toString() {
			return ToString.withFieldName(this);
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
			return ToString.withFieldName(this);
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
		return ToString.withFieldName(this);
	}
}