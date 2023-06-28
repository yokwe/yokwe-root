package yokwe.stock.jp.sony;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import yokwe.stock.jp.Storage;
import yokwe.util.ScrapeUtil;
import yokwe.util.UnexpectedException;
import yokwe.util.http.HttpUtil;

public class UpdateFundInfo {
	private static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();

	// 現在値
	// var msFundCode = '2013121001';
	public static class MSFundCode {
		public static final Pattern PAT = Pattern.compile(
			"var msFundCode = '(?<value>.+?)';"
		);
		public static MSFundCode getInstance(String page) {
			return ScrapeUtil.get(MSFundCode.class, PAT, page);
		}

		public final String value;
		
		public MSFundCode(String value) {
			this.value = value;
		}
		
		@Override
		public String toString() {
			return String.format("{%s}", value);
		}
	}
	
	// ファンドの特色
//	<section class="dd_box mt10"><h2>ファンドの特色</h2>
//	<hr class="hr1">
//	<p>日本を除く世界主要先進国の株式に投資することにより､MSCIコクサイ･インデックス(配当込み､円換算ベース)に連動する投資成果をめざす｡独自の計量モデル等を活用し､ポートフォリオを構築する｡購入時および換金時の手数料は無料､信託財産留保額なし｡原則として､対円での為替ヘッジは行わない｡ファミリーファンド方式で運用｡11月決算｡</p></section>
	public static class Description {
		public static final Pattern PAT = Pattern.compile(
			"<section class=\"dd_box mt10\"><h2>ファンドの特色</h2>\\s+" +
			"<hr class=\"hr1\">\\s+" +
			"<p>(?<value>.+)</p>"
		);
		public static Description getInstance(String page) {
			return ScrapeUtil.get(Description.class, PAT, page);
		}

		public final String value;
		
		public Description(String value) {
			this.value = value;
		}
		
		@Override
		public String toString() {
			return String.format("{%s}", value);
		}
	}

	// 設定日
//	<dt>設定日</dt>
//	<dd>2013年12月10日</dd>
	public static class InceptionDate {
		public static final Pattern PAT = Pattern.compile(
			"<dt>設定日</dt>\\s+" +
			"<dd>(?<value>.+)</dd>"
		);
		public static InceptionDate getInstance(String page) {
			return ScrapeUtil.get(InceptionDate.class, PAT, page);
		}

		public final String value;
		
		public InceptionDate(String value) {
			this.value = value;
		}
		
		@Override
		public String toString() {
			return String.format("{%s}", value);
		}
	}

	// 償還日
//	<dt>償還日</dt>
//	<dd>無期限</dd>
	public static class RedemptionDate {
		public static final Pattern PAT = Pattern.compile(
			"<dt>償還日</dt>\\s+" +
			"<dd>(?<value>.+)</dd>"
		);
		public static RedemptionDate getInstance(String page) {
			return ScrapeUtil.get(RedemptionDate.class, PAT, page);
		}

		public final String value;
		
		public RedemptionDate(String value) {
			this.value = value;
		}
		
		@Override
		public String toString() {
			return String.format("{%s}", value);
		}
	}

	// 決算日
//	<dt>決算日</dt>
//	<dd>11月20日</dd>
	public static class ClosingDate {
		public static final Pattern PAT = Pattern.compile(
			"<dt>決算日</dt>\\s+" +
			"<dd>(?<value>[^<]+)</dd>"
		);
		public static ClosingDate getInstance(String page) {
			return ScrapeUtil.get(ClosingDate.class, PAT, page);
		}

		public final String value;
		
		public ClosingDate(String value) {
			this.value = value.replace(" ", "").replace("\r", "").replace("\n", "");
		}
		
		@Override
		public String toString() {
			return String.format("{%s}", value);
		}
	}

	// 購入単位
//	<dt>購入単位</dt>
//	<dd>通常購入：1万円以上1円単位<br>積立：1千円以上1円単位</dd>
	public static class PurchaseUnit {
		public static final Pattern PAT = Pattern.compile(
			"<dt>購入単位</dt>\\s+" +
			"<dd>通常購入：(?<value>[^<]+)<"
		);
		public static PurchaseUnit getInstance(String page) {
			return ScrapeUtil.get(PurchaseUnit.class, PAT, page);
		}

		public final String value;
		
		public PurchaseUnit(String value) {
			this.value = value.replace(" ", "").replace("\r", "").replace("\n", "");
		}
		
		@Override
		public String toString() {
			return String.format("{%s}", value);
		}
	}

