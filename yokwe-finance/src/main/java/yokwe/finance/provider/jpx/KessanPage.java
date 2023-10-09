package yokwe.finance.provider.jpx;

import java.util.List;
import java.util.regex.Pattern;

import yokwe.util.ScrapeUtil;
import yokwe.util.StringUtil;

public class KessanPage {
	public static final String NO_INFORMATION = "該当するデータはありません";
	
	public static final String TERM_1 = "第１";
	public static final String TERM_2 = "第２";
	public static final String TERM_3 = "第３";
	public static final String TERM_4 = "通期";
	
	public static final String CONSOLIDATED     = "連";
	public static final String NON_CONSOLIDATED = "単";
	
	public static final String SECTION_KESSAN = "決算データ";
	
	
	public static class Section {
		public static Section getInstance(String sectioName, String page) {
			 Pattern pat = Pattern.compile(
					"<table .+?>\\s+" +
					"<tbody><tr>\\s+" +
					"<td .+?>" + sectioName + "</td>\\s+" +
					"</tr></tbody>\\s+" +
					"</table>\\s+" +
					"<table .+?>(?<string>.+?)</table>",
					Pattern.DOTALL
				);
			return ScrapeUtil.get(Section.class, pat, page);
		}

		public final String string;
		
		public Section(String string) {
			this.string = string;
		}
		
		@Override
		public String toString() {
			return StringUtil.toString(this);
		}
	}
	
	
	public static class Kessan_A {
		public static final Pattern PAT = Pattern.compile(
			"<tr .+>\\s+" +
			"<td .+?>(?<term>.*?)</td>\\s+" +
			"<td .+?>(?<termName>.*?)</td>\\s+" +
			"<td .+?>(?<termEnd>.*?)</td>\\s+" +
			"<td .+?>(?<consolidateFlag>.*?)</td>\\s+" +
			"<td .+?>(?<earning>.*?)</td>\\s+" + 
			"<!--.+?-->\\s+" +
			"<td .+?>(?<operatingIncome>.*?)</td>\\s+" +
			"<td .+?>(?<ordinaryProfit>.*?)</td>\\s+" +
			"<td .+?>(?<netIncome>.*?)</td>\\s+" +
			"<td .+?>(?<dividendTerm>.*?)</td>\\s+" +
			"<td .+?>(?<dividendYear>.*?)</td>\\s+" +
			"</tr>" +
			""
		);
		public static List<Kessan_A> getList(String page) {
			return ScrapeUtil.getList(Kessan_A.class, PAT, page);
		}

		public final String term;            // 決算期//
		public final String termName;        ////
		public final String termEnd;         // 期末月//
		public final String consolidateFlag; // 連単種別//
		//
		public final String earning;         // 売上高//
		public final String operatingIncome; // 営業利益//
		//
		public final String ordinaryProfit;  // 経常利益//
		public final String netIncome;       // 当期純利益//
		public final String dividendTerm;    // 四半期末配当金//
		public final String dividendYear;    // １株当たり配当金
		
		public Kessan_A(
				String term,
				String termName,
				String termEnd,
				String consolidateFlag,
				String earning,
				String operatingIncome,
				String ordinaryProfit,
				String netIncome,
				String dividendTerm,
				String dividendYear
				) {
			this.term            = term;
			this.termName        = termName;
			this.termEnd         = termEnd;
			this.consolidateFlag = consolidateFlag;
			this.earning         = earning.replace(",", "");
			this.operatingIncome = operatingIncome.replace(",", "");
			this.ordinaryProfit  = ordinaryProfit.replace(",", "");
			this.netIncome       = netIncome.replace(",", "");
			this.dividendTerm    = dividendTerm.replace(",", "");
			this.dividendYear    = dividendYear.replace(",", "");
		}
		
		@Override
		public String toString() {
			return StringUtil.toString(this);
		}
	}
	
	
	public static class Kessan_B {
		public static final Pattern PAT = Pattern.compile(
			"<tr .+>\\s+" +
			"<td .+?>(?<term>.*?)</td>\\s+" +
			"<td .+?>(?<termName>.*?)</td>\\s+" +
			"<td .+?>(?<termEnd>.*?)</td>\\s+" +
			"<td .+?>(?<consolidateFlag>.*?)</td>\\s+" +
			"<td .+?>(?<ordinaryRevenue>.*?)</td>\\s+" +
			"<td .+?>(?<ordinaryProfit>.*?)</td>\\s+" +
			"<td .+?>(?<netIncome>.*?)</td>\\s+" +
			"<td .+?>(?<dividendTerm>.*?)</td>\\s+" +
			"<td .+?>(?<dividendYear>.*?)</td>\\s+" +
			"</tr>" +
			""
		);
		public static List<Kessan_B> getList(String page) {
			return ScrapeUtil.getList(Kessan_B.class, PAT, page);
		}

		public final String term;            // 決算期
		public final String termName;        //
		public final String termEnd;         // 期末月
		public final String consolidateFlag; // 連単種別
		//
		public final String ordinaryRevenue; // 経常収益
		//
		public final String ordinaryProfit;  // 経常利益
		public final String netIncome;       // 当期純利益
		public final String dividendTerm;    // 四半期末配当金
		public final String dividendYear;    // １株当たり配当金
		
		public Kessan_B(
				String term,
				String termName,
				String termEnd,
				String consolidateFlag,
				String ordinaryRevenue,
				String ordinaryProfit,
				String netIncome,
				String dividendTerm,
				String dividendYear
				) {
			this.term            = term;
			this.termName        = termName;
			this.termEnd         = termEnd;
			this.consolidateFlag = consolidateFlag;
			this.ordinaryRevenue = ordinaryRevenue.replace(",", "");
			this.ordinaryProfit  = ordinaryProfit.replace(",", "");
			this.netIncome       = netIncome.replace(",", "");
			this.dividendTerm    = dividendTerm.replace(",", "");
			this.dividendYear    = dividendYear.replace(",", "");
		}
		
		@Override
		public String toString() {
			return StringUtil.toString(this);
		}
	}

}
