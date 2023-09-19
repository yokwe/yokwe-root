package yokwe.finance.provider.jpx;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

import yokwe.util.ScrapeUtil;

public class StockPage {
	public static final String NO_INFORMATION = "指定された銘柄が見つかりません";
	
	// 会社基本情報  コード ISINコード 業種 所属部
	public static class CompanyInfo {
		public static final Pattern PAT = Pattern.compile(
				"<tr>\\s+" +
				"<td .+?><font .+?>コード</font></td>\\s+" +
				"<td .+?><font .+?>ISINコード</font></td>\\s+" +
				"<td .+?><font .+?>業種</font></td>\\s+" +
				"<td .+?><font .+?>所属部</font></td>\\s+" +
				"</tr>\\s+" +
				"<tr>\\s+" +
				"<td .+?><p><font .+?>(?<code>.+?)<br></font></p></td>\\s+" +
				"<td .+?><font .+?>(?<isin>[A-Z0-9]+)<br></font></td>\\s+" +
				"<td .+?><font .+?>(?<industry>.+?)<br></font></td>\\s+" +
				"<td .+?><font .+?>(?<category>.+?)<br></font></td>\\s+" +
				"</tr>"
		);
		public static CompanyInfo getInstance(String page) {
			return ScrapeUtil.get(CompanyInfo.class, PAT, page);
		}

		public final String code;
		public final String isin;
		public final String industry;
		public final Optional<String> category;
		
		public CompanyInfo(String code, String isin, String industry, Optional<String> category) {
			this.code     = code;
			this.isin     = isin;
			this.industry = industry;
			this.category = category;
		}
		
		@Override
		public String toString() {
			return String.format("{%s %s %s %s}", code, isin, industry, category);
		}
	}

	// 発行済株式数
	public static class Issued {
		public static final Pattern PAT = Pattern.compile(
			"<td .+?><font .+?>発行済株式数</font></td>\\s*" +
			"<td .+?><font .+?>\\s*(?<value>.*?)\\s*<br></font></td>\\s*"
		);
		public static Issued getInstance(String page) {
			return ScrapeUtil.get(Issued.class, PAT, page);
		}

		public final long value;
		
		public Issued(long value) {
			this.value = value;
		}
		
		@Override
		public String toString() {
			return String.format("%d", value);
		}
	}

	// 売買単位
	public static class TradeUnit {
		public static final Pattern PAT = Pattern.compile(
			"<td .+?><font .+?>売買単位</font></td>\\s+" +
			"<td .+?><font .+?>(?<value>[0-9,]+)株<br></font></td>\\s+"
		);
		public static TradeUnit getInstance(String page) {
			return ScrapeUtil.get(TradeUnit.class, PAT, page);
		}
		
		public final int value;
		
		public TradeUnit(int value) {
			this.value = value;
		}
		
		@Override
		public String toString() {
			return String.format("%d", value);
		}
	}

	// 日足ヒストリカル
	public static class PriceVolume {
		public static final Pattern PAT = Pattern.compile(
			"(?<yyyy>20[0-9][0-9])/(?<mm>[01][0-9])/(?<dd>[0123][0-9])," +
			"(?<open>([0-9]+\\.[0-9])?)," +
			"(?<high>([0-9]+\\.[0-9])?)," +
			"(?<low>([0-9]+\\.[0-9])?)," +
			"(?<close>([0-9]+\\.[0-9])?)," +
			"(?<volume>[0-9]+)," +
			"([0-9]+)?" +
			"[\\r\\n]+"
		);
		public static List<PriceVolume> getInstance(String page) {
			return ScrapeUtil.getList(PriceVolume.class, PAT, page);
		}
		
		public final String yyyy;
		public final String mm;
		public final String dd;
		
		@ScrapeUtil.AsNumber
		public final Optional<String> open;
		@ScrapeUtil.AsNumber
		public final Optional<String> high;
		@ScrapeUtil.AsNumber
		public final Optional<String> low;
		@ScrapeUtil.AsNumber
		public final Optional<String> close;
		public final long             volume;
		
		public PriceVolume(String yyyy, String mm, String dd, Optional<String> open, Optional<String> high, Optional<String> low, Optional<String> close, long volume) {
			this.yyyy   = yyyy;
			this.mm     = mm;
			this.dd     = dd;
			this.open   = open;
			this.high   = high;
			this.low    = low;
			this.close  = close;
			this.volume = volume;
		}
		
		public LocalDate getDate() {
			return LocalDate.parse(String.format("%s-%s-%s", yyyy, mm, dd));
		}
		public BigDecimal getOpen() {
			return open.isPresent() ?  new BigDecimal(open.get()) : null;
		}
		public BigDecimal getHigh() {
			return high.isPresent() ?  new BigDecimal(high.get()) : null;
		}
		public BigDecimal getLow() {
			return low.isPresent() ?  new BigDecimal(low.get()) : null;
		}
		public BigDecimal getClose() {
			return close.isPresent() ?  new BigDecimal(close.get()) : null;
		}
		
		@Override
		public String toString() {
			return String.format("{%s %s %s %s %s %s %s %s}", yyyy, mm, dd, open, high, low, close, volume);
		}
	}
}
