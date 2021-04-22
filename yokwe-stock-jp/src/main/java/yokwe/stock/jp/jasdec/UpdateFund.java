package yokwe.stock.jp.jasdec;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.slf4j.LoggerFactory;

import yokwe.util.ScrapeUtil;
import yokwe.util.StringUtil;
import yokwe.util.UnexpectedException;
import yokwe.util.http.HttpUtil;

public class UpdateFund {
	static final org.slf4j.Logger logger = LoggerFactory.getLogger(UpdateFund.class);

	public static class PageLink {
		// [<a href="wait_itmei.php?delhead=&amp;linkno=1&amp;rowcntmax=5758&amp;offset=-50&amp;invite=&amp;brandname=&amp;name="><b>1</b></a>]
		public static final Pattern PAT = Pattern.compile(
			"\\[" +
			"<a href=\"(?<url>.+?)\">" +
			"<b>1</b></a>" +
			"\\]"
		);
		
		public static final Pattern PAT_ROWCNTMAX = Pattern.compile("rowcntmax=(?<no>[0-9]+)");
		public static final Pattern PAT_OFFSET    = Pattern.compile("offset=(?<no>-?[0-9]+)");
		
		public static List<PageLink> getInstance(String page) {
			return ScrapeUtil.getList(PageLink.class, PAT, page);
		}
		
		public PageLink(String url) {
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
	
	private static String URL_ROOT = "http://www.jasdec.com/reading/itmei.php?error_flg=&todb=ok&isincode=&name=&brandname=&nexts=&offset=&rowcntmax=&linkno=1&delhead=&invite=onclick&kensaku=ok";
	private static String getURL(int rowcntmax, int offset) {
		return String.format("http://www.jasdec.com/reading/itmei.php?error_flg=&todb=ok&isincode=&name=&brandname=&nexts=&offset=%d&rowcntmax=%d&linkno=1&delhead=&invite=&kensaku=", offset, rowcntmax);
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
		
		public Item(String idno, String isinCode, String issueDate, String redemptionDate, String offerCategory, String fundCategory, String issuer, String name) {
			this.idno          = idno.trim();
			
			this.isinCode      = isinCode.trim();
			
			// YYYY/MM/DD => YYYY-MM-DD
			this.issueDate     = issueDate.trim().replace("/", "-");
			this.redemptionDate = redemptionDate.trim().replace("/", "-");
			if (this.redemptionDate.equals("無期限")) {
				this.redemptionDate = Fund.NO_LIMIT;
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
	
	public static void main(String[] args) {
		logger.info("START");
		
		HttpUtil httpUtil = HttpUtil.getInstance().withCharset("Shift_JIS");
				
		PageLink pageLink;
		{
			String page = httpUtil.download(URL_ROOT).result;
			logger.info("page {}", page.length());
			
			List<PageLink> pageLinkList = PageLink.getInstance(page);
			logger.info("pageLinkList {}", pageLinkList.size());
			
			pageLink = pageLinkList.get(0);
			logger.info("pageLink {}", pageLink);
		}
		
		List<Fund> fundList = new ArrayList<>();

		for(int i = 0; i < pageLink.rowcntmax; i += 50) {
			logger.info("page {}", String.format("%4d .. %4d", i + 1, i + 50));
			String url = getURL(pageLink.rowcntmax, i - 50);
//			logger.info("url {}", url);
			String page = httpUtil.download(url).result;
//			logger.info("page {}", page.length());
			
			List<Item> items = Item.getInstance(page);			
//			logger.info("items {}", items.size());
			
			for(var e: items) {
				Fund fund = new Fund(Integer.parseInt(e.idno), e.isinCode, e.issueDate, e.redemptionDate, e.offerCategory, e.fundCategory, e.issuer, e.name);
				fundList.add(fund);
			}
		
			// Make pause for not stress server
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				logger.error("Unexpected InterruptedException");
				
				String exceptionName = e.getClass().getSimpleName();
				logger.error("{} {}", exceptionName, e);
				throw new UnexpectedException("Unexpected InterruptedException");
			}
		}
		
		logger.info("save {} {}", fundList.size(), Fund.getPath());
		Fund.save(fundList);
		
		logger.info("STOP");
	}
}
