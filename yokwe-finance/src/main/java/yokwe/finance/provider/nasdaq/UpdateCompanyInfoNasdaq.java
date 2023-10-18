package yokwe.finance.provider.nasdaq;

import java.util.Collections;
import java.util.stream.Collectors;

import yokwe.finance.Storage;
import yokwe.finance.provider.nasdaq.api.CompanyProfile;
import yokwe.finance.stock.StorageStock;
import yokwe.finance.type.CompanyInfoType;
import yokwe.finance.type.StockInfoUSType;


public class UpdateCompanyInfoNasdaq {
	private static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();
	
	private static final long SLEEP_IN_MILLI = 1500;

	private static int updateCompanyInfo() {
		// read existing data
		var list = StorageNasdaq.CompanyInfoNasdaq.getList().stream().collect(Collectors.toList());
		logger.info("companyInfo  {}", list.size());
		// remove if sector or industry is empty
		list.removeIf(o -> o.sector.isEmpty() || o.industry.isEmpty());
		logger.info("companyInfo  {}", list.size());
		// remove if stockCode is preferred
		list.removeIf(o -> o.stockCode.contains("-"));
		logger.info("companyInfo  {}", list.size());
		
		// set of required stockCode
		var stockInfoList = StorageStock.StockInfoUS.getList();
		logger.info("stockInfo    {}", stockInfoList.size());
		// remove if not stock
		stockInfoList.removeIf(o -> !o.type.isStock());
		logger.info("stockInfo    {}", stockInfoList.size());
		// remove if stockCode is preferred
		stockInfoList.removeIf(o -> o.stockCode.contains("-"));
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
			
			var stockCode = stockInfo.stockCode;
			
			var companyProfile = CompanyProfile.getInstance(StockInfoUSType.toNASDAQSymbol(stockCode));
			if (companyProfile == null) {
				try {
					logger.info("companyProfile is null  {}", stockCode);
					Thread.sleep(SLEEP_IN_MILLI);
				} catch (InterruptedException e) {
					//
				}
				continue;
			}
			if (companyProfile.data == null) continue;
			
			var sector   = companyProfile.data.sector.value.replace(",", "");
			var industry = companyProfile.data.industry.value.replace(",", "");
			
			// skipe if sector or industry is empty
			if (sector.isEmpty() || industry.isEmpty()) continue;
			
			list.add(new CompanyInfoType(stockCode, sector, industry));
			countMod++;
			
			if ((countMod % 10) == 1) StorageNasdaq.CompanyInfoNasdaq.save(list);
		}
		
		logger.info("countMod  {}", countMod);
		logger.info("save  {}  {}", list.size(), StorageNasdaq.CompanyInfoNasdaq.getPath());
		StorageNasdaq.CompanyInfoNasdaq.save(list);
		
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
