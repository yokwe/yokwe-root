package yokwe.stock.jp.xbrl.tdnet.report;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class UpdateDividendREIT {
	private static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();
	
	
	public static void main(String[] args) {
		logger.info("START");
		
		// FIXME
		Map<String, DividendREIT> map = new TreeMap<>();
		//  key
		
		// REITReport
		{
			for(var e: REITReport.getList()) {
				// skip if no dividend pay
				if (e.distributionsDate.isEmpty()) continue;
				
				String stockCode                = e.stockCode;
				String yearEnd                  = e.yearEnd;
				String payDate                  = e.distributionsDate;
				double dividend                 = e.distributionsPerUnit.doubleValue();
				double nextDividendForecast     = e.distributionsPerUnitNextYear.doubleValue();
				double nextNextDividendForecast = e.distributionsPerUnitNext2Year.doubleValue();
				String filename                 = e.filename;
				
				DividendREIT div = new DividendREIT(stockCode, yearEnd, payDate, dividend, nextDividendForecast, nextNextDividendForecast, filename);
				
				String key = String.format("%s-%s", stockCode, yearEnd);
				if (map.containsKey(key)) {
					logger.info("update");
					logger.info("  new  {}", div);
					logger.info("  old  {}", map.get(key));
				}
				map.put(key, div);
			}
		}
		
		List<DividendREIT> list = new ArrayList<>(map.values());
		Collections.sort(list);
		
		DividendREIT.save(list);
		logger.info("save {} {}", DividendREIT.getPath(), list.size());

		logger.info("STOP");
	}
}
