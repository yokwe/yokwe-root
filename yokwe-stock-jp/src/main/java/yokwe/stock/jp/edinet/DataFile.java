package yokwe.stock.jp.edinet;

import java.io.File;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import yokwe.stock.jp.edinet.API.Disclose;
import yokwe.stock.jp.edinet.API.Withdraw;
import yokwe.util.FileUtil;
import yokwe.util.UnexpectedException;

public class DataFile {
	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(DataFile.class);
	
	private static void update(LocalDate lastDate, LocalDate today) {
		Map<String, Document> map = Document.getDocumentMap();
		
		Set<LocalDate> dateSet = new TreeSet<>();
		{
			Set<LocalDate> downloadDateSet = map.values().stream().map(o -> o.downloadDate).collect(Collectors.toSet());
			List<LocalDate> downloadDateList = new ArrayList<>(downloadDateSet);
			Collections.sort(downloadDateList);
			
			if (!downloadDateList.isEmpty()) {
				LocalDate downloadFirstDate = downloadDateList.get(0);
				LocalDate downloadLastDate  = downloadDateList.get(downloadDateList.size() - 1);
				logger.info("date {} - {}", downloadFirstDate, downloadLastDate);
			}

			dateSet.add(today); // add today
			
			for(LocalDate date  = today; date.isAfter(lastDate); date = date.minusDays(1)) {
				if (downloadDateSet.contains(date)) continue;
				dateSet.add(date);
			}
			
			logger.info("exist    {}", downloadDateSet.size());
			logger.info("count    {}", dateSet.size());
		}
		
		int countUpdate   = 0;
		int countExpire   = 0;
		int countWithdraw = 0;
		int countDisclose = 0;
		for(LocalDate date: dateSet) {
			API.ListDocument.Response response = API.ListDocument.getInstance(date, API.ListDocument.Type.DATA);
			if (response.results.length != 0) {
				logger.info("{}  {}", date, String.format("%4d", response.results.length));
			}
			
			for(var e: response.results) {
				// skip if edinetCode is empty and withdrawStatsu is normal (expired document)
				if (e.edinetCode.isEmpty() && e.withdrawalStatus == Withdraw.NORMAL) {
					logger.warn("skip expire  {} {} {}", date, e.seqNumber, e.docID);
					countExpire++;
					continue;
				}

				// skip if withdrawalStatus is not NORMAL
				if (!(e.withdrawalStatus == Withdraw.NORMAL)) {
					logger.warn("skip withdraw {} {} {}", date, e.seqNumber, e.docID);
					countWithdraw++;
					continue;
				}
				
				// skip if discloseStatus is not NORMAL or not DISCLOSE
				if (!(e.disclosureStatus == Disclose.NORMAL || e.disclosureStatus == Disclose.DISCLOSE)) {
					logger.warn("skip disclose {} {} {}", date, e.seqNumber, e.docID);
					countDisclose++;
					continue;
				}
				
				if (e.edinetCode.isEmpty()) {
					logger.error("edinetCode is empty");
					logger.error("  e {}", e.toString());
					throw new UnexpectedException("edinetCode is empty");
				}
				
				String docID = e.docID;
				if (!map.containsKey(docID)) {
					Document document = new Document(
						date,
						e.seqNumber,
						
						e.docID,
						e.edinetCode,
						
						e.xbrlFlag.toBoolean(),
						
						e.stockCode,
						e.fundCode,
						e.ordinanceCode,
						e.formCode,

						e.docTypeCode,

						e.submitDateTime,
						
						e.docDescription
						);
					map.put(docID, document);

					countUpdate++;
					if ((countUpdate % 10000) == 0) {
						List<Document> list = new ArrayList<>(map.values());
						logger.info("save {} {}", list.size(), Document.PATH_DOCUMENT_FILE);
						Document.save(list);
					}
				}
			}
		}
		
		logger.info("expire   {}", countExpire);
		logger.info("withdraw {}", countWithdraw);
		logger.info("disclose {}", countDisclose);
		logger.info("update   {}", countUpdate);
		if (0 < countUpdate) {
			List<Document> list = new ArrayList<>(map.values());
			logger.info("save {} {}", list.size(), Document.PATH_DOCUMENT_FILE);
			Document.save(map.values());
		}
	}
	
	private static void download() {
		List<Document> list = new ArrayList<>();
		
		{
			List<Document> existingList = Document.load();
			Collections.sort(existingList);
			
			if (!existingList.isEmpty()) {
				LocalDate firstDate = existingList.get(0).submitDateTime.toLocalDate();
				LocalDate lastDate  = existingList.get(existingList.size() - 1).submitDateTime.toLocalDate();
				logger.info("date {} - {}", firstDate, lastDate);
			}
			
			int countExist = 0;
			int countSkip  = 0;
			for(var e: existingList) {
				// skip if already exists
				if (e.toFile().exists()) {
					countExist++;
					continue;
				}
							
				// skip if contains no xbrl
				if (!e.xbrlFlag) {
					countSkip++;
					continue;
				}
				
				list.add(e);
			}
			
			logger.info("total   {}", existingList.size());
			logger.info("exist   {}", countExist);
			logger.info("skip    {}", countSkip);
			logger.info("count   {}", list.size());
		}
		
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

		logger.info("update  {}", count);
		if (0 < count) {
			Document.touch();
		}
	}
	
	private static void check() {
		List<Document> documentList = Document.load();
		if (documentList == null) {
			documentList = new ArrayList<>();
		}
		
		Set<File> fileSet = new HashSet<>(FileUtil.listFile(Document.PATH_DOCUMENT_DIR));
		Set<File> docList = documentList.stream().map(o -> o.toFile()).collect(Collectors.toSet());
		
		logger.info("files        {}", fileSet.size());
		logger.info("document     {}", docList.size());
		
		if (!docList.isEmpty()) {
			fileSet.removeAll(docList);
			List<File> fileList = new ArrayList<>(fileSet);
			Collections.sort(fileList);

			logger.info("unknown      {}", fileList.size());
			
			if (100 < fileList.size()) {
				logger.error("Unexpected");
				logger.error("  {}", fileList.size());
				throw new UnexpectedException("Unexpected");
			} else {
				for(var e: fileList) {
//					logger.warn("  {}", e);
					e.delete();
				}
			}
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
		
		logger.info("check");
		check();

		logger.info("update");
		update(lastDate, date);
				
		logger.info("download");
		download();

		logger.info("STOP");
	}
}
