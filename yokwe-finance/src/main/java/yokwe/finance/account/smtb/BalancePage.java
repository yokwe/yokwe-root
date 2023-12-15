package yokwe.finance.account.smtb;

import java.math.BigDecimal;
import java.util.regex.Pattern;

import yokwe.util.ScrapeUtil;

public class BalancePage {
	public static class DepositJPY {
		//  <td align="left" class="data-table1-td2 mcp-mtx-hutuuu-tuutyouSyubetu">
		//	    総合口座
		//	</td>
		//	<td align="right" class="data-table1-td2 mcp-mtx-hutuuu-zandaka">
		//	      100,009円
		//	</td>
		public static final Pattern PAT = Pattern.compile(
			"<td .+?>\\s+総合口座\\s+</td>\\s+" +
			"<td .+?>\\s+(?<value>[0-9,]+)円\\s+</td>\\s+" +
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
			return String.format("{%s}", value.toPlainString());
		}
	}

	
	//
	// 定期預金
	//
	public static class TermDepositJPY {
		//  <td align="left" class="data-table1-td1 mcp-mtx-teiki-syouhin">定期預金</td>
		//	<td align="right" class="data-table1-td2 mcp-mtx-teiki-zandaka">
		//	    3,000,000円
		//  </td>
		public static final Pattern PAT = Pattern.compile(
			"<td .+?>定期預金</td>\\s+" +
			"<td .+?>\\s+(?<value>[0-9,]+)円\\s+</td>\\s+" +
			""
		);
		public static TermDepositJPY getInstance(String page) {
			return ScrapeUtil.get(TermDepositJPY.class, PAT, page);
		}
		
		public final BigDecimal value;
		
		public TermDepositJPY(BigDecimal value) {
			this.value = value;
		}
		
		@Override
		public String toString() {
			return String.format("{%s}", value.toPlainString());
		}
	}

}
