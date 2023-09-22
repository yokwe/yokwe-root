package yokwe.finance.fund.jp;

import java.math.BigDecimal;
import java.time.LocalDate;

import yokwe.finance.provider.jita.DivPrice;
import yokwe.finance.type.DailyValue;
import yokwe.finance.type.FundPriceJP;

public class UpdateFundDivPrice {
	private static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();
	
	private static void update() {
		var fundInfoList = FundInfo.getList();
		logger.info("fundInfoList  {}", fundInfoList.size());
		
		int count = 0;
		for(var fundInfo: fundInfoList) {
			count++;
			if ((count % 100) == 1) logger.info("{}  /  {}", count, fundInfoList.size());

			String isinCode = fundInfo.isinCode;
			
			var divPriceList = DivPrice.getList(isinCode);
			
			// update div
			{
				var divMap      = FundDiv.getMap(isinCode);
				int countModify = 0;
				for(var divPrice: divPriceList) {					
					if (divPrice.div.isEmpty()) continue;
					LocalDate  date     = divPrice.date;
					DailyValue newValue = new DailyValue(date, new BigDecimal(divPrice.div));
					
					if (divMap.containsKey(date)) {
						var oldValue = divMap.get(date);
						if (oldValue.equals(newValue)) continue;
					}
					divMap.put(date, newValue);
					countModify++;
				}
				if (countModify != 0) FundDiv.save(isinCode, divMap.values());
			}
			// update price
			{
				var priceMap    = FundPrice.getMap(isinCode);
				int countModify = 0;
				for(var divPrice: divPriceList) {
					LocalDate   date     = divPrice.date;
					FundPriceJP newValue = new FundPriceJP(date, divPrice.nav.scaleByPowerOfTen(6), divPrice.price); // 純資産総額（百万円）
					
					if (priceMap.containsKey(date)) {
						var oldValue = priceMap.get(date);
						if (oldValue.equals(newValue)) continue;
					}
					priceMap.put(date, newValue);
					countModify++;
				}
				if (countModify != 0) FundPrice.save(isinCode, priceMap.values());
			}
		}
		
	}
	
	public static void main(String[] args) {
		logger.info("START");
		
		update();
		
		logger.info("STOP");
	}
}
