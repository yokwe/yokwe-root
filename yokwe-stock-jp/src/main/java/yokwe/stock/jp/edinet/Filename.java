package yokwe.stock.jp.edinet;

import java.time.LocalDate;
import java.util.regex.Pattern;

import yokwe.util.ScrapeUtil;
import yokwe.util.UnexpectedException;

public class Filename {
	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(Filename.class);

	public static class Manifest {
		public static final String NAME = "manifest_PublicDoc.xml";
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

		@Override
		public String toString() {
			return string;
		}

		@Override
		public int compareTo(Instance that) {
			int ret = this.code.compareTo(that.code);
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
		
		@Override
		public String toString() {
			return string;
		}
		
		@Override
		public int compareTo(Honbun that) {
			int ret = this.no.compareTo(that.no);
			return ret;
		}
	}
}