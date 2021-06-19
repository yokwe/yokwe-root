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
	private static Map<String, Document> documentMap = null;
	//                 docID
	public static Map<String, Document> getDocumentMap() {
		if (documentMap == null) {
			documentMap = new TreeMap<>();
			for(Document e: getList()) {
				String key = e.docID;
				if (documentMap.containsKey(key)) {
					logger.error("Duplicate key {}", key);
					logger.error("  old {}", documentMap.get(key));
					logger.error("  new {}", e);
					throw new UnexpectedException("Duplicate key");
				} else {
					documentMap.put(key, e);
				}
			}
		}
		return documentMap;
	}
	public static Document getDocument(String docID) {
		Map<String, Document> map = getDocumentMap();
		if (map.containsKey(docID)) {
			return map.get(docID);
		} else {
			logger.error("Unknown docID");
			logger.error("  docID {}", docID);
			throw new UnexpectedException("Unknown docID");
		}
	}
	public static File getFile(String docID) {
		return getDocument(docID).toFile();
	}
	public static LocalDate getDownloadDate(String docID) {
		return getDocument(docID).downloadDate;
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
	
	public LocalDate	 downloadDate;
	public Integer  	 seqNumber;
	
	public LocalDateTime submitDateTime;	
	public String        docID;
	public String        edinetCode;
	
	public Boolean       xbrlFlag;
	
	public DocType       docTypeCode;
	public String        ordinanceCode;
	public String        formCode;
	
	public String        fundCode;
	public String        stockCode;
	
	public String        docDescription;

	public Document(
		LocalDate     downloadDate,
		Integer       seqNumber,
			
		String        docID,
		String        edinetCode,
		
		Boolean       xbrlFlag,
		
		String        stockCode,
		String        fundCode,
		String        ordinanceCode,
		String        formCode,

		DocType       docTypeCode,

		LocalDateTime submitDateTime,
		
		String        docDescription
		) {
		this.downloadDate   = downloadDate;
		this.seqNumber      = seqNumber;
		
		this.docID          = docID;
		this.edinetCode     = edinetCode;
		
		this.xbrlFlag       = xbrlFlag;
		
		this.stockCode      = stockCode;
		this.fundCode       = fundCode;
		this.ordinanceCode  = ordinanceCode;
		this.formCode       = formCode;

		this.docTypeCode    = docTypeCode;

		this.submitDateTime = submitDateTime;
		
		this.docDescription = docDescription;
	}
	public Document() {
		this(null, null, null, null, null, null, null, null, null, null, null, null);
	}
	
	public String docID() {
		return this.docID;
	}
	
	public static final String PATH_DOCUMENT_DIR = EDINET.getPath("document");
	
	public File toFile() {
		String path = String.format("%s/%04d/%02d/%02d/%s",
			PATH_DOCUMENT_DIR, downloadDate.getYear(), downloadDate.getMonthValue(), downloadDate.getDayOfMonth(), docID);
		return new File(path);
	}
	
	@Override
	public int compareTo(Document that) {
		int ret = 0;
		if (ret == 0) ret = this.downloadDate.compareTo(that.downloadDate);
		if (ret == 0) ret = this.seqNumber.compareTo(that.seqNumber);
		return ret;
	}
}
