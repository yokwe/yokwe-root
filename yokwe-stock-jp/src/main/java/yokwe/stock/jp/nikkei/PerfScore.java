package yokwe.stock.jp.nikkei;

import java.util.regex.Pattern;

import yokwe.util.ScrapeUtil;
import yokwe.util.ToString;

/*
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
*/

public class PerfScore {
	public static final String HEADER = "<!-- ▼ QP-SCORE：QUICK投信モニタリングスコア ▼ -->";
	public static final Pattern PAT = Pattern.compile(
		HEADER + "\\s+" +
		"<div .+?>\\s+" +
		"<div .+?>\\s+" +
		"<h2 .+?>QUICKファンドスコア<a .+?>.+?</a>　\\((?<asOf>.+?)\\)</h2>\\s+" +
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
	public static PerfScore getInstance(String page) {
		return ScrapeUtil.get(PerfScore.class, PAT, page);
	}
	
	public String asOf;
	public String scoreOverAll;
	public String scoreRisk;
	public String scoreReturn;
	public String scoreDownsideResistance;
	public String scoreCost;
	public String scoreDivHealth;
	
	public PerfScore(
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
		return ToString.withFieldName(this);
	}
}