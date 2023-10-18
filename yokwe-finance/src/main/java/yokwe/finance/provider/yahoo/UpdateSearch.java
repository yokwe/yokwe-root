package yokwe.finance.provider.yahoo;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

import yokwe.finance.stock.StorageStock;
import yokwe.finance.type.StockInfoJPType;
import yokwe.finance.type.StockInfoUSType;
import yokwe.util.http.HttpUtil;
import yokwe.util.json.JSON;

public class UpdateSearch {
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
			@JSON.Name("prevTicker")       @JSON.Ignore public String     prevTicker;
			@JSON.Name("tickerChangeDate") @JSON.Ignore public String     tickerChangeDate;
			
			@JSON.Name("quoteType")                     public String     type;
			@JSON.Name("typeDisp")       @JSON.Ignore   public String     typeDisp;
			
			@JSON.Name("exchange")                      public String     exchange;
			@JSON.Name("exchDisp")                      public String     exchDisp;
			
			@JSON.Name("shortname")      @JSON.Optional public String     shortname;
			@JSON.Name("longname")       @JSON.Optional public String     longname;
			@JSON.Name("prevName")       @JSON.Ignore   public String     prevName;
			@JSON.Name("nameChangeDate") @JSON.Ignore   public String     nameChangeDate;

			
			@JSON.Name("index")          @JSON.Ignore   public String     index;
			@JSON.Name("score")          @JSON.Ignore   public BigDecimal score;
			@JSON.Name("isYahooFinance")                public boolean    isYahooFinance;

			@JSON.Name("sector")         @JSON.Ignore   public String     sector;         // only for EQUITY
			@JSON.Name("sectorDisp")     @JSON.Optional public String     sectorDisp;     // only for EQUITY
			@JSON.Name("industry")       @JSON.Ignore   public String     industry;       // only for EQUITY
			@JSON.Name("industryDisp")   @JSON.Optional public String     industryDisp;   // only for EQUITY
			
			@JSON.Name("newListingDate") @JSON.Ignore   public String     newListingDate; // only for EQUITY
			@JSON.Name("dispSecIndFlag") @JSON.Ignore   public String     dispSecIndFlag; // only for EQUITY
			
			// dispSecIndFlag
			@Override
			public String toString() {
				return String.format("{%s  %s  \"%s\"  \"%s\"  \"%s\"  \"%s\"  \"%s\"  %s}", symbol, type, exchange, exchDisp, longname, sector, industry, isYahooFinance);
			}
		}
	}
	
	private static String getURL(String q) {
		LinkedHashMap<String, String> map = new LinkedHashMap<>();
		map.put("q",  q);
		map.put("quotesCount", "3");
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
	
	private static final Map<String, String> exchangeMap = new TreeMap<>();
	static {
		// USA
		exchangeMap.put("NYQ", "NYSE");
		exchangeMap.put("PCX", "NYSE");  // NYSE ARCA
		exchangeMap.put("ASE", "NYSE");  // NYSE AMERICAN

		exchangeMap.put("NAS", "NASDAQ");
		exchangeMap.put("NMS", "NASDAQ");
		exchangeMap.put("NCM", "NASDAQ");
		exchangeMap.put("NGM", "NASDAQ");
		exchangeMap.put("NIM", "NASDAQ");
		
		exchangeMap.put("BTS", "BATS");
		
		exchangeMap.put("PNK", "OTC");    // pink -- over the counter
		// JAPAN
		exchangeMap.put("JPX", "JPX");
		exchangeMap.put("OSA", "JPX");
		// INDEX
		exchangeMap.put("SNP", "S&P");
		exchangeMap.put("CXI", "CBOE");
		// EUROPE
		exchangeMap.put("FRA", "FRANKFURT");
	}
	
	public static Search getInstance(String key) {
		String string = getString(key);
		
		RAW.Result raw = JSON.unmarshal(RAW.Result.class, string);
		if (raw.quotes == null) {
			logger.warn("raw.quotes is null  {}", key);
//			logger.warn("  string  {}", string);
			return null;
		}
		if (raw.quotes.length == 0) {
//			logger.warn("raw.quotes.length is zero  {}", key);
//			logger.warn("  string  {}", string);
			return null;
		}
		for(var e: raw.quotes) {
			if (e.symbol.equals(key)) {
				if (e.longname.isEmpty() && e.shortname.isEmpty()) {
					logger.warn("no longname and no shortname  {}", e);
//					logger.warn("  key     {}", key);
//					logger.warn("  quote   {}", e);
					return null;
				}
				var name     = e.longname.isEmpty() ? e.shortname : e.longname;
				var exchange = exchangeMap.get(e.exchange);
				if (exchange == null) {
					logger.warn("unexpected exchange  {}", e);
					return null;
				}
				return new Search(e.symbol, e.type, exchange, e.sectorDisp, e.industryDisp, name);
			}
		}
//		logger.warn("no key in quotes  {}", key);
//		for(var e: raw.quotes) {
//			logger.warn("  quote   {}", e);
//		}
		return null;
	}
	
	private static int SLEEP_IN_MILLI = 1500;
	
	public static String toYahooSymbol(String stockCode) {
		Character c0 = Character.valueOf(stockCode.charAt(0));
		if (Character.isDigit(c0)) {
			return StockInfoJPType.toYahooSymbol(stockCode);
		} else {
			return StockInfoUSType.toYahooSymbol(stockCode);
		}
	}
	
	private static void update(String label, Map<String, Search> map, List<String> list) {
		Collections.shuffle(list);
		{
			int countTotal = list.size();
			int countAdd   = 0;
			int count      = 0;
			for(var stockCode: list) {
				if ((++count % 100) == 1) logger.info("{}  {}  /  {}", label, count, countTotal);
				if (map.containsKey(stockCode)) continue;
				
				var search = getInstance(stockCode);
				if (search != null) {
					search.name = search.name.replace(",", ""); // remove comma
					map.put(stockCode, search);
					// save if needed
					if ((++countAdd % 10) == 1) Search.save(map.values());
				}
				
				// sleep
				try {
					Thread.sleep(SLEEP_IN_MILLI);
				} catch (InterruptedException ie) {
					//
				}
			}
			logger.info("{}  countAdd  {}", label, countAdd);
		}
	}
	
	public static void main(String[] args) throws IOException {
		logger.info("START");
		
		var map  = Search.getMap();
		logger.info("map   {}", map.size());
		
		{
			String label = "stock-us";
			
			var list = StorageStock.StockInfoUS.getList().stream().map(o -> StockInfoUSType.toYahooSymbol(o.stockCode)).collect(Collectors.toList());
			logger.info("{}  list  {}", label, list.size());
			list.removeIf(o -> map.containsKey(o));
			logger.info("{}  list  {}", label, list.size());
			update(label, map, list);
			
			logger.info("{}  save  {}  {}", label, map.size(), Search.getPath());
			Search.save(map.values());
		}
		{
			String label = "stock-jp";
			
			var list = StorageStock.StockInfoJP.getList().stream().map(o -> StockInfoJPType.toYahooSymbol(o.stockCode)).collect(Collectors.toList());
			logger.info("{}  list  {}", label, list.size());
			list.removeIf(o -> map.containsKey(o));
			logger.info("{}  list  {}", label, list.size());
			update(label, map, list);
			
			logger.info("{}  save  {}  {}", label, map.size(), Search.getPath());
			Search.save(map.values());
		}
		
		logger.info("STOP");
	}
}
