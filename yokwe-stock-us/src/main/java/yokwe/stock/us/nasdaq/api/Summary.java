package yokwe.stock.us.nasdaq.api;

import yokwe.util.StringUtil;
import yokwe.util.json.JSON;

public final class Summary {
	// https://api.nasdaq.com/api/quote/LMT/summary?assetclass=stocks
	// https://api.nasdaq.com/api/quote/YYY/summary?assetclass=etf
	
	public static String getURL(String symbol, String assetClass) {
		return String.format("https://api.nasdaq.com/api/quote/%s/summary?assetclass=%s",
			API.encodeSymbolForURL(symbol), assetClass);
	}
	
	public static final class LabelValue {
		public String label;
		public String value;
		
		public LabelValue() {
			label = null;
			value = null;
		}
		
		@Override
		public String toString() {
			return StringUtil.toString(this);
		}
	}

	public static final class Stock {
		public static Stock getInstance(String symbol) {
			return API.getInstance(Stock.class, Summary::getURL, API.STOCK, symbol);
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
			public String      additionalData;
			public String      assetClass;
			public SummaryData summaryData;
			public String      symbol;
			
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
			return API.getInstance(ETF.class, Summary::getURL, API.ETF, symbol);
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
			public String      additionalData;
			public String      assetClass;
			public SummaryData summaryData;
			public String      symbol;
			
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
