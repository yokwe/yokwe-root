package yokwe.stock.jp.data;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

import yokwe.stock.jp.tdnet.Category;
import yokwe.stock.jp.tdnet.SummaryFilename;
import yokwe.stock.jp.tdnet.TDNET;
import yokwe.stock.jp.xbrl.tdnet.inline.Document;
import yokwe.stock.jp.xbrl.tdnet.report.StockReport;

public class UpdateDividendStock {
	static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(UpdateDividendStock.class);
	
	public static void main(String[] args) {
		logger.info("START");
		
		Map<String, DividendStock> map = new TreeMap<>();
		// key is "stockCode yearEnd quarter"
		
		{
			Map<SummaryFilename, StockReport> reportMap = StockReport.getMap();
			logger.info("reportMap {}", reportMap.size());
			
			Map<SummaryFilename, File> fileMap = TDNET.getSummaryFileMap().entrySet().stream().
					filter(o -> o.getKey().category == Category.EDJP || o.getKey().category == Category.EDIF || o.getKey().category == Category.EDUS).
					collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
			logger.info("fileMap {}", fileMap.size());
			
			
			int count = 0;
			for(Map.Entry<SummaryFilename, File> entry: fileMap.entrySet()) {
				SummaryFilename key  = entry.getKey();
				File            file = entry.getValue();
				if ((count % 1000) == 0) {
					logger.info("{} {}", String.format("%5d / %5d", count, fileMap.size()), key);
				}
				count++;
								
				final String  date;
				final String  stockCode;
				final Double  dividend;
				
				final String  yearEnd;
				final Integer quarter;
				
				final SummaryFilename filename;

				{
					final StockReport value;

					// Skip if already processed
					if (reportMap.containsKey(key)) {
						value = reportMap.get(key);
					} else {
						Document document = Document.getInstance(file);
						value = StockReport.getInstance(document);
					}
					date      = value.dividendPayableDateAsPlanned;
					stockCode = value.stockCode;
					dividend  = value.dividendPerShare.doubleValue();
					
					yearEnd   = value.yearEnd;
					quarter   = value.quarterlyPeriod;
					
					filename = value.filename;
				}

								
				String        mapKey   = String.format("%s %s %s", stockCode, yearEnd, quarter);
				DividendStock mapValue = new DividendStock(stockCode, yearEnd, quarter, date, dividend, filename);

				// Sanity check
				{
					if (date.isEmpty()) {
//						logger.warn("date is null {}", mapValue);
						continue;
					}
					if (stockCode.isEmpty()) {
						logger.warn("stockCode is null {}", mapValue);
						continue;
					}
					if (yearEnd.isEmpty()) {
						logger.warn("yearEnd is null {}", mapValue);
						continue;
					}
				}

				if (map.containsKey(mapKey)) {
					DividendStock oldValue = map.get(mapKey);
					if (!mapValue.equals(oldValue)) {
						logger.warn("====");
						logger.warn("Overwrite existing {}", key);
						logger.warn("  old {}", oldValue);
						logger.warn("  new {}", mapValue);
					}
				}
				map.put(mapKey, mapValue);
			}
		}
		
		logger.info("map {}", map.size());
		{
			List<DividendStock> dividendList = new ArrayList<>(map.values());
			
			logger.info("save {}  {}", DividendStock.PATH_FILE, dividendList.size());
			DividendStock.save(dividendList);
		}

		logger.info("STOP");
	}
}
