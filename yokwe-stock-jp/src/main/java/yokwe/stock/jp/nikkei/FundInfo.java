package yokwe.stock.jp.nikkei;

import java.util.regex.Pattern;

import yokwe.util.ScrapeUtil;
import yokwe.util.ToString;

/*
//<!-- ▼ QP-FDINFO：ファンド概要 ▼ -->
//<div class="m-articleFrame">
//    <div class="m-articleFrame_header">
//        <div class="m-headline">
//            <h2 class="m-headline_text">ファンド概要</h2>
//        </div>
//    </div>
//</div>
//<div class="m-block stockInfo a-mb20">
//    <div class="m-stockInfo_top">
//        <div class="m-stockInfo_detail m-stockInfo_detail_02 a-m0">
//            <div class="m-stockInfo_detail_common">
//                <ul class="m-stockInfo_detail_list">
//                    <li>
//                        <span class="m-stockInfo_detail_title a-fz12 a-minW7em">運用会社名：</span>
//                        <span class="m-stockInfo_detail_value a-fz15"><a href="/markets/fund/search/result/?company=03">三菱ＵＦＪ国際投信</a></span>
//                    </li>
//                     <li>
//                        <span class="m-stockInfo_detail_title a-fz12 a-minW9em">QUICK投信分類：</span>
//                        <span class="m-stockInfo_detail_value a-fz15"><a href="/markets/fund/search/result/?category1=bal&amp;category2=">バランス</a>－<a href="/markets/fund/search/result/?category1=bal&category2=mid">アロケーション固定(中リスク)</a>－為替リスクあり</span>
//                    </li>
//
//                    <li>
//                        <span class="m-stockInfo_detail_title a-fz12 a-minW7em">QUICK略称：</span>
//                        <span class="m-stockInfo_detail_value a-fz15">ライフ・バランスファンド（安定型）</span>
//                    </li><li>
//                        <span class="m-stockInfo_detail_title a-fz12 a-minW7em">愛称：</span>
//                        <span class="m-stockInfo_detail_value a-fz15">--</span>
//                    </li>
//                </ul>
//            </div>
//            <div class="m-stockInfo_detail_left">
//                <ul class="m-stockInfo_detail_list">
//                    <li>
//                        <span class="m-stockInfo_detail_title a-fz12 a-minW7em">日経略称：</span>
//                        <span class="m-stockInfo_detail_value a-fz15">ＬＢ安定</span>
//                    </li>
//                    <li>
//                        <span class="m-stockInfo_detail_title a-fz12 a-minW7em">決算頻度(年)：</span>
//                        <span class="m-stockInfo_detail_value a-fz15">年2回</span>
//                    </li>
//                    <li>
//                        <span class="m-stockInfo_detail_title a-fz12 a-minW5em">設定日：</span>
//                        <span class="m-stockInfo_detail_value a-fz15">2000年2月8日</span>
//                    </li>
//                    <li>
//                        <span class="m-stockInfo_detail_title a-fz12 a-minW5em">償還日：</span>
//                        <span class="m-stockInfo_detail_value a-fz15">無期限</span>
//                    </li>
//                </ul>
//            </div>
//            <div class="m-stockInfo_detail_right">
//                <ul class="m-stockInfo_detail_list">
//                   <li>
//                    <span class="m-stockInfo_detail_title a-fz12 a-minW6em">販売区分：</span>
//                    <span class="m-stockInfo_detail_value a-fz15">--</span>
//                    </li>
//                    <li>
//                        <span class="m-stockInfo_detail_title a-fz12 a-minW6em">運用区分：</span>
//                        <span class="m-stockInfo_detail_value a-fz15">アクティブ型</span>
//                    </li>
//                    <li>
//                        <span class="m-stockInfo_detail_title a-fz12 a-minW10em">購入時手数料(税込)：</span>
//                        <span class="m-stockInfo_detail_value a-fz15">2.2<span class="qc-stock-info-unit"> %</span></span>
//                    </li>
//                    <li>
//                        <span class="m-stockInfo_detail_title a-fz12 a-minW8em">実質信託報酬：</span>
//                        <span class="m-stockInfo_detail_value a-fz15">1.43<span class="qc-stock-info-unit"> %</span></span>
//                    </li>
//                </ul>
//            </div>
//        </div>
//    </div>
//    <ul class="qc-help a-pl5">
//        <li>※各項目の詳しい説明はヘルプ<a href="//www.nikkei.com/help/contents/markets/fund/" target="_blank" class="m-iconQ a-valign-texttop">（解説）</a>をご覧ください。</li>
//    </ul>
//</div>
//<!-- ▲ QP-FDINFO：ファンド概要 ▲ -->
*/

