package yokwe.finance.account.nikko;

import java.util.regex.Pattern;

import yokwe.util.ScrapeUtil;

public class BalanceBankPage {
	// 円流動性預金
	public static class DepositInfo {
		public static final Pattern PAT = Pattern.compile(
			"<tr>\\s+" +
			"<td .+?><span .+?><a .+?>円流動性預金等<br>（ＭＲＦ含む</a>）</span></td>\\s+" +
			"<td .+?><span .+?>(?<value>.+?)円</span></td>\\s+" +
			"<td .+?>.+?</td>\\s+" +
			"<td .+?>.+?</td>\\s+" +
			"<td .+?>.+?</td>\\s+" +
			"</tr>" +
			""
		);
		public static DepositInfo getInstance(String page) {
			return ScrapeUtil.get(DepositInfo.class, PAT, page);
		}
		
		@ScrapeUtil.AsNumber
		public final int value;
		
		public DepositInfo(int value) {
			this.value = value;
		}
		
		@Override
		public String toString() {
			return String.format("%d", value);
		}
	}
	// 円定期性預金
	public static class TermDepositInfo {
		public static final Pattern PAT = Pattern.compile(
			"<tr>\\s+" +
			"<td .+?><span .+?><a .+?>円定期性預金</a></span></td>\\s+" +
			"<td .+?><span .+?>(?<value>.+?)円</span></td>\\s+" +
			"<td .+?>.+?</td>\\s+" +
			"<td .+?>.+?</td>\\s+" +
			"<td .+?>.+?</td>\\s+" +
			"</tr>" +
			""
		);
		public static TermDepositInfo getInstance(String page) {
			return ScrapeUtil.get(TermDepositInfo.class, PAT, page);
		}
		
		@ScrapeUtil.AsNumber
		public final int value;
		
		public TermDepositInfo(int value) {
			this.value = value;
		}
		
		@Override
		public String toString() {
			return String.format("%d", value);
		}
	}	
	// 外貨預金
	public static class ForeignDepositInfo {
		public static final Pattern PAT = Pattern.compile(
			"<tr>\\s+" +
			"<td .+?><span .+?><a .+?>外貨預金</a></span></td>\\s+" +
			"<td .+?><span .+?>(?<value>.+?)円</span></td>\\s+" +
			"<td .+?>.+?</td>\\s+" +
			"<td .+?>.+?</td>\\s+" +
			"<td .+?>.+?</td>\\s+" +
			"</tr>" +
			""
		);
		public static ForeignDepositInfo getInstance(String page) {
			return ScrapeUtil.get(ForeignDepositInfo.class, PAT, page);
		}
		
		@ScrapeUtil.AsNumber
		public final int value;
		
		public ForeignDepositInfo(int value) {
			this.value = value;
		}
		
		@Override
		public String toString() {
			return String.format("%d", value);
		}
	}	
}