	// 購入約定日
//	<dt>購入約定日</dt>
//	<dd>購入申込受付日の翌営業日</dd>
	public static class PurchaseDate {
		public static final Pattern PAT = Pattern.compile(
			"<dt>購入約定日</dt>\\s+" +
			"<dd>(?<value>[^<]+)</dd>"
		);
		public static PurchaseDate getInstance(String page) {
			return ScrapeUtil.get(PurchaseDate.class, PAT, page);
		}

		public final String value;
		
		public PurchaseDate(String value) {
			this.value = value.replace(" ", "").replace("\r", "").replace("\n", "");
		}
		
		@Override
		public String toString() {
			return String.format("{%s}", value);
		}
	}

	// 購入価格
//	<dt>購入価格</dt>
//	<dd>購入約定日の基準価額</dd>
	public static class PurchasePrice {
		public static final Pattern PAT = Pattern.compile(
			"<dt>購入価格</dt>\\s+" +
			"<dd>(?<value>[^<]+)</dd>"
		);
		public static PurchasePrice getInstance(String page) {
			return ScrapeUtil.get(PurchasePrice.class, PAT, page);
		}

		public final String value;
		
		public PurchasePrice(String value) {
			this.value = value.replace(" ", "").replace("\r", "").replace("\n", "");
		}
		
		@Override
		public String toString() {
			return String.format("{%s}", value);
		}
	}

	// 解約単位
//	<dt>解約単位</dt>
//	<dd>1円以上1円単位</dd>
	public static class CancelUnit {
		public static final Pattern PAT = Pattern.compile(
			"<dt>解約単位</dt>\\s+" +
			"<dd>(?<value>[^<]+)</dd>"
		);
		public static CancelUnit getInstance(String page) {
			return ScrapeUtil.get(CancelUnit.class, PAT, page);
		}

		public final String value;
		
		public CancelUnit(String value) {
			this.value = value.replace(" ", "").replace("\r", "").replace("\n", "");
		}
		
		@Override
		public String toString() {
			return String.format("{%s}", value);
		}
	}
	
	// 解約価格
//	<dt>解約価格</dt>
//	<dd>解約申込受付日の翌営業日の基準価額</dd>
	public static class CancellPrice {
		public static final Pattern PAT = Pattern.compile(
			"<dt>解約価格</dt>\\s+" +
			"<dd>(?<value>[^<]+)</dd>"
		);
		public static CancellPrice getInstance(String page) {
			return ScrapeUtil.get(CancellPrice.class, PAT, page);
		}

		public final String value;
		
		public CancellPrice(String value) {
			this.value = value.replace(" ", "").replace("\r", "").replace("\n", "");
		}
		
		@Override
		public String toString() {
			return String.format("{%s}", value);
		}
	}

	// 解約代金支払日
//	<dt>解約代金支払日</dt>
//	<dd>解約申込受付日から6営業日目</dd>
	public static class CancelPaymentDate {
		public static final Pattern PAT = Pattern.compile(
			"<dt>解約代金支払日</dt>\\s+" +
			"<dd>(?<value>[^<]+)</dd>"
		);
		public static CancelPaymentDate getInstance(String page) {
			return ScrapeUtil.get(CancelPaymentDate.class, PAT, page);
		}

		public final String value;
		
		public CancelPaymentDate(String value) {
			this.value = value.replace(" ", "").replace("\r", "").replace("\n", "");
		}
		
		@Override
		public String toString() {
			return String.format("{%s}", value);
		}
	}

	// 購入・解約の申込締切
//	<dt>購入・解約の申込締切</dt>
//	<dd>15:00</dd>
	public static class Deadline {
		public static final Pattern PAT = Pattern.compile(
			"<dt>購入・解約の申込締切</dt>\\s+" +
			"<dd>(?<value>[^<]+)</dd>"
		);
		public static Deadline getInstance(String page) {
			return ScrapeUtil.get(Deadline.class, PAT, page);
		}

		public final String value;
		
		public Deadline(String value) {
			this.value = value.replace(" ", "").replace("\r", "").replace("\n", "");
		}
		
		@Override
		public String toString() {
			return String.format("{%s}", value);
		}
	}

	// 販売手数料（税込）
//	<dt>販売手数料（税込）</dt>
//	<dd>
//	<div class="bg_f2f0d9">50口未満 なし</div>
//	<div>50口以上500口未満 なし</div>
//	<div class="bg_f2f0d9">500口以上 なし</div>
//	</dd>
	public static class SalesFee {
		public static final Pattern PAT = Pattern.compile(
			"<dt>販売手数料（税込）</dt>\\s+" + 
			"<dd>\\s+" +
			"<div.*?>(?<a>.+)</div>\\s+" +
			"<div.*?>(?<b>.+)</div>\\s+" +
			"<div.*?>(?<c>.+)</div>\\s+" +
			"</dd>"
		);
		public static SalesFee getInstance(String page) {
			return ScrapeUtil.get(SalesFee.class, PAT, page);
		}

