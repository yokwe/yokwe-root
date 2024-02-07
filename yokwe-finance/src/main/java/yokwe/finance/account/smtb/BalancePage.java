package yokwe.finance.account.smtb;

import java.math.BigDecimal;
import java.util.List;
import java.util.regex.Pattern;

import yokwe.util.ScrapeUtil;
import yokwe.util.StringUtil;

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
	
	
	//
	// 投資信託
	//
	public static class Fund {
		//<td colspan="5" align="left" class="data-table1-td2 mc-btx-tfund">\\s+
		//<a href="https://www.smtb.jp/personal/saving/investment/fund/6431117B" ... </tr>
		//<tr align="right" valign="top">
		//<td class="data-table1-td2 mc-btx-tzandaka">
		// 467,837口</td>
		//<td class="data-table1-td2 mc-btx-tkobetsu">
		// 17,954.97円</td>
		//<td class="data-table1-td2 mc-btx-tjika">
		// 835,510円</td>
		//<td class="data-table1-td2 mc-btx-tsyutokukingaku">
		//  840,002円
		// </td>
		public static final Pattern PAT = Pattern.compile(
			"<td colspan=\"5\" align=\"left\" class=\"data-table1-td2 mc-btx-tfund\">\\s+" +
			"<a href=\"https://www.smtb.jp/personal/saving/investment/fund/(?<fundCode>.+?)\".+?</tr>\\s+" +
			"<tr align=\"right\" valign=\"top.+?>\\s+" +
			"<td class=\"data-table1-td2 mc-btx-tzandaka\">\\s+(?<units>[0-9,\\.]+)口</td>\\s+" +
			"<td class=\"data-table1-td2 mc-btx-tkobetsu\">\\s+(?<unitPrice>[0-9,\\.]+)円</td>\\s+" +
			"<td class=\"data-table1-td2 mc-btx-tjika\">\\s+(?<value>[0-9,]+)円</td>\\s+" +
			"<td class=\"data-table1-td2 mc-btx-tsyutokukingaku\">\\s+(?<cost>[0-9,]+)円" +
			"",
			Pattern.DOTALL
		);
		public static List<Fund> getInstance(String page) {
			return ScrapeUtil.getList(Fund.class, PAT, page);
		}
		
		public final String     fundCode;
		public final BigDecimal units;
		public final BigDecimal unitPrice;
		public final BigDecimal value;
		public final BigDecimal cost;
		
		public Fund(
			String     fundCode,
			BigDecimal units,
			BigDecimal unitPrice,
			BigDecimal value,
			BigDecimal cost
			) {
			this.fundCode  = fundCode;
			this.units     = units;
			this.unitPrice = unitPrice;
			this.value     = value;
			this.cost      = cost;
		}
		
		@Override
		public String toString() {
//			return String.format("{%s  %s  %s  %s  %s}", fundCode, units.toPlainString(), unitPrice.toPlainString(), value.toPlainString(), cost.toPlainString());
			return StringUtil.toString(this);
		}
	}

}
