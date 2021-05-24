package yokwe.stock.jp.data;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.LoggerFactory;

import yokwe.util.UnexpectedException;
import yokwe.stock.jp.tdnet.Category;
import yokwe.stock.jp.tdnet.SummaryFilename;
import yokwe.stock.jp.tdnet.TDNET;
import yokwe.stock.jp.xbrl.tdnet.inline.Document;
import yokwe.stock.jp.xbrl.tdnet.report.StockReport;

//
//Update reit-report.csv
//

public class UpdateStockReport {
	static final org.slf4j.Logger logger = LoggerFactory.getLogger(UpdateStockReport.class);
	
	public static void main(String[] args) {
		logger.info("START");
		
		{
			Map<SummaryFilename, StockReport> reportMap = StockReport.getMap();
			logger.info("reportMap {}", reportMap.size());

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
			
			int count = 0;
			int countUpdate = 0;
			List<StockReport> reportList = new ArrayList<>();
			{
				for(File file: fileList) {
					if ((count % 1000) == 0) {
						logger.info("{} {}", String.format("%5d / %5d", count, fileList.size()), file.getName());
					}
					count++;
					
					final SummaryFilename filename = SummaryFilename.getInstance(file.getName());
					final StockReport report;
					
					if (reportMap.containsKey(filename)) {
						report = reportMap.get(filename);
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
					reportList.add(report);
				}
				
			}
			
			logger.info("count {} / {}", countUpdate, count);
			if (0 < countUpdate) {
				logger.info("save {} {}", StockReport.PATH_FILE, reportList.size());
				StockReport.save(reportList);
			}
		}

		logger.info("STOP");
	}
}
