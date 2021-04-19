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

	public static final int DEFAULT_PERIOD = 7;
	
	public static void main(String[] args) {
		logger.info("START");
		
		Integer updatePeriod = Integer.getInteger("updatePeriod", DEFAULT_PERIOD);
		logger.info("updatePeriod {}", updatePeriod);

		Map<String, Document> map = Document.getMap();
		
		LocalDate today = LocalDate.now();
//		LocalDate date = today.minusYears(5).plusDays(1);
		LocalDate date = today.minusDays(updatePeriod);
		
		int countUpdate = 0;
		for(;;) {
			if (date.isAfter(today)) break;
			
			ListDocument.Response response = ListDocument.getInstance(date, ListDocument.Type.DATA);
			logger.info("{}  {}", date, String.format("%4d", response.results.length));
//			logger.info("response {}", response);
			for(ListDocument.Result e: response.results) {
				if (e.edinetCode.isEmpty()) continue;
				
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
						logger.info("save {} {}", Document.PATH_FILE, list.size());
						Document.save(list);
					}
				}
			}
			date = date.plusDays(1);
		}
		
		{
			logger.info("countUpdate {}", countUpdate);
			if (0 < countUpdate) {
				List<Document> list = new ArrayList<>(map.values());
				logger.info("save {} {}", Document.PATH_FILE, list.size());
				Document.save(list);
			}
		}
		
		logger.info("STOP");
	}
}