		public final String a;
		public final String b;
		public final String c;
		
		public SalesFee(String a, String b, String c) {
			this.a = a;
			this.b = b;
			this.c = c;
		}
		
		@Override
		public String toString() {
			return String.format("{%s %s %s}", a, b, c);
		}
	}

	// 信託報酬（税込）
//	<dt>信託報酬（税込）</dt>
//	<dd>0.1023%
//	                                </dd>
	public static class TrustFee {
		public static final Pattern PAT = Pattern.compile(
			"<dt>(管理|信託)報酬（税込）</dt>\\s+" +
			"<dd>(?<value>[^<]+)</dd>"
		);
		public static TrustFee getInstance(String page) {
			return ScrapeUtil.get(TrustFee.class, PAT, page);
		}

		public final String value;
		
		public TrustFee(String value) {
			this.value = value.replace(" ", "").replace("\r", "").replace("\n", "");
		}
		
		@Override
		public String toString() {
			return String.format("{%s}", value);
		}
	}

	// 実質信託報酬（税込）
//	<dt>実質信託報酬（税込）</dt>
//	<dd>0.1023%</dd>
	public static class RealTrustFee {
		public static final Pattern PAT = Pattern.compile(
			"<dt>実質(管理|信託)報酬（税込）</dt>\\s+" +
			"<dd>(?<value>[^<]+)</dd>"
		);
		public static RealTrustFee getInstance(String page) {
			return ScrapeUtil.get(RealTrustFee.class, PAT, page);
		}

		public final String value;
		
		public RealTrustFee(String value) {
			this.value = value.replace(" ", "").replace("\r", "").replace("\n", "");
		}
		
		@Override
		public String toString() {
			return String.format("{%s}", value);
		}
	}

	// 信託財産留保額
//	<dt>信託財産留保額</dt>
//	<dd>
//												-
//											</dd>
	public static class CancelFee {
		public static final Pattern PAT = Pattern.compile(
			"<dt>信託財産留保額</dt>\\s+" +
			"<dd>(?<value>[^<]+)</dd>"
		);
		public static CancelFee getInstance(String page) {
			return ScrapeUtil.get(CancelFee.class, PAT, page);
		}

		public final String value;
		
		public CancelFee(String value) {
			this.value = value.replace(" ", "").replace("\t", "").replace("\r", "").replace("\n", "").replace("-", "");
		}
		
		@Override
		public String toString() {
			return String.format("{%s}", value);
		}
	}

//	<dl class="kihon3 mt5 clearfix bg_fdf9f5">
//	<dt>2019年11月20日</dt>
//	<dd>0円</dd>
//	</dl>
	public static class DivHistory {
		public static final Pattern PAT = Pattern.compile(
			"<dl class=\"kihon(3|4).*?\">\\s+" +
			"<dt>(?<date>.+?)</dt>\\s+" +
			"<dd>(?<value>.+?)</dd>\\s+" +
			"</dl>"
		);
		public static List<DivHistory> getInstance(String page) {
			return ScrapeUtil.getList(DivHistory.class, PAT, page);
		}
		
		public final String date;
		public final String value;
		
		public DivHistory(String date, String value) {
			this.date  = date;
			this.value = value;
		}
		
		static final Pattern JYMD = Pattern.compile("(?<yy>.+)年(?<mm>.+)月(?<dd>.+)日");
		public LocalDate getLocalDate() {
			Matcher m = JYMD.matcher(date);
			if (m.find()) {
				String yy = m.group("yy");
				String mm = m.group("mm");
				String dd = m.group("dd");
				
				String string = String.format("%s-%s-%s", yy, mm, dd);
				return LocalDate.parse(string);
			} else {
				logger.error("Unexpected string");
				logger.error("  date {}!", date);
				throw new UnexpectedException("Unexpected string");
			}
		}
		public BigDecimal getValue() {
			String string = value.replace(",", "").replace("円", "").replace("米ドル", "");
			return new BigDecimal(string);
		}
		
