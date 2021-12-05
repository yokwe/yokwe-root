package yokwe.stock.us.nasdaq.api;

import yokwe.stock.us.Storage;
import yokwe.util.StringUtil;

public class Dividends {
	public static final String PATH_DIR = Storage.NASDAQ.getPath("api/dividends");
	public static String getPath(String symbol) {
		return String.format("%s/%s.json", PATH_DIR, symbol);
	}

	// https://api.nasdaq.com/api/quote/YYY/dividends?assetclass=etf
	// https://api.nasdaq.com/api/quote/LMT/dividends?assetclass=stocks

	public static String getURL(String symbol, String assetClass, int limit) {
		return String.format("https://api.nasdaq.com/api/quote/%s/dividends?assetclass=%s&limit=%d",
			API.encodeSymbolForURL(symbol), API.checkAssetClass(assetClass), limit);
	}

	public static Dividends getInstance(String symbol, String assetClass, int limit) {
		String url  = getURL(symbol, assetClass, limit);
		String path = getPath(symbol);
		return API.getInstance(Dividends.class, url, path);
	}
	public static Dividends getInstance(String symbol, String assetClass) {
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