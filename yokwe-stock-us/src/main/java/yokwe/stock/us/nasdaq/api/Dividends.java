package yokwe.stock.us.nasdaq.api;

import yokwe.stock.us.Storage;
import yokwe.util.ToString;

public class Dividends {
	public static final String PATH_DIR = Storage.NASDAQ.getPath("api/dividends");
	public static String getPath(String symbol) {
		return String.format("%s/%s.json", PATH_DIR, symbol);
	}

	// https://api.nasdaq.com/api/quote/YYY/dividends?assetclass=etf
	// https://api.nasdaq.com/api/quote/LMT/dividends?assetclass=stocks
	
	public static String encodeSymbolForURL(String symbol) {
		// TRTN-A => TRTN%5EA
		// RDS.B  => RDS.B
		return symbol.replace("-", "%5E");
	}

	public static String getURL(String symbol, AssetClass assetClass, int limit) {
		return String.format("https://api.nasdaq.com/api/quote/%s/dividends?assetclass=%s&limit=%d",
				encodeSymbolForURL(symbol), assetClass, limit);
	}

	public static Dividends getInstance(String symbol, AssetClass assetClass, int limit) {
		String url  = getURL(symbol, assetClass, limit);
		return API.getInstance(Dividends.class, url);
//		return API.getInstance(Dividends.class, url, getPath(symbol));
	}
	public static Dividends getInstance(String symbol, AssetClass assetClass) {
		return getInstance(symbol, assetClass, 9999);
	}

	
	public static class Values {
		// {"exOrEffDate":"05/26/2022","type":"CASH","amount":"$0.12","declarationDate":"05/25/2022","recordDate":"05/27/2022","paymentDate":"05/31/2022","currency":"USD"}
		public String exOrEffDate;
		public String type;
		public String amount;
		public String declarationDate;
		public String recordDate;
		public String paymentDate;
		public String currency;
		
		public Values() {
			exOrEffDate     = null;
			type            = null;
			amount          = null;
			declarationDate = null;
			recordDate      = null;
			paymentDate     = null;
			currency        = null;
		}
        
		@Override
		public String toString() {
			return ToString.withFieldName(this);
		}
	}
	
	public static class Header {
		public String exOrEffDate;
		public String type;
		public String amount;
		public String declarationDate;
		public String recordDate;
		public String paymentDate;
		
		public Header() {
			exOrEffDate     = null;
			type            = null;
			amount          = null;
			declarationDate = null;
			recordDate      = null;
			paymentDate     = null;
		}
        
		@Override
		public String toString() {
			return ToString.withFieldName(this);
		}
	}


	public static class Table {
		public String   asOf;
		public Header   headers;
		public Values[] rows;
		
		public Table() {
			headers = null;
			rows   =  null;
		}
        
		@Override
		public String toString() {
			return ToString.withFieldName(this);
		}
	}
	public static class Data {
//		      "annualizedDividend" : "11.2",
//		      "dividendPaymentDate" : "12/27/2021",
//		      "exDividendDate" : "11/30/2021",
//		      "payoutRatio" : "15.75",
//		      "yield" : "3.03%"

		public LabelValue[] dividendHeaderValues;
		public String       annualizedDividend;
		public String       dividendPaymentDate;
		public String       exDividendDate;
		public String       payoutRatio;
		public String       yield;
		public Table        dividends;
        
		public Data() {
			dividendHeaderValues = null;
			annualizedDividend   = null;
			dividendPaymentDate  = null;
			exDividendDate       = null;
			payoutRatio          = null;
			yield                = null;
			dividends            = null;
		}
		
		@Override
		public String toString() {
			return ToString.withFieldName(this);
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
		return ToString.withFieldName(this);
	}
}