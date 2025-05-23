package yokwe.finance.provider.jreit;

import java.io.File;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import yokwe.finance.stock.StorageStock;
import yokwe.finance.type.StockCodeJP;
import yokwe.util.FileUtil;
import yokwe.util.ScrapeUtil;
import yokwe.util.ToString;
import yokwe.util.UnexpectedException;
import yokwe.util.http.HttpUtil;

public class UpdateJREITInfo {
	private static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();
	
	private static final boolean DEBUG_USE_FILE = false;

	private static String download(String url, String charset, String filePath, boolean useFile) {
		final String page;
		{
			File file = new File(filePath);
			if (useFile && file.exists()) {
				page = FileUtil.read().file(file);
			} else {
				HttpUtil.Result result = HttpUtil.getInstance().withCharset(charset).download(url);
				if (result == null || result.result == null) {
					logger.error("Unexpected");
					logger.error("  result  {}", result);
					throw new UnexpectedException("Unexpected");
				}
				page = result.result;
				// debug
				if (DEBUG_USE_FILE) logger.info("save  {}  {}", page.length(), file.getPath());
				FileUtil.write().file(file, page);
			}
		}
		return page;
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
		
		public LocalDate getLocalDate() {
			return LocalDate.of(yyyy, mm, dd);
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
		
		public int getSize() {
			String[] token = value.split("/");
			return token.length;
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
	
	
	private static Map<String, String> getCategoryMap() {
		//             stockCode
		//                     category
		String url     = "https://www.japan-reit.com/list/link/";
		String charset = "UTF-8";
		String filePath = StorageJREIT.storage.getPath("link.html");
		
		String page = download(url, charset, filePath, DEBUG_USE_FILE);
		
		var list = Link.getInstance(page);
		// sanity check
		if (list == null) {
			throw new UnexpectedException("list is null");
		}
		if (list.isEmpty()) {
			throw new UnexpectedException("list is empty");
		}
		
		return list.stream().collect(Collectors.toMap(o -> StockCodeJP.toStockCode5(o.stockCode), o -> o.category));
	}
	
	
	private static JREITInfoType getREIT(String stockCode, String url, String category) {
		String charset  = "UTF-8";
		String filePath = StorageJREIT.storage.getPath("page-info", stockCode + ".html");
		
		String page = download(url, charset, filePath, DEBUG_USE_FILE);
		
		LocalDate listingDate = ListingDate.getInstance(page).getLocalDate();
		int       divFreq     = Settlement.getInstance(page).getSize();
		String    name        = Name.getInstance(page).value;

		return new JREITInfoType(stockCode, listingDate, divFreq, category, name);
	}
	
	private static void update() {
		final List<String> reitList;
		final List<String> infraList;
		final Map<String, String> nameMap;
		//        stockCode name
		{
			var list = StorageStock.StockInfoJP.getList();
			reitList  = list.stream().filter(o -> o.type.isREIT()).map(o -> o.stockCode).collect(Collectors.toList());
			infraList = list.stream().filter(o -> o.type.isInfra()).map(o -> o.stockCode).collect(Collectors.toList());
			nameMap   = list.stream().collect(Collectors.toMap(o -> o.stockCode, o -> o.name));
		}
		logger.info("reit  {}", reitList.size());
		logger.info("infra {}", infraList.size());
		
		var categoryMap = getCategoryMap();
		
		var list = new ArrayList<JREITInfoType>();
		{
			int count = 0;
			for(var stockCode: reitList) {
				if ((++count % 10) == 1) logger.info("reit   {}  /  {}  {}", count, reitList.size(), stockCode);
				var name = nameMap.get(stockCode);
				
				String url      = String.format("https://www.japan-reit.com/meigara/%s/info/", StockCodeJP.toStockCode4(stockCode));			
				String category = categoryMap.get(stockCode);
				if (category == null) {
					logger.warn("unknown  {}  {}", stockCode, name);
					continue;
				}
				
				final JREITInfoType reitInfo = getREIT(stockCode, url, category);
				if (reitInfo == null) {
					logger.warn("unknown  {}  {}", stockCode, name);
					continue;
				}
				list.add(reitInfo);
			}
		}
		{
			int count = 0;
			for(var stockCode: infraList) {
				if ((++count % 10) == 1) logger.info("infra  {}  /  {}  {}", count, reitList.size(), stockCode);
				var name = nameMap.get(stockCode);

				String url      = String.format("https://www.japan-reit.com/infra/%s/info/", StockCodeJP.toStockCode4(stockCode));			
				String category = JREITInfoType.CATEGORY_INFRA_FUND;

				final JREITInfoType reitInfo = getREIT(stockCode, url, category);
				if (reitInfo == null) {
					logger.warn("unknown  {}  {}", stockCode, name);
					continue;
				}
				list.add(reitInfo);
			}
		}
		
		logger.info("save  {}  {}", list.size(), StorageJREIT.JREITInfo.getPath());
		StorageJREIT.JREITInfo.save(list);
	}
	
	
	public static void main(String[] args) {
		logger.info("START");
		
		update();
				
		logger.info("STOP");
	}
}