public class FundInfo {
	public static final String HEADER = "<!-- ▼ QP-FDINFO：ファンド概要 ▼ -->";
	public static final Pattern PAT = Pattern.compile(
		HEADER + "\\s+" +
		"<div .+?>\\s+" +
		"<div .+?>\\s+" +
		"<div .+?>\\s+" +
		"<h2 .+>ファンド概要</h2>\\s+" +
		"</div>\\s+" +
		"</div>\\s+" +
		"</div>\\s+" +
		"<div .+?>\\s+" +
		"<div .+?>\\s+" +
		"<div .+?>\\s+" +
		"<div .+?>\\s+" +
		"<ul .+?>\\s+" +
		"<li>\\s+" +
		"<span .+?>運用会社名：</span>\\s+" +
		"<span .+?><a .+?>(?<company>.+?)</a></span>\\s+" +
		"</li>\\s+" +
		"<li>\\s+" +
		"<span .+?>QUICK投信分類：</span>\\s+" +
		"<span .+?><a .+?>(?<category1>.+?)</a>(?:－<a .+?>(?<category2>.+?)</a>)?(?:－(?<category3>.+?))?</span>\\s+" +
		"</li>\\s+" +
		"<li>\\s+" +
		"<span .+?>QUICK略称：</span>\\s+" +
		"<span .+?>.+?</span>\\s+" +
		"</li>\\s*" +
		"<li>\\s+" +
		"<span .+?>愛称：</span>\\s+" +
		"<span .+?>.+?</span>\\s+" +
		"</li>\\s+" +
		"</ul>\\s+" +
		"</div>\\s+" +
		"<div .+?>\\s+" +
		"<ul .+?>\\s+" +
		"<li>\\s+" +
		"<span .+?>日経略称：</span>\\s+" +
		"<span .+?>.+?</span>\\s+" +
		"</li>\\s+" +
		"<li>\\s+" +
		"<span .+?>決算頻度\\(年\\)：</span>\\s+" +
		"<span .+?>年(?<settlementFrequency>.+?)回</span>\\s+" +
		"</li>\\s+" +
		"<li>\\s+" +
		"<span .+?>設定日：</span>\\s+" +
		"<span .+?>(?<establishmentDate>.+?)</span>\\s+" +
		"</li>\\s+" +
		"<li>\\s+" +
		"<span .+?>償還日：</span>\\s+" +
		"<span .+?>(?<redemptionDate>.+?)</span>\\s+" +
		"</li>\\s+" +
		"</ul>\\s+" +
		"</div>\\s+" +
		"<div .+?>\\s+" +
		"<ul .+?>\\s+" +
		"<li>\\s+" +
		"<span .+?>販売区分：</span>\\s+" +
		"<span .+?>(?<salesType>.+?)</span>\\s+" +
		"</li>\\s+" +
		"<li>\\s+" +
		"<span .+?>運用区分：</span>\\s+" +
		"<span .+?>(?<fundType>.+?)</span>\\s+" +
		"</li>\\s+" +
		"<li>\\s+" +
		"<span .+?>購入時手数料\\(税込\\)：</span>\\s+" +
		"<span .+?>(?<initialFee>.+?)<span .+?>.+?</span></span>\\s+" +
		"</li>\\s+" +
		"<li>\\s+" +
		"<span .+?>実質信託報酬：</span>\\s+" +
		"<span .+?>(?<trustFee>.+?)<span .+?>.+?</span></span>\\s+" +
		"</li>\\s+" +
		"</ul>\\s+" +
		"</div>\\s+" +
		"</div>\\s+" +
		"</div>\\s+" +
		""
	);
	public static FundInfo getInstance(String page) {
		return ScrapeUtil.get(FundInfo.class, PAT, page);
	}
	
	public String company;
	public String category1;
	public String category2;
	public String category3;
	public String settlementFrequency;
	public String establishmentDate;
	public String redemptionDate;
	public String salesType;
	public String fundType;
	public String initialFee;
	public String trustFee;
	
	public FundInfo(
			String company,
			String category1,
			String category2,
			String category3,
			String settlementFrequency,
			String establishmentDate,
			String redemptionDate,
			String salesType,
			String fundType,
			String initialFee,
			String trustFee
		) {
		this.company = company;
		this.category1 = category1;
		this.category2 = category2;
		this.category3 = category3;
		this.settlementFrequency = settlementFrequency;
		this.establishmentDate = establishmentDate;
		this.redemptionDate = redemptionDate;
		this.salesType = salesType;
		this.fundType = fundType;
		this.initialFee = initialFee;
		this.fundType = fundType;
		this.initialFee = initialFee;
		this.trustFee = trustFee;
	}
	@Override
	public String toString() {
		return ToString.withFieldName(this);
	}
}