package yokwe.stock.jp.edinet;

import java.io.File;
import java.time.LocalDate;
import java.util.regex.Pattern;

import yokwe.util.ScrapeUtil;
import yokwe.util.UnexpectedException;

public class Filename {
	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(Filename.class);

	public static final String PATH_FILE_DIR = EDINET.getPath("file");
	private static final File toFile(String docID, String string) {
		Document document = Document.getDocument(docID);
		String path = String.format("%s/%04d/%02d/%02d/%s/%s",
				PATH_FILE_DIR, document.submitDateTime.getYear(), document.submitDateTime.getMonthValue(), document.submitDateTime.getDayOfMonth(), docID, string);
		return new File(path);
	}

	public static class Manifest {
		public static final String NAME = "manifest_PublicDoc.xml";
	}
	
	public enum Report {
		// Occurrence
		//	  14688 asr
		//	     11 drs
		//	   3505 ssr
		//	   3777 q1r
		//	   3757 q2r
		//	   3768 q3r
		//	      7 q4r
		
		ASR("asr", "有価証券報告書"),
		DRS("drs", "有価証券報告書【みなし有価証券届出書】"),
//		QSR("qsr", "四半期報告書"),
		SSR("ssr", "中間期報告書"),

		SRS("srs", "有価証券届出書"),
		ESR("esr", "臨時報告書"),
		RST("rst", "発行登録書"),
		REP("rep", "発行登録追補書類"),
		SBR("sbr", "自己株券買付状況報告書"),
		TON("ton", "公開買付届出書"),
		PST("pst", "意見表明報告書"),
		WTO("wto", "公開買付撤回届出書"),
		TOR("tor", "公開買付報告書"),
		TOA("toa", "対質問回答報告書"),
		LVH("lvh", "大量保有報告書"),
		ICR("icr", "内部統制報告書"),
		
		Q1R("q1r", "第１四半期報告書"),
		Q2R("q2r", "第２四半期報告書"),
		Q3R("q3r", "第３四半期報告書"),
		Q4R("q4r", "第４四半期報告書"),
		Q5R("q5r", "第５四半期報告書");

		public final String value;
		public final String message;
		Report(String value, String message) {
			this.value   = value;
			this.message = message;
		}
		
		@Override
		public String toString() {
			return value;
		}
		
