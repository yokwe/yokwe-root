package yokwe.stock.jp.toushin2;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.regex.Pattern;

import yokwe.util.FileUtil;
import yokwe.util.ScrapeUtil;

public class Detail {
	private static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();
	
	private static final Pattern PAT = Pattern.compile(
		"<span class=\"fds-gray-fg fds-font-size-small ml-auto mt-auto\">評価基準日.+(?<yyyy>20[0-9][0-9])年(?<mm>[01]?[0-9])月(?<dd>[0123]?[0-9])日</span>" +
	    ".+?" +
		"<div .+?>\\s+基準価額.+?</div>\\s+" +
	    "<div .+?>\\s+" +
	    "<span .+?>\\s+(?<price>.+?)\\s+</span>\\s+" +
	    "<small>円</small>\\s+" +
	    "</div>" +
	    ".+?" +
	    "<!-- 前日比 -->\\s+" +
	    "<div .+?>\\s+" +
	    "<span .+?> <i\\s+class=\"fa fa-long-arrow-(?<upDown>(up|down))\"></i>\\s+(?<change>.+?)\\s+</span>\\s+" +
	    "<small>円</small>\\s+" +
	    ".+?" +
	    "<div .+?>\\s+純資産総額.+?>.+?</div>\\s+" +
	    "<div>\\s+(?<nav>.+?)百万円\\s+</div>" +
	    ".+?" +
	    "<div .+?>\\s+商品分類.+?>.+?</div>\\s+" +
	    "<div .+?>\\s+" +
	    "<span\\s+class=.+?>(?<cat1>.+?)</span>\\s+" +
	    "<span\\s+class=.+?>/</span>\\s+" +
	    "<span\\s+class=.+?>(?<cat2>.+?)</span>\\s+" +
	    "<span\\s+class=.+?>/</span>\\s+" +
	    "<span\\s+class=.+?>(?<cat3>.+?)</span>\\s+" +
	    "</div>" +
	    ".+?" +
	    "<input type=\"hidden\" id=\"isinCd\"\\s+value=\"(?<isinCode>.+?)\">" +
		
		"",
		Pattern.DOTALL
		);
		
	public static Detail getInstance(String page) {
		return ScrapeUtil.get(Detail.class, PAT, page);
	}
	
	
    public String isinCode;
    public String yyyy;
    public String mm;
    public String dd;
    @ScrapeUtil.AsNumber
	public String price;  // 基準価額
	public String upDown; // 前日比 up or down
    @ScrapeUtil.AsNumber
	public String change; // 前日比
    @ScrapeUtil.AsNumber
	public String nav;    // 純資産総額（百万円）
	public String cat1;   // 商品分類
	public String cat2;   // 商品分類
	public String cat3;   // 商品分類
	
    @ScrapeUtil.Ignore
	public LocalDate date;     // 年月日
    @ScrapeUtil.Ignore
    public BigDecimal todayValue;
    @ScrapeUtil.Ignore
    public BigDecimal previousValue;
    @ScrapeUtil.Ignore
    public BigDecimal navValue;
    @ScrapeUtil.Ignore
    public String category;

	
	public Detail(String isinCode, String yyyy, String mm, String dd, String price, String upDown, String change, String nav, String cat1, String cat2, String cat3) {
		this.isinCode = isinCode;
		this.yyyy     = yyyy;
		this.mm       = mm;
		this.dd       = dd;
		this.price    = price;
		this.upDown   = upDown;
		this.change   = change;
		this.nav      = nav;
		this.cat1     = cat1;
		this.cat2     = cat2;
		this.cat3     = cat3;
		
		this.date = LocalDate.of(Integer.parseInt(yyyy), Integer.parseInt(mm), Integer.parseInt(dd));
		this.todayValue = new BigDecimal(this.price);
		
		BigDecimal delta = new BigDecimal(this.change);
		if (this.upDown.equals("up")) delta = delta.negate();
		
		this.previousValue = this.todayValue.add(delta);
		this.navValue = new BigDecimal(this.nav);
		this.category = cat1 + "/" + cat2 + "/" + cat3;
	}
	
	public Detail() {
		//
	}
	@Override
	public String toString() {
		return String.format("{isinCode: %s, date: %s, today: %s, prev: %s, nav: %s, category: %s}",
				isinCode, date, todayValue.toPlainString(), previousValue.toPlainString(), navValue.toPlainString(), category);
	}
	
	public static class Div {
		public String yyyy;
		public String mm;
		public String dd;
		public String amount;
		
		@ScrapeUtil.Ignore
		public LocalDate date;
		@ScrapeUtil.Ignore
		public BigDecimal value;
		
		public Div(String yyyy, String mm, String dd, String amount) {
			this.yyyy   = yyyy;
			this.mm     = mm;
			this.dd     = dd;
			this.amount = amount;
			
			this.date = LocalDate.of(Integer.parseInt(yyyy), Integer.parseInt(mm), Integer.parseInt(dd));
			this.value = new BigDecimal(amount);
		}
		
		@Override
		public String toString() {
			return String.format("{%s %s}", date, value.toPlainString());
		}
	}
	private static final Pattern PAT_DIV = Pattern.compile(
		"<tr>\\s+" +
	    "<th .+?>(?<yyyy>(19|20)[0-9]{2})/(?<mm>[01]?[0-9])/(?<dd>[0123]?[0-9])\\s+</th>\\s+" +
		"<td .+?>(?<amount>.+?)円\\s+</td>\\s+" +
	    "</tr>" +
		"",
		Pattern.DOTALL
		);
	
	public static void main(String[] args) {
		logger.info("START");
		
		String page = FileUtil.read().file("tmp/JP90C00052U5.html");
//		Matcher m = PAT.matcher(page);
//		if (m.find()) {
//			logger.info("found");
//			logger.info("yyyy   !{}!", m.group("yyyy"));
//			logger.info("mm     !{}!", m.group("mm"));
//			logger.info("dd     !{}!", m.group("dd"));
//			logger.info("price  !{}!", m.group("price"));
//			logger.info("upDown !{}!", m.group("upDown"));
//			logger.info("change !{}!", m.group("change"));
//			logger.info("nav    !{}!", m.group("nav"));
//			logger.info("cat1   !{}!", m.group("cat1"));
//			logger.info("cat2   !{}!", m.group("cat2"));
//			logger.info("cat3   !{}!", m.group("cat3"));
//			logger.info("isin   !{}!", m.group("isinCode"));
//		} else {
//			logger.info("not found");
//		}
		
		Detail detail = ScrapeUtil.get(Detail.class, PAT, page);
		logger.info("detail {}", detail);
		
		List<Div> divList = ScrapeUtil.getList(Div.class, PAT_DIV, page);
		logger.info("divList {}", divList);
		
		logger.info("STOP");
	}

}
