package yokwe.stock.jp.edinet;

import java.io.File;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import yokwe.util.FileUtil;
import yokwe.util.UnexpectedException;

public class DataFile {
	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(DataFile.class);
	
	private static void update(LocalDate lastDate) {
		LocalDate date  = LocalDate.now();
		int       count = 0;
		
		Map<String, Document> map = Document.getMap();
		
		for(;;) {
			API.ListDocument.Response response = API.ListDocument.getInstance(date, API.ListDocument.Type.DATA);
			logger.info("{}  {}", date, String.format("%4d", response.results.length));
			for(var e: response.results) {
				if (e.edinetCode.isEmpty()) {
					//
				} else {
					if (e.docTypeCode == null) {
						logger.error("docTypeCode is null");
						logger.error("  {}", e.toString());
						logger.error("e.edinetCode {}", e.edinetCode);
						throw new UnexpectedException("docTypeCode is null");
					}
					
					String docID = e.docID;
					if (!map.containsKey(docID)) {
						Document document = new Document(
								e.docID,
								e.edinetCode,
								e.stockCode,
								e.fundCode,
								e.ordinanceCode,
								e.formCode,

								e.docTypeCode,

								e.submitDateTime);
						map.put(docID, document);

						count++;
						if ((count % 10000) == 0) {
							List<Document> list = new ArrayList<>(map.values());
							logger.info("save {} {}", list.size(), Document.PATH_DOCUMENT_FILE);
							Document.save(list);
						}
					}
				}
			}
			
			date = date.minusDays(1);
			if (date.equals(lastDate)) break;
		}
		
		logger.info("update {}", count);
		if (0 < count) {
			List<Document> list = new ArrayList<>(map.values());
			logger.info("save {} {}", list.size(), Document.PATH_DOCUMENT_FILE);
			Document.save(map.values());
		}
	}
	
	private static void download(LocalDate lastDate) {
		// Existing file map.  key is docID
		Map<String, File> documentFileMap = Document.getDocumentFileMap();
		logger.info("documentFileMap  {}", documentFileMap.size());
		
		List<Document> list = new ArrayList<>();
		for(var e: Document.load()) {
			if (documentFileMap.containsKey(e.docID)) continue;
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
				
		logger.info("list   {}", list.size());
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

		logger.info("update {}", count);
		if (0 < count) {
			Document.touch();
		}

	}

	public static void main(String[] args) {
		logger.info("START");
		
		int days = Integer.getInteger("days", 10);
		logger.info("days {}", days);
		
		LocalDate date  = LocalDate.now();
		LocalDate lastDate = date.minusDays(days);
		logger.info("date {} - {}", lastDate, date);

		logger.info("update");
		update(lastDate);
		logger.info("download");
		download(lastDate);

		logger.info("STOP");
	}

}
