package yokwe.security.japan.test;

import java.io.IOException;

import org.slf4j.LoggerFactory;

import yokwe.security.japan.data.DividendAnnual;
import yokwe.security.japan.xbrl.report.StockReport;

public class T029 {
	static final org.slf4j.Logger logger = LoggerFactory.getLogger(T029.class);
	
	public static void main(String[] args) throws IOException {
		logger.info("START");
		
		String stockCode = "94320";
		
		{
			DividendAnnual dividendAnnual = DividendAnnual.get(stockCode);
			logger.info("{}", dividendAnnual);
		}
		{
			for(StockReport e: StockReport.getList()) {
				if (!e.stockCode.equals(stockCode)) continue;
				logger.info("{}", e);
				logger.info("  {}", e.annualDividendPerShare);
				logger.info("  {}", e.annualDividendPerShareForeast);
				logger.info("  {}", e.annualDividendPerShareResult);
				logger.info("  {}", e.dividendPerShareQ1);
				logger.info("  {}", e.dividendPerShareQ2);
				logger.info("  {}", e.dividendPerShareQ3);
				logger.info("  {}", e.dividendPerShareQ4);
			}
		}
		
		logger.info("STOP");
	}
}
