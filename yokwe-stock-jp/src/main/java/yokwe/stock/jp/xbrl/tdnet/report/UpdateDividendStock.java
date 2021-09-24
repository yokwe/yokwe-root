package yokwe.stock.jp.xbrl.tdnet.report;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class UpdateDividendStock {
	static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(UpdateDividendStock.class);
	
	
	public static void main(String[] args) {
		logger.info("START");
		
		// FIXME
		Map<String, DividendStock> map = new TreeMap<>();
		
		// StockReport
		{
			for(var e: StockReport.getList()) {
				// skip if no dividend pay
				if (e.dividendPayableDateAsPlanned.isEmpty()) continue;
				
				// sanity check
				if (e.quarterlyPeriod == 0) {
					logger.warn("Unexpected");
					logger.warn("  quarterlyPeriod is zero");
					logger.warn("  {} {}", e.filename, e.toString());
					continue;
				}
				
				String stockCode      = e.stockCode;
				String yearEnd        = e.yearEnd;
				int    quarter        = e.quarterlyPeriod;
				String payDate        = e.dividendPayableDateAsPlanned;
				double dividend       = e.dividendPerShare.doubleValue();
				double annualDividend = e.annualDividendPerShare.doubleValue();
				String filename       = e.filename;
				
				DividendStock div = new DividendStock(stockCode, yearEnd, quarter, payDate, dividend, annualDividend, filename);
				
				String key = String.format("%s-%s-%d", stockCode, yearEnd, quarter);
				if (map.containsKey(key)) {
					logger.info("update");
					logger.info("  new  {}", div);
					logger.info("  old  {}", map.get(key));
				}
				map.put(key, div);
			}
		}
		
		List<DividendStock> list = new ArrayList<>(map.values());
		Collections.sort(list);
		
		DividendStock.save(list);
		logger.info("save {} {}", DividendStock.getPath(), list.size());

		logger.info("STOP");
	}
}
