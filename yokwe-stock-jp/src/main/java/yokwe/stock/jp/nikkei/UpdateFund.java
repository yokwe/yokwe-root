package yokwe.stock.jp.nikkei;

import java.io.File;
import java.util.regex.Pattern;

import yokwe.util.FileUtil;
import yokwe.util.ScrapeUtil;
import yokwe.util.StringUtil;

public class UpdateFund {
	private static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();

	// common
	public static class QPCOMMON01 {
		//<!-- ▼ QP-COMMON01：共通（コード他） ▼ -->
		//<div class="m-companyCategory">
		//  <span class="m-companyCategory_text  a-baseLinkStyleType02">03311002 : <a href="/markets/fund/search/result/?category1=bal&amp;category2=">バランス</a> ＞ <a href="/markets/fund/search/result/?category1=bal&category2=mid">アロケーション固定(中リスク)</a></span>
		//	<span class="m-companyCategory_text m-company_data_tag">運用会社 : <a href="/markets/fund/search/result/?company=03">三菱UFJ国際</a></span>
		//</div>
		//<!-- ▲ QP-COMMON01：共通（コード他） ▲ -->
		public static final String HEADER = "<!-- ▼ QP-COMMON01：共通（コード他） ▼ -->";
		public static final Pattern PAT = Pattern.compile(
			HEADER + "\\s+" +
			"<div .+?>\\s+"+
			"<span .+?>(?<code>.+?) : <a .+?>(?<category1>.+?)</a>(?:.+?<a .+?>(?<category2>.+?)</a>)?</span>\\s+" +
			"<span .+?>.+?<a .+?>(?<company>.+?)</a></span>\\s+" +
			""
		);
		public static QPCOMMON01 getInstance(String page) {
			return ScrapeUtil.get(QPCOMMON01.class, PAT, page);
		}

		public String code;       // コード
		public String category1;  // バランス
		public String category2;  // アロケーション固定(中リスク)
		public String company;    // 運用会社
		
		public QPCOMMON01(String code, String category1, String category2, String company) {
			this.code      = code;
			this.category1 = category1;
			this.category2 = category2;
			this.company   = company;
		}
		@Override
		public String toString() {
			return StringUtil.toString(this);
		}
	}
	public static class QPCOMMON02 {
		//<!-- ▼ QP-COMMON02：共通（ファンド名称） ▼ -->
		//<div class="l-corpLogo a-mb15">
		//    <div class="l-corpLogo_main">
		//        <div class="m-headlineLarge a-mb0">
		//            <h1 class="m-headlineLarge_text">
		//            三菱ＵＦＪ ライフ・バランスファンド（安定型）
		//            </h1>
		
		//            <h1 class="m-headlineLarge_text">
		//            <a href="/nkd/fund/?fcode=03311002">三菱ＵＦＪ ライフ・バランスファンド（安定型）
		//              </a>
		//        
		//            </h1>
		
		public static final String HEADER = "<!-- ▼ QP-COMMON02：共通（ファンド名称） ▼ -->";
		public static final Pattern PAT = Pattern.compile(
			HEADER + "\\s+" +
			"<div .+?>\\s+"+
			"<div .+?>\\s+"+
			"<div .+?>\\s+"+
			"<h1 .+?>\\s+(?:<a .+?>)?(?<name>.+?)\\s+(?:</a>\\s+)?</h1>" +
			""
		);
		public static QPCOMMON02 getInstance(String page) {
			return ScrapeUtil.get(QPCOMMON02.class, PAT, page);
		}

		public String name;
		
