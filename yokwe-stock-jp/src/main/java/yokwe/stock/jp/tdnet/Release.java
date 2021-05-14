package yokwe.stock.jp.tdnet;

import java.io.File;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

import org.slf4j.LoggerFactory;

import yokwe.util.CSVUtil;
import yokwe.util.FileUtil;
import yokwe.util.UnexpectedException;

public class Release implements Comparable<Release> {
	static final org.slf4j.Logger logger = LoggerFactory.getLogger(Release.class);
	
	private static final String PATH_DATA_DIR = getDataDir();
	public static String getDataDir() {
		return String.format("%s/release", TDNET.getPath());
	}
	private static List<File> dataFileList = null;
	public static List<File> getDataFileList() {
		if (dataFileList == null) {
			dataFileList = FileUtil.listFile(PATH_DATA_DIR).stream().
					filter(o -> o.getName().endsWith(".pdf") || o.getName().endsWith(".zip")).
					collect(Collectors.toList());
		}
		return dataFileList;
	}
	public static File getDataFile(LocalDate date, String filename) {
		int y = date.getYear();
		int m = date.getMonthValue();
		int d = date.getDayOfMonth();
		String path = String.format("%s/%04d/%02d/%02d/%s", PATH_DATA_DIR, y, m, d, filename);
		return new File(path);
	}
	
	
	private static final String PATH_FILE = getPath();
	public static String getPath() {
		return String.format("%s/release.csv", TDNET.getPath());
	}

	public static void save(Collection<Release> collection) {
		List<Release> list = new ArrayList<>(collection);
		save(list);
	}
	public static void save(List<Release> list) {
		// Sort before save
		Collections.sort(list);
		CSVUtil.write(Release.class).file(PATH_FILE, list);
	}
	
	public static List<Release> load() {
		return CSVUtil.read(Release.class).file(PATH_FILE);
	}
	private static List<Release> list = null;
	public static List<Release> getList() {
		if (list == null) {
			list = load();
		}
		return list;
	}
	private static Map<String, Release> map = null;
	// key is id
	public static Map<String, Release> getMap() {
		if (map == null) {
			map = new TreeMap<>();
			List<Release> list = getList();
			if (list == null) return map;
			
			for(Release e: list) {
				String key = e.id;
				if (map.containsKey(key)) {
					logger.error("Duplicate key {}", key);
					logger.error("  old {}", map.get(key));
					logger.error("  new {}", e);
					throw new UnexpectedException("Duplicate key");
				} else {
					map.put(key, e);
				}
			}
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
