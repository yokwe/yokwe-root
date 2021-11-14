package yokwe.stock.jp.xbrl.tdnet.report;

import java.util.ArrayList;
import java.util.List;

public class UpdateDividend {
	static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(UpdateDividend.class);
	
	public static void main(String[] args) {
		logger.info("START");
		
		// Build list from DividendRET and DividendStock
		List<Dividend> list = new ArrayList<>();
		
		// from DividendStock
		{
			for(var e: DividendStock.getList()) {
				String stockCode = e.stockCode;
				String yearEnd   = e.yearEnd;
				int    quarter   = e.quarter;
				String payDate   = e.payDate;
				double dividend  = e.dividend;
				String filename  = e.filename;
				
				list.add(new Dividend(stockCode, yearEnd, quarter, payDate, dividend, filename));
			}
		}
		// from DividendREIT
		{
			for(var e: DividendREIT.getList()) {
				String stockCode = e.stockCode;
				String yearEnd   = e.yearEnd;
				int    quarter   = 0;
				String payDate   = e.payDate;
				double dividend  = e.dividend;
				String filename  = e.filename;
				
				list.add(new Dividend(stockCode, yearEnd, quarter, payDate, dividend, filename));
			}
		}
		
		Dividend.save(list);
		logger.info("save {} {}", Dividend.getPath(), list.size());

		logger.info("STOP");
	}
}