		public QPCOMMON02(String name) {
			this.name = name;
		}
		@Override
		public String toString() {
			return StringUtil.toString(this);
		}
	}
	public static class QPCommon03 {
		//<!-- ▼ QP-COMMON03：共通（価格） ▼ -->
		//<div class="m-stockPriceElm">
		//    <dl>
		//        <dt class="m-stockPriceElm_title">基準価格(5/2)：</dt>
		//        <dd class="m-stockPriceElm_value now">11,408<span class="m-stockPriceElm_value_unit">円</span></dd>
		//    </dl>
		//    <dl>
		//        <dt class="m-stockPriceElm_title">前日比：</dt>
		//        <dd class="m-stockPriceElm_value comparison minus">-8<span class="a-fs16 a-ml5">(-0.07%)</span></dd>
		//    </dl>
		//</div>
		public static final String HEADER = "<!-- ▼ QP-COMMON03：共通（価格） ▼ -->";
		public static final Pattern PAT = Pattern.compile(
				HEADER + "\\s+" +
				"<div .+?>\\s+" +
				"<dl>\\s+" +
				"<dt .+?>基準価格\\((?<date>.+?)\\)：</dt>\\s+" +
				"<dd .+?>(?<value>.+?)<span .+?>(?<unit>.+?)</span></dd>\\s+" +
				""
			);
			public static QPCommon03 getInstance(String page) {
				return ScrapeUtil.get(QPCommon03.class, PAT, page);
			}

			public String date;
			@ScrapeUtil.AsNumber
			public String value;
			public String unit;
			
			public QPCommon03(String date, String value, String unit) {
				this.date  = date;
				this.value = value;
				this.unit  = unit;
			}
			@Override
			public String toString() {
				return StringUtil.toString(this);
			}
	}
	

