package yokwe.stock.jp.nikkei;

import java.util.regex.Pattern;

import yokwe.util.ScrapeUtil;
import yokwe.util.ToString;

// common
public class CommonCode {
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
	public static CommonCode getInstance(String page) {
		return ScrapeUtil.get(CommonCode.class, PAT, page);
	}

	public String code;       // コード
	public String category1;  // バランス
	public String category2;  // アロケーション固定(中リスク)
	public String company;    // 運用会社
	
	public CommonCode(String code, String category1, String category2, String company) {
		this.code      = code;
		this.category1 = category1;
		this.category2 = category2;
		this.company   = company;
	}
	@Override
	public String toString() {
		return ToString.withFieldName(this);
	}
}