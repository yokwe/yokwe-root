package yokwe.finance.account.prestia;

import java.math.BigDecimal;
import java.util.List;
import java.util.regex.Pattern;

import yokwe.util.ScrapeUtil;

public class FundPage {
	public static class FundInfo {
		public static final Pattern PAT = Pattern.compile(
			"<td .+?><a href=\"\" onclick=\"MM_openBrWindow\\('https://www.smbctb.co.jp/re/mf_service/D01-10_fund.html\\?fundcode=(?<fundCode>[0-9]+)'.+?>(?<fundName>.+?)</a></td>\\s+" +
			"<td .+?>(?<accountType>.+?)</td>\\s+" +
			"<td .+?>\\s+(?<price>[0-9,\\.,]+)<br><br>\\s+(?<priceDate>.+?)\\s+</td>\\s+" +
			"<td .+?>\\s+(?<units>[0-9\\.,]+)\\s+<br><br>\\s+(?<currency>...)/(?<fxRate>[0-9\\.]+)</td>\\s+" +
			"<td .+?>\\s+(?<valueJPY>[0-9\\.,]+)<br><br>\\s+(?<value>[0-9\\.,]+)\\s+</td>\\s+" +
			"<td .+?>.+?</td>\\s+" +
			"<td .+?>.+?</td>\\s+" +
			"<td .+?>.+?</td>\\s+" +
			"<td .+?>.+?</td>\\s+" +
			"</tr>" +
			"",
			Pattern.DOTALL
		);
		public static List<FundInfo> getInstance(String page) {
			return ScrapeUtil.getList(FundInfo.class, PAT, page);
		}
		
		public final String     fundCode;
		public final String     fundName;
		public final String     accountType;
		public final BigDecimal price;
		public final String     priceDate;
		public final BigDecimal units;
		public final String     currency;
		public final BigDecimal fxRate;
		public final BigDecimal valueJPY;
		public final BigDecimal value;
		
		public FundInfo(
			String fundCode, String fundName, String accountType, BigDecimal price, String priceDate,
			BigDecimal units, String currency, BigDecimal fxRate, BigDecimal valueJPY, BigDecimal value) {
			this.fundCode    = fundCode;
			this.fundName    = fundName;
			this.accountType = accountType;
			this.price       = price;
			this.priceDate   = priceDate;
			this.units       = units;
			this.currency    = currency;
			this.fxRate      = fxRate;
			this.valueJPY    = valueJPY;
			this.value       = value;
		}
		
		@Override
		public String toString() {
			return String.format("{%s  %s  %s  %s  %s  %s}", fundCode, accountType, price.toPlainString(), units.toPlainString(), currency, fundName);
		}
	}

}
