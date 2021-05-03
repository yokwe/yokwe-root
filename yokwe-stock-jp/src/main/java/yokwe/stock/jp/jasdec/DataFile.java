package yokwe.stock.jp.jasdec;

import java.io.File;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.slf4j.LoggerFactory;

import yokwe.util.FileUtil;
import yokwe.util.ScrapeUtil;
import yokwe.util.StringUtil;
import yokwe.util.UnexpectedException;
import yokwe.util.http.HttpUtil;

public class DataFile {
	static final org.slf4j.Logger logger = LoggerFactory.getLogger(DataFile.class);
	
	private static final String PREFIX = "download";
	
	public static String getPath(String path) {
		return JASDEC.getPath(String.format("%s/%s", PREFIX, path));
	}
	public static String getPath() {
		return getPath("");
	}
	
	private static void deleteForeignFile(String path, Set<String> set) {
		File dir = new File(path);
		dir.mkdirs();
		
		for(var e: dir.listFiles()) {
			if (e.isDirectory()) continue;
			// delete empty file
			if (e.length() == 0) {
				e.delete();
				continue;
			}
			// skip if set contains the name
			if (set.contains(e.getName())) continue;
			
			// otherwise delete the file
			e.delete();
		}
	}

	private static List<String> notExistingList(String path, Set<String> set) {
		File dir = new File(path);
		dir.mkdirs();
		
		List<String> ret = new ArrayList<>();
		for(var e: set) {
			File file = new File(dir, e);
			if (file.exists()) continue;
			ret.add(e);
		}
		return ret;
	}

	
	private static long SLEEP_FOR_SERVER = 4000;
	private static void sleepForServer() {
		try {
			Thread.sleep(SLEEP_FOR_SERVER);
		} catch (InterruptedException e) {
			logger.error("Unexpected InterruptedException");
			
			String exceptionName = e.getClass().getSimpleName();
			logger.error("{} {}", exceptionName, e);
			throw new UnexpectedException("Unexpected InterruptedException");
		}
	}

	private static class Page {
		private static final String PREFIX = "page";
		
		public static String getPath(String pageNo) {
			return DataFile.getPath(String.format("%s/%s", PREFIX, pageNo));
		}
		public static String getPath(int pageNo) {
			return getPath(String.valueOf(pageNo));
		}
		public static String getPath() {
			return getPath("");
		}
		
		private static String URL_ROOT = "http://www.jasdec.com/reading/itmei.php?error_flg=&todb=ok&isincode=&name=&brandname=&nexts=&offset=&rowcntmax=&linkno=1&delhead=&invite=onclick&kensaku=ok";

		public static class RootPage {
			// [<a href="wait_itmei.php?delhead=&amp;linkno=1&amp;rowcntmax=5758&amp;offset=-50&amp;invite=&amp;brandname=&amp;name="><b>1</b></a>]
			public static final Pattern PAT = Pattern.compile(
				"\\[" +
				"<a href=\"(?<url>.+?)\">" +
				"<b>1</b></a>" +
				"\\]"
			);
			
			public static final Pattern PAT_ROWCNTMAX = Pattern.compile("rowcntmax=(?<no>[0-9]+)");
			public static final Pattern PAT_OFFSET    = Pattern.compile("offset=(?<no>-?[0-9]+)");
			
			@SuppressWarnings("exports")
			public static RootPage getInstance(String page) {
				return ScrapeUtil.get(RootPage.class, PAT, page);
			}
			
			@SuppressWarnings("unused")
			public RootPage(String url) {
				this.url       = url;
				this.rowcntmax = Integer.parseInt(StringUtil.getGroupOne(PAT_ROWCNTMAX, url));
				this.offset    = Integer.parseInt(StringUtil.getGroupOne(PAT_OFFSET, url));
			}

			public String url;
			@ScrapeUtil.Ignore
			public int rowcntmax;
			@ScrapeUtil.Ignore
			public int offset;
			
			@Override
			public String toString() {
				return String.format("{%d %d %s}", rowcntmax, offset, url);
			}
		}

