package yokwe.stock.jp.xbrl.tdnet.report;

import java.time.LocalDate;
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
			
			String thisYear = array[0].yearEnd.substring(0, 4);
			String lastYear = String.format("%04d", Integer.valueOf(thisYear) - 1);
			
			int    divc = 0;
			double div  = 0;
			for(var ee: array) {
				String yearString = ee.yearEnd.substring(0, 4);
				
				if (yearString.equals(thisYear)) {
					//
				} else if (yearString.equals(lastYear)) {
					if (ee.quarter <= quarter) break;
					//
				} else {
					break;
				}
				
				divc++;
				div += ee.dividend;
			}
			if (divc == 1 || divc == 2 || divc == 4) {
				// OK
			} else {
				logger.warn("Unexpected divc");
				logger.warn("  stockCode {}", stockCode);
				logger.warn("  divc      {}", divc);
				for(var ee: array) {
					String yearString = ee.yearEnd.substring(0, 4);
					if (yearString.equals(thisYear) || yearString.equals(lastYear)) {
						logger.warn("  {}", ee);						
					} else {
						break;
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
