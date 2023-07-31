package yokwe.util.yahoo.finance;

import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.stream.Collectors;

import yokwe.util.http.HttpUtil;
import yokwe.util.json.JSON;

public class Search {
	private static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();
	
	//
	// operation relate to download end point
	//
	
	private static final String  URL            = "https://query1.finance.yahoo.com/v1/finance/search";
	private static final Charset CHARSET_UTF_8  = StandardCharsets.UTF_8;
	
	public static class RAW {
		public static class Result {
			@JSON.Name("explains")                       @JSON.Ignore public String[] explains;
			@JSON.Name("count")                                       public int      count;
			@JSON.Name("quotes")                                      public Quote[]  quotes;
			@JSON.Name("news")                           @JSON.Ignore public String[] news;
			@JSON.Name("nav")                            @JSON.Ignore public String[] nav;
			@JSON.Name("lists")                          @JSON.Ignore public String[] lists;
			@JSON.Name("researchReports")                @JSON.Ignore public String[] researchReports;
			@JSON.Name("screenerFieldResults")           @JSON.Ignore public String[] screenerFieldResults;
			@JSON.Name("totalTime")                                   public int      totalTime;
			@JSON.Name("timeTakenForQuotes")             @JSON.Ignore public int      timeTakenForQuotes;
			@JSON.Name("timeTakenForNews")               @JSON.Ignore public int      timeTakenForNews;
			@JSON.Name("timeTakenForAlgowatchlist")      @JSON.Ignore public int      timeTakenForAlgowatchlist;
			@JSON.Name("timeTakenForPredefinedScreener") @JSON.Ignore public int      timeTakenForPredefinedScreener;
			@JSON.Name("timeTakenForCrunchbase")         @JSON.Ignore public int      timeTakenForCrunchbase;
			@JSON.Name("timeTakenForNav")                @JSON.Ignore public int      timeTakenForNav;
			@JSON.Name("timeTakenForResearchReports")    @JSON.Ignore public int      timeTakenForResearchReports;
			@JSON.Name("timeTakenForScreenerField")      @JSON.Ignore public int      timeTakenForScreenerField;
			@JSON.Name("timeTakenForCulturalAssets")     @JSON.Ignore public int      timeTakenForCulturalAssets;
			
			@Override
			public String toString() {
				return String.format("{%d  %d  %s}", count, totalTime, (quotes == null) ? "null" : Arrays.stream(quotes).toList());
			}
		}
		public static class Quote {
			@JSON.Name("symbol")                        public String     symbol;
			@JSON.Name("quoteType")                     public String     quoteType;
			@JSON.Name("typeDisp")       @JSON.Ignore   public String     typeDisp;
			@JSON.Name("exchange")                      public String     exchange;
			@JSON.Name("exchDisp")                      public String     exchDisp;
			@JSON.Name("shortname")      @JSON.Ignore   public String     shortname;
			@JSON.Name("longname")                      public String     longname;
			@JSON.Name("index")          @JSON.Ignore   public String     index;
			@JSON.Name("score")          @JSON.Ignore   public BigDecimal score;
			@JSON.Name("isYahooFinance")                public boolean    isYahooFinance;

			@JSON.Name("sector")         @JSON.Optional public String     sector;         // only for EQUITY
			@JSON.Name("industry")       @JSON.Optional public String     industry;       // only for EQUITY
			@JSON.Name("newListingDate") @JSON.Ignore   public String     newListingDate; // only for EQUITY
			@JSON.Name("dispSecIndFlag") @JSON.Ignore   public String     dispSecIndFlag; // only for EQUITY
			
			// dispSecIndFlag
			@Override
			public String toString() {
				return String.format("{%s  %s  %s  \"%s\"  \"%s\"  \"%s\"  \"%s\"  %s}", symbol, quoteType, exchange, exchDisp, longname, sector, industry, isYahooFinance);
			}
		}
	}
	
	private static String getURL(String q) {
		LinkedHashMap<String, String> map = new LinkedHashMap<>();
		map.put("q",  q);
		map.put("quotesCount", "1");
		map.put("newsCount",   "0");
		map.put("listsCount",  "0");
		String queryString = map.entrySet().stream().map(o -> o.getKey() + "=" + URLEncoder.encode(o.getValue(), CHARSET_UTF_8)).collect(Collectors.joining("&"));
		
		return String.format("%s?%s", URL, queryString);
	}
	
	private static String getString(String key) {
		String url = getURL(key);
		
		HttpUtil httpUtil = HttpUtil.getInstance();
		HttpUtil.Result result = httpUtil.download(url);
		if (result == null) {
			logger.warn("result is null");
			return null;
		}
		if (result.result == null) {
			logger.warn("result.result is null");
			return null;
		}
		return result.result;
	}
	
	public static Symbol getSymbol(String key) {
		String string = getString(key);
		
		RAW.Result raw = JSON.unmarshal(RAW.Result.class, string);
		if (raw.quotes == null) {
			logger.warn("raw.quotes is null");
			logger.warn("  string  {}", string);
			return null;
		}
		if (raw.quotes.length != 1) {
			logger.warn("raw.quotes.length is not one");
			logger.warn("  string  {}", string);
			return null;
		}
		var quote = raw.quotes[0];
		if (!quote.isYahooFinance) {
			logger.warn("isYahooFinance is not true");
			logger.warn("  string  {}", string);
			return null;
		}
		
		return new Symbol(quote.symbol, quote.quoteType, quote.exchange, quote.exchDisp, quote.longname);
	}
	
//	public static void test(String key) {
//		Symbol symbol = getSymbol(key);
//		logger.info("symbol  {}  --  {}", key, symbol);
//	}
//	public static void main(String[] args) {
//		logger.info("START");
//		
////		test("JP3257200000");
//		test("1301.T");
////		
////		test("JP3027710007");
//		test("IE0030804631");
//		
//		test("QQQ");
////		test("SBI");
//		
//		test("^N225");
//		test("^GSPC");
//		test("^NDX");
//		test("^VIX");
//		
//		test("LU0159489490");
//		
//		test("JP3048810000");
//		test("2031.T");
//		test("1568.T");
//
//		logger.info("STOP");
//	}

}
