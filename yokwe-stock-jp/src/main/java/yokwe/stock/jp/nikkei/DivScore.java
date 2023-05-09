package yokwe.stock.jp.nikkei;

import java.util.regex.Pattern;

import yokwe.util.ScrapeUtil;
import yokwe.util.StringUtil;

/*
//<!-- ▼ QP-BUNPAISD：分配金健全度 ▼ -->
//<div class="m-articleFrame a-w100p">
//    <div class="m-headline">
//        <h2 class="m-headline_text">分配金健全度<a href="//www.nikkei.com/help/contents/markets/fund/#qf13" target="_blank" class="m-iconQ">（解説）</a></h2>
//    </div>
//    <div class="m-tableType01 a-mb40">
//        <div class="m-tableType01_table">
//            <table class="w668">
//                <thead>
//                <tr>
//                    <th class="a-taC a-w25p">1年</th>
//                    <th class="a-taC a-w25p">3年</th>
//                    <th class="a-taC a-w25p">5年</th>
//                    <th class="a-taC a-w25p">10年</th>
//                </tr>
//                </thead>
//                <tbody>
//                <tr>
//                    <td class="a-taR">0.00%</td>
//                    <td class="a-taR">100.00%</td>
//                    <td class="a-taR">100.00%</td>
//                    <td class="a-taR">100.00%</td>
//                </tr>
//                </tbody>
//            </table>
//        </div>
//    </div>
//</div>
//<!-- ▲ QP-BUNPAISD：分配金健全度 ▲ -->
*/

public class DivScore {
	public static final String HEADER = "<!-- ▼ QP-BUNPAISD：分配金健全度 ▼ -->";
	public static final Pattern PAT = Pattern.compile(
		HEADER + "\\s+" +
		"<div .+?>\\s+" +
		"<div .+?>\\s+" +
		"<h2 .+?>分配金健全度.+?</h2>\\s+" +
		"</div>\\s+" +
		"<div .+?>\\s+" +
		"<div .+?>\\s+" +
		"<table .+?>\\s+" +
		"<thead>\\s+" +
		"<tr>\\s+" +
		"<th .+?>1年</th>\\s+" +
		"<th .+?>3年</th>\\s+" +
		"<th .+?>5年</th>\\s+" +
		"<th .+?>10年</th>\\s+" +
		"</tr>\\s+" +
		"</thead>\\s+" +
		"<tbody>\\s+" +
		"<tr>\\s+" +
		"<td .+?>(?<score1Y>.+?)</td>\\s+" +
		"<td .+?>(?<score3Y>.+?)</td>\\s+" +
		"<td .+?>(?<score5Y>.+?)</td>\\s+" +
		"<td .+?>(?<score10Y>.+?)</td>\\s+" +
		"</tr>\\s+" +
		"</tbody>\\s+" +
		"</table>\\s+" +
		"</div>\\s+" +
		"</div>\\s+" +
		"</div>\\s+" +

		""
	);
	public static DivScore getInstance(String page) {
		return ScrapeUtil.get(DivScore.class, PAT, page);
	}

	public String score1Y;
	public String score3Y;
	public String score5Y;
	public String score10Y;

	public DivScore(
		String score1Y,
		String score3Y,
		String score5Y,
		String score10Y
	) {
		this.score1Y = score1Y;
		this.score3Y = score3Y;
		this.score5Y = score5Y;
		this.score10Y = score10Y;
	}
	@Override
	public String toString() {
		return StringUtil.toString(this);
	}
}