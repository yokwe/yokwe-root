package yokwe.stock.us.nasdaq.api;

import yokwe.stock.us.Storage;
import yokwe.util.StringUtil;
import yokwe.util.json.JSON;

public final class Summary {
	public static final String PATH_DIR = Storage.NASDAQ.getPath("api/summary");
	public static String getPath(String symbol) {
		return String.format("%s/%s.json", PATH_DIR, symbol);
	}

	// https://api.nasdaq.com/api/quote/LMT/summary?assetclass=stocks
	// https://api.nasdaq.com/api/quote/YYY/summary?assetclass=etf
	
	public static String getURL(String symbol, String assetClass) {
		return String.format("https://api.nasdaq.com/api/quote/%s/summary?assetclass=%s",
			API.encodeSymbolForURL(symbol), API.checkAssetClass(assetClass));
	}
	
	public static final class AdditionalData {
		@JSON.Name("ComplianceStatusLink")  public LabelValue  complianceStatusLink;
	}

	public static final class Stock {
		public static Stock getInstance(String symbol) {
			String url  = getURL(symbol, API.STOCK);
			String path = getPath(symbol);
			return API.getInstance(Stock.class, url, path);
		}
		
		public static final class SummaryData {
			@JSON.Name("AnnualizedDividend")  public LabelValue annualizedDividend;
			@JSON.Name("Beta")                public LabelValue beta;
			@JSON.Name("DividendPaymentDate") public LabelValue dividendPaymentDate;
			@JSON.Name("ExDividendDate")      public LabelValue exDividendDate;
			@JSON.Name("FiftTwoWeekHighLow")  public LabelValue fiftTwoWeekHighLow;
			@JSON.Name("MarketCap")           public LabelValue marketCap;
			@JSON.Name("PreviousClose")       public LabelValue previousClose;
			@JSON.Name("ShareVolume")         public LabelValue shareVolume;
			@JSON.Name("TodayHighLow")        public LabelValue todayHighLow;
			@JSON.Name("Yield")               public LabelValue yield;
			
			@JSON.Ignore @JSON.Name("SpecialDividendDate")        public LabelValue specialDividendDate;
			@JSON.Ignore @JSON.Name("SpecialDividendAmount")      public LabelValue specialDividendAmount;
			@JSON.Ignore @JSON.Name("SpecialDividendPaymentDate") public LabelValue specialDividendPaymentDate;
			
			// Stock unique
			@JSON.Name("AverageVolume")       public LabelValue averageVolume;
			@JSON.Name("EarningsPerShare")    public LabelValue earningsPerShare;
			@JSON.Name("Exchange")            public LabelValue exchange;
			@JSON.Name("ForwardPE1Yr")        public LabelValue forwardPE1Yr;
			@JSON.Name("Industry")            public LabelValue industry;
			@JSON.Name("OneYrTarget")         public LabelValue oneYrTarget;
			@JSON.Name("PERatio")             public LabelValue peRatio;
			@JSON.Name("Sector")              public LabelValue sector;

			public SummaryData() {
				annualizedDividend  = null;
				beta                = null;
				dividendPaymentDate = null;
				exDividendDate      = null;
				fiftTwoWeekHighLow  = null;
				marketCap           = null;
				previousClose       = null;
				shareVolume         = null;
				todayHighLow        = null;
				yield               = null;

				specialDividendDate        = null;
				specialDividendAmount      = null;
				specialDividendPaymentDate = null;

				averageVolume       = null;
				earningsPerShare    = null;
				exchange            = null;
				forwardPE1Yr        = null;
				industry            = null;
				oneYrTarget         = null;
				peRatio             = null;
				sector              = null;
			}
			
			@Override
			public String toString() {
				return StringUtil.toString(this);
			}
		}
		
		public static final class Data {
			public AdditionalData additionalData;
			public String         assetClass;
			public SummaryData    summaryData;
			public String         symbol;
			
			public Data() {
				additionalData = null;
				assetClass     = null;
				summaryData    = null;
				symbol         = null;
			}
			
			@Override
			public String toString() {
				return StringUtil.toString(this);
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
		
		@Override
		public String toString() {
			return StringUtil.toString(this);
		}
	}
	public static final class ETF {
		public static ETF getInstance(String symbol) {
			String url  = getURL(symbol, API.ETF);
			String path = getPath(symbol);
			return API.getInstance(ETF.class, url, path);
		}
		
		public static final class SummaryData {
			@JSON.Name("AnnualizedDividend")  public LabelValue annualizedDividend;
			@JSON.Name("Beta")                public LabelValue beta;
			@JSON.Name("DividendPaymentDate") public LabelValue dividendPaymentDate;
			@JSON.Name("ExDividendDate")      public LabelValue exDividendDate;
			@JSON.Name("FiftTwoWeekHighLow")  public LabelValue fiftTwoWeekHighLow;
			@JSON.Name("MarketCap")           public LabelValue marketCap;
			@JSON.Name("PreviousClose")       public LabelValue previousClose;
			@JSON.Name("ShareVolume")         public LabelValue shareVolume;
			@JSON.Name("TodayHighLow")        public LabelValue todayHighLow;
			@JSON.Name("Yield")               public LabelValue yield;
			
			@JSON.Ignore @JSON.Name("SpecialDividendDate")        public LabelValue specialDividendDate;
			@JSON.Ignore @JSON.Name("SpecialDividendAmount")      public LabelValue specialDividendAmount;
			@JSON.Ignore @JSON.Name("SpecialDividendPaymentDate") public LabelValue specialDividendPaymentDate;
			
			// ETF unique
			@JSON.Name("AUM")                 public LabelValue aum;
			@JSON.Name("Alpha")               public LabelValue alpha;
			@JSON.Name("AvgDailyVol20Days")   public LabelValue avgDailyVol20Days;
			@JSON.Name("AvgDailyVol65Days")   public LabelValue avgDailyVol65Days;
			@JSON.Name("ExpenseRatio")        public LabelValue expenseRatio;
			@JSON.Name("FiftyDayAvgDailyVol") public LabelValue fiftyDayAvgDailyVol;
			@JSON.Name("StandardDeviation")   public LabelValue standardDeviation;
			@JSON.Name("WeightedAlpha")       public LabelValue weightedAlpha;

			public SummaryData() {
				annualizedDividend  = null;
				beta                = null;
				dividendPaymentDate = null;
				exDividendDate      = null;
				fiftTwoWeekHighLow  = null;
				marketCap           = null;
				previousClose       = null;
				shareVolume         = null;
				todayHighLow        = null;
				yield               = null;
				
				specialDividendDate        = null;
				specialDividendAmount      = null;
				specialDividendPaymentDate = null;

				aum                 = null;
				alpha               = null;
				avgDailyVol20Days   = null;
				avgDailyVol65Days   = null;
				expenseRatio        = null;
				fiftyDayAvgDailyVol = null;
				standardDeviation   = null;
				weightedAlpha       = null;
			}
			
			@Override
			public String toString() {
				return StringUtil.toString(this);
			}
		}
		
		public static final class Data {
			public AdditionalData additionalData;
			public String         assetClass;
			public SummaryData    summaryData;
			public String         symbol;
			
			public Data() {
				additionalData = null;
				assetClass     = null;
				summaryData    = null;
				symbol         = null;
			}
			
			@Override
			public String toString() {
				return StringUtil.toString(this);
			}
		    
		}
		
		public Data   data;
		public String message;
		public Status status;

		public ETF() {
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