	// fund
	public static class QPBASPR {
	//<!-- ▼ QP-BASPR：ファンド基本情報 ▼ -->
	//<div class="m-stockInfo_detail m-stockInfo_detail_01">
	//    <div class="m-stockInfo_detail_left">
	//        <ul class="m-stockInfo_detail_list">
	//            <li>
	//                <span class="m-stockInfo_detail_title a-fz12"><a href="/markets/fund/ranking/?type=netasset">純資産総額<span class="font-m">(5/2)</span></a></span>
	//                <span class="m-stockInfo_detail_value">2.47 <span class="qc-stock-info-unit">億円</span></span>
	//            </li>
	//            <li>
	//                <span class="m-stockInfo_detail_title a-fz12"><a href="/nkd/fund/dividend/?fcode=03311002">直近分配金<span class="font-m">(23/2/7)</span></a></span>
	//                <span class="m-stockInfo_detail_value">0 <span class="qc-stock-info-unit">円</span></span>
	//            </li>
	//            <li>
	//                <span class="m-stockInfo_detail_title a-fz12"><a href="/markets/fund/ranking/?type=yield&amp;dividend_scrn=on">分配金健全度<span class="font-m">(1年)</span></a></span>
	//                <span class="m-stockInfo_detail_value">100.00<span class="qc-stock-info-unit"> %</span></span>
	//            </li>
	//            <li>
	//                <span class="m-stockInfo_detail_title a-fz12"><a href="/markets/fund/ranking/?type=flowioup">資金流出入<span class="font-m">(1ヵ月)</span></a></span>
	//                <span class="m-stockInfo_detail_value">-0.02 <span class="qc-stock-info-unit">億円</span></span>
	//            </li>
	//        </ul>
	//    </div>
	//    <div class="m-stockInfo_detail_right">
	//        <ul class="m-stockInfo_detail_list">
	//            <li>
	//                <span class="m-stockInfo_detail_title a-fz12"><a href="/markets/fund/ranking/?type=returnup&amp;term=1y">リターン<span class="font-m">(1年)</span></a></span>
	//                <span class="m-stockInfo_detail_value">+1.57<span class="qc-stock-info-unit"> %</span></span>
	//            </li>
	//            <li>
	//                <span class="m-stockInfo_detail_title a-fz12"><a href="/markets/fund/ranking/?type=risk&amp;term=1y">リスク<span class="font-m">(1年)</span></a></span>
	//                <span class="m-stockInfo_detail_value">6.48<span class="qc-stock-info-unit"> %</span></span>
	//            </li>
	//            <li>
	//                <span class="m-stockInfo_detail_title a-fz12"><a href="/markets/fund/ranking/?type=sharperatio&amp;term=1y">シャープレシオ<span class="font-m">(1年)</span></a></span>
	//                <span class="m-stockInfo_detail_value">0.28</span>
	//            </li>
	//            <li>
	//                <span class="m-stockInfo_detail_title a-fz12"><a href="/markets/fund/search/result/?list=score&amp;qfrisk=2">QUICKファンドリスク</a></span>
	//                <span class="m-stockInfo_detail_value">2</span>
	//            </li>
	//        </ul>
	//    </div>
	//</div>
	//<div class="m-stockInfo_date a-fz12">2023年4月末</div>
	//<!-- ▲ QP-BASPR：ファンド基本情報 ▲ -->
		public static final String HEADER = "<!-- ▼ QP-BASPR：ファンド基本情報 ▼ -->";
		public static final Pattern PAT = Pattern.compile(
				HEADER + "\\s+" +
				"<div .+?>\\s+" +
				"<div .+?>\\s+" +
				"<ul .+?>\\s+" +
				"<li>\\s+" +
				"<span .+?><a .+?>純資産総額<span .+>\\((?<uamDate>.+?)\\)</span></a></span>\\s+" +
				"<span .+?>(?<uamValue>.+?)\\s?<span .+?>(?<uamUnit>.+?)</span></span>\\s+" +
				"</li>\\s+" +
				"<li>\\s+" +
				"<span .+?><a .+?>直近分配金<span .+>\\((?<divDate>.+?)\\)</span></a></span>\\s+" +
				"<span .+?>(?<divValue>.+?)\\s?<span .+?>(?<divUnit>.+?)</span></span>\\s+" +
				"</li>\\s+" +
				"<li>\\s+" +
				"<span .+?><a .+?>分配金健全度<span .+>\\((?<divHealthDuration>.+?)\\)</span></a></span>\\s+" +
				"<span .+?>(?<divHealthValue>.+?)\\s?<span .+?>.+?</span></span>\\s+" +
				"</li>\\s+" +
				"<li>\\s+" +
				"<span .+?><a .+?>資金流出入<span .+>\\((?<flowDuration>.+?)\\)</span></a></span>\\s+" +
				"<span .+?>(?<flowValue>.+?)\\s?<span .+?>(?<flowUnit>.+?)</span></span>\\s+" +
				"</li>\\s+" +
				"</ul>\\s+" +
				"</div>\\s+" +
				"<div .+?>\\s+" +
				"<ul .+?>\\s+" +
				"<li>\\s+" +
				"<span .+?><a .+?>リターン<span .+>\\((?<returnDuration>.+?)\\)</span></a></span>\\s+" +
				"<span .+?>(?<returnValue>.+?)\\s?<span .+?>.+?</span></span>\\s+" +
				"</li>\\s+" +
				"<li>\\s+" +
				"<span .+?><a .+?>リスク<span .+>\\((?<riskDuration>.+?)\\)</span></a></span>\\s+" +
				"<span .+?>(?<riskValue>.+?)\\s?<span .+?>.+?</span></span>\\s+" +
				"</li>\\s+" +
				"<li>\\s+" +
				"<span .+?><a .+?>シャープレシオ<span .+>\\((?<sharpRatioDuration>.+?)\\)</span></a></span>\\s+" +
				"<span .+?>(?<sharpRatioValue>.+?)\\s?</span>\\s+" +
				"</li>\\s+" +
				"<li>\\s+" +
				"<span .+?>.*?QUICKファンドリスク.*?</span>\\s+" +
				"<span .+?>(?<quickFundRiskValue>.+?)\\s?</span>\\s+" +
				"</li>\\s+" +
				"</ul>\\s+" +
				"</div>\\s+" +
				"</div>\\s+" +
				"<div .+?>(?<asOf>.+?)</div>" +
				""
			);
			public static QPBASPR getInstance(String page) {
				return ScrapeUtil.get(QPBASPR.class, PAT, page);
			}

			public String uamDate;
			@ScrapeUtil.AsNumber
			public String uamValue;
			public String uamUnit;
			public String divDate;
			@ScrapeUtil.AsNumber
			public String divValue;
			public String divUnit;
			public String divHealthDuration;
			public String divHealthValue;
			public String flowDuration;
			@ScrapeUtil.AsNumber
			public String flowValue;
			public String flowUnit;
			public String returnDuration;
			public String returnValue;
			public String riskDuration;
			public String riskValue;
			public String sharpRatioDuration;
			public String sharpRatioValue;
			public String quickFundRiskValue;
			public String asOf;

