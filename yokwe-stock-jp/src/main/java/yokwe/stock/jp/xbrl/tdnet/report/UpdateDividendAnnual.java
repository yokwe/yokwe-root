package yokwe.stock.jp.xbrl.tdnet.report;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class UpdateDividendAnnual {
	static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(UpdateDividendAnnual.class);
	
	public static void main(String[] args) {
		logger.info("START");
		
		// Build list from Dividend
		List<DividendAnnual> list = new ArrayList<>();
		
		// build divMap from Dividend
		Map<String, List<Dividend>> divMap = new TreeMap<>();
		//  stockCode
		for(var e: Dividend.getList()) {
			String stockCode = e.stockCode;
			List<Dividend> divList;
			if (divMap.containsKey(stockCode)) {
				divList = divMap.get(stockCode);
			} else {
				divList = new ArrayList<>();
				divMap.put(stockCode, divList);
			}
			divList.add(e);
		}
		
		for(var e: divMap.entrySet()) {
			String     stockCode = e.getKey();
			Dividend[] array     = e.getValue().toArray(new Dividend[0]);
			Arrays.sort(array, Collections.reverseOrder());
			int    quarter     = array[0].quarter;
			String lastPayDate = array[0].payDate;
			
			int thisYear = Integer.valueOf(array[0].yearEnd.substring(0, 4));
			int lastYear = thisYear - 1;
			
			// 2021-02-27
			// 01234567
			int thisMonth = Integer.valueOf(array[0].yearEnd.substring(5, 7));
			
			int    divc = 0;
			double div  = 0;
			for(var ee: array) {
				int year  = Integer.valueOf(ee.yearEnd.substring(0, 4));
				int month = Integer.valueOf(ee.yearEnd.substring(5, 7));
				
				if (quarter == 0) {
					// for REIT
					if (year == thisYear) {
						// accept all
					} else if (year == lastYear) {
						// accept greater than thisMonth
						if (month <= thisMonth) break;
					} else {
						break;
					}
				} else {
					// for stock
					if (year == thisYear) {
						// accept all
					} else if (year == lastYear) {
						// accept greater than quarter
						if (ee.quarter <= quarter) break;
					} else {
						break;
					}
				}
				
				divc++;
				div += ee.dividend;
			}
			if (divc == 1 || divc == 2 || divc == 4) {
				// Looks normal
			} else {
				logger.warn("Unexpected divc");
				logger.warn("  stockCode {}", stockCode);
				logger.warn("  divc      {}", divc);
				for(var ee: array) {
					String yearString = ee.yearEnd.substring(0, 4);
					int year = Integer.parseInt(yearString);
					if (year == thisYear || year == lastYear) {
						logger.warn("  {}", ee);						
					}
				}
			}
			
			list.add(new DividendAnnual(stockCode, lastPayDate, divc, div));
		}
		
		DividendAnnual.save(list);
		logger.info("save {} {}", DividendAnnual.getPath(), list.size());

		logger.info("STOP");
	}
}
