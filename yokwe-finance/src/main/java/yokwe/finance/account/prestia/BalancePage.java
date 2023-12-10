package yokwe.finance.account.prestia;

import java.math.BigDecimal;
import java.util.List;
import java.util.regex.Pattern;

import yokwe.util.ScrapeUtil;

public class BalancePage {
	//
	// 円普通預金
	//
	public static class DepositJPY {
		public static final Pattern PAT = Pattern.compile(
			"<tr>\\s+" +
			"<td>\\s+円普通預金\\s+</td>\\s+" +
			"<td>\\s+(?<accountNo>[0-9]+)\\s+</td>\\s+" +
			"<td .+?>\\s+(?<currency>[A-Z]{3})\\s+</td>\\s+" +
			"<td .+?>\\s+(?<value>[0-9,]+) [A-Z]{3}\\s+</td>\\s+" +
			"<td>\\s+<a .+?>.+?</a>\\s+</td>\\s+" +
			"</tr>" +
			"",
			Pattern.DOTALL
		);
		public static DepositJPY getInstance(String page) {
			return ScrapeUtil.get(DepositJPY.class, PAT, page);
		}
		
		public final String accountNo;
		public final String currency;
		@ScrapeUtil.AsNumber
		public final int    value;
		
		public DepositJPY(String accountNo, String currency, int value) {
			this.accountNo = accountNo;
			this.currency  = currency;
			this.value     = value;
		}
		
		@Override
		public String toString() {
			return String.format("{%s  %s  %d}", accountNo, currency, value);
		}
	}
	
	//
	// プレスティア マルチマネー口座円普通預金
	//
	public static class DepositMultiMoneyJPY {
		public static final Pattern PAT = Pattern.compile(
			"<tr>\\s+" +
			"<td>\\s+プレスティア マルチマネー口座円普通預金\\s+</td>\\s+" +
			"<td>\\s+(?<accountNo>[0-9]+)\\s+</td>\\s+" +
			"<td .+?>\\s+(?<currency>[A-Z]{3})\\s+</td>\\s+" +
			"<td .+?>\\s+(?<value>[0-9,]+) [A-Z]{3}\\s+</td>\\s+" +
			"<td>\\s+<a .+?>.+?</a>\\s+</td>\\s+" +
			"</tr>" +
			"",
			Pattern.DOTALL
		);
		public static DepositMultiMoneyJPY getInstance(String page) {
			return ScrapeUtil.get(DepositMultiMoneyJPY.class, PAT, page);
		}
		
		public final String accountNo;
		public final String currency;
		@ScrapeUtil.AsNumber
		public final int    value;
		
		public DepositMultiMoneyJPY(String accountNo, String currency, int value) {
			this.accountNo = accountNo;
			this.currency  = currency;
			this.value     = value;
		}
		
		@Override
		public String toString() {
			return String.format("{%s  %s  %d}", accountNo, currency, value);
		}
	}
	
	//
	// 米ドル普通預金
	//
	public static class DepositUSD {
		public static final Pattern PAT = Pattern.compile(
			"<tr>\\s+" +
			"<td>\\s+米ドル普通預金\\s+</td>\\s+" +
			"<td>\\s+(?<accountNo>[0-9]+)\\s+</td>\\s+" +
			"<td .+?>\\s+(?<currency>[A-Z]{3})\\s+</td>\\s+" +
			"<td .+?>\\s+(?<value>[0-9,\\.]+) [A-Z]{3}\\s+</td>\\s+" +
			"<td>\\s+<a .+?>.+?</a>\\s+</td>\\s+" +
			"</tr>" +
			"",
			Pattern.DOTALL
		);
		public static DepositUSD getInstance(String page) {
			return ScrapeUtil.get(DepositUSD.class, PAT, page);
		}
		
		public final String     accountNo;
		public final String     currency;
		@ScrapeUtil.AsNumber
		public final BigDecimal value;
		
		public DepositUSD(String accountNo, String currency, BigDecimal value) {
			this.accountNo = accountNo;
			this.currency  = currency;
			this.value     = value;
		}
		
		@Override
		public String toString() {
			return String.format("{%s  %s  %s}", accountNo, currency, value.toPlainString());
		}
	}
	
	
	//
	// プレスティア マルチマネー口座外貨普通預金
	//
	public static class DepositMultiMoney {
		public static final Pattern PAT = Pattern.compile(
			"<tr>\\s+" +
			"<td>\\s+プレスティア マルチマネー口座外貨普通預金\\s+</td>\\s+" +
			"<td>\\s+(?<accountNo>[0-9]+)\\s+</td>\\s+" +
			"<td .+?>\\s+(?<currency>[A-Z]{3})\\s+</td>\\s+" +
			"<td .+?>\\s+(?<value>[0-9,\\.]+) [A-Z]{3}\\s+</td>\\s+" +
			"<td>\\s+<a .+?>.+?</a>\\s+</td>\\s+" +
			"</tr>" +
			"",
			Pattern.DOTALL
		);
		public static List<DepositMultiMoney> getInstance(String page) {
			return ScrapeUtil.getList(DepositMultiMoney.class, PAT, page);
		}
		
		public final String     accountNo;
		public final String     currency;
		@ScrapeUtil.AsNumber
		public final BigDecimal value;
		
		public DepositMultiMoney(String accountNo, String currency, BigDecimal value) {
			this.accountNo = accountNo;
			this.currency  = currency;
			this.value     = value;
		}
		
		@Override
		public String toString() {
			return String.format("{%s  %s  %s}", accountNo, currency, value.toPlainString());
		}
	}
	
	
	//
	// 外貨定期預金
	//
	public static class TermDepositForeign {
		public static final Pattern PAT = Pattern.compile(
			"<tr>\\s+" +
			"<td>\\s+外貨定期預金\\s+</td>\\s+" +
			"<td>\\s+(?<accountNo>[0-9]+)\\s+</td>\\s+" +
			"<td .+?>\\s+(?<seqNo>[0-9]{5})\\s+</td>\\s+" +
			"<td .+?>\\s+(?<date>.+?)\\s+</td>\\s+" +
			"<td .+?>\\s+(?<currency>[A-Z]{3})\\s+</td>\\s+" +
			"<td .+?>\\s+(?<value>[0-9,\\.]+) [A-Z]{3}\\s+</td>\\s+" +
			"<td>\\s+<a .+?>.+?</a>\\s+</td>\\s+" +
			"</tr>" +
			"",
			Pattern.DOTALL
		);
		public static List<TermDepositForeign> getInstance(String page) {
			return ScrapeUtil.getList(TermDepositForeign.class, PAT, page);
		}
		
		public final String     accountNo;
		public final String     seqNo;
		public final String     date;
		public final String     currency;
		@ScrapeUtil.AsNumber
		public final BigDecimal value;
		
		public TermDepositForeign(String accountNo, String seqNo, String date, String currency, BigDecimal value) {
			this.accountNo = accountNo;
			this.seqNo     = seqNo;
			this.date      = date;
			this.currency  = currency;
			this.value     = value;
		}
		
		@Override
		public String toString() {
			return String.format("{%s  %s  %s  %s  %s}", accountNo, seqNo, date, currency, value.toPlainString());
		}
	}

}
