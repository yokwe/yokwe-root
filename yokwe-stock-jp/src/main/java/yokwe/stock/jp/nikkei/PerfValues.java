package yokwe.stock.jp.nikkei;

import java.util.regex.Pattern;

import yokwe.util.ScrapeUtil;
import yokwe.util.StringUtil;

/*
//<!-- ▼ QP-RSRET：リスク・リターン ▼ -->
//<div class="m-articleFrame a-w100p">
//    <div class="m-headline">
//        <h2 class="m-headline_text">リスク・リターン　(2023年4月末時点)</h2>
//    </div>
//    <div class="m-tableType01 a-mb40">
//        <div class="m-tableType01_table">
//            <table class="w668">
//                <thead>
//                <tr>
//                    <th class="a-w40p"></th>
//                    <th class="a-taC a-w12p">6ヵ月</th>
//                    <th class="a-taC a-w12p">1年</th>
//                    <th class="a-taC a-w12p">3年</th>
//                    <th class="a-taC a-w12p">5年</th>
//                    <th class="a-taC a-w12p">10年</th>
//                </tr>
//                </thead>
//                <tbody>
//                <tr>
//                    <th>リターン(年率) <a href="//www.nikkei.com/help/contents/markets/fund/#qf15" target="_blank" class="m-iconQ">（解説）</a></th>
//                    <td class="a-taR a-nowrap">+2.49%</td>
//                    <td class="a-taR a-nowrap">+1.57%</td>
//                    <td class="a-taR a-nowrap">+4.46%</td>
//                    <td class="a-taR a-nowrap">+2.30%</td>
//                    <td class="a-taR a-nowrap">+3.27%</td>
//                </tr>
//                <tr class="bgcGray">
//                    <th>分類平均指数のリターン(年率) <a href="//www.nikkei.com/help/contents/markets/fund/#qf38" target="_blank" class="m-iconQ">（解説）</a></th>
//                    <td class="a-taR a-nowrap">+1.44%</td>
//                    <td class="a-taR a-nowrap">+1.92%</td>
//                    <td class="a-taR a-nowrap">+8.57%</td>
//                    <td class="a-taR a-nowrap">+4.18%</td>
//                    <td class="a-taR a-nowrap">+4.60%</td>
//                </tr>
//                <tr>
//                    <th>分配金累計 <a href="//www.nikkei.com/help/contents/markets/fund/#qf11" target="_blank" class="m-iconQ">（解説）</a></th>
//                    <td class="a-taR a-nowrap">0円</td>
//                    <td class="a-taR a-nowrap">50円</td>
//                    <td class="a-taR a-nowrap">450円</td>
//                    <td class="a-taR a-nowrap">750円</td>
//                    <td class="a-taR a-nowrap">2,200円</td>
//                </tr>
//                <tr class="bgcGray">
//                    <th>分配金受取ベースのリターン(年率) <a href="//www.nikkei.com/help/contents/markets/fund/#qf16" target="_blank" class="m-iconQ">（解説）</a></th>
//                    <td class="a-taR a-nowrap">+2.50%</td>
//                    <td class="a-taR a-nowrap">+1.57%</td>
//                    <td class="a-taR a-nowrap">+4.44%</td>
//                    <td class="a-taR a-nowrap">+2.24%</td>
//                    <td class="a-taR a-nowrap">+3.01%</td>
//                </tr>
//                <tr>
//                    <th>リスク(年率) <a href="//www.nikkei.com/help/contents/markets/fund/#qf17" target="_blank" class="m-iconQ">（解説）</a></th>
//                    <td class="a-taR a-nowrap">5.46%</td>
//                    <td class="a-taR a-nowrap">6.48%</td>
//                    <td class="a-taR a-nowrap">5.31%</td>
//                    <td class="a-taR a-nowrap">5.55%</td>
//                    <td class="a-taR a-nowrap">5.14%</td>
//                </tr>
//                <tr class="bgcGray">
//                    <th>分類平均指数のリスク(年率) <a href="//www.nikkei.com/help/contents/markets/fund/#qf39" target="_blank" class="m-iconQ">（解説）</a></th>
//                    <td class="a-taR a-nowrap">8.00%</td>
//                    <td class="a-taR a-nowrap">9.67%</td>
//                    <td class="a-taR a-nowrap">7.51%</td>
//                    <td class="a-taR a-nowrap">8.50%</td>
//                    <td class="a-taR a-nowrap">8.06%</td>
//                </tr>
//                <tr>
//                    <th>シャープレシオ(年率) <a href="//www.nikkei.com/help/contents/markets/fund/#qf18" target="_blank" class="m-iconQ">（解説）</a></th>
//                    <td class="a-taR a-nowrap">0.60</td>
//                    <td class="a-taR a-nowrap">0.28</td>
//                    <td class="a-taR a-nowrap">0.85</td>
//                    <td class="a-taR a-nowrap">0.44</td>
//                    <td class="a-taR a-nowrap">0.64</td>
//                </tr>
//                <tr class="bgcGray">
//                    <th>分類平均指数のシャープレシオ(年率) <a href="//www.nikkei.com/help/contents/markets/fund/#qf40" target="_blank" class="m-iconQ">（解説）</a></th>
//                    <td class="a-taR a-nowrap">0.28</td>
//                    <td class="a-taR a-nowrap">0.25</td>
//                    <td class="a-taR a-nowrap">1.14</td>
//                    <td class="a-taR a-nowrap">0.53</td>
//                    <td class="a-taR a-nowrap">0.59</td>
//                </tr>
//                </tbody>
//            </table>
//        </div>
//    </div>
//</div>
//<!-- ▲ QP-RSRET：リスク・リターン ▲ -->
*/

