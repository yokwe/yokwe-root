package yokwe.stock.us.webull;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.stream.Collectors;

import yokwe.stock.us.Stock;
import yokwe.stock.us.Storage;
import yokwe.util.ListUtil;
import yokwe.util.StringUtil;
import yokwe.util.UnexpectedException;
import yokwe.util.http.HttpUtil;
import yokwe.util.json.JSON;

public class UpdateWebullStock {
	private static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();
	
	private static final String URL          = "https://jptrade.wbjpsecurities.com/api/trading/v1/global/ticker/security/querySecurityByPage";
	private static final String REFERER      = "https://www.webull.co.jp/search\n";
	private static final String CONTENT_TYPE = "application/json";
	
	
	public static class Item implements Comparable<Item> {
		public int    tickerId;
		public int    securityId;
		public String symbol;
		@JSON.Ignore
		public String jpName = "";
		public String name;
		public String market;
		
		@Override
		public String toString() {
			return StringUtil.toString(this);
		}

		@Override
		public int compareTo(Item that) {
			return this.symbol.compareTo(that.symbol);
		}
	}

	public static class QueryResult {		
		public boolean hasNextPage;
		public String  nextPageSecurityId;
		public Item[]  items;
		
		@Override
		public String toString() {
			return StringUtil.toString(this);
		}
	}
	
	private static String getPostBody(String securityId) {
		// {"pageSize":20,"searchValue":"","securityTypes":[2,4,5,7]}
		// {"lastSecurityId":10161111799,"pageSize":20,"searchValue":"","securityTypes":[2,4,5,7]}

		LinkedHashMap<String, String> map = new LinkedHashMap<>();
		if (securityId != null) map.put("lastSecurityId", "\"" + securityId + "\"");
		map.put("pageSize",      "\"100\"");
		map.put("searchValue",   "\"\"");
		map.put("securityTypes", "[2,4,5,7]");
		
		String string = map.entrySet().stream().map(o -> "\"" + o.getKey() + "\":" + o.getValue()).collect(Collectors.joining(",", "{", "}"));
		return string;
	}
	
	public static String getPage(String securityId) {
		String postBody = getPostBody(securityId);
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
//		logger.info("result {}", result.result);
		
		return result.result;
	}
	
	public static List<Item> getItemList() {
		List<Item> itemList = new ArrayList<>();
		
		int count = 0;
		String securityId = null;
		for(;;) {
			logger.info("securityId  {}  {}", securityId, count++);
			String page = getPage(securityId);
			QueryResult queryResult = JSON.unmarshal(QueryResult.class, page);
			for(var e: queryResult.items) {
				itemList.add(e);
			}
			if (!queryResult.hasNextPage) break;
			securityId = queryResult.nextPageSecurityId;
		}
		
		return itemList;
	}
	
	public static void main(String[] args) {
		logger.info("START");
		
		List<Stock> stockList = new ArrayList<>();
		{
			var stockMap = Stock.getMap();
			List<Item> itemList = getItemList();
			{
				// save for debug
				String path = Storage.Webull.getPath("item.csv");
				ListUtil.save(Item.class, path, itemList);
			}
			
			logger.info("itemList  {}", itemList.size());
			
			for(var e: itemList) {
				// LHC U
				String symbol = e.symbol.replace(" PR", "-").replace(" ", ".");
				
				if (stockMap.containsKey(symbol)) {
					Stock stock = stockMap.get(symbol);
					stockList.add(stock);
				} else {
					logger.info("unexpeced symbol  {}  {}", symbol, e.name);
				}
			}
		}
		
		logger.info("save   {}  {}", stockList.size(), WebullStock.getPath());
		WebullStock.save(stockList);
		
		logger.info("STOP");
	}
}
