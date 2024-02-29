package yokwe.finance.provider.nikko;

import java.util.List;
import java.util.regex.Pattern;

import yokwe.util.ScrapeUtil;

public class FundPage {
	public static class FundInfo {
		public static final Pattern PAT = Pattern.compile(
			"<tr>\\s+" +
			"<td class=\"left\"><p><a href=\"qsearch.exe\\?F=detail_.+?&KEY1=(?<fundCode>.+?)\">(?<fundName>.+?)</a></p></td>\\s+" +
			"<td .+?>.+?</td>\\s+" +
			"<td .+?>.+?</td>\\s+" +
			"<td .+?>.+?</td>\\s+" +
			"<td .+?>.+?</td>\\s+" +
			"<td .+?>.+?</td>\\s+" +
			"<td .+?>\\s+(?<td7>.+?)\\s+</td>\\s+" +
			"<td .+?>\\s+(?<td8>.+?)\\s+</td>\\s+" +
			"<td .+?>.+?</td>\\s+" +
			"</tr>" +
			"", Pattern.DOTALL
		);
		public static List<FundInfo> getInstance(String page) {
			return ScrapeUtil.getList(FundInfo.class, PAT, page.replaceAll("<!--[\\s\\S]*?-->", ""));
		}
		
		public final String fundCode;
		public final String fundName;
		public final String td7;
		public final String td8;
		
		@ScrapeUtil.Ignore
		public final boolean prospectus;
		@ScrapeUtil.Ignore
		public final boolean direct;
		@ScrapeUtil.Ignore
		public final boolean sougou;
		
		public FundInfo(String fundCode, String fundName, String td7, String td8) {
			this.fundCode = fundCode;
			this.fundName = fundName;
			this.td7      = td7;
			this.td8      = td8;
			
			this.prospectus = td7.contains("目論見書");
			this.direct     = td8.contains("ダイレクト");
			this.sougou     = td8.contains("総合");
		}
		
		@Override
		public String toString() {
			return String.format("{%s  %s  %s  %s}", fundCode, prospectus, direct, fundName);
		}
	}
}
