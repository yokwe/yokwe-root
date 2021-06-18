package yokwe.stock.jp.edinet;

import java.io.File;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import yokwe.stock.jp.edinet.API.Disclose;
import yokwe.stock.jp.edinet.API.Withdraw;
import yokwe.util.FileUtil;
import yokwe.util.UnexpectedException;

public class DataFile {
	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(DataFile.class);
	
	private static void update(LocalDate lastDate) {
		Map<String, Document> map = Document.getDocumentMap();
		Set<LocalDate> dateSet = map.values().stream().map(o -> o.submitDateTime.toLocalDate()).collect(Collectors.toSet());
		
		int count = 0;
		LocalDate today = LocalDate.now();
		
		for(LocalDate date  = today; date.isAfter(lastDate); date = date.minusDays(1)) {
			// Skip if date is appeared in dateSet
			if (dateSet.contains(date)) {
				// special handling of today. Because there will be update for today.
				if (date.isBefore(today)) continue;
			}
			
			API.ListDocument.Response response = API.ListDocument.getInstance(date, API.ListDocument.Type.DATA);
			if (response.results.length != 0) {
				logger.info("{}  {}", date, String.format("%4d", response.results.length));
			}
			
			for(var e: response.results) {
				// skip if edinetCode is empty and withdrawStatsu is normal (expired document)
				if (e.edinetCode.isEmpty() && e.withdrawalStatus == Withdraw.NORMAL) continue;

				// skip if withdrawalStatus is not NORMAL
				if (!(e.withdrawalStatus == Withdraw.NORMAL)) continue;
				
				// skip if discloseStatus is not NORMAL or not DISCLOSE
				if (!(e.disclosureStatus == Disclose.NORMAL || e.disclosureStatus == Disclose.DISCLOSE)) continue;
				
				if (e.edinetCode.isEmpty()) {
					logger.error("edinetCode is empty");
					logger.error("  e {}", e.toString());
					throw new UnexpectedException("edinetCode is empty");
				}
				
				String docID = e.docID;
				if (!map.containsKey(docID)) {
					Document document = new Document(
						e.docID,
						e.edinetCode,
						
						e.xbrlFlag.toBoolean(),
						
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
		
		logger.info("update {}", count);
		if (0 < count) {
			List<Document> list = new ArrayList<>(map.values());
			logger.info("save {} {}", list.size(), Document.PATH_DOCUMENT_FILE);
			Document.save(map.values());
		}
	}
	
	private static void download(LocalDate lastDate) {
		List<Document> list = new ArrayList<>();
		for(var e: Document.load()) {
			// skip if already exists
			if (e.toFile().exists()) continue;
			
			// skip if submitDateTime is before lastDate
//			if (e.submitDateTime.toLocalDate().isBefore(lastDate)) continue;
			
			// skip if contains no xbrl
			if (!e.xbrlFlag) continue;
			
			list.add(e);
			
//			switch(e.docTypeCode) {
//			case ANNUAL_REPORT:
//			case ANNUAL_REPORT_AMENDMENT:
//			case QUARTERLY_REPORT:
//			case QUARTERLY_REPORT_AMENDMENT:
//			case SEMI_ANNUAL_REPORT:
//			case SEMI_ANNUAL_REPORT_AMENDMENT:
//				list.add(e);
//				break;
//			default:
//				break;
//			}
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
				File file = e.toFile();
				FileUtil.rawWrite().file(file, data);
			}
		}

		logger.info("update {}", count);
		if (0 < count) {
			Document.touch();
		}

	}

	private static final int DEFAULT_MONTHS_NUMBER = 1;
	
	public static void main(String[] args) {
		logger.info("START");
		
		int months = Integer.getInteger("months", DEFAULT_MONTHS_NUMBER);
		logger.info("months {}", months);

		LocalDate date  = LocalDate.now();
		LocalDate lastDate = date.minusMonths(months);
		logger.info("date {} - {}", lastDate, date);

		logger.info("update");
		update(lastDate);
		logger.info("download");
		download(lastDate);

		logger.info("STOP");
	}

}