		public String toMessage() {
			return message;
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

	
	private static final String PAT_STRING =
			"(?<form>jp[a-z]{3}[0-9]{6})" +
			"-" +
			"(?<report>[a-z0-9]{3})-(?<reportNo>[0-9]{3})" +
			"_" +
			"(?<code>[EG][0-9]{5})-(?<codeNo>[0-9]{3})" +
			"_" +
			"(?<date>20[0-9]{2}-[01][0-9]-[0-3][0-9])" +
			"_" +
			"(?<submitNo>[0-9]{2})" +
			"_" +
			"(?<submitDate>20[0-9]{2}-[01][0-9]-[0-3][0-9])";
	
	public static boolean equals(Instance instance, Honbun honbun) {
		if (honbun == null) return false;
		return
			instance.form.equals(honbun.form) &&
			instance.report.equals(honbun.report) &&
			instance.reportNo.equals(honbun.reportNo) &&
			instance.code.equals(honbun.code) &&
			instance.codeNo.equals(honbun.codeNo) &&
			instance.date.equals(honbun.date) &&
			instance.submitNo.equals(honbun.submitNo) &&
			instance.submitDate.equals(honbun.submitDate);
	}

	public static class Instance implements Comparable<Instance> {
		// jp{府令様式番号}-{報告書略号}-{報告書連番(3 桁)}_{EDINET コード又はファンドコード}-{追番(3 桁)}_{報告対象期間期末日|報告義務発生日}_{報告書提出回数(2 桁)}_{報告書提出日}.xbrl 
		// jpcrp040300-q3r-001_E01442-000_2020-01-31_01_2020-03-06.xbrl
		private static final Pattern PAT = Pattern.compile(PAT_STRING + "\\.xbrl");

		public String    form;
		public Report    report;
		public String    reportNo;
		public String    code;
		public String    codeNo;
		public LocalDate date;
		public String    submitNo;
		public LocalDate submitDate;
		
		@ScrapeUtil.Ignore
		public String    string;
		
		public Instance (
			String    form,
			Report    report,
			String    reportNo,
			String    code,
			String    codeNo,
			LocalDate date,
			String    submitNo,
			LocalDate submitDate
			) {
			this.form       = form;
			this.report     = report;
			this.reportNo   = reportNo;
			this.code       = code;
			this.codeNo     = codeNo;
			this.date       = date;
			this.submitNo   = submitNo;
			this.submitDate = submitDate;
			
			// jpcrp040300-q3r-001_E01442-000_2020-01-31_01_2020-03-06.xbrl
			this.string     = String.format("%s-%s-%s_%s-%s_%s_%s_%s.xbrl", form, report, reportNo, code, codeNo, date, submitNo, submitDate);
		}
		
		public static Instance getInstance(String string) {
			Instance ret = ScrapeUtil.get(Instance.class, PAT, string);
			// sanity check
			if (ret != null && !ret.string.equals(string)) {
				logger.error("Unexpected ret");
				logger.error("  string {}", string);
				logger.error("  ret    {}", ret.string);
				throw new UnexpectedException("Unexpected ret");
			}
			return ret;
		}
		
		public File toFile(String docID) {
			return Filename.toFile(docID, string);
		}

		@Override
		public String toString() {
			return string;
		}

		@Override
		public int compareTo(Instance that) {
			int ret = 0;
			if (ret == 0) ret = this.code.compareTo(that.code);
			if (ret == 0) ret = this.codeNo.compareTo(that.codeNo);
			if (ret == 0) ret = this.submitDate.compareTo(that.submitDate);
			if (ret == 0) ret = this.submitNo.compareTo(that.submitNo);
			return ret;
		}
	}
	public static class Honbun implements Comparable<Honbun> {
		// {7 桁数値}_{英字(6 文字)}_jp{府令様式番号}-{報告書略号}-{報告書連番(3 桁)}_{EDINET コード又はファンドコード}-{追番(3 桁)}_{報告対象期間期末日|報告義務発生日}_{報告書提出回数(2 桁)}_{報告書提出日}_ixbrl.htm
		// 0101010_honbun_jpcrp040300-q3r-001_E01442-000_2020-01-31_01_2020-03-06_ixbrl.htm
		private static final Pattern PAT = Pattern.compile("(?<no>[0-9]{7})_(?<title>[a-z]{6})_" + PAT_STRING + "_ixbrl\\.htm");
		
		public String    no;
		public String    title;
		public String    form;
		public Report    report;
		public String    reportNo;
		public String    code;
		public String    codeNo;
		public LocalDate date;
		public String    submitNo;
		public LocalDate submitDate;
		
		@ScrapeUtil.Ignore
		public String    string;

		public Honbun (
			String    no,
			String    title,
			String    form,
			Report    report,
			String    reportNo,
			String    code,
			String    codeNo,
			LocalDate date,
			String    submitNo,
			LocalDate submitDate
			) {
			this.no         = no;
			this.title      = title;
			this.form       = form;
			this.report     = report;
			this.reportNo   = reportNo;
			this.code       = code;
			this.codeNo     = codeNo;
			this.date       = date;
			this.submitNo   = submitNo;
			this.submitDate = submitDate;
			
			// 0101010_honbun_jpcrp040300-q3r-001_E01442-000_2020-01-31_01_2020-03-06_ixbrl.htm
			this.string     = String.format("%s_%s_%s-%s-%s_%s-%s_%s_%s_%s_ixbrl.htm", no, title, form, report, reportNo, code, codeNo, date, submitNo, submitDate);
		}
		
		public static Honbun getInstance(String string) {
			Honbun ret = ScrapeUtil.get(Honbun.class, PAT, string);
			// sanity check
			if (ret != null && !ret.string.equals(string)) {
				logger.error("Unexpected ret");
				logger.error("  string {}", string);
				logger.error("  ret    {}", ret.string);
				throw new UnexpectedException("Unexpected ret");
			}
			return ret;
		}
		
		public File toFile(String docID) {
			return Filename.toFile(docID, string);
		}

		@Override
		public String toString() {
			return string;
		}
		
		@Override
		public int compareTo(Honbun that) {
			int ret = 0;
			if (ret == 0) ret = this.code.compareTo(that.code);
			if (ret == 0) ret = this.codeNo.compareTo(that.codeNo);
			if (ret == 0) ret = this.submitDate.compareTo(that.submitDate);
			if (ret == 0) ret = this.submitNo.compareTo(that.submitNo);
			if (ret == 0) ret = this.no.compareTo(that.no);
			return ret;
		}
	}
}
