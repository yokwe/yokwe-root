package yokwe.stock.jp.toushin;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import yokwe.util.finance.BigDecimalUtil;
import yokwe.util.finance.ReinvestedPrice;

public class UpdatePriceStats {
	private static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();

	public static void main(String[] args) {
		logger.info("START");
		
		var fundList = Fund.getList();
		logger.info("fundList {}", fundList.size());
		
		int count = 0;
		for(var fund: fundList) {
//			if (fund.isinCode.compareTo("JP90C0008X42") != 0 &&         // JP90C0008X42 has monthly dividend
//				fund.isinCode.compareTo("JP3046490003") != 0) continue; // JP3046490003 has no dividend

			count++;
			if ((count % 500) == 1) logger.info("{}", String.format("%4d / %4d", count, fundList.size()));
			String isinCode = fund.isinCode;
			
			var priceList = Price.getList(isinCode);
			if (priceList.isEmpty()) continue;
			
			var divMap    = Dividend.getMap(isinCode);
			
			ReinvestedPrice reinvestedPrice = new ReinvestedPrice();
			BigDecimal      totalDiv        = BigDecimal.ZERO;
			BigDecimal      previousPrice;
			BigDecimal      previousLogPrice;
			{
				Price first = priceList.get(0);
				previousPrice    = first.price;
				previousLogPrice = BigDecimalUtil.mathLog(previousPrice).round(BigDecimalUtil.DEFAULT_MATH_CONTEXT);
			}
			
			List<PriceStats> statsList = new ArrayList<>();
			for(var e: priceList) {
				LocalDate  date  = e.date;
				BigDecimal nav   = e.nav;
				BigDecimal price = e.price;
				BigDecimal units = e.units;
				
				BigDecimal logPrice  = BigDecimalUtil.mathLog(price).round(BigDecimalUtil.DEFAULT_MATH_CONTEXT);
				BigDecimal logReturn = logPrice.subtract(previousLogPrice);
				
				BigDecimal simpleReturn = price.divide(previousPrice, BigDecimalUtil.DEFAULT_MATH_CONTEXT).subtract(BigDecimal.ONE);

				BigDecimal div;
				if (divMap.containsKey(date)) {
					div      = divMap.get(date).amount;
					totalDiv = totalDiv.add(div);
				} else {
					div      = BigDecimal.ZERO;
				}
				
				BigDecimal reinvestedPriceValue = reinvestedPrice.apply(price, div);
				
				statsList.add(
					new PriceStats(
						date,
						nav,
						price,
						units,
						
						totalDiv.stripTrailingZeros(),
						reinvestedPriceValue.stripTrailingZeros(),
						logReturn.stripTrailingZeros(),
						simpleReturn.stripTrailingZeros()));
				
				// update for next iteration
				previousPrice           = price;
				previousLogPrice        = logPrice;
			}
			PriceStats.save(isinCode, statsList);
		}
		
		logger.info("STOP");
	}
}
