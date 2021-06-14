package yokwe.stock.jp.xbrl.edinet;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import yokwe.stock.jp.edinet.Filename;
import yokwe.stock.jp.edinet.Report;
import yokwe.util.CSVUtil;

public class Manifest implements Comparable<Manifest> {
//	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(Manifest.class);

	private static final String PATH_DATA = EDINET.getPath("manifest.csv");
	public static String getPath() {
		return PATH_DATA;
	}
	
	public static final void save(Collection<Manifest> list) {
		save(new ArrayList<>(list));
	}
	public static final void save(List<Manifest> list) {
		// Sort before write
		Collections.sort(list);
		CSVUtil.write(Manifest.class).file(PATH_DATA, list);
	}
	
	public static List<Manifest> load() {
		return CSVUtil.read(Manifest.class).file(PATH_DATA);
	}
	
	public static List<Manifest> getList() {
		List<Manifest> ret = load();
		if (ret == null) {
			ret = new ArrayList<>();
		}
		return ret;
	}
	
	public String    docID; // S1007V9A
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
	
	public Manifest(String docID, Filename.Honbun honbun) {
		this.docID      = docID;
		this.no         = honbun.no;
		this.title      = honbun.title;
		this.form       = honbun.form;
		this.report     = honbun.report;
		this.reportNo   = honbun.reportNo;
		this.code       = honbun.code;
		this.codeNo     = honbun.codeNo;
		this.date       = honbun.date;
		this.submitNo   = honbun.submitNo;
		this.submitDate = honbun.submitDate;
	}
	public Manifest() {
		this.docID      = null;
		this.no         = null;
		this.title      = null;
		this.form       = null;
		this.report     = null;
		this.reportNo   = null;
		this.code       = null;
		this.codeNo     = null;
		this.date       = null;
		this.submitNo   = null;
		this.submitDate = null;
	}
	
	public Filename.Instance toInstance() {
		return new Filename.Instance(form, report, reportNo, code, codeNo, date, submitNo, submitDate);
	}
	public Filename.Honbun toHonbun() {
		return new Filename.Honbun(no, title, form, report, reportNo, code, codeNo, date, submitNo, submitDate);
	}
	
	@Override
	public int compareTo(Manifest that) {
		int ret = 0;
		if (ret == 0) ret = this.docID.compareTo(that.docID);
		if (ret == 0) ret = this.no.compareTo(that.no);
		if (ret == 0) ret = this.title.compareTo(that.title);
		if (ret == 0) ret = this.form.compareTo(that.form);
		if (ret == 0) ret = this.report.compareTo(that.report);
		if (ret == 0) ret = this.reportNo.compareTo(that.reportNo);
		if (ret == 0) ret = this.code.compareTo(that.code);
		if (ret == 0) ret = this.codeNo.compareTo(that.codeNo);
		if (ret == 0) ret = this.date.compareTo(that.date);
		if (ret == 0) ret = this.submitNo.compareTo(that.submitNo);
		if (ret == 0) ret = this.submitDate.compareTo(that.submitDate);
		return ret;
	}
	
	@Override
	public boolean equals(Object o) {
		if (o == null) {
			return false;
		} else {
			if (o instanceof Manifest) {
				Manifest that = (Manifest)o;
				return this.compareTo(that) == 0;
			} else {
				return false;
			}
		}
	}

}
