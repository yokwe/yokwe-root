package yokwe.stock.jp.japanreit;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import yokwe.stock.jp.jpx.Stock;
import yokwe.util.FileUtil;
import yokwe.util.ListUtil;
import yokwe.util.ScrapeUtil;
import yokwe.util.ScrapeUtil.AsNumber;
import yokwe.util.ToString;
import yokwe.util.http.HttpUtil;
import yokwe.util.json.JSON;

public class UpdateREIT {
	private static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();

	public static class RAW {
		public static class Dividend {
			//	    "date" : "2023-02-28",
			//	    "estimate" : 3900,
			//	    "result" : null,
			//	    "term" : 42
			public String  date;
			public Integer estimate;
			public Integer result;
			public int     term;
			
			public Dividend(String date, Integer estimage, Integer result, int term) {
				this.date     = date;
				this.estimate = estimage;
				this.result   = result;
				this.term     = term;
			}
			public Dividend() {
				this(null, null, null, 0);
			}
			
			@Override
			public String toString() {
				return ToString.withFieldName(this);
			}
		}
	}
	
	// 名称
	public static class Name {
		public static final Pattern PAT = Pattern.compile(
			//  <tr>
			//    <th scope="col">名称</th>
			//    <td colspan="3" scope="col">日本ビルファンド投資法人</td>
			//  </tr>
			"<tr>\\s+" +
			"<th .+>名称</th>\\s+" +
			"<td .+>(?<value>.+?)</td>\\s+" +
			"</tr>"
		);
		public static Name getInstance(String page) {
			return ScrapeUtil.get(Name.class, PAT, page);
		}

		public final String value;
		
		public Name(String value) {
			this.value = value.trim();
		}
		
		@Override
		public String toString() {
			return ToString.withFieldName(this);
		}
	}

	// 上場日
	public static class ListingDate {
		public static final Pattern PAT = Pattern.compile(
			// <th>上場日</th>
		    // <td>2002/06/12</td>
			"<th>上場日</th>\\s*" +
			"<td>(?<yyyy>20[0-9]{1,2})/(?<mm>[0-9]{1,2})/(?<dd>[0-9]{1,2})</td>"
		);
		public static ListingDate getInstance(String page) {
			return ScrapeUtil.get(ListingDate.class, PAT, page);
		}

		public final int yyyy;
		public final int mm;
		public final int dd;
		
		public ListingDate(int yyyy, int mm, int dd) {
			this.yyyy = yyyy;
			this.mm   = mm;
			this.dd   = dd;
		}
		
		public String getDate() {
			return String.format("%d-%02d-%02d", yyyy, mm, dd);
		}
		
		@Override
		public String toString() {
			return ToString.withFieldName(this);
		}
	}

	// 決算月
	public static class Settlement {
		public static final Pattern PAT = Pattern.compile(
			// <th>決算月</th>
		    // <td>5月/11月</td>
			"<th>決算月</th>\\s+" +
			"<td>(?<value>.+?)</td>"
		);
		public static Settlement getInstance(String page) {
			return ScrapeUtil.get(Settlement.class, PAT, page);
		}

		public final String value;
		
		public Settlement(String value) {
			this.value = value.trim();
		}
		
		@Override
		public String toString() {
			return ToString.withFieldName(this);
		}
		
		public List<Integer> getList() {
			List<Integer> ret = new ArrayList<>();
			
			String[] token = value.split("/");
			for(var e: token) {
				String string = e.replace("月", "").trim();
				ret.add(Integer.parseInt(string));
			}
			return ret;
		}
	}

	// 分配金の推移
	public static class Dividend {
		// 分配金の推移
		//	<tr>
		//	<td>16</td>
		//	<td>2023-11-30</td>
		//	<td></td>
		//	<td>0</td>
		//	<tr>

		public static final Pattern PAT = Pattern.compile(
			"<tr>\\s+" +
			"<td>(?<term>[0-9]+)</td>\\s+" +
			"<td>(?<yyyy>20[0-9][0-9])-(?<mm>[01]?[0-9])-(?<dd>[0123]?[0-9])</td>\\s+" +
			"<td>(?<result>[0-9,]*)</td>\\s+" +
			"<td>(?<estimate>[0-9,]*)</td>\\s+" +
			"</tr>"
		);
		public static List<Dividend> getInstance(String page) {
			return ScrapeUtil.getList(Dividend.class, PAT, page);
		}
		