			public QPBASPR(
					String uamDate, String uamValue, String uamUnit,
					String divDate, String divValue, String divUnit,
					String divHealthDuration, String divHealthValue,
					String flowDuration, String flowValue, String flowUnit,
					String returnDuration, String returnValue,
					String riskDuration, String riskValue,
					String sharpRatioDuration, String sharpRatioValue,
					String quickFundRiskValue,
					String asOf
				) {
				this.uamDate  = uamDate;
				this.uamValue = uamValue;
				this.uamUnit  = uamUnit;
				this.divDate  = divDate;
				this.divValue = divValue;
				this.divUnit  = divUnit;
				this.divHealthDuration  = divHealthDuration;
				this.divHealthValue = divHealthValue;
				this.flowDuration  = flowDuration;
				this.flowValue = flowValue;
				this.flowUnit  = flowUnit;
				this.returnDuration  = returnDuration;
				this.returnValue = returnValue;
				this.riskDuration  = riskDuration;
				this.riskValue = riskValue;
				this.sharpRatioDuration  = sharpRatioDuration;
				this.sharpRatioValue = sharpRatioValue;
				this.quickFundRiskValue = quickFundRiskValue;
				this.asOf = asOf;
			}
			@Override
			public String toString() {
				return StringUtil.toString(this);
			}
	}

	public static class QPFDINFO {
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
		public static QPFDINFO getInstance(String page) {
			return ScrapeUtil.get(QPFDINFO.class, PAT, page);
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
		
		public QPFDINFO(
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
			return StringUtil.toString(this);
		}
	}
	public static class QPINVPO {
		//<!-- ▼ QP-INVPO：運用方針 ▼ -->
		//<div class="m-block comment a-mb40">
		//    <div class="m-articleFrame">
		//        <div class="m-articleFrame_header">
		//            <div class="m-headline">
		//                <h2 class="m-headline_text">運用方針</h2>
		//            </div>
		//        </div>
		//        <div class="m-articleFrame_body">
		//            <div class="cmn-article_summary">
		//                <p>   各種マザーファンドを通じ、国内債券、同株式、外国債券及び同株式へバランスよく分散投資。株式組入比率の上限を45%未満、外貨建資産組入比率の上限を35%未満とする。他のライフ・バランスファンドに比べ、比較的値動きが小さい。原則、為替ヘッジなし。各ファンドは無手数料でスイッチング可能。</p>
		//                <p class="a-fz12">主な運用対象：<span class="a-fz15"><a href="/markets/fund/search/result/?portfolio=blns">バランス</a></span></p>
		//            </div>
		//        </div>
		//    </div>
		//</div>
		//<!-- ▲ QP-INVPO：運用方針 ▲ -->

		public static final String HEADER = "<!-- ▼ QP-INVPO：運用方針 ▼ -->";
		public static final Pattern PAT = Pattern.compile(
			HEADER + "\\s+" +
			"<div .+?>\\s+"+
			"<div .+?>\\s+"+
			"<div .+?>\\s+"+
			"<div .+?>\\s+"+
			"<h2 .+?>運用方針</h2>\\s+" +
			"</div>\\s+" +
			"</div>\\s+" +
			"<div .+?>\\s+"+
			"<div .+?>\\s+"+
			"<p>\\s*(?<policy>.+?)\\s*</p>\\s+" +
			"<p .+?>主な運用対象：<span .+><a .+?>(?<target>.+?)</a></span></p>\\s+" +
			"</div>\\s+" +
			"</div>\\s+" +
			"</div>\\s+" +
			"</div>\\s+" +
			""
		);
		public static QPINVPO getInstance(String page) {
			return ScrapeUtil.get(QPINVPO.class, PAT, page);
		}

		public String policy;
		public String target;
		
		public QPINVPO(String policy, String target) {
			this.policy = policy;
			this.target = target;
		}
		@Override
		public String toString() {
			return StringUtil.toString(this);
		}
	}

