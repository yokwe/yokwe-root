package yokwe.stock.jp.edinet;

import java.io.File;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.LoggerFactory;

import yokwe.stock.jp.edinet.API.DocType;
import yokwe.util.FileUtil;

public class DownloadDocument {
	static final org.slf4j.Logger logger = LoggerFactory.getLogger(DownloadDocument.class);

	public static final int DEFAULT_DOWNLOAD_LIMIT_YEAR = 1;
	
	public static void main(String[] args) {
		logger.info("START");
		
		int downloadLimitYear = Integer.getInteger("downloadLimitYear", DEFAULT_DOWNLOAD_LIMIT_YEAR);
		logger.info("downloadLimitYear {}", downloadLimitYear);
		
		// Existing file map.  key is docID
		Map<String, File> dataFileMap = Document.getDataFileMap();
		logger.info("dataFileMap  {}", dataFileMap.size());
		
		LocalDate dateStart = LocalDate.now().minusYears(downloadLimitYear);
		
		List<Document> documentList = Document.getList().stream().
				filter(o -> (o.docTypeCode == DocType.ANNUAL_REPORT    ||
				             o.docTypeCode == DocType.QUARTERLY_REPORT ||
				             o.docTypeCode == DocType.SEMI_ANNUAL_REPORT)).
				filter(o -> o.submitDateTime.toLocalDate().isAfter(dateStart)).
				filter(o -> !(o.fundCode.isEmpty() && o.stockCode.isEmpty())). // Skip if no fundCode and no stockCode
				filter(o -> !dataFileMap.containsKey(o.docID)).
				collect(Collectors.toList());
		logger.info("documentList {}", documentList.size());
		Collections.shuffle(documentList);
		
		int count = 0;
		for(Document document: documentList) {
			if ((count % 100) == 0) {
				logger.info("{} {}", String.format("%5d / %5d", count, documentList.size()), document.docID);
			}
			count++;
			
			byte[] data = API.Document.getInstance(document.docID, API.Document.Type.WHOLE);
			if (data == null) {
				logger.info("download failed {}", document.docID);
			} else {
				File file = Document.getDataFile(document.submitDateTime.toLocalDate(), document.docID);
				FileUtil.rawWrite().file(file, data);
			}
		}

		if (0 < count) {
			Document.touch();
		}
		
		logger.info("STOP");
	}
}
