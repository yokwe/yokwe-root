package yokwe.finance.provider.yahoo;

import java.util.Collections;
import java.util.stream.Collectors;

import yokwe.finance.Storage;
import yokwe.finance.stock.StorageStock;
import yokwe.finance.type.CompanyInfoType;
import yokwe.finance.type.StockCodeJP;


public class UpdateCompanyInfoJPYahoo {
	private static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();
	
	private static final long    SLEEP_IN_MILLI = 1500;
	
	private static int updateCompanyInfo() {
		// read existing data
		var list = StorageYahoo.CompanyInfoJPYahoo.getList().stream().collect(Collectors.toList());
		logger.info("companyInfo  {}", list.size());
		// remove if sector or industry is empty
		list.removeIf(o -> o.sector.isEmpty() || o.industry.isEmpty());
		logger.info("companyInfo  {}", list.size());

		// set of required stockCode
		var stockInfoList = StorageStock.StockInfoJP.getList();
		logger.info("stockInfo    {}", stockInfoList.size());
		// remove if not stock
		stockInfoList.removeIf(o -> !o.type.isStock());
		logger.info("stockInfo    {}", stockInfoList.size());
		// remove if already processed
		{
			var set = list.stream().map(o -> o.stockCode).collect(Collectors.toSet());
			stockInfoList.removeIf(o -> set.contains(o.stockCode));
		}
		logger.info("stockInfo    {}", stockInfoList.size());
		
		Collections.shuffle(stockInfoList);
		int count = 0;
		int countMod = 0;
		for(var stockInfo: stockInfoList) {
			if ((++count % 100) == 1) logger.info("{}  /  {}", count, stockInfoList.size());
			
			try {
				Thread.sleep(SLEEP_IN_MILLI);
			} catch (InterruptedException e) {
				//
			}
			
			var stockCode = stockInfo.stockCode;
			
			var companyInfo = CompanyInfoYahoo.getInstance(StockCodeJP.toYahooSymbol(stockCode));
			if (companyInfo == null) continue;
			
			var sector   = companyInfo.sector.replace(",", "").replace("—", "-");
			var industry = companyInfo.industry.replace(",", "").replace("—", "-");
			
			// skipe if sector or industry is empty
			if (sector.isEmpty() || industry.isEmpty()) continue;
			
			list.add(new CompanyInfoType(stockCode, sector, industry));
			countMod++;
			
			if ((countMod % 10) == 1) StorageYahoo.CompanyInfoJPYahoo.save(list);
		}
		
		logger.info("countMod  {}", countMod);
		logger.info("save  {}  {}", list.size(), StorageYahoo.CompanyInfoJPYahoo.getPath());
		StorageYahoo.CompanyInfoJPYahoo.save(list);
		
		return countMod;
	}
	
	
	private static void update() {
		Storage.initialize();
		
		for(int take = 1; take < 9; take++) {
			logger.info("start take {}", take);
			int countMod = updateCompanyInfo();
			if (countMod == 0) break;
			try {
				Thread.sleep(SLEEP_IN_MILLI);
			} catch (InterruptedException e) {
				//
			}
		}
	}
	
	public static void main(String[] args) {
		logger.info("START");
		
		update();
		
		logger.info("STOP");
	}
}
