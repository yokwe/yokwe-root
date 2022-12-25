package yokwe.stock.jp.tdnet;

import java.io.File;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import yokwe.stock.jp.Storage;
import yokwe.util.FileUtil;
import yokwe.util.ListUtil;
import yokwe.util.UnexpectedException;

public class Release implements Comparable<Release> {
	private static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();
	
	private static final String PATH_RELEASE_DIR = Storage.TDNET.getPath("release");
	public static String getReleaseDir() {
		return PATH_RELEASE_DIR;
	}
	private static List<File> releaseFileList = null;
	public static List<File> getReleaseFileList() {
		if (releaseFileList == null) {
			releaseFileList = FileUtil.listFile(getReleaseDir()).stream().
					filter(o -> o.getName().endsWith(".pdf") || o.getName().endsWith(".zip")).
					collect(Collectors.toList());
		}
		return releaseFileList;
	}
	public static File getReleaseFile(LocalDate date, String filename) {
		int y = date.getYear();
		int m = date.getMonthValue();
		int d = date.getDayOfMonth();
		String path = String.format("%s/%04d/%02d/%02d/%s", PATH_RELEASE_DIR, y, m, d, filename);
		return new File(path);
	}
	
	
	private static final String PATH_FILE = Storage.TDNET.getPath("release.csv");
	public static String getPath() {
		return PATH_FILE;
	}

	public static void save(Collection<Release> collection) {
		List<Release> list = new ArrayList<>(collection);
		save(list);
	}
	public static void save(List<Release> list) {
		ListUtil.save(Release.class, getPath(), list);
	}
	
	public static List<Release> load() {
		return ListUtil.load(Release.class, getPath());
	}
	private static List<Release> list = null;
	public static List<Release> getList() {
		if (list == null) {
			list = ListUtil.getList(Release.class, getPath());
		}
		return list;
	}
	private static Map<String, Release> map = null;
	//                 id
	public static Map<String, Release> getMap() {
		//            id
		if (map == null) {
			var list = getList();
			ListUtil.checkDuplicate(list, o -> o.id);
			map = list.stream().collect(Collectors.toMap(o -> o.id, o -> o));
		}
		return map;
	}


	public LocalDateTime dateTime;
	public String id;
	public String pdf;
	public String xbrl;
	public String code;   // FIXME not stockCode
	public String title;

	public String name;
	public String place;
	public String history;
	
	public Release() {
		this.dateTime= null;
		this.id      = null;
		this.pdf     = null;
		this.xbrl    = null;
		this.code    = null;
		this.title   = null;
		
		this.name    = null;
		this.place   = null;
		this.history = null;
	}
	
	public Release(LocalDateTime dateTime, String code, String name, String pdf, String title, String xbrl, String place, String history) {
		// sanity check
		{
			if (!pdf.endsWith(".pdf")) {
				logger.error("Unexpected pdf");
				logger.error("  pdf {}", pdf);
				throw new UnexpectedException("Unexpected pdf");
			}
			if (xbrl != null && !xbrl.endsWith(".zip")) {
				logger.error("Unexpected xbrl");
				logger.error("  xbrl {}", xbrl);
				throw new UnexpectedException("Unexpected xbrl");
			}
		}
		
		String pdfID = pdf.replace(".pdf", "").substring(4);
		// sanity check
		{
			if (xbrl != null) {
				String zipID = xbrl.replace(".zip", "").substring(4);

				if (!pdfID.equals(zipID)) {
					logger.error("id mismatch");
					logger.error("  pdfID {}", pdfID);
					logger.error("  zipID {}", zipID);
					throw new UnexpectedException("id mismatch");
				}
			}
		}
		
		this.dateTime = dateTime;
		this.id       = pdfID;
		this.pdf      = pdf;
		this.xbrl     = (xbrl == null) ? "" : xbrl;
		this.code     = code;
		this.title    = title;

		this.name     = name;
		this.place    = place;
		this.history  = history;
	}
	@Override
	public String toString() {
		return String.format("{%s %s %s %22s %s %s}", dateTime, id, pdf, xbrl, code, title);
//		return String.format("{%s %s %s %s %s %s %s %s %s}", id, pdf, xbrl, title, time, code, name, place, history);
	}
	
	@Override
	public boolean equals(Object o) {
		if (o == null) return false;
		if (o instanceof Release) {
			Release that = (Release)o;
			return this.id.equals(that.id);
		} else {
			return false;
		}
	}
	
	@Override
	public int compareTo(Release that) {
		int ret = this.dateTime.compareTo(that.dateTime);
		if (ret == 0) ret = this.id.compareTo(that.id);
		
		// sanity check
		if (ret == 0) {
			logger.error("Duplicate key");
			logger.error("  this {}", this);
			logger.error("  that {}", that);
			throw new UnexpectedException("Duplicate key");
		}
		return ret;
	}
}
