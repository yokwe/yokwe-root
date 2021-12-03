package yokwe.stock.us.nasdaq.api;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

import yokwe.stock.us.nasdaq.api.Quote.Status;
import yokwe.util.StringUtil;

public class Historical {
	// https://api.nasdaq.com/api/quote/LMT/historical?assetclass=stocks&fromdate=2020-11-25&limit=9999&todate=2021-11-25
	// https://api.nasdaq.com/api/quote/YYY/historical?assetclass=etf&fromdate=2020-11-25&limit=18&todate=2021-11-25

	public static String getURL(String assetClass, String symbol, LocalDate fromDate, LocalDate toDate, long limit) {
		return String.format("https://api.nasdaq.com/api/quote/%s/historical?assetclass=%s&fromdate=%s&todate=%s&limit=%d",
				Quote.encodeSymbolForURL(symbol), assetClass.toString(), fromDate.toString(), toDate.toString(), limit);
	}
	public static String getURL(String assetClass, String symbol, LocalDate fromDate, LocalDate toDate) {
		long days = ChronoUnit.DAYS.between(fromDate, toDate);
		return getURL(assetClass, symbol, fromDate, toDate, days + 1);
	}

	public static Historical getInstance(String assetClass, String symbol, LocalDate fromDate, LocalDate toDate) {
		String url = getURL(assetClass, symbol, fromDate, toDate);
		return Quote.getInstance(Historical.class, url);
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
			public Historical.Values   headers;
			public Historical.Values[] rows;
			
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
		public Data.TradesTable tradesTable;
		
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
	
	public Historical.Data   data;
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