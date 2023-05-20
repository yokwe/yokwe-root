package yokwe.stock.jp.edinet;

import java.io.File;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import yokwe.stock.jp.Storage;
import yokwe.stock.jp.edinet.API.DocType;
import yokwe.util.FileUtil;
import yokwe.util.ListUtil;
import yokwe.util.UnexpectedException;

public class Document implements Comparable<Document> {
	private static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();
	
	public static final String PATH_TOUCH_FILE    = Storage.EDINET.getPath("edinet.touch");
	public static void touch() {
		logger.info("touch {}", PATH_TOUCH_FILE);
		FileUtil.touch(PATH_TOUCH_FILE);
	}
	
	public static final String PATH_DOCUMENT_FILE = Storage.EDINET.getPath("document.csv");

	public static List<Document> load() {
		return ListUtil.load(Document.class, PATH_DOCUMENT_FILE);
	}
	private static List<Document> list = null;
	public static List<Document> getList() {
		if (list == null) {
			list = ListUtil.getList(Document.class, PATH_DOCUMENT_FILE);
		}
		return list;
	}
	private static Map<String, Document> documentMap = null;
	//                 docID
	public static Map<String, Document> getDocumentMap() {
		if (documentMap == null) {
			var list = getList();
			documentMap = ListUtil.checkDuplicate(list, o -> o.docID);
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
		ListUtil.save(Document.class, PATH_DOCUMENT_FILE, collection);
	}
	public static void save(List<Document> list) {
		ListUtil.save(Document.class, PATH_DOCUMENT_FILE, list);
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
	
	public static final String PATH_DOCUMENT_DIR = Storage.EDINET.getPath("document");
	
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
