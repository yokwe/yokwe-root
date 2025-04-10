package yokwe.stock.jp.nikkei;

import java.util.regex.Pattern;

import yokwe.util.ScrapeUtil;
import yokwe.util.ToString;

/*
//<!-- ▼ QP-YIELD：分配金利回り ▼ -->
//<div class="m-articleFrame a-w100p">
//    <div class="m-headline">
//        <h2 class="m-headline_text">分配金利回り<a href="//www.nikkei.com/help/contents/markets/fund/#qf12" target="_blank" class="m-iconQ">（解説）</a></h2>
//    </div>
//    <div class="m-tableType01 a-mb40">
//        <div class="m-tableType01_table">
//            <table class="w668 rsp_table">
//                <tbody>
//                <tr>
//                    <th>直近決算日</th>
//                    <td class="a-taR">2023年1月24日</td>
//                </tr>
//                <tr class="bgcGray">
//                    <th>分配金</th>
//                    <td class="a-taR">160円</td>
//                </tr>
//                <tr>
//                    <th>分配金利回り(1年)</th>
//                    <td class="a-taR">2.55%</td>
//                </tr>
//                <tr class="bgcGray">
//                    <th>決算日の基準価格</th>
//                    <td class="a-taR">13,138円</td>
//                </tr>
//                </tbody>
//            </table>
//        </div>
//    </div>
//</div>
//<!-- ▲ QP-YIELD：分配金利回り ▲ -->
*/

public class DivLast {
	public static final String HEADER = "<!-- ▼ QP-YIELD：分配金利回り ▼ -->";
	public static final Pattern PAT = Pattern.compile(
		HEADER + "\\s+" +
		"<div .+?>\\s+" +
		"<div .+?>\\s+" +
		"<h2 .+?>分配金利回り.+?</h2>\\s+" +
		"</div>\\s+" +
		"<div .+?>\\s+" +
		"<div .+?>\\s+" +
		"<table .+?>\\s+" +
		"<tbody>\\s+" +
		"<tr.*?>\\s+" +
		"<th>直近決算日</th>\\s+" +
		"<td .+?>(?<date>.+?)</td>\\s+" +
		"</tr>\\s+" +
		"<tr.*?>\\s+" +
		"<th>分配金</th>\\s+" +
		"<td .+?>(?<amount>.+?)</td>\\s+" +
		"</tr>\\s+" +
		"<tr.*?>\\s+" +
		"<th>分配金利回り\\(1年\\)</th>\\s+" +
		"<td .+?>(?<rate>.+?)</td>\\s+" +
		"</tr>\\s+" +
		"<tr.*?>\\s+" +
		"<th>決算日の基準価格</th>\\s+" +
		"<td .+?>(?<price>.+?)</td>\\s+" +
		"</tr>\\s+" +
		"</tbody>\\s+" +
		"</table>\\s+" +
		"</div>\\s+" +
		"</div>\\s+" +
		"</div>\\s+" +

		""
	);
	public static DivLast getInstance(String page) {
		return ScrapeUtil.get(DivLast.class, PAT, page);
	}

	public String date;
	public String amount;
	public String rate;
	public String price;
	
	public DivLast(
		String date,
		String amount,
		String rate,
		String price
	) {
		this.date = date;
		this.amount = amount;
		this.rate = rate;
		this.price = price;
	}
	@Override
	public String toString() {
		return ToString.withFieldName(this);
	}
}