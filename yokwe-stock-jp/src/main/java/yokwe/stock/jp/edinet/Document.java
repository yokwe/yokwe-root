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

import yokwe.stock.jp.edinet.API.DocType;
import yokwe.util.CSVUtil;
import yokwe.util.FileUtil;
import yokwe.util.UnexpectedException;

public class Document implements Comparable<Document> {
	static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(Document.class);
	
	public static final String PATH_TOUCH_FILE = EDINET.getPath("edinet.touch");
	public static void touch() {
		logger.info("touch {}", PATH_TOUCH_FILE);
		FileUtil.touch(PATH_TOUCH_FILE);
	}
	
	public static final String PATH_DOCUMENT_DIR = EDINET.getPath("document");
	
	public static File getDocumentFile(LocalDate date, String docID) {
		int y = date.getYear();
		int m = date.getMonthValue();
		int d = date.getDayOfMonth();
		String path = String.format("%s/%04d/%02d/%02d/%s", PATH_DOCUMENT_DIR, y, m, d, docID);
		return new File(path);
	}
	
	private static List<File> docmentFileList = null;
	public static List<File> getDocumentFileList() {
		if (docmentFileList == null) {
			docmentFileList = FileUtil.listFile(PATH_DOCUMENT_DIR).stream().
					collect(Collectors.toList());
		}
		return docmentFileList;
	}
	private static Map<String, File> documentFileMap = null;
	//                 docID
	public static Map<String, File> getDocumentFileMap() {
		if (documentFileMap == null) {
			documentFileMap = new TreeMap<>();
			for(File file: getDocumentFileList()) {
				String name = file.getName();
				documentFileMap.put(name, file);
			}
		}
		return documentFileMap;
	}


	public static final String PATH_DOCUMENT_FILE     = EDINET.getPath("document.csv");

	public static List<Document> load() {
		List<Document> ret = CSVUtil.read(Document.class).file(PATH_DOCUMENT_FILE);
		return ret;
	}
	private static List<Document> list = null;
	public static List<Document> getList() {
		if (list == null) {
			list = load();
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
		CSVUtil.write(Document.class).file(PATH_DOCUMENT_FILE, list);
	}
	
	public static final String PATH_XBRL_DIR = EDINET.getPath("xbrl");
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