		public static class Item {
			private static final Pattern PAT = Pattern.compile(
					"<table border=\"1\" cellpadding=\"3\" cellspacing=\"1\" width=\"505\" class=\"maincolor\" >" +
					".+?" +
					"<span class=\"hy\">.+?</span>" + // 銘柄正式名称
					".+?" +
					"<span class=\"hy\">(?<name>.+?)</span>" +
					".+?" +
					"<span class=\"hy\">(?:.+?)</span>" + // 銘柄略称
					".+?" +
					"<span class=\"hy\">(?:.+?)</span>" +
					".+?" +
					"<span class=\"hy\">(?:.+?)</span>" + // 発行者名
					".+?" +
					"<span class=\"hy\">(?<issuer>.+?)</span>" +
					".+?" +
					"<span class=\"hy\">(?:.+?)</span>" + // 受託会社名（原信託）
					".+?" +
					"<span class=\"hy\">(?:.+?)</span>" +
					".+?" +
					"<span class=\"hy\">(?:.+?)</span>" + // 受託会社名（接続先）
					".+?" +
					"<span class=\"hy\">(?:.+?)</span>" +
					".+?" +
					"<span class=\"hy\">(?:.+?)</span>" + // ISINコード
					".+?" +
					"<span class=\"hy\">(?<isinCode>.+?)</span>" +
					".+?" +
					"<span class=\"hy\">(?:.+?)</span>" + // ファンドコード
					".+?" +
					"<span class=\"hy\">(?:.+?)</span>" +
					".+?" +
					"<span class=\"hy\">(?:.+?)</span>" + // 募集区分
					".+?" +
					"<span class=\"hy\">(?<offerCategory>.+?)</span>" +
					".+?" +
					"<span class=\"hy\">(?:.+?)</span>" + // 投信区分
					".+?" +
					"<span class=\"hy\">(?<fundCategory>.+?)</span>" +
					".+?" +
					"<span class=\"hy\">(?:.+?)</span>" + // 設定日
					".+?" +
					"<span class=\"hy\">(?<issueDate>.+?)</span>" +
					".+?" +
					"<span class=\"hy\">(?:.+?)</span>" + // 償還日
					".+?" +
					"<span class=\"hy\">(?<redemptionDate>.+?)</span>" +
					".+?" +
					"<a href=\"it_details.php\\?idno_1=(?<idno>[0-9]+)\"" +
					".+?" +
					"</table>",
					Pattern.DOTALL
				);
			
			@SuppressWarnings("exports")
			public static List<Item> getInstance(String page) {
				return ScrapeUtil.getList(Item.class, PAT, page);
			}
			
			public String idno;           // IDNO
			public String isinCode;       // ISINコード
			public String issueDate;      // 設定日
			public String redemptionDate; // 償還日
			public String offerCategory;  // 募集区分
			public String fundCategory;   // 投信区分
			public String issuer;         // 発行者名	
			public String name;           // 銘柄正式名称
			
			@SuppressWarnings("unused")
			public Item(String idno, String isinCode, String issueDate, String redemptionDate, String offerCategory, String fundCategory, String issuer, String name) {
				this.idno          = idno.trim();
				
				this.isinCode      = isinCode.trim();
				
				// YYYY/MM/DD => YYYY-MM-DD
				this.issueDate     = issueDate.trim().replace("/", "-");
				this.redemptionDate = redemptionDate.trim().replace("/", "-");
				if (this.redemptionDate.equals("無期限")) {
					this.redemptionDate = Fund.INDEFINITE;
				}
				
				this.offerCategory = offerCategory.trim().replace("<br>", "");
				this.fundCategory  = fundCategory.trim();
				this.issuer        = issuer.trim();
				this.name          = name.trim().replace("<br>", "");
			}

			@Override
			public String toString() {
				return StringUtil.toString(this);
			}
		}

		private static String getURL(int rowcntmax, int offset) {
			return String.format("http://www.jasdec.com/reading/itmei.php?error_flg=&todb=ok&isincode=&name=&brandname=&nexts=&offset=%d&rowcntmax=%d&linkno=1&delhead=&invite=&kensaku=", offset, rowcntmax);
		}

		private static void download(HttpUtil httpUtil) {
			logger.info("download page");

			String rootPageString = httpUtil.download(URL_ROOT).result;
			RootPage rootPage = RootPage.getInstance(rootPageString);
						
			Set<String> set = new TreeSet<>();
			{
				int fileNo = 0;
				for(int i = 0; i < rootPage.rowcntmax; i += 50) {
					set.add(String.valueOf(fileNo++));
				}
			}
			String dir = getPath();
			deleteForeignFile(dir, set);
			
			{
				int fileNo = 0;
				for(int i = 0; i < rootPage.rowcntmax; i += 50) {
					// skip if file already exists
					File file = new File(getPath(fileNo++));
					if (file.exists()) continue;
					
					logger.info("{}", String.format("%4d / %4d", fileNo, set.size()));
					
					String url = getURL(rootPage.rowcntmax, i - 50);
					String page = httpUtil.download(url).result;
					FileUtil.write().file(file, page);
				
					sleepForServer();
				}
			}
		}
		
