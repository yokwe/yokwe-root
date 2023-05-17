package yokwe.stock.jp.toushin;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class UpdatePriceStats {
	private static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();

	public static void main(String[] args) {
		logger.info("START");
		
		var fundList = Fund.getList();
		logger.info("fundList {}", fundList.size());
		
		int count = 0;
		for(var fund: fundList) {
			count++;
			if ((count % 500) == 1) logger.info("{}", String.format("%4d / %4d", count, fundList.size()));
			String isinCode = fund.isinCode;
			
			var priceList = Price.getList(isinCode);
			if (priceList.isEmpty()) continue;
			
			var divMap    = Dividend.getMap(isinCode);
			
			double previousPrice           = -1;
			double previousReinvestedPrice = -1;
			double previousLogPrice        = -1;
			
			List<PriceStats> statsList = new ArrayList<>();
			for(var e: priceList) {
				LocalDate  date  = e.date;
				BigDecimal nav   = e.nav;
				BigDecimal price = e.price;
				BigDecimal units = e.units;
				
				double doublePrice = price.doubleValue();
				double logPrice    = Math.log(doublePrice);
				
				if (previousPrice == -1) {
					previousPrice           = doublePrice;
					previousReinvestedPrice = doublePrice;
					previousLogPrice        = logPrice;
				}
				
				double logReturn = logPrice - previousLogPrice;
				
				// 日次リターンを「（当日の基準価格＋分配金）÷前営業日基準価格 -1」として計算
				double div = divMap.containsKey(date) ? divMap.get(date).amount.doubleValue() : 0;				
				double dailyReturnPlusOne = (doublePrice + div) / previousPrice;
				//	<計算式>前営業日の分配金再投資基準価格 × (1+日次リターン)
				double reinvestedPrice = previousReinvestedPrice * dailyReturnPlusOne;
				
				statsList.add(new PriceStats(date, nav, price, units, logReturn, reinvestedPrice));
				
				// update for next iteration
				previousPrice           = doublePrice;
				previousReinvestedPrice = reinvestedPrice;
				previousLogPrice        = logPrice;
			}
			PriceStats.save(isinCode, statsList);
		}
		
		logger.info("STOP");
	}
}
