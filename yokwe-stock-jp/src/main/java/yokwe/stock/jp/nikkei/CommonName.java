package yokwe.stock.jp.nikkei;

import java.util.regex.Pattern;

import yokwe.util.ScrapeUtil;
import yokwe.util.StringUtil;

public class CommonName {
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
	public static CommonName getInstance(String page) {
		return ScrapeUtil.get(CommonName.class, PAT, page);
	}

	public String name;
	
	public CommonName(String name) {
		this.name = name;
	}
	@Override
	public String toString() {
		return StringUtil.toString(this);
	}
}