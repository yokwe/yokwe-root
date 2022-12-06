package yokwe.stock.jp.xbrl.tdnet.report;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

import yokwe.stock.jp.tdnet.Category;
import yokwe.stock.jp.tdnet.SummaryFilename;
import yokwe.stock.jp.tdnet.TDNET;
import yokwe.stock.jp.xbrl.inline.Document;
import yokwe.util.UnexpectedException;

//
//Update reit-report.csv
//

public class UpdateStockReport {
	private static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();
	
	public static void main(String[] args) {
		logger.info("START");
		
		// make map from existing stockReport
		Map<String, StockReport> map = new TreeMap<>();
		//  filename
		{
			for(var e: StockReport.getList()) {
				if (map.containsKey(e.filename)) {
					logger.error("duplicate filename");
					logger.error(" {}", e.filename);
					throw new UnexpectedException("duplicate filename");
				} else {
					map.put(e.filename, e);
				}
			}
			logger.info("reportMap {}", map.size());
		}

		// make candidate from existing data file
		List<File> fileList = new ArrayList<>();
		{
			Map<SummaryFilename, File> fileMap= TDNET.getSummaryFileMap();
			logger.info("fileMap   {}", fileMap.size());
			
			List<SummaryFilename> keyList = fileMap.keySet().stream().
					filter(o -> (o.category == Category.EDJP) || (o.category == Category.EDIF) || (o.category == Category.EDUS)).
					collect(Collectors.toList());
			Collections.sort(keyList);
			
			for(SummaryFilename key: keyList) {
				File file = fileMap.get(key);
				fileList.add(file);
			}
			
			logger.info("fileList  {}", fileList.size());
		}
		
		// make list
		int count = 0;
		int countUpdate = 0;
		List<StockReport> list = new ArrayList<>();
		{
			for(File file: fileList) {
				if ((count % 1000) == 0) {
					logger.info("{} {}", String.format("%5d / %5d", count, fileList.size()), file.getName());
					StockReport.save(list);
				}
				count++;
				
				final String filename = file.getName();
				final StockReport report;
				
				if (map.containsKey(filename)) {
					report = map.get(filename);
				} else {
					try {
						countUpdate++;
						Document document = Document.getInstance(file);
						report = StockReport.getInstance(document);
					} catch(UnexpectedException e) {
						logger.error("file {}", file.getName());
						throw e;
					}
				}
				list.add(report);
			}
		}
		
		// save list
		logger.info("count {} / {}", countUpdate, count);
		if (0 < countUpdate) {
			logger.info("save {} {}", StockReport.PATH_FILE, list.size());
			StockReport.save(list);
		}

		logger.info("STOP");
	}
}
