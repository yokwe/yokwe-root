package yokwe.stock.us.nikko;

import java.io.File;
import java.io.StringReader;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import yokwe.stock.us.Stock;
import yokwe.stock.us.Storage;
import yokwe.util.FileUtil;
import yokwe.util.ListUtil;
import yokwe.util.StringUtil;
import yokwe.util.UnexpectedException;
import yokwe.util.http.HttpUtil;
import yokwe.util.json.JSON;

public class UpdateNikkoStock {
	private static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();
	
	private static final boolean DEBUG_USE_FILE  = false;
	
	private static final Charset UTF_8           = StandardCharsets.UTF_8;
	private static final String  URL             = "https://fstock.smbcnikko.co.jp/smbcnikko/searchresultdata";
	private static final String  CONTENT_TYPE    = "application/x-www-form-urlencoded; charset=UTF-8";
	private static final String  REFERER         = "https://fstock.smbcnikko.co.jp/smbcnikko/search?pop=";
	
	public static String getPostBody(int pageNo) {
		LinkedHashMap<String, String> map = new LinkedHashMap<>();
		map.put("namecode", "");
		map.put("keyword",  "");
		map.put("exchange", "");
		map.put("sector",   "");
		map.put("etf",      "");
		map.put("page",     String.valueOf(pageNo));
		map.put("type",     "0");
		map.put("asc",      "1");
		map.put("c",        "0");
		
		String string = map.entrySet().stream().map(o -> o.getKey() + "=" + URLEncoder.encode(o.getValue(), UTF_8)).collect(Collectors.joining("&"));
		return string;
	}
	
	public static String getPage(int pageNo) {
		final File file;
		{
			String name = String.format("searchresultdata-%d.json", pageNo);;
			String path = Storage.Nikko.getPath("page", name);
			file = new File(path);
		}
		
		if (DEBUG_USE_FILE) {
			if (file.exists()) return FileUtil.read().file(file);
		}

		String postBody = getPostBody(pageNo);
		String url      = URL;
		HttpUtil.Result result = HttpUtil.getInstance().withReferer(REFERER).withPost(postBody, CONTENT_TYPE).download(url);
				
		if (result == null) {
			logger.error("result == null");
			logger.error("  url {}!", url);
			throw new UnexpectedException("result == null");
		}
		if (result.result == null) {
			logger.error("result.result == null");
			logger.error("  url       {}!", url);
			logger.error("  response  {}  {}", result.code, result.reasonPhrase);
			throw new UnexpectedException("result.result == null");
		}
		
		String page = result.result;
		
		// for debug
		FileUtil.write().file(file, page);

		return page;
	}
	
	public static class SearchResultData {
		public static class StockInfo implements Comparable<StockInfo> {
			public String asset_category;
			public String exch;
			public String nm;
			public String ric;
			public String smbc_nikko_ticker;
			public String sym;
			public String trbc_nm;
			
			@Override
			public String toString() {
				return StringUtil.toString(this);
			}

			@Override
			public int compareTo(StockInfo that) {
				return this.sym.compareTo(that.sym);
			}
		}
		
		@JSON.Name("datalist")
		Map<String, StockInfo> stockInfoMap;
		
		public String  decoder_nc;
		public String  endidx;
		public String  endpage;
		public boolean hasnext;
		public String  lastpage;
		public String  pageno;
		public String  startidx;
		public String  startpage;
		public String  totalrec;
		
		@Override
		public String toString() {
			return StringUtil.toString(this);
		}
	}
	
	
	public static void main(String[] args) {
		logger.info("START");
		
		List<SearchResultData.StockInfo> list = new ArrayList<>();
		{
			int lastPage;
			int totalRecord;
			
			for(int pageNo = 1; pageNo <= 9999; pageNo++) {
				if ((pageNo % 50) == 1) logger.info("pageNo  {}", pageNo);
				
				String page = getPage(pageNo);
				SearchResultData searchResultData = JSON.unmarshal(SearchResultData.class, new StringReader(page));
				
				for(var e: searchResultData.stockInfoMap.values()) {
					list.add(e);
				}

				lastPage    = Integer.valueOf(searchResultData.lastpage);
				totalRecord = Integer.valueOf(searchResultData.totalrec);
				
				if (pageNo == 1) {
					logger.info("lastPage    {}", lastPage);
					logger.info("totalRecord {}", totalRecord);
				}
				
				if (!searchResultData.hasnext) {
					if (pageNo != lastPage)         logger.warn("Unexpected pageNo  {}  {}", pageNo, lastPage);
					if (list.size() != totalRecord) logger.warn("Unexpected list size  {}  {}", list.size(), totalRecord);
					break;
				};
			}
			
			// save list as stock-inf.csv
			{
				String path = Storage.Nikko.getPath("stock-info.csv");
				logger.info("save   {}  {}", list.size(), path);
				ListUtil.save(SearchResultData.StockInfo.class, path, list);
			}
		}
		
		List<Stock> stockList = new ArrayList<>();
		{
			Map<String, Stock> stockMap = Stock.getMap();
			//  symbol
			
			for(var e: list) {
				if (e.smbc_nikko_ticker.isEmpty()) continue;
				String symbol = e.sym.replace("/", ".");
				
				if (stockMap.containsKey(symbol)) {
					Stock stock = stockMap.get(symbol);
					stockList.add(stock);
				} else {
					logger.warn("Unexpected symbol  {}", e);
				}
			}
		}
		
		logger.info("save   {}  {}", stockList.size(), NikkoStock.getPath());
		NikkoStock.save(stockList);
		
		logger.info("STOP");
	}
}
