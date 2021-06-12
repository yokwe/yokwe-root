package yokwe.stock.jp.xbrl.edinet.manifest;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import yokwe.stock.jp.edinet.Filename;
import yokwe.stock.jp.edinet.Report;
import yokwe.stock.jp.xbrl.edinet.EDINET;
import yokwe.util.CSVUtil;

public class ManifestInfo implements Comparable<ManifestInfo> {
	private static final String PATH_DATA = EDINET.getPath("manifest-info.csv");
	public static String getPath() {
		return PATH_DATA;
	}
	
	public static final void save(List<ManifestInfo> list) {
		// Sort before write
		Collections.sort(list);
		CSVUtil.write(ManifestInfo.class).file(PATH_DATA, list);
	}
	
	public static List<ManifestInfo> load() {
		return CSVUtil.read(ManifestInfo.class).file(PATH_DATA);
	}
	
	private static List<ManifestInfo> list = null;
	public static List<ManifestInfo> getList() {
		if (list == null) {
			list = load();
			if (list == null) {
				list = new ArrayList<>();
			}
		}
		return list;
	}
	
	private static Map<String, List<ManifestInfo>> map = null;
	//                 docID
	public static Map<String, List<ManifestInfo>> getMap() {
		if (map == null) {
			map = new TreeMap<>();
			for(var e: getList()) {
				List<ManifestInfo> list;
				if (map.containsKey(e.docID)) {
					list = map.get(e.docID);
				} else {
					list = new ArrayList<>();
					map.put(e.docID, list);
				}
				list.add(e);
			}
		}
		return map;
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
	
	public ManifestInfo(String docID, Filename.Honbun honbun) {
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
	public ManifestInfo() {
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
	
	@Override
	public int compareTo(ManifestInfo that) {
		int ret = 0;
		if (ret == 0) ret = this.docID.compareTo(that.docID);
		if (ret == 0) ret = this.no.compareTo(that.no);
		return ret;
	}

}
