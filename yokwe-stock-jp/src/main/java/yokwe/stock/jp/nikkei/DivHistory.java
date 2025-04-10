package yokwe.stock.jp.nikkei;

import java.util.regex.Pattern;

import yokwe.util.ScrapeUtil;
import yokwe.util.ToString;

/*
//<!-- ▼ QP-BUNPAI：分配金実績 ▼ -->
//<div class="m-articleFrame a-w100p">
//    <div class="m-headline">
//        <h2 class="m-headline_text">分配金実績<a href="//www.nikkei.com/help/contents/markets/fund/#qf10" target="_blank" class="m-iconQ">（解説）</a> (課税前)　直近6期分</h2>
//    </div>
//    <div class="m-tableType01 a-mb40">
//        <div class="m-tableType01_table">
//            <table class="w668">
//                <thead>
//                <tr>
//                    <th class="a-taC a-w25p">決算日</th>
//                    <th class="a-taC a-w25p">分配金(税引前)</th>
//                    <th class="a-taC a-w25p">基準価格</th>
//                    <th class="a-taC a-w25p">純資産総額</th>
//                </tr>
//                </thead>
//                <tbody>
//                <tr>
//                    <th class="a-taC">2023/1/24</th>
//                    <td class="a-taR">160円</td>
//                    <td class="a-taR">13,138円</td>
//                    <td class="a-taR">42.09億円</td>
//                </tr>
//                
//                <tr class="bgcGray">
//                    <th class="a-taC">2022/7/25</th>
//                    <td class="a-taR">180円</td>
//                    <td class="a-taR">14,119円</td>
//                    <td class="a-taR">46.00億円</td>
//                </tr>
//                
//                <tr>
//                    <th class="a-taC">2022/1/24</th>
//                    <td class="a-taR">340円</td>
//                    <td class="a-taR">15,688円</td>
//                    <td class="a-taR">49.84億円</td>
//                </tr>
//                
//                <tr class="bgcGray">
//                    <th class="a-taC">2021/7/26</th>
//                    <td class="a-taR">310円</td>
//                    <td class="a-taR">15,944円</td>
//                    <td class="a-taR">49.74億円</td>
//                </tr>
//                
//                <tr>
//                    <th class="a-taC">2021/1/25</th>
//                    <td class="a-taR">210円</td>
//                    <td class="a-taR">14,264円</td>
//                    <td class="a-taR">43.90億円</td>
//                </tr>
//                
//                <tr class="bgcGray">
//                    <th class="a-taC">2020/7/27</th>
//                    <td class="a-taR">90円</td>
//                    <td class="a-taR">11,717円</td>
//                    <td class="a-taR">37.48億円</td>
//                </tr>
//                </tbody>
//            </table>
//        </div>
//    </div>
//</div>
//<!-- ▲ QP-BUNPAI：分配金実績 ▲ -->
*/

