package yokwe.stock.jp.edinet;

import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.slf4j.LoggerFactory;

import yokwe.util.UnexpectedException;
import yokwe.util.StringUtil;

public class InstanceFilename implements Comparable<InstanceFilename> {
	private static final org.slf4j.Logger logger = LoggerFactory.getLogger(InstanceFilename.class);

	// jp{府令略号}{様式番号}-{報告書略号}-{報告書連番(3 桁)}_{EDINET コード又はファンドコード}-{追番(3 桁)}_{報告対象期間期末日|報告義務発生日}_{報告書提出回数(2 桁)}_{報告書提出日}.xbrl 
	// jp crp 040300     -q3r        -001             _E01442                      -000        _2020-01-31                   _01                 _2020-03-06.xbrl
	// jpcrp040300-q3r-001_E01442-000_2020-01-31_01_2020-03-06.xbrl
	
	private static final Pattern PAT = Pattern.compile("jp(?<ordinance>[a-z]+)(?<form>[0-9]{6})-(?<report>.+?)-(?<reportNo>[0-9]{3})_(?<code>[EG][0-9]{5})-(?<extraNo>[0-9]{3})_(?<date>20[0-9]{2}-[01][0-9]-[0-3][0-9])_(?<submitNo>[0-9]{2})_(?<submitDate>20[0-9]{2}-[01][0-9]-[0-3][0-9]).xbrl");

	private static final StringUtil.MatcherFunction<InstanceFilename> OP = (m -> new InstanceFilename(
			m.group("ordinance"),
			m.group("form"),
			m.group("report"),
			m.group("reportNo"),
			m.group("code"),
			m.group("extraNo"),
			m.group("date"),
			m.group("submitNo"),
			m.group("submitDate")));

	public final String  ordinance;
	public final String  form;
	public final Report  report;
	public final String  reportNo;
	public final String  code;
	public final String  extraNo;
	public final String  date;
	public final String  submitNo;
	public final String  submitDate;
	public final String  string;
	
	public InstanceFilename(String ordinance, String form, String report, String reportNo, String code, String extraNo, String date, String submitNo, String submitDate) {
		this.ordinance  = ordinance;
		this.form       = form;
		this.report     = Report.getInstance(report);
		this.reportNo   = reportNo;
		this.code       = code;
		this.extraNo    = extraNo;
		this.date       = date;
		this.submitNo   = submitNo;
		this.submitDate = submitDate;
		
		this.string = String.format("jp%s%s-%s-%s_%s-%s_%s_%s_%s.xbrl", ordinance, form, report, reportNo, code, extraNo, date, submitNo, submitDate);
	}
	
	public static InstanceFilename getInstance(String string) {
		List<InstanceFilename> list = StringUtil.find(string, PAT, OP).collect(Collectors.toList());
		if (list.size() == 0) return null;
		if (list.size() == 1) return list.get(0);
		logger.error("Unexpected value %s!", list);
		throw new UnexpectedException("Unexpected value");
	}
	
	@Override
	public String toString() {
		return string;
	}
	@Override
	public int compareTo(InstanceFilename that) {
		// jpcrp040300-q3r-001_E01442-000_2020-01-31_01_2020-03-06.xbrl
		//                     111111 444            33 2222222222
		int ret = this.code.compareTo(that.code);
		if (ret == 0) ret = this.submitDate.compareTo(that.submitDate);
		if (ret == 0) ret = this.submitNo.compareTo(submitNo);
		if (ret == 0) ret = this.extraNo.compareTo(extraNo);
		return ret;
	}
	@Override
	public boolean equals(Object o) {
		if (o == null) return false;
		if (o instanceof InstanceFilename) {
			InstanceFilename that = (InstanceFilename)o;
			return this.string.equals(that.string);
		} else {
			return false;
		}
	}
}
