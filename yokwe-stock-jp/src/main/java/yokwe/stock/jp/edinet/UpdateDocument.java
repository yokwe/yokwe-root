package yokwe.stock.jp.edinet;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.slf4j.LoggerFactory;

import yokwe.util.UnexpectedException;
import yokwe.stock.jp.edinet.API.ListDocument;

public class UpdateDocument {
	static final org.slf4j.Logger logger = LoggerFactory.getLogger(UpdateDocument.class);

	public static void main(String[] args) {
		logger.info("START");
		
		Map<String, Document> map = Document.getMap();
		
		int days = Integer.getInteger("days", 10);
		logger.info("days {}", days);
		
		LocalDate date  = LocalDate.now();
		LocalDate lastDate = date.minusDays(days);
		logger.info("date {} - {}", lastDate, date);

		
		int countUpdate = 0;
		for(;;) {
			ListDocument.Response response = ListDocument.getInstance(date, ListDocument.Type.DATA);
			logger.info("{}  {}", date, String.format("%4d", response.results.length));
			for(ListDocument.Result e: response.results) {
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

						countUpdate++;
						if ((countUpdate % 10000) == 0) {
							List<Document> list = new ArrayList<>(map.values());
							logger.info("save {} {}", Document.PATH_DOCUMENT_FILE, list.size());
							Document.save(list);
						}
					}
				}
			}
			
			date = date.minusDays(1);
			if (date.equals(lastDate)) break;
		}
		
		{
			logger.info("countUpdate {}", countUpdate);
			if (0 < countUpdate) {
				List<Document> list = new ArrayList<>(map.values());
				logger.info("save {} {}", Document.PATH_DOCUMENT_FILE, list.size());
				Document.save(list);
			}
		}
		
		logger.info("STOP");
	}
}