		@Override
		public String toString() {
			return String.format("{%s %s}", date, value);
		}
	}

	
	private static void updateList(List<FundInfo> infoList, List<Dividend> divList, Fund fund, String page) {
		MSFundCode        msFundCode        = MSFundCode.getInstance(page);
		Description       description       = Description.getInstance(page);
		InceptionDate     inceptionDate     = InceptionDate.getInstance(page);
		RedemptionDate    redemptionDate    = RedemptionDate.getInstance(page);
		ClosingDate       closingDate       = ClosingDate.getInstance(page);
		PurchaseUnit      purchaseUnit      = PurchaseUnit.getInstance(page);
		PurchaseDate      purchaseDate      = PurchaseDate.getInstance(page);
		PurchasePrice     purchasePrice     = PurchasePrice.getInstance(page);
		CancelUnit        cancelUnit        = CancelUnit.getInstance(page);
		CancellPrice      cancelPrice       = CancellPrice.getInstance(page);
		CancelPaymentDate cancelPaymentDate = CancelPaymentDate.getInstance(page);
		Deadline          deadline          = Deadline.getInstance(page);
		SalesFee          salesFee          = SalesFee.getInstance(page);
		TrustFee          trustFee          = TrustFee.getInstance(page);
		RealTrustFee      realTrustFee      = RealTrustFee.getInstance(page);
		CancelFee         cancelFee         = CancelFee.getInstance(page);
		List<DivHistory>  divHistoryList    = DivHistory.getInstance(page);
		
		FundInfo fundInfo = new FundInfo();
		
		fundInfo.isinCode          = fund.isinCode;
		fundInfo.fundName          = fund.fundName;
		fundInfo.currency          = fund.currency;
		
		fundInfo.msFundCode        = (msFundCode == null) ? "" : msFundCode.value;
		fundInfo.description       = description.value;
		fundInfo.inceptionDate     = inceptionDate.value;
		fundInfo.redemptionDate    = redemptionDate.value;
		fundInfo.closingDate       = closingDate.value;
		fundInfo.purchaseUnit      = purchaseUnit.value;
		fundInfo.purchaseDate      = purchaseDate.value;
		fundInfo.purchasePrice     = purchasePrice.value;
		fundInfo.cancelUnit        = cancelUnit.value;
		fundInfo.cancelPrice       = cancelPrice.value;
		fundInfo.cancelPaymentDate = cancelPaymentDate.value;
		fundInfo.deadline          = deadline.value;
		fundInfo.cancelFee         = (cancelFee == null) ? "" : cancelFee.value;
		fundInfo.salesFeeA         = (salesFee == null) ? "" : salesFee.a;
		fundInfo.salesFeeB         = (salesFee == null) ? "" : salesFee.b;
		fundInfo.salesFeeC         = (salesFee == null) ? "" : salesFee.c;
		fundInfo.trustFee          = trustFee.value;
		fundInfo.realTrustFee      = realTrustFee.value;
		
		infoList.add(fundInfo);

		for(DivHistory e: divHistoryList) {
			Dividend dividend = new Dividend(e.getLocalDate(), fund.isinCode, fund.currency, e.getValue());
			divList.add(dividend);
		}
		
	}

	private static String FORMAT_URL = "https://apl.wealthadvisor.jp/webasp/sonybk/detail/%s.html";
	public static String getURL(String isinCode) {
		return String.format(FORMAT_URL, isinCode);
	}
	
	private static final String PREFIX = "page";
	public static String getPath(String isinCode) {
		return Storage.Sony.getPath(PREFIX, isinCode + "html");
	}

	public static void main(String[] args) {
		logger.info("START");
		
		List<FundInfo> infoList = new ArrayList<>();
		List<Dividend> divList  = new ArrayList<>();
		
		// Build infoLsit and divList
		for(Fund e: Fund.getList()) {
			String isinCode = e.isinCode;
			logger.info("{} {}", isinCode, e.fundName);
			
			String url = getURL(isinCode);
			HttpUtil.Result result = HttpUtil.getInstance().download(url);
			String page = result.result;
			
			updateList(infoList, divList, e, page);
		}
		
		// Save infoList
		logger.info("save {} {}", infoList.size(), FundInfo.getPath());
		FundInfo.save(infoList);
		
		// Save divList
		{
			logger.info("divList {}", divList.size());
			Map<String, List<Dividend>> map = new TreeMap<>();
			//  isinCode
			for(Dividend e: divList) {
				String key = e.isinCode;
				if (map.containsKey(key)) {
					map.get(key).add(e);
				} else {
					List<Dividend> list = new ArrayList<>();
					list.add(e);
					map.put(key, list);
				}
			}
			logger.info("divList map {}", map.size());
			for(var e: map.entrySet()) {
				var isinCode = e.getKey();
				var list     = e.getValue();
				Dividend.save(isinCode, list);
			}
		}
		
		logger.info("STOP");
	}
}
