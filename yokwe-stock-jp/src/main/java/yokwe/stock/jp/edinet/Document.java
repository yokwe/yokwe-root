package yokwe.stock.jp.edinet;

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

import yokwe.util.UnexpectedException;
import yokwe.stock.jp.edinet.API.DocType;
import yokwe.util.CSVUtil;
import yokwe.util.FileUtil;

public class Document implements Comparable<Document> {
	static final org.slf4j.Logger logger = LoggerFactory.getLogger(Document.class);
	
	public static final String PATH_TOUCH_FILE = "tmp/data/edinet.touch";
	public static void touch() {
		logger.info("touch {}", PATH_TOUCH_FILE);
		FileUtil.touch(PATH_TOUCH_FILE);
	}
	
	public static final String PATH_DATA_DIR = "tmp/disclosure";
	
	public static File getDataFile(LocalDate date, String docID) {
		int y = date.getYear();
		int m = date.getMonthValue();
		int d = date.getDayOfMonth();
		String path = String.format("%s/%04d/%02d/%02d/%s", PATH_DATA_DIR, y, m, d, docID);
		return new File(path);
	}
	
	private static List<File> dataFileList = null;
	public static List<File> getDataFileList() {
		if (dataFileList == null) {
			dataFileList = FileUtil.listFile(PATH_DATA_DIR).stream().
					collect(Collectors.toList());
		}
		return dataFileList;
	}
	private static Map<String, File> dataFileMap = null;
	//                 docID
	public static Map<String, File> getDataFileMap() {
		if (dataFileMap == null) {
			dataFileMap = new TreeMap<>();
			for(File file: getDataFileList()) {
				String name = file.getName();
				dataFileMap.put(name, file);
			}
		}
		return dataFileMap;
	}


	public static final String PATH_FILE     = "tmp/data/edinet-document.csv";

	private static List<Document> list = null;
	public static List<Document> getList() {
		if (list == null) {
			list = CSVUtil.read(Document.class).file(PATH_FILE);
			if (list == null) {
				list = new ArrayList<>();
			}
		}
		return list;
	}
	private static Map<String, Document> map = null;
	//                 docID
	public static Map<String, Document> getMap() {
		if (map == null) {
			map = new TreeMap<>();
			for(Document e: getList()) {
				String key = e.docID;
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
	public static void save(Collection<Document> collection) {
		List<Document> list = new ArrayList<>(collection);
		save(list);
	}
	public static void save(List<Document> list) {
		// Sort before save
		Collections.sort(list);
		CSVUtil.write(Document.class).file(PATH_FILE, list);
	}
	
	public static final String PATH_XBRL_DIR = "tmp/data/edinet";
	public static File getXBRLFile(InstanceFilename filename) {
		String path = String.format("%s/%s/%s", PATH_XBRL_DIR, filename.code, filename.toString());
		return new File(path);
	}


	public LocalDateTime submitDateTime;	
	public String        docID;
	public String        edinetCode;
	public DocType       docTypeCode;
	public String        ordinanceCode;
	public String        formCode;
	
	public String        fundCode;
	public String        stockCode;

	public Document(		
		String        docID,
		String        edinetCode,
		String        stockCode,
		String        fundCode,
		String        ordinanceCode,
		String        formCode,

		DocType       docTypeCode,

		LocalDateTime submitDateTime
		) {
		this.docID          = docID;
		this.edinetCode     = edinetCode;
		this.stockCode      = stockCode;
		this.fundCode       = fundCode;
		this.ordinanceCode  = ordinanceCode;
		this.formCode       = formCode;

		this.docTypeCode    = docTypeCode;

		this.submitDateTime = submitDateTime;
	}
	public Document() {
		this(null, null, null, null, null, null, null, null);
	}
	
	@Override
	public int compareTo(Document that) {
		return this.submitDateTime.compareTo(that.submitDateTime);
	}
}
