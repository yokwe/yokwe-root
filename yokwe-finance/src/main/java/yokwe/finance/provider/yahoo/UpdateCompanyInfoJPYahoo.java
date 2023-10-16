package yokwe.finance.provider.yahoo;

import java.util.Collections;
import java.util.stream.Collectors;

import yokwe.finance.stock.StockInfoJP;
import yokwe.finance.type.StockInfoJPType;


public class UpdateCompanyInfoJPYahoo {
	private static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();
	
	private static final long    SLEEP_IN_MILLI = 1500;
	
	private static void update() {
		var map = CompanyInfoJPYahoo.getMap();
		logger.info("companyInfo  {}", map.size());

		{
			int count = 0;
			// stockCodeList contains only STOCK
			var stockCodeList = StockInfoJP.getList().stream().filter(o -> o.type.isStock()).map(o -> o.stockCode).collect(Collectors.toList());
			logger.info("stockCode    {}", stockCodeList.size());
			
			stockCodeList.removeIf(o -> map.containsKey(o));
			logger.info("stockCode    {}", stockCodeList.size());
			
			Collections.shuffle(stockCodeList);
			for(var stockCode: stockCodeList) {
				if ((++count % 100) == 1) logger.info("{}  /  {}", count, stockCodeList.size());
				
				try {
					Thread.sleep(SLEEP_IN_MILLI);
				} catch (InterruptedException e) {
					//
				}

				var companyInfo = CompanyInfoYahoo.getInstance(StockInfoJPType.toYahooSymbol(stockCode));
				if (companyInfo == null) {
					continue;
				}
				
				// override stockCode
				companyInfo.stockCode   = stockCode;
				companyInfo.sector      = companyInfo.sector.replace(",", "");
				companyInfo.industry    = companyInfo.industry.replace(",", "");
				
				map.put(companyInfo.stockCode, companyInfo);
				
				if ((map.size() % 10) == 1) CompanyInfoJPYahoo.save(map.values());
			}
		}
		
		{
			var list = map.values().stream().collect(Collectors.toList());
			// remove if entry of list is not in StockInfoJP
			var stockCodeSet = StockInfoJP.getList().stream().filter(o -> o.type.isStock()).map(o -> o.stockCode).collect(Collectors.toSet());
			list.removeIf(o -> !stockCodeSet.contains(o.stockCode));
			
			logger.info("save  {}  {}", list.size(), CompanyInfoJPYahoo.getPath());
			CompanyInfoJPYahoo.save(list);
		}
	}
	
	public static void main(String[] args) {
		logger.info("START");
		
		update();
		
		logger.info("STOP");
	}
}
