package yokwe.finance.provider.nasdaq;

import java.util.Collections;
import java.util.stream.Collectors;

import yokwe.finance.provider.nasdaq.api.CompanyProfile;
import yokwe.finance.stock.StockInfoUS;
import yokwe.finance.type.CompanyInfoType;
import yokwe.finance.type.StockInfoUSType;


public class UpdateCompanyInfoNasdaq {
	private static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();
	
	private static void update() {
		var map = CompanyInfoNasdaq.getMap();
		logger.info("companyInfo  {}", map.size());

		{
			int count = 0;
			// stockCodeList contains only STOCK
			var stockCodeList = StockInfoUS.getList().stream().filter(o -> o.type.isStock()).map(o -> o.stockCode).collect(Collectors.toList());
			logger.info("stockCode    {}", stockCodeList.size());

			stockCodeList.removeIf(o -> map.containsKey(o));
			logger.info("stockCode    {}", stockCodeList.size());

			Collections.shuffle(stockCodeList);
			for(var stockCode: stockCodeList) {
				if ((++count % 100) == 1) logger.info("{}  /  {}", count, stockCodeList.size());
				
				var companyProfile = CompanyProfile.getInstance(StockInfoUSType.toNASDAQSymbol(stockCode));
				if (companyProfile == null) {
					try {
						logger.info("companyProfile is null  {}", stockCode);
						Thread.sleep(2000);
					} catch (InterruptedException e) {
						//
					}
					continue;
				}
				if (companyProfile.data == null) continue;
				
				var companyInfo = new CompanyInfoType();
				companyInfo.stockCode   = stockCode;
				companyInfo.sector      = companyProfile.data.sector.value.replace(",", "");
				companyInfo.industry    = companyProfile.data.industry.value.replace(",", "");
				
				map.put(companyInfo.stockCode, companyInfo);
				
				if ((map.size() % 10) == 1) CompanyInfoNasdaq.save(map.values());
			}
		}
		
		logger.info("save  {}  {}", map.size(), CompanyInfoNasdaq.getPath());
		CompanyInfoNasdaq.save(map.values());
	}
	
	public static void main(String[] args) {
		logger.info("START");
		
		update();
		
		logger.info("STOP");
	}
}