		private static List<Fund> update() {
			logger.info("update page");
			
			List<Fund> ret = new ArrayList<>();
			
			File dir = new File(getPath());
			for(var file: dir.listFiles()) {
				if (file.isDirectory()) continue;
				
				String page = FileUtil.read().file(file);
				List<Item> list = Item.getInstance(page);
				
				for(var e: list) {
					int        idno           = Integer.valueOf(e.idno);
					String     isinCode       = e.isinCode;
					LocalDate  issueDate      = LocalDate.parse(e.issueDate);
					LocalDate  redemptionDate = LocalDate.parse(e.redemptionDate);
					BigDecimal unitPrice      = BigDecimal.ZERO;
					BigDecimal minimuUnit     = BigDecimal.ZERO;
					String     offerCategory  = e.offerCategory;
					String     fundCategory   = e.fundCategory;
					String     issuer         = e.issuer;
					String     name           = e.name;
					
					Fund fund = new Fund(
						idno, isinCode, issueDate, redemptionDate, unitPrice, minimuUnit, 
						offerCategory, fundCategory, issuer, name);
					ret.add(fund);
				}
			}
			
			Collections.sort(ret);
			return ret;
		}
	}

	private static class Detail {
		private static final String PREFIX = "detail";
		
		public static String getPath(String pageNo) {
			return DataFile.getPath(String.format("%s/%s", PREFIX, pageNo));
		}
		public static String getPath() {
			return getPath("");
		}
		
		private static final String URL_DETAIL = "http://www.jasdec.com/reading/it_details.php?idno_1=%d";

		public static class DetailPage {
			private static final Pattern PAT = Pattern.compile(
				"<span .+?>\\s+当初1口当たり元本\\s+</span>.+?<span .+?>(?<unitPrice>.+?)</span>" +
				".+?" +
				"<span .+?>\\s+最低発行単位口数\\s+</span>.+?<span .+?>(?<minimuUnit>.+?)</span>" +
				"",
				Pattern.DOTALL
			);
			
			@SuppressWarnings("exports")
			public static DetailPage getInstance(String page) {
				return ScrapeUtil.get(DetailPage.class, PAT, page);
			}

			public String unitPrice;  // 当初1口当たり元本
			public String minimuUnit; // 最低発行単位口数
			
			@SuppressWarnings("unused")
			public DetailPage(String unitPrice, String minimumUnit) {
				this.unitPrice  = unitPrice.trim().replace(",", "");
				this.minimuUnit = minimumUnit.trim().replace(",", "");
			}
			
			@Override
			public String toString() {
				return StringUtil.toString(this);
			}
		}

		private static void download(HttpUtil httpUtil, Map<String, Fund> fundMap) {
			logger.info("download detail");

			Set<String> set = fundMap.keySet();

			String dir = getPath();
			
			// delete foreign file
			deleteForeignFile(dir, set);
			// create list of not existing file
			List<String> list = notExistingList(dir, set);
			
			Collections.shuffle(list);

			int count = 0;
			for(var isinCode: list) {
				count++;

				int    idno = fundMap.get(isinCode).idno;
				String url  = String.format(URL_DETAIL, idno);
				File   file = new File(getPath(isinCode));
				
				if (file.exists()) continue;

				int countMinusOne = count - 1;
				if ((countMinusOne % 50) == 0) {
					logger.info("{}", String.format("%4d / %4d", countMinusOne, list.size()));
				}

				String page = httpUtil.download(url).result;
				FileUtil.write().file(file, page);
				
				sleepForServer();
			}
		}
		
		private static void update(Map<String, Fund> fundMap) {
			logger.info("update detail");
			File dir = new File(getPath());
			for(var file: dir.listFiles()) {
				if (file.isDirectory()) continue;
				
				String page = FileUtil.read().file(file);
				DetailPage detailPage = DetailPage.getInstance(page);
				
				String isinCode = file.getName();
				Fund fund = fundMap.get(isinCode);
				
				fund.unitPrice  = new BigDecimal(detailPage.unitPrice);
				fund.minimuUnit = new BigDecimal(detailPage.minimuUnit);
			}
		}
	}

	
	public static void main(String[] args) {
		logger.info("START");
		
		HttpUtil httpUtil = HttpUtil.getInstance().withCharset("Shift_JIS");

		Page.download(httpUtil);
		List<Fund> list = Page.update();
		
		// build isinCode to idno map
		Map<String, Fund> fundMap = new TreeMap<>();
		for(var e: list) {
			fundMap.put(e.isinCode, e);
		}

		Detail.download(httpUtil, fundMap);
		Detail.update(fundMap);
		
		{
			List<Fund> fundList = fundMap.values().stream().collect(Collectors.toList());
			Fund.save(fundList);
			logger.info("save {} {}", fundList.size(), Fund.getPath());
		}

		logger.info("STOP");
	}	
}
