package yokwe.finance.provider.nikko;

import java.util.List;
import java.util.regex.Pattern;

import yokwe.util.ScrapeUtil;
import yokwe.util.StringUtil;

public class FundPage {
	//<tr>
	//<td class="left"><p><a href="qsearch.exe?F=detail_kokunai1&KEY1=0331418A"><!--<a href="qsearch.exe?F=detail_tani&KEY1=0331418A">-->ｅＭＡＸＩＳ Ｓｌｉｍ 全世界株式（オール・カントリー）</a></p></td>
	//<td class="left rwdTableCellPC"><p>三菱UFJアセット</p></td>
	//<td class="cell-MR"><p>23,206</p>
	//	<p><span class="text-style-red">+17</span></p>
	//	<p>(02/28)</p></td>
	//<td class="cell-MR rwdTableCellPC"><p>26,241.44</p></td>
	//<td class="cell-MR rwdTableCellPC"><p><span class="text-style-red">+10.28</span><br>
	//	<span class="text-style-red">+32.14</span></p></td>
	//<td class="cell-MR rwdTableCellPC"><p>0.00</p>
	//	<p>0.00</p></td>
	//<td class="cell-MC rwdTableCellPC">
	//	<p>
	//	<a href="https://www.smbcnikko.co.jp/doc-pdf/8782_001.pdf" target="_blank"><img src="https://www.smbcnikko.co.jp/smbc_nikko_fund/img/list_btn_001.png" alt="目論見書" class="rollover"></a>
	//	</p>
	//	<p>
	//		<script type="text/javascript">
	//		<!--
	//		if(0){
	//			document.write('<a href="https://www.smbcnikko.co.jp/inv_pdf/8782mr.pdf" target="_blank"><img src="https://www.smbcnikko.co.jp/smbc_nikko_fund/img/list_btn_002.png" alt="レポート" class="rollover"></a>')
	//		}
	//		else if(1 && 1 && 1 && 1){
	//			document.write('<a href="https://qweb7.qhit.net/var/report.asp?com=NIKKO&rptype=M&FC=0331418A" target="_blank"><img src="https://www.smbcnikko.co.jp/smbc_nikko_fund/img/list_btn_002.png" alt="レポート" class="rollover"></a>')
	//		}
	//		else{
	//			document.write('--');
	//		}
	//		-->
	//		</script>
	//	</p>
	//</td>
	//<td class="cell-MC">
	//	<p></p>
	//	<p><img src="https://www.smbcnikko.co.jp/smbc_nikko_fund/img/myfund_ico_002.png" alt="ダイレクト"></p>
	//</td>
	//<td class="cell-MC rwdTableCellPC">
	//	<p><a href="https://trade.smbcnikko.co.jp/Login/DirectL/login/ipan_web/hyoji/?url=member%2FH000%2Ftsnodr%2Fcourse_switch.htm%3FsearchStr%3D8782&bid=HPC_DL_inv_8782"><img src="https://www.smbcnikko.co.jp/smbc_nikko_fund/img/myfund_btn_001.png" alt="購入注文" class="rollover"></a></p>
	//	<p><a href="https://trade.smbcnikko.co.jp/Login/DirectL/login/ipan_web/hyoji/?url=tumitate%2Fplan%2Fcart%2Ftork%3Fnaigaikbn%3D0%26syohinkbn%3D03%26meigcd%3D0087820000&bid=HPC_DL_tsumitate_8782"><img src="https://www.smbcnikko.co.jp/smbc_nikko_fund/img/myfund_btn_002.png" alt="積立申込" class="rollover"></a></p>
	//	<p id="Fav2" class="mt04"><script>setFavorite2('0331418A','2');</script></p>
	//	<script>setSimBtn('20240229','20181031','0331418A');</script>
	//</td>
	//</tr>
	public static class FundInfo {
		//<tr>
		//<td class="left"><p><a href="qsearch.exe?F=detail_kokunai1&KEY1=0331418A"><!--<a href="qsearch.exe?F=detail_tani&KEY1=0331418A">-->ｅＭＡＸＩＳ Ｓｌｉｍ 全世界株式（オール・カントリー）</a></p></td>
		//<td class="left"><p><!--<a href="qsearch.exe?F=detail_kokunai1&KEY1=7921122C">--><a href="qsearch.exe?F=detail_tani&KEY1=7921122C">シティグループ社債／ダブル・アクセス戦略ファンド２０２２－１２</a></p></td>

		public static final Pattern PAT = Pattern.compile(
			"<tr>\\s+" +
			"<td class=\"left\"><p>.+?<a href=\"qsearch.exe\\?F=detail_tani&KEY1=(?<fundCode>.+?)\">(?:-->)?(?<fundName>.+?)</a></p></td>\\s+" +
			""
		);
		public static List<FundInfo> getInstance(String page) {
			return ScrapeUtil.getList(FundInfo.class, PAT, page);
		}
		
		public final String fundCode;
		public final String fundName;
		
		public FundInfo(String fundCode, String fundName) {
			this.fundCode  = fundCode;
			this.fundName = fundName;
		}
		
		@Override
		public String toString() {
			return StringUtil.toString(this);
		}
	}
}
