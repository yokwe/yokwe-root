package yokwe.stock.jp.data;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

import org.slf4j.LoggerFactory;

import yokwe.stock.jp.tdnet.Category;
import yokwe.stock.jp.tdnet.Period;
import yokwe.stock.jp.tdnet.SummaryFilename;
import yokwe.stock.jp.tdnet.TDNET;
import yokwe.stock.jp.xbrl.tdnet.inline.Document;
import yokwe.stock.jp.xbrl.tdnet.report.REITReport;

public class UpdateDividendREIT {
	static final org.slf4j.Logger logger = LoggerFactory.getLogger(UpdateDividendREIT.class);
	
	public static void main(String[] args) {
		logger.info("START");
		
		Map<String, DividendREIT> map = new TreeMap<>();
		
		{
			Map<SummaryFilename, REITReport> reportMap = REITReport.getMap();
			logger.info("reportMap {}", reportMap.size());
			
			Map<SummaryFilename, File> fileMap = TDNET.getSummaryFileMap().entrySet().stream().
					filter(o -> o.getKey().category == Category.REJP).
					filter(o -> o.getKey().period == Period.ANNUAL).
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
					final REITReport value;
					if (reportMap.containsKey(key)) {
						value = reportMap.get(key);
					} else {
						Document document = Document.getInstance(file);
						value = REITReport.getInstance(document);
					}
					
					date      = value.distributionsDate;
					stockCode = value.stockCode;
					dividend  = value.distributionsPerUnit.doubleValue() + value.distributionsInExcessOfProfitPerUnit.doubleValue();
					
					yearEnd   = value.yearEnd;
					quarter   = 4;
					
					filename = value.filename;
				}
								
				String        mapKey   = String.format("%s %s %s", stockCode, yearEnd, quarter);
				DividendREIT mapValue = new DividendREIT(stockCode, yearEnd, quarter, date, dividend, filename);

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
					DividendREIT oldValue = map.get(mapKey);
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
			List<DividendREIT> dividendList = new ArrayList<>(map.values());
			
			logger.info("save {}  {}", DividendREIT.PATH_FILE, dividendList.size());
			DividendREIT.save(dividendList);
		}

		logger.info("STOP");
	}
}
