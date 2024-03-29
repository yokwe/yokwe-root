package yokwe.stock.jp.edinet;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

import yokwe.stock.jp.Storage;
import yokwe.stock.jp.edinet.Filename.Report;
import yokwe.util.ListUtil;

public class Manifest implements Comparable<Manifest> {
	private static final String PATH_FILE = Storage.EDINET.getPath("manifest.csv");
	public static final String getPath() {
		return PATH_FILE;
	}
	
	public static final void save(Collection<Manifest> collection) {
		ListUtil.save(Manifest.class, getPath(), collection);
	}
	public static final void save(List<Manifest> list) {
		ListUtil.save(Manifest.class, getPath(), list);
	}
	
	public static List<Manifest> load() {
		return ListUtil.load(Manifest.class, getPath());
	}
	
	public static List<Manifest> getList() {
		return ListUtil.getList(Manifest.class, getPath());
	}
	
	public LocalDate downloadDate;
	public Integer	 seqNumber;
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
	
	public Manifest(LocalDate downloadDate, Integer seqNumber, String docID, Filename.Honbun honbun) {
		this.downloadDate = downloadDate;
		this.seqNumber    = seqNumber;
		this.docID        = docID;
		this.no           = honbun.no;
		this.title        = honbun.title;
		this.form         = honbun.form;
		this.report       = honbun.report;
		this.reportNo     = honbun.reportNo;
		this.code         = honbun.code;
		this.codeNo       = honbun.codeNo;
		this.date         = honbun.date;
		this.submitNo     = honbun.submitNo;
		this.submitDate   = honbun.submitDate;
	}
	public Manifest(Document document, Filename.Honbun honbun) {
		this(document.downloadDate, document.seqNumber, document.docID, honbun);
	}
	public Manifest() {
		this.downloadDate = null;
		this.seqNumber    = null;
		this.docID        = null;
		this.no           = null;
		this.title        = null;
		this.form         = null;
		this.report       = null;
		this.reportNo     = null;
		this.code         = null;
		this.codeNo       = null;
		this.date         = null;
		this.submitNo     = null;
		this.submitDate   = null;
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
		if (ret == 0) ret = this.downloadDate.compareTo(that.downloadDate);
		if (ret == 0) ret = this.seqNumber.compareTo(that.seqNumber);
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
