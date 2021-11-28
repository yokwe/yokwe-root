package yokwe.stock.us.nasdaq.api;

import yokwe.util.http.HttpUtil;
import yokwe.util.json.JSON;

public class Screener {
	public static class Status {
		public String bCodeMessage;
		public String developerMessage;
		public int    rCode;
	}

	public static class Stock {
		public static final String URL = "https://api.nasdaq.com/api/screener/stocks?download=true";

		public static Stock getInstance() {
			HttpUtil.Result result = HttpUtil.getInstance().download(URL);
			return result == null ? null : JSON.unmarshal(Stock.class, result.result);
		}

		public static class Values {
			public String country;
			public String industry;
			public String ipoyear;
			public String lastsale;
			public String marketCap;
			public String name;
			public String netchange;
			public String pctchange;
			public String sector;
			public String symbol;
			public String url;
			public String volume;
			
			public Values() {
				country   = null;
				industry  = null;
				ipoyear   = null;
				lastsale  = null;
				marketCap = null;
				name      = null;
				netchange = null;
				pctchange = null;
				sector    = null;
				symbol    = null;
				url       = null;
				volume    = null;
			}
		}

		public static class Data {
			public Values   headers;
			public Values[] rows;
			
			public Data() {
				headers = null;
				rows    = null;
			}
		}

		public Data   data;
		public String message;
		public Status status;
		
		public Stock() {
			data    = null;
			message = null;
			status  = null;
		}
	}
	
	public static class ETF {
		public static final String URL = "https://api.nasdaq.com/api/screener/etf?download=true";
		
		public static ETF getInstance() {
			HttpUtil.Result result = HttpUtil.getInstance().download(URL);
			return result == null ? null : JSON.unmarshal(ETF.class, result.result);
		}
		
		public static class Values {
			public String companyName;
			public String deltaIndicator;
			public String lastSalePrice;
			public String netChange;
			public String oneYearPercentage;
			public String percentageChange;
			public String symbol;
			
			public Values() {
				companyName       = null;
				deltaIndicator    = null;
				lastSalePrice     = null;
				netChange         = null;
				oneYearPercentage = null;
				percentageChange  = null;
				symbol            = null;
			}
		}
		
		public static class Data {
			public Values   headers;
			public Values[] rows;
			
			public Data() {
				headers = null;
				rows    = null;
			}
		}
		
		public static class DataOuter {
			public Data   data;
			public String dataAsOf;
			
			public DataOuter() {
				data     = null;
				dataAsOf = null;
			}
		}
		
		public DataOuter data;
		public String    message;
		public Status    status;
		
		public ETF() {
			data    = null;
			message = null;
			status  = null;
		}
	}

}