		public final int    term;
		public final int    yyyy;
		public final int    mm;
		public final int    dd;
		@AsNumber
		public final String result;
		@AsNumber
		public final String estimate;
		
		public Dividend(int term, int yyyy, int mm, int dd, String result, String estimate) {
			this.term     = term;
			this.yyyy     = yyyy;
			this.mm       = mm;
			this.dd       = dd;
			this.result   = result.trim();
			this.estimate = estimate.trim();
		}
		
		@Override
		public String toString() {
			return ToString.withFieldName(this);
		}
	}

	public static class Link {
		//<tr>
		//<td><a href="/meigara/3279/">3279</a></td>
		//<td><a href="http://www.activia-reit.co.jp/" target="_blank">アクティビア・プロパティーズ投資法人</a><!--アクティビア・プロパティーズ投資法人--></td>
		//<td><a href="http://www.tokyu-trm.co.jp/" target="_blank">東急不動産リート・マネジメント投信株式会社</a><!--東急不動産リート・マネジメント投信株式会社--></td>
		//<td><a href="http://www.activia-reit.co.jp/index.html" target="_blank">データシート</a></td>
		//<td>複合型（オフィス＋都市型商業施設）</td>
		//</tr>
		public static final Pattern PAT = Pattern.compile(
			"<tr>\\s+" +
			"<td><a .+>(?<stockCode>.+)</a></td>\\s+" +
			"<td><a .+>(?<reitName>.+)</a>.*</td>\\s+" +
			"<td><a .+>(?<managementName>.+)</a>.*</td>\\s+" +
			"<td><a .+>データシート</a></td>\\s+" +
			"<td>(?<category>.+)</td>\\s+" +
			"</tr>"
		);
		public static List<Link> getInstance(String page) {
			return ScrapeUtil.getList(Link.class, PAT, page);
		}
		
		public final String stockCode;
		public final String reitName;
		public final String managementName;
		public final String category;
		
		public Link(String stockCode, String reitName, String managementName, String category) {
			this.stockCode      = stockCode;
			this.reitName       = reitName;
			this.managementName = managementName;
			this.category       = category;
		}
		
		@Override
		public String toString() {
			return ToString.withFieldName(this);
		}
	}
	
