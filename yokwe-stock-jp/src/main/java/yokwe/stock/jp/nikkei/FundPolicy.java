package yokwe.stock.jp.nikkei;

import java.util.regex.Pattern;

import yokwe.util.ScrapeUtil;
import yokwe.util.ToString;

/*
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
*/

/*
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
//                <p>--</p>
//                <p class="a-fz12">主な運用対象：<span class="a-fz15">--</span></p>
//            </div>
//        </div>
//    </div>
//</div>
//<!-- ▲ QP-INVPO：運用方針 ▲ -->
*/

public class FundPolicy {
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
		"<p .+?>主な運用対象：<span .+>(?:<a .+?>)?(?<target>.+?)(?:</a>)?</span></p>\\s+" +
		"</div>\\s+" +
		"</div>\\s+" +
		"</div>\\s+" +
		"</div>\\s+" +
		""
	);
	public static FundPolicy getInstance(String page) {
		return ScrapeUtil.get(FundPolicy.class, PAT, page);
	}

	public String policy;
	public String target;
	
	public FundPolicy(String policy, String target) {
		this.policy = policy;
		this.target = target;
	}
	@Override
	public String toString() {
		return ToString.withFieldName(this);
	}
}