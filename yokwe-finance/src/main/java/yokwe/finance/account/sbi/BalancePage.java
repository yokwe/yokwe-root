package yokwe.finance.account.sbi;

import java.math.BigDecimal;
import java.util.List;
import java.util.regex.Pattern;

import yokwe.util.ScrapeUtil;
import yokwe.util.StringUtil;

public class BalancePage {
	public static class DepositJPY {
		public static final Pattern PAT = Pattern.compile(
			"<tr>\\s+" +
			"<td .+?><div .+?><font .+?>現金残高等</font></div></td>\\s+" +
			"<td .+?><div .+?><font .+?>(?<value>[0-9,]+)</font>.+?</div></td>\\s+" +
			"</tr>" +
			""
		);
		public static DepositJPY getInstance(String page) {
			return ScrapeUtil.get(DepositJPY.class, PAT, page);
		}
		
		public final BigDecimal value;
		
		public DepositJPY(BigDecimal value) {
			this.value = value;
		}
		
		@Override
		public String toString() {
			return String.format("%d", value.toPlainString());
		}
	}

	public static class DepositForeign {
		public static final Pattern PAT = Pattern.compile(
			"<td .+?><a href=\".+?\" onclick=\"javascript:ex_hidden\\('(?<currency>...)'\\)\"><img .+?></a></td>\\s+" +
			"<td .+?>\\s+" +
			"<table .+?>\\s+" +
			"<tbody><tr>\\s+" +
			"<td .+?>現金</td>\\s+" +
			"<td .+?>(?<value>[0-9,\\.]+)</td>\\s+" +
			"</tr>" +
			""
		);
		public static List<DepositForeign> getInstance(String page) {
			return ScrapeUtil.getList(DepositForeign.class, PAT, page);
		}
		
		public final String     currency;
		public final BigDecimal value;
		
		public DepositForeign(String currency, BigDecimal value) {
			this.currency = currency;
			this.value    = value;
		}
		
		@Override
		public String toString() {
			return String.format("{%s  %s}", currency, value.toPlainString());
		}
	}
	
	public static class StockUS {
		public static final Pattern PAT = Pattern.compile(
			"<a href=.+?;sw_param4=(?<code>[A-Z]+)&amp;sw_param5=STOCK.+?>.+?</a>\\s+" +
			"</td>\\s+" +
			"<td .+?>.+?</td>\\s+" +
			"<td .+?>.+?</td>\\s+" +
			"<td .+?>\\s+(?<value>[0-9\\.]+)<br>.+?</td>" +
			"",
			Pattern.DOTALL
		);
		public static List<StockUS> getInstance(String page) {
			return ScrapeUtil.getList(StockUS.class, PAT, page);
		}
		
		public final String     code;
		public final BigDecimal value;
		
		public StockUS(String code, BigDecimal value) {
			this.code  = code;
			this.value = value;
		}
		
		@Override
		public String toString() {
			return StringUtil.toString(this);
		}
	}
	
	public static class MMFInfo {
		public static final Pattern PAT = Pattern.compile(
			"<a href=\"https://site2\\.sbisec\\.co\\.jp/ETGate/\\?_ControlID=WPLETfiR001Control&amp;_PageID=WPLETfiR001Mlst10&amp;_DataStoreID=DSWPLETfiR001Control.+?>(?<name>.+?)</a>\\s+</td>\\s+" +
			"<td .+?>\\s+(?<units>[0-9,\\.]+)<br>\\s+(?<factor>[0-9,\\.]+)\\s+</td>\\s+" +
			"<td .+?>\\s+(?<value>[0-9,\\.]+)<br>\\s+(?<buyFXRate>[0-9,\\.]+)\\s+</td>\\s+" +
			"",
			Pattern.DOTALL
		);
		public static List<MMFInfo> getInstance(String page) {
			return ScrapeUtil.getList(MMFInfo.class, PAT, page);
		}
		
		public final String     name;
		public final BigDecimal units;
		public final BigDecimal factor;
		public final BigDecimal value;
		public final BigDecimal buyFXRate;
		
		public MMFInfo(String name, BigDecimal units, BigDecimal factor, BigDecimal value, BigDecimal buyFXRate) {
			this.name      = name;
			this.units     = units;
			this.factor    = factor;
			this.value     = value;
			this.buyFXRate = buyFXRate;
		}
		
		@Override
		public String toString() {
			return StringUtil.toString(this);
		}
	}
	
	
	public static class BondForeign {
		public static final Pattern PAT = Pattern.compile(
			"<a href=\"https://search\\.sbisec\\.co\\.jp/v2/popwin/info/connect/bond/(?<code>[A-Z0-9]+)\\.html\".+?>\\s+[A-Z0-9]+ (?<name>.+?)\\s+</a>\\s+</td>\\s+" +
			"<td .+?>\\s+(?<faceValue>[0-9,\\.]+)<br>.+?</td>\\s+" +
			"<td .+?>\\s+(?<buyRatio>.+?)<br>\\s+(?<buyFXRate>[0-9,\\.]+)\\s+</td>\\s+" +
			"",
			Pattern.DOTALL
		);
		public static List<BondForeign> getInstance(String page) {
			return ScrapeUtil.getList(BondForeign.class, PAT, page);
		}
		
		public final String     code;
		public final String     name;
		public final BigDecimal faceValue;
		public final BigDecimal buyRatio;
		public final BigDecimal buyFXRate;
		
		public BondForeign(String code, String name, BigDecimal faceValue, BigDecimal buyRatio, BigDecimal buyFXRate) {
			this.code       = code;
			this.name       = name;
			this.faceValue  = faceValue;
			this.buyRatio   = buyRatio;
			this.buyFXRate  = buyFXRate;
		}
		
		@Override
		public String toString() {
			return StringUtil.toString(this);
		}
	}
}
