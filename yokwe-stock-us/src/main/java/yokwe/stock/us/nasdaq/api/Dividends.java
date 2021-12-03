package yokwe.stock.us.nasdaq.api;

import yokwe.stock.us.nasdaq.api.Quote.Status;
import yokwe.util.StringUtil;

public class Dividends {
		// https://api.nasdaq.com/api/quote/YYY/dividends?assetclass=etf
		// https://api.nasdaq.com/api/quote/LMT/dividends?assetclass=stocks

		public static String getURL(String assetClass, String symbol, int limit) {
			return String.format("https://api.nasdaq.com/api/quote/%s/dividends?assetclass=%s&limit=%d",
				Quote.encodeSymbolForURL(symbol), assetClass, limit);
		}

		public static Dividends getInstance(String assetClass, String symbol, int limit) {
			String url = getURL(assetClass, symbol, limit);
			return Quote.getInstance(Dividends.class, url);
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
			public Dividends.Values   headers;
			public Dividends.Values[] rows;
			
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
			public Dividends.Table  dividends;
            
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