public class DivHistory {
	public static final String HEADER = "<!-- ▼ QP-BUNPAI：分配金実績 ▼ -->";
	public static final Pattern PAT = Pattern.compile(
		HEADER + "\\s+" +
		"<div .+?>\\s+" +
		"<div .+?>\\s+" +
		"<h2 .+?>分配金実績.+?</h2>\\s+" +
		"</div>\\s+" +
		"<div .+?>\\s+" +
		"<div .+?>\\s+" +
		"<table .+?>\\s+" +
		"<thead>\\s+" +
		"<tr>\\s+" +
		"<th .+?>決算日</th>\\s+" +
		"<th .+?>分配金\\(税引前\\)</th>\\s+" +
		"<th .+?>基準価格</th>\\s+" +
		"<th .+?>純資産総額</th>\\s+" +
		"</tr>\\s+" +
		"</thead>\\s+" +
		"<tbody>\\s+" +
		
		"<tr.*?>\\s+" +
		"<th .+?>(?<date1>.+?)</th>\\s+" +
		"<td .+?>(?<amount1>.+?)</td>\\s+" +
		"<td .+?>(?<price1>.+?)</td>\\s+" +
		"<td .+?>(?<uam1>.+?)</td>\\s+" +
		"</tr>\\s+" +
		
		"<tr.*?>\\s+" +
		"<th .+?>(?<date2>.+?)</th>\\s+" +
		"<td .+?>(?<amount2>.+?)</td>\\s+" +
		"<td .+?>(?<price2>.+?)</td>\\s+" +
		"<td .+?>(?<uam2>.+?)</td>\\s+" +
		"</tr>\\s+" +
		
		"<tr.*?>\\s+" +
		"<th .+?>(?<date3>.+?)</th>\\s+" +
		"<td .+?>(?<amount3>.+?)</td>\\s+" +
		"<td .+?>(?<price3>.+?)</td>\\s+" +
		"<td .+?>(?<uam3>.+?)</td>\\s+" +
		"</tr>\\s+" +
		
		"<tr.*?>\\s+" +
		"<th .+?>(?<date4>.+?)</th>\\s+" +
		"<td .+?>(?<amount4>.+?)</td>\\s+" +
		"<td .+?>(?<price4>.+?)</td>\\s+" +
		"<td .+?>(?<uam4>.+?)</td>\\s+" +
		"</tr>\\s+" +
		
		"<tr.*?>\\s+" +
		"<th .+?>(?<date5>.+?)</th>\\s+" +
		"<td .+?>(?<amount5>.+?)</td>\\s+" +
		"<td .+?>(?<price5>.+?)</td>\\s+" +
		"<td .+?>(?<uam5>.+?)</td>\\s+" +
		"</tr>\\s+" +
		
		"<tr.*?>\\s+" +
		"<th .+?>(?<date6>.+?)</th>\\s+" +
		"<td .+?>(?<amount6>.+?)</td>\\s+" +
		"<td .+?>(?<price6>.+?)</td>\\s+" +
		"<td .+?>(?<uam6>.+?)</td>\\s+" +
		"</tr>\\s+" +
		
		"</tbody>\\s+" +
		"</table>\\s+" +
		"</div>\\s+" +
		"</div>\\s+" +
		"</div>\\s+" +

		""
	);
	public static DivHistory getInstance(String page) {
		return ScrapeUtil.get(DivHistory.class, PAT, page);
	}
	
	public String date1;
	public String amount1;
	public String price1;
	public String uam1;
	
	public String date2;
	public String amount2;
	public String price2;
	public String uam2;
	
	public String date3;
	public String amount3;
	public String price3;
	public String uam3;
	
	public String date4;
	public String amount4;
	public String price4;
	public String uam4;
	
	public String date5;
	public String amount5;
	public String price5;
	public String uam5;
	
	public String date6;
	public String amount6;
	public String price6;
	public String uam6;

	public DivHistory(
			String date1,
			String amount1,
			String price1,
			String uam1,
			
			String date2,
			String amount2,
			String price2,
			String uam2,
			
			String date3,
			String amount3,
			String price3,
			String uam3,
			
			String date4,
			String amount4,
			String price4,
			String uam4,
			
			String date5,
			String amount5,
			String price5,
			String uam5,
			
			String date6,
			String amount6,
			String price6,
			String uam6
		) {
			this.date1 = date1;
			this.amount1 = amount1;
			this.price1 = price1;
			this.uam1 = uam1;
			
			this.date2 = date2;
			this.amount2 = amount2;
			this.price2 = price2;
			this.uam2 = uam2;
			
			this.date3 = date3;
			this.amount3 = amount3;
			this.price3 = price3;
			this.uam3 = uam3;
			
			this.date4 = date4;
			this.amount4 = amount4;
			this.price4 = price4;
			this.uam4 = uam4;
			
			this.date5 = date5;
			this.amount5 = amount5;
			this.price5 = price5;
			this.uam5 = uam5;
			
			this.date6 = date6;
			this.amount6 = amount6;
			this.price6 = price6;
			this.uam6 = uam6;
		}
		@Override
		public String toString() {
			return ToString.withFieldName(this);
		}
}