	private static REIT getREIT(Stock stock, List<REITDiv> divList) {
		String stockCode = stock.stockCode;
		
		// category
		final Map<String, Link> linkMap;
		//  stockCode
		{
			String url = "https://www.japan-reit.com/list/link/";
			HttpUtil.Result result = HttpUtil.getInstance().download(url);
			String page = result.result;
			if (page == null) {
				logger.warn("failed to download info {}", stockCode);
				return null;
			}
			
			var list = Link.getInstance(page);
			if (list == null) {
				logger.warn("failed to getList {}", stockCode);
				return null;
			}
			if (list.isEmpty()) {
				logger.warn("list is empty", stockCode);
			}

			// sanity check
			linkMap = ListUtil.checkDuplicate(list, o -> Stock.toStockCode5(o.stockCode));
		}
		
		final REIT reit;
		{
			String url = String.format("https://www.japan-reit.com/meigara/%s/info/", Stock.toStockCode4(stockCode));			
			HttpUtil.Result result = HttpUtil.getInstance().download(url);
			String page = result.result;
			if (page == null) {
				logger.warn("failed to download info {}", stockCode);
				return null;
			}
			
			Name        name        = Name.getInstance(page);
			ListingDate listingDate = ListingDate.getInstance(page);
			Settlement  settlement  = Settlement.getInstance(page);
			
			if (name == null) {
				logger.warn("failed to scrape name {}", stockCode);
				return null;
			}
			if (listingDate == null) {
				logger.warn("failed to scrape listingDate {}", stockCode);
				return null;
			}
			if (settlement == null) {
				logger.warn("failed to scrape settlement {}", stockCode);
				return null;
			}
			
			String category;
			if (linkMap.containsKey(stockCode)) {
				category = linkMap.get(stockCode).category;
			} else {
				logger.warn("no linkMap entry {}", stockCode);
				category = "UNKNOWN";
			}

			reit = new REIT(stockCode, listingDate.getDate(), settlement.getList().size(), category, name.value);
		}
		
		// build divList
		{
			String url = String.format("https://www.japan-reit.com/meigara/%s/bunpai.json", Stock.toStockCode4(stockCode));
			HttpUtil.Result result = HttpUtil.getInstance().download(url);
			String page = result.result;
			if (page == null) {
				logger.warn("failed to download div {}", stockCode);
				return null;
			}
			
			var list = JSON.getList(RAW.Dividend.class, page);
			if (list == null) {
				logger.warn("failed to getList {}", stockCode);
				return null;
			}
			if (list.isEmpty()) {
				logger.warn("no dividend {}  {}", stockCode, reit.listingDate);
			}
			for(var e: list) {
				String date  = e.date;
				int estimate = (e.estimate == null) ? REITDiv.NO_VALUE : e.estimate.intValue();
				int actual   = (e.result   == null) ? REITDiv.NO_VALUE : e.result.intValue();
				
				REITDiv div = new REITDiv(date, estimate, actual);
				if (div.hasValue()) {
					divList.add(div);
				}
			}
		}
		
		return reit;
	}
	private static REIT getInfraFund(Stock stock, List<REITDiv> divList) {
		String stockCode = stock.stockCode;
		
		REIT reit;
		{
			String url = String.format("https://www.japan-reit.com/infra/%s/info/", Stock.toStockCode4(stockCode));			
			HttpUtil.Result result = HttpUtil.getInstance().download(url);
			String page = result.result;
			if (page == null) {
				logger.warn("failed to download info {}", stockCode);
				return null;
			}
			
			var name        = Name.getInstance(page);
			var listingDate = ListingDate.getInstance(page);
			var settlement  = Settlement.getInstance(page);

			if (name == null) {
				logger.warn("failed to scrape name {}", stockCode);
				return null;
			}
			if (listingDate == null) {
				logger.warn("failed to scrape listingDate {}", stockCode);
				return null;
			}
			if (settlement == null) {
				logger.warn("failed to scrape settlement {}", stockCode);
				return null;
			}
			reit = new REIT(stockCode, listingDate.getDate(), settlement.getList().size(), "INFRA FUND", name.value);
		}
		{
			final String url = String.format("https://www.japan-reit.com/infra/%s/dividend/", Stock.toStockCode4(stockCode));			
			HttpUtil.Result result = HttpUtil.getInstance().download(url);
			String page = result.result;
			if (page == null) {
				logger.warn("failed to download div {}", stockCode);
				return null;
			}
			var list = Dividend.getInstance(page);

			// build divList
			if (list.isEmpty()) {
				logger.warn("no dividend {}  {}", stockCode, reit.listingDate);
			}
			for(var e: list) {
			    String date     = String.format("%d-%02d-%02d", e.yyyy, e.mm, e.dd);
				int    estimate = (e.estimate == null || e.estimate.isEmpty()) ? REITDiv.NO_VALUE : Integer.parseInt(e.estimate);
				int    actual   = (e.result   == null || e.result.isEmpty())   ? REITDiv.NO_VALUE : Integer.parseInt(e.result);
				REITDiv div = new REITDiv(date, estimate, actual);
				if (div.hasValue()) {
					divList.add(div);
				}
			}
		}
		
		return reit;
	}

	private static void update() {
		List<Stock> stockList = Stock.getList().stream().filter(o -> o.isREIT() || o.isInfraFund()).collect(Collectors.toList());
		Collections.shuffle(stockList);
		logger.info("reit {}", stockList.size());
		
		List<REIT> list = new ArrayList<>();
		int count = 0;
		for(var e: stockList) {
			if ((count % 10) == 0) logger.info(String.format("%4d / %4d  %s", count, stockList.size(), e.stockCode));
			count++;

			String stockCode = e.stockCode;
			
			final REIT reit;
			final List<REITDiv> divList = new ArrayList<>();
			if (e.isInfraFund()) {
				reit = getInfraFund(e, divList);
			} else {
				reit = getREIT(e, divList);
			}
			if (reit == null) continue;
			
			// save div
			logger.info("save dividend {} {}", stockCode, divList.size());

			REITDiv.save(stockCode, divList);
			
			list.add(reit);
		}
		
		logger.info("save {} {}", list.size(), REIT.getPath());
		REIT.save(list);
	}
	
	private static void moveUnknownFile() {
		Set<String> validNameSet = new TreeSet<>();
		for(var e: Stock.getList()) {
			File file = new File(REITDiv.getPath(e.stockCode));
			validNameSet.add(file.getName());
		}
		
		// reit-div
		FileUtil.moveUnknownFile(validNameSet, REITDiv.getPath(), REITDiv.getPathDelist());
	}


	public static void main(String[] args) {
		logger.info("START");
		update();
		moveUnknownFile();
		logger.info("STOP");
	}
}