public class PerfValues {
	public static final String HEADER = "<!-- ▼ QP-RSRET：リスク・リターン ▼ -->";
	public static final Pattern PAT = Pattern.compile(
		HEADER + "\\s+" +
		"<div .+?>\\s+" +
		"<div .+?>\\s+" +
		"<h2 .+>リスク・リターン　\\((?<asOf>.+?)\\)</h2>\\s+" +
		"</div>\\s+" +
		"<div .+?>\\s+" +
		"<div .+?>\\s+" +
		"<table .+?>\\s+" +
		"<thead>\\s+" +
		"<tr>\\s+" +
		"<th .+?></th>\\s+" +
		"<th .+?>6ヵ月</th>\\s+" +
		"<th .+?>1年</th>\\s+" +
		"<th .+?>3年</th>\\s+" +
		"<th .+?>5年</th>\\s+" +
		"<th .+?>10年</th>\\s+" +
		"</tr>\\s+" +
		"</thead>\\s+" +
		"<tbody>\\s+" +
		"<tr.*?>\\s+" +
		"<th>リターン\\(年率\\).+?</th>\\s+" +
		"<td .+?>(?<reinvestRetrun6m>.+?)</td>\\s+" +
		"<td .+?>(?<reinvestRetrun1y>.+?)</td>\\s+" +
		"<td .+?>(?<reinvestRetrun3y>.+?)</td>\\s+" +
		"<td .+?>(?<reinvestRetrun5y>.+?)</td>\\s+" +
		"<td .+?>(?<reinvestRetrun10y>.+?)</td>\\s+" +
		"</tr>\\s+" +
		"<tr.*?>\\s+" +
		"<th>分類平均指数のリターン\\(年率\\).+?</th>\\s+" +
		"<td .+?>.+?</td>\\s+" +
		"<td .+?>.+?</td>\\s+" +
		"<td .+?>.+?</td>\\s+" +
		"<td .+?>.+?</td>\\s+" +
		"<td .+?>.+?</td>\\s+" +
		"</tr>\\s+" +
		"<tr.*?>\\s+" +
		"<th>分配金累計.+?</th>\\s+" +
		"<td .+?>(?<div6m>.+?)</td>\\s+" +
		"<td .+?>(?<div1y>.+?)</td>\\s+" +
		"<td .+?>(?<div3y>.+?)</td>\\s+" +
		"<td .+?>(?<div5y>.+?)</td>\\s+" +
		"<td .+?>(?<div10y>.+?)</td>\\s+" +
		"</tr>\\s+" +
		"<tr.*?>\\s+" +
		"<th>分配金受取ベースのリターン\\(年率\\).+?</th>\\s+" +
		"<td .+?>(?<return6m>.+?)</td>\\s+" +
		"<td .+?>(?<return1y>.+?)</td>\\s+" +
		"<td .+?>(?<return3y>.+?)</td>\\s+" +
		"<td .+?>(?<return5y>.+?)</td>\\s+" +
		"<td .+?>(?<return10y>.+?)</td>\\s+" +
		"</tr>\\s+" +
		"<tr.*?>\\s+" +
		"<th>リスク\\(年率\\).+?</th>\\s+" +
		"<td .+?>(?<risk6m>.+?)</td>\\s+" +
		"<td .+?>(?<risk1y>.+?)</td>\\s+" +
		"<td .+?>(?<risk3y>.+?)</td>\\s+" +
		"<td .+?>(?<risk5y>.+?)</td>\\s+" +
		"<td .+?>(?<risk10y>.+?)</td>\\s+" +
		"</tr>\\s+" +
		"<tr.*?>\\s+" +
		"<th>分類平均指数のリスク\\(年率\\).+?</th>\\s+" +
		"<td .+?>.+?</td>\\s+" +
		"<td .+?>.+?</td>\\s+" +
		"<td .+?>.+?</td>\\s+" +
		"<td .+?>.+?</td>\\s+" +
		"<td .+?>.+?</td>\\s+" +
		"</tr>\\s+" +
		"<tr.*?>\\s+" +
		"<th>シャープレシオ\\(年率\\).+?</th>\\s+" +
		"<td .+?>(?<sharpRatio6m>.+?)</td>\\s+" +
		"<td .+?>(?<sharpRatio1y>.+?)</td>\\s+" +
		"<td .+?>(?<sharpRatio3y>.+?)</td>\\s+" +
		"<td .+?>(?<sharpRatio5y>.+?)</td>\\s+" +
		"<td .+?>(?<sharpRatio10y>.+?)</td>\\s+" +
		"</tr>\\s+" +
		"<tr.*?>\\s+" +
		"<th>分類平均指数のシャープレシオ\\(年率\\).+?</th>\\s+" +
		"<td .+?>.+?</td>\\s+" +
		"<td .+?>.+?</td>\\s+" +
		"<td .+?>.+?</td>\\s+" +
		"<td .+?>.+?</td>\\s+" +
		"<td .+?>.+?</td>\\s+" +
		"</tr>\\s+" +
		"</tbody>\\s+" +
		"</table>\\s+" +
		"</div>\\s+" +
		"</div>\\s+" +
		"</div>\\s+" +
		""
	);
	public static PerfValues getInstance(String page) {
		return ScrapeUtil.get(PerfValues.class, PAT, page);
	}
	
