package yokwe.stock.jp.edinet;

import java.io.File;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.slf4j.LoggerFactory;

import yokwe.util.FileUtil;

public class DownloadDocument {
	static final org.slf4j.Logger logger = LoggerFactory.getLogger(DownloadDocument.class);

	public static final int DEFAULT_DOWNLOAD_LIMIT_YEAR = 1;
	
	public static void main(String[] args) {
		logger.info("START");
		
		int days = Integer.getInteger("days", 10);
		logger.info("days {}", days);
		
		LocalDate date  = LocalDate.now();
		LocalDate lastDate = date.minusDays(days);
		logger.info("date {} - {}", lastDate, date);
		
		// Existing file map.  key is docID
		Map<String, File> dataFileMap = Document.getDocumentFileMap();
		logger.info("dataFileMap  {}", dataFileMap.size());
		
		List<Document> list = new ArrayList<>();
		for(var e: Document.getList()) {
			if (dataFileMap.containsKey(e.docID)) continue;
			if (e.fundCode.isEmpty() && e.stockCode.isEmpty()) continue;
			if (e.submitDateTime.toLocalDate().isBefore(lastDate)) continue;
			
			switch(e.docTypeCode) {
			case ANNUAL_REPORT:
			case QUARTERLY_REPORT:
			case SEMI_ANNUAL_REPORT:
				list.add(e);
				break;
			default:
				break;
			}
		}
				
		logger.info("list {}", list.size());
		Collections.shuffle(list);
		
		int count = 0;
		for(var e: list) {
			if ((count % 100) == 0) {
				logger.info("{} {}", String.format("%5d / %5d", count, list.size()), e.docID);
			}
			count++;
			
			byte[] data = API.Document.getInstance(e.docID, API.Document.Type.WHOLE);
			if (data == null) {
				logger.info("download failed {}", e.docID);
			} else {
				File file = Document.getDocumentFile(e.submitDateTime.toLocalDate(), e.docID);
				FileUtil.rawWrite().file(file, data);
			}
		}

		if (0 < count) {
			Document.touch();
		}
		
		logger.info("STOP");
	}
}
