package yokwe.stock.jp.edinet;

import yokwe.util.UnexpectedException;

public enum Report {
	// Occurrence
	//	  14688 asr
	//	     11 drs
	//	   3505 ssr
	//	   3777 q1r
	//	   3757 q2r
	//	   3768 q3r
	//	      7 q4r
	
	ASR("asr"), // 有価証券報告書
	DRS("drs"), // 有価証券報告書【みなし有価証券届出書】
//	QSR("qsr"), // 四半期報告書
	SSR("ssr"), // 中間期報告書

	SRS("srs"), // 有価証券届出書
	ESR("esr"), // 臨時報告書
	RST("rst"), // 発行登録書
	REP("rep"), // 発行登録追補書類
	SBR("sbr"), // 自己株券買付状況報告書
	TON("ton"), // 公開買付届出書
	PST("pst"), // 意見表明報告書
	WTO("wto"), // 公開買付撤回届出書
	TOR("tor"), // 公開買付報告書
	TOA("toa"), // 対質問回答報告書
	LVH("lvh"), // 大量保有報告書
	ICR("icr"), // 内部統制報告書
	
	Q1R("q1r"), // 第１四半期報告書
	Q2R("q2r"), // 第２四半期報告書
	Q3R("q3r"), // 第３四半期報告書
	Q4R("q4r"), // 第４四半期報告書
	Q5R("q5r"); // 第５四半期報告書

	public final String value;
	Report(String value) {
		this.value = value;
	}
	
	@Override
	public String toString() {
		return value;
	}
	
	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(Report.class);
	
	private static final Report[] VALUES = Report.values();
	public static Report getInstance(String value) {
		if (value == null || value.isEmpty()) return null;
		for(Report report: VALUES) {
			if (value.equals(report.value)) return report;
		}
		logger.error("Unknown value {}!", value);
		throw new UnexpectedException("Unknown value");
	}
}

