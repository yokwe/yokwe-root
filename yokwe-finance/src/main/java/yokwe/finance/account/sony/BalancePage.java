package yokwe.finance.account.sony;

import java.math.BigDecimal;
import java.util.List;
import java.util.regex.Pattern;

import yokwe.util.ScrapeUtil;

public class BalancePage {
	//
	// 円預金
	//
	public static class DepositJPY {
		public static final Pattern PAT = Pattern.compile(
			"<tr>\\s+" +
			"<td><p .+?><span .+?>(?<name>.+?)</span><a .+?>通帳</a></p></td>\\s+" +
			"<td><p>(?<value>[0-9,]+)円</p></td>\\s+" +
			"<td><p>.+?</p></td>\\s+" +
			"<td .+?><p .+?><a .+?><span .+?>振込</span></a></p></td>\\s+" +
			"</tr>" +
			""
		);
		public static List<DepositJPY> getInstance(String page) {
			return ScrapeUtil.getList(DepositJPY.class, PAT, page);
		}
		
		public final String     name;
		public final BigDecimal value;
		
		public DepositJPY(String name, BigDecimal value) {
			this.name  = name;
			this.value = value;
		}
		
		@Override
		public String toString() {
			return String.format("{%s  %s}", name, value.toPlainString());
		}
	}
	//
	// 外貨預金
	//
	public static class DepositForeign {
		public static final Pattern PAT = Pattern.compile(
			"<tr>\\s+" +
			"<td><p>(?<value>[0-9,\\.]+)(?<currency>...)</p></td>\\s+" +
			"<td><p>[0-9,]+円</p></td>\\s+" +
			"<td><p>[0-9,\\.]+%</p></td>\\s+" +
			"<td .+?><p>1...=[0-9\\.]+円</p></td>\\s+" +
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
	
	//	<tr>
	//	<td class="fundname"><p>特定口座<br><a href="javascript:fndNmLink('103030441')">ｅＭＡＸＩＳ国内物価連動国債インデックス</a></p></td>
	//	<td><p>8,031,728円</p></td>
	//	<td><p>8,057,512口</p></td>
	//	<td><p>9,968円</p></td>
	//	<td><p>31,424円</p></td>
	//	<td class="last"><div class="btnBox clearFix">
	//	<span class="btn03 whover"><a href="javascript:EnTKonyu('103030441')"><span class="swpImg">購入</span></a></span>
	//	<span class="btn15 whover"><a href="javascript:EnTKaiyaku('103030441','1')"><span class="swpImg">解約</span></a></span>
	//	<script type="text/javascript">writeLink('0', '103030441','1');</script>
	//	</div></td>
	//	</tr>
	
	//
	// 投資信託
	//
	public static class FundJPY {
		public static final Pattern PAT = Pattern.compile(
			"<tr>\\s+" +
			"<td .+?><p>.+?<br><a .+?fndNmLink.+?>(?<name>.+?)</a></p></td>\\s+" +
			"<td><p>(?<value>[0-9,]+)円</p></td>\\s+" +
			"<td><p>[0-9,]+口</p></td>\\s+" +
			"<td><p>[0-9,]+円</p></td>\\s+" +
			"<td><p>[0-9,]+円</p></td>\\s+" +
			"<td .+?><div .+?>.+?</div></td>\\s+" +
			"</tr>" +
			"",
			Pattern.DOTALL
		);
		public static List<FundJPY> getInstance(String page) {
			return ScrapeUtil.getList(FundJPY.class, PAT, page);
		}
		
		public final String     name;
		public final BigDecimal value;
		
		public FundJPY(String name, BigDecimal value) {
			this.name  = name;
			this.value = value;
		}
		
		@Override
		public String toString() {
			return String.format("{%s  %s}", name, value.toPlainString());
		}
	}


	
}
