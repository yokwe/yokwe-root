package yokwe.stock.jp.nikkei;

import java.util.regex.Pattern;

import yokwe.util.ScrapeUtil;
import yokwe.util.ToString;

/*
//<!-- ▼ QP-BASPR：ファンド基本情報 ▼ -->
//<div class="m-stockInfo_detail m-stockInfo_detail_01">
//  <div class="m-stockInfo_detail_left">
//      <ul class="m-stockInfo_detail_list">
//          <li>
//              <span class="m-stockInfo_detail_title a-fz12"><a href="/markets/fund/ranking/?type=netasset">純資産総額<span class="font-m">(5/2)</span></a></span>
//              <span class="m-stockInfo_detail_value">2.47 <span class="qc-stock-info-unit">億円</span></span>
//          </li>
//          <li>
//              <span class="m-stockInfo_detail_title a-fz12"><a href="/nkd/fund/dividend/?fcode=03311002">直近分配金<span class="font-m">(23/2/7)</span></a></span>
//              <span class="m-stockInfo_detail_value">0 <span class="qc-stock-info-unit">円</span></span>
//          </li>
//          <li>
//              <span class="m-stockInfo_detail_title a-fz12"><a href="/markets/fund/ranking/?type=yield&amp;dividend_scrn=on">分配金健全度<span class="font-m">(1年)</span></a></span>
//              <span class="m-stockInfo_detail_value">100.00<span class="qc-stock-info-unit"> %</span></span>
//          </li>
//          <li>
//              <span class="m-stockInfo_detail_title a-fz12"><a href="/markets/fund/ranking/?type=flowioup">資金流出入<span class="font-m">(1ヵ月)</span></a></span>
//              <span class="m-stockInfo_detail_value">-0.02 <span class="qc-stock-info-unit">億円</span></span>
//          </li>
//      </ul>
//  </div>
//  <div class="m-stockInfo_detail_right">
//      <ul class="m-stockInfo_detail_list">
//          <li>
//              <span class="m-stockInfo_detail_title a-fz12"><a href="/markets/fund/ranking/?type=returnup&amp;term=1y">リターン<span class="font-m">(1年)</span></a></span>
//              <span class="m-stockInfo_detail_value">+1.57<span class="qc-stock-info-unit"> %</span></span>
//          </li>
//          <li>
//              <span class="m-stockInfo_detail_title a-fz12"><a href="/markets/fund/ranking/?type=risk&amp;term=1y">リスク<span class="font-m">(1年)</span></a></span>
//              <span class="m-stockInfo_detail_value">6.48<span class="qc-stock-info-unit"> %</span></span>
//          </li>
//          <li>
//              <span class="m-stockInfo_detail_title a-fz12"><a href="/markets/fund/ranking/?type=sharperatio&amp;term=1y">シャープレシオ<span class="font-m">(1年)</span></a></span>
//              <span class="m-stockInfo_detail_value">0.28</span>
//          </li>
//          <li>
//              <span class="m-stockInfo_detail_title a-fz12"><a href="/markets/fund/search/result/?list=score&amp;qfrisk=2">QUICKファンドリスク</a></span>
//              <span class="m-stockInfo_detail_value">2</span>
//          </li>
//      </ul>
//  </div>
//</div>
//<div class="m-stockInfo_date a-fz12">2023年4月末</div>
//<!-- ▲ QP-BASPR：ファンド基本情報 ▲ -->
*/

public class FundValues {
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
		public static FundValues getInstance(String page) {
			return ScrapeUtil.get(FundValues.class, PAT, page);
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

		public FundValues(
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
			return ToString.withFieldName(this);
		}
}