	public String asOf;
	public String reinvestRetrun6m;
	public String reinvestRetrun1y;
	public String reinvestRetrun3y;
	public String reinvestRetrun5y;
	public String reinvestRetrun10y;
	public String div6m;
	public String div1y;
	public String div3y;
	public String div5y;
	public String div10y;
	public String return6m;
	public String return1y;
	public String return3y;
	public String return5y;
	public String return10y;
	public String risk6m;
	public String risk1y;
	public String risk3y;
	public String risk5y;
	public String risk10y;
	public String sharpRatio6m;
	public String sharpRatio1y;
	public String sharpRatio3y;
	public String sharpRatio5y;
	public String sharpRatio10y;

	public PerfValues(
			String asOf,
			String reinvestRetrun6m,
			String reinvestRetrun1y,
			String reinvestRetrun3y,
			String reinvestRetrun5y,
			String reinvestRetrun10y,
			String div6m,
			String div1y,
			String div3y,
			String div5y,
			String div10y,
			String return6m,
			String return1y,
			String return3y,
			String return5y,
			String return10y,
			String risk6m,
			String risk1y,
			String risk3y,
			String risk5y,
			String risk10y,
			String sharpRatio6m,
			String sharpRatio1y,
			String sharpRatio3y,
			String sharpRatio5y,
			String sharpRatio10y
		) {
		this.asOf = asOf;
		this.reinvestRetrun6m = reinvestRetrun6m;
		this.reinvestRetrun1y = reinvestRetrun1y;
		this.reinvestRetrun3y = reinvestRetrun3y;
		this.reinvestRetrun5y = reinvestRetrun5y;
		this.reinvestRetrun10y = reinvestRetrun10y;
		this.div6m = div6m;
		this.div1y = div1y;
		this.div3y = div3y;
		this.div5y = div5y;
		this.div10y = div10y;
		this.return6m = return6m;
		this.return1y = return1y;
		this.return3y = return3y;
		this.return5y = return5y;
		this.return10y = return10y;
		this.risk6m = risk6m;
		this.risk1y = risk1y;
		this.risk3y = risk3y;
		this.risk5y = risk5y;
		this.risk10y = risk10y;
		this.sharpRatio6m = sharpRatio6m;
		this.sharpRatio1y = sharpRatio1y;
		this.sharpRatio3y = sharpRatio3y;
		this.sharpRatio5y = sharpRatio5y;
		this.sharpRatio10y = sharpRatio10y;
	}
	@Override
	public String toString() {
		return StringUtil.toString(this);
	}
}