package yokwe.finance.account.prestia;

import java.math.BigDecimal;
import java.util.List;
import java.util.regex.Pattern;

import yokwe.util.ScrapeUtil;
import yokwe.util.StringUtil;

public class FundPage {
	public static class FundReturns {
		private static String PAT_STRING = 
			"<tr>\\s+<td colspan=\"7\" class=\"#CLASS_NAME# .+?><a .+?fundcode=(?<fundCode>.+?)'.+?>(?<fundName>.+?)</a></td>\\s+</tr>\\s+" +
			"<tr>\\s+" +
			"<td .+?>(?<accountTypeA>.+?)<br>\\s+(?<accountTypeB>.+?)</td>\\s+" +
			"<td .+?>(?<units>.+?)</td>\\s+" +
			"<td .+?>(?<value>.+?)</td>\\s+" +
			"<td .+?>(?<divTotal>.+?)</td>\\s+" +
			"<td .+?>(?<soldTotal>.+?)</td>\\s+" +
			"<td .+?>(?<buyTotal>.+?)</td>\\s+" +
			"<td .+?>(?<returns>.+?)</td>\\s+" +
			"</tr>\\s+" +
			"<tr>\\s+" +
			"<td .+?>(?<dateStart>.+?)</td>\\s+" +
			"<td .+?>(?<unitPrice>.+?)</td>\\s+" +
			"</tr>\\s+" +
			"";

		private static Pattern getPattern(String className) {
			var patternString = PAT_STRING.replace("#CLASS_NAME#", className);
			return Pattern.compile(patternString, Pattern.DOTALL);
		}
		
		
		public static List<FundReturns> getInstanceUS(String page) {
			var pat = getPattern("GAI_FUND_NAME");
			return ScrapeUtil.getList(FundReturns.class, pat, page);
		}
		public static List<FundReturns> getInstanceJP(String page) {
			var pat = getPattern("NAI_FUND_NAME");
			return ScrapeUtil.getList(FundReturns.class, pat, page);
		}
		
		public final String     fundCode;
		public final String     fundName;
		public final String		accountTypeA;
		public final String		accountTypeB;
		
		public final BigDecimal units;
		public final BigDecimal value;
		public final BigDecimal divTotal;
		public final BigDecimal soldTotal;
		public final BigDecimal buyTotal;
		public final BigDecimal returns;
		
		public final String     dateStart;
		public final BigDecimal unitPrice;
		
		public FundReturns(
			String fundCode, String fundName, String accountTypeA, String accountTypeB,
			BigDecimal units, BigDecimal value, BigDecimal divTotal, BigDecimal soldTotal, BigDecimal buyTotal, BigDecimal returns,
			String dateStart, BigDecimal unitPrice) {
			this.fundCode     = fundCode;
			this.fundName     = fundName;
			this.accountTypeA = accountTypeA;
			this.accountTypeB = accountTypeB;
			
			this.units        = units;
			this.value        = value;
			this.divTotal     = divTotal;
			this.soldTotal    = soldTotal;
			this.buyTotal     = buyTotal;
			this.returns      = returns;

			this.dateStart    = dateStart;
			this.unitPrice    = unitPrice;
		}
		
		@Override
		public String toString() {
			return StringUtil.toString(this);
		}
	}
}