	// perf
	public static class QPRSRET {
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
		public static QPRSRET getInstance(String page) {
			return ScrapeUtil.get(QPRSRET.class, PAT, page);
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

		public QPRSRET(
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
	public static class QPSCORE {
		//<!-- ▼ QP-SCORE：QUICK投信モニタリングスコア ▼ -->
		//<div class="m-articleFrame a-w100p" id="fundscore">
		//    <div class="m-headline">
		//        <h2 class="m-headline_text">QUICKファンドスコア<a href="//www.nikkei.com/help/contents/markets/fund/#qf6" target="_blank" class="m-iconQ">（解説）</a>　(2023年4月末時点)</h2>
		//    </div>
		//    <div class="m-tableType01 a-mb10">
		//        <div class="m-tableType01_table">
		//            <table class="w668 rsp_table">
		//                <thead>
		//                <tr>
		//                    <th class="border-gray a-w5p"></th>
		//                    <th class="border-gray a-w30p">総合</th>
		//                    <th class="border-gray a-taR">7</th>
		//                </tr>
		//                </thead>
		//                <tbody>
		//                <tr>
		//                    <th class="bgcLGray a-w5p"></th>
		//                    <th class="a-w30p">リスク</th>
		//                    <td class="a-taR">2.90</td>
		//                </tr>
		//                <tr class="bgcGray">
		//                    <th class="bgcLGray a-w5p"></th>
		//                    <th class="a-w30p">リターン</th>
		//                    <td class="a-taR">9.20</td>
		//                </tr>
		//                <tr>
		//                    <th class="bgcLGray a-w5p"></th>
		//                    <th class="a-w30p">下値抵抗力</th>
		//                    <td class="a-taR">5.70</td>
		//                </tr>
		//                <tr class="bgcGray">
		//                    <th class="bgcLGray a-w5p"></th>
		//                    <th class="a-w30p">コスト</th>
		//                    <td class="a-taR">4.20</td>
		//                </tr>
		//                <tr>
		//                    <th class="bgcLGray a-w5p"></th>
		//                    <th class="a-w30p">分配金健全度</th>
		//                    <td class="a-taR">8.20</td>
		//                </tr>
		//                </tbody>
		//            </table>
		//        </div>
		//    </div>
		//    <div class="a-fs12 a-lh14">※QUICKファンドスコアはQUICK投信分類「バランス」に属するファンドの相対値です。<br>
		//    ※総合スコアは、5つの評価指標を総合し、最も評価の高いファンドを10、最も評価の低いファンドを1で表します。
		//    </div>
		//    <div class="a-mb40"></div>
		//</div>
		//<!-- ▲ QP-SCORE：QUICK投信モニタリングスコア ▲ -->
		public static final String HEADER = "<!-- ▼ QP-SCORE：QUICK投信モニタリングスコア ▼ -->";
		public static final Pattern PAT = Pattern.compile(
			HEADER + "\\s+" +
			"<div .+?>\\s+" +
			"<div .+?>\\s+" +
			"<h2 .+?>QUICKファンドスコア<a .+?>.+?</a>　\\((?<asOf>.+?)</h2>\\s+" +
			"</div>\\s+" +
			"<div .+?>\\s+" +
			"<div .+?>\\s+" +
			"<table .+?>\\s+" +
			"<thead>\\s+" +
			"<tr>\\s+" +
			"<th .+?></th>\\s+" +
			"<th .+?>総合</th>\\s+" +
			"<th .+?>(?<scoreOverAll>.+?)</th>\\s+" +
			"</tr>\\s+" +
			"</thead>\\s+" +
			"<tbody>\\s+" +
			"<tr.*?>\\s+" +
			"<th .+?></th>\\s+" +
			"<th .+?>リスク</th>\\s+" +
			"<td .+?>(?<scoreRisk>.+?)</td>\\s+" +
			"</tr>\\s+" +
			"<tr.*?>\\s+" +
			"<th .+?></th>\\s+" +
			"<th .+?>リターン</th>\\s+" +
			"<td .+?>(?<scoreReturn>.+?)</td>\\s+" +
			"</tr>\\s+" +
			"<tr.*?>\\s+" +
			"<th .+?></th>\\s+" +
			"<th .+?>下値抵抗力</th>\\s+" +
			"<td .+?>(?<scoreDownsideResistance>.+?)</td>\\s+" +
			"</tr>\\s+" +
			"<tr.*?>\\s+" +
			"<th .+?></th>\\s+" +
			"<th .+?>コスト</th>\\s+" +
			"<td .+?>(?<scoreCost>.+?)</td>\\s+" +
			"</tr>\\s+" +
			"<tr.*?>\\s+" +
			"<th .+?></th>\\s+" +
			"<th .+?>分配金健全度</th>\\s+" +
			"<td .+?>(?<scoreDivHealth>.+?)</td>\\s+" +
			"</tr>\\s+" +

			""
		);
		public static QPSCORE getInstance(String page) {
			return ScrapeUtil.get(QPSCORE.class, PAT, page);
		}
		
		public String asOf;
		public String scoreOverAll;
		public String scoreRisk;
		public String scoreReturn;
		public String scoreDownsideResistance;
		public String scoreCost;
		public String scoreDivHealth;
		
		public QPSCORE(
			String asOf,
			String scoreOverAll,
			String scoreRisk,
			String scoreReturn,
			String scoreDownsideResistance,
			String scoreCost,
			String scoreDivHealth
		) {
			this.asOf = asOf;
			this.scoreOverAll = scoreOverAll;
			this.scoreRisk = scoreRisk;
			this.scoreReturn = scoreReturn;
			this.scoreDownsideResistance = scoreDownsideResistance;
			this.scoreCost = scoreCost;
			this.scoreDivHealth = scoreDivHealth;
		}
		@Override
		public String toString() {
			return StringUtil.toString(this);
		}
	}
	
	// div
	// FIXME   <!-- ▼ QP-BUNPAI：分配金実績 ▼ -->
	public static class QPBUNPAI {
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
		public static QPBUNPAI getInstance(String page) {
			return ScrapeUtil.get(QPBUNPAI.class, PAT, page);
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

		public QPBUNPAI(
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
				return StringUtil.toString(this);
			}
	}
	// FIXME   <!-- ▼ QP-BUNPAISD：分配金健全度 ▼ -->
	public static class QPBUNPAISD {
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
		public static QPBUNPAISD getInstance(String page) {
			return ScrapeUtil.get(QPBUNPAISD.class, PAT, page);
		}

		public String score1Y;
		public String score3Y;
		public String score5Y;
		public String score10Y;

		public QPBUNPAISD(
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
	// FIXME   <!-- ▼ QP-YIELD：分配金利回り ▼ -->
	public static class QPYIELD {
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
		public static QPYIELD getInstance(String page) {
			return ScrapeUtil.get(QPYIELD.class, PAT, page);
		}

		public String date;
		public String amount;
		public String rate;
		public String price;
		
		public QPYIELD(
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
			return StringUtil.toString(this);
		}
	}

	private static void testFund() {
		var info = DownloadFile.downloadFundInfo;
		var list = DownloadFile.getCodeListFromFund();
		int count = 0;
		for(var code: list) {
			String path = info.getPath(code);
			String page = FileUtil.read().file(path);

			count++;
			if ((count % 1000) == 1) logger.info("{}", String.format("%4d / %4d", count, list.size()));
			//logger.info("{} {}", path, String.format("%4d / %4d", count, list.size()));
			
			boolean hasError = false;
			
			var qpCommon01 = QPCOMMON01.getInstance(page);
			if (qpCommon01 == null) {
				logger.error("{}  qpCommon01 is null", path);
				hasError = true;
			}
			var qpCommon02 = QPCOMMON02.getInstance(page);
			if (qpCommon02 == null) {
				logger.error("{}  qpCommon02 is null", path);
				hasError = true;
			}
			var qpCommon03 = QPCommon03.getInstance(page);
			if (qpCommon03 == null) {
				logger.error("{}  qpCommon03 is null", path);
				hasError = true;
			}
			var qpBasic = QPBASPR.getInstance(page);
			if (qpBasic == null) {
				logger.error("{}  qpBasic is null", path);
				hasError = true;
			}
			var qpFund = QPFDINFO.getInstance(page);
			if (qpFund == null) {
				logger.error("{}  qpFund is null", path);
				hasError = true;
			}
			var qpPolicy = QPINVPO.getInstance(page);
			if (qpPolicy == null) {
				logger.error("{}  qpPolicy is null", path);
				hasError = true;
			}
			
			if (hasError) break;

			if (count <= 5) {
				logger.info("{}", code);
				logger.info("  {}", qpCommon01.toString());
				logger.info("  {}", qpCommon02.toString());
				logger.info("  {}", qpCommon03.toString());
				logger.info("  {}", qpBasic.toString());
				logger.info("  {}", qpFund.toString());
				logger.info("  {}", qpPolicy.toString());
			} else {
//				break;
			}
		}
	}
	private static void testPerf() {
		var info = DownloadFile.downloadPerfInfo;
		var list = DownloadFile.getCodeListFromFund();
		int count = 0;
		for(var code: list) {
			String path = info.getPath(code);
			String page = FileUtil.read().file(path);

			count++;
			if ((count % 1000) == 1) logger.info("{}", String.format("%4d / %4d", count, list.size()));
			//logger.info("{} {}", path, String.format("%4d / %4d", count, list.size()));
			
			boolean hasError = false;
			
			var qpCommon01 = QPCOMMON01.getInstance(page);
			if (qpCommon01 == null) {
				logger.error("{}  qpCommon01 is null", path);
				hasError = true;
			}
			var qpCommon02 = QPCOMMON02.getInstance(page);
			if (qpCommon02 == null) {
				logger.error("{}  qpCommon02 is null", path);
				hasError = true;
			}
			var qpCommon03 = QPCommon03.getInstance(page);
			if (qpCommon03 == null) {
				logger.error("{}  qpCommon03 is null", path);
				hasError = true;
			}
			var qpRiskReturn = QPRSRET.getInstance(page);
			if (qpRiskReturn == null) {
				logger.error("{}  qpRiskReturn is null", path);
				hasError = true;
			}
			var qpScore = QPSCORE.getInstance(page);
			if (qpScore == null) {
				logger.error("{}  qpScore is null", path);
				hasError = true;
			}

			if (hasError) break;

			if (count <= 5) {
				logger.info("{}", code);
				logger.info("  {}", qpCommon01.toString());
				logger.info("  {}", qpCommon02.toString());
				logger.info("  {}", qpCommon03.toString());
				logger.info("  {}", qpRiskReturn.toString());
				logger.info("  {}", qpScore.toString());
			} else {
//				break;
			}
		}
	}
	private static void testDiv() {
		var info = DownloadFile.downloadDivInfo;
		var list = DownloadFile.getCodeListFromFund();
		int count = 0;
		for(var code: list) {
			String path = info.getPath(code);
			File   file = new File(path);
			if (!file.exists()) continue;
			
			String page = FileUtil.read().file(path);

			count++;
			if ((count % 1000) == 1) logger.info("{}", String.format("%4d / %4d", count, list.size()));
			//logger.info("{} {}", path, String.format("%4d / %4d", count, list.size()));
			
			boolean hasError = false;
			
			var qpCommon01 = QPCOMMON01.getInstance(page);
			if (qpCommon01 == null) {
				logger.error("{}  qpCommon01 is null", path);
				hasError = true;
			}
			var qpCommon02 = QPCOMMON02.getInstance(page);
			if (qpCommon02 == null) {
				logger.error("{}  qpCommon02 is null", path);
				hasError = true;
			}
			var qpCommon03 = QPCommon03.getInstance(page);
			if (qpCommon03 == null) {
				logger.error("{}  qpCommon03 is null", path);
				hasError = true;
			}
			var qpDiv = QPBUNPAI.getInstance(page);
			if (qpDiv == null) {
				logger.error("{}  qpDiv is null", path);
				hasError = true;
			}
			var qpDivHealth = QPBUNPAISD.getInstance(page);
			if (qpDivHealth == null) {
				logger.error("{}  qpDivHealth is null", path);
				hasError = true;
			}
			var qpYield = QPYIELD.getInstance(page);
			if (qpYield == null) {
				logger.error("{}  qpYield is null", path);
				hasError = true;
			}

			if (hasError) break;

			if (count <= 5) {
				logger.info("{}", code);
				logger.info("  {}", qpCommon01.toString());
				logger.info("  {}", qpCommon02.toString());
				logger.info("  {}", qpCommon03.toString());
				logger.info("  {}", qpDiv.toString());
				logger.info("  {}", qpDivHealth.toString());
				logger.info("  {}", qpYield.toString());
			} else {
//				break;
			}
		}
	}
	
	public static void main(String[] args) {
		logger.info("START");
		
		//testFund();
		//testPerf();
		testDiv();
		
		logger.info("STOP");
	}
}
