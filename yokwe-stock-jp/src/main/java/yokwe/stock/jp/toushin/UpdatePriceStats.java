package yokwe.stock.jp.toushin;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import yokwe.util.finance.BigDecimalUtil;

public class UpdatePriceStats {
	private static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();

	public static void main(String[] args) {
		logger.info("START");
		
		var fundList = Fund.getList();
		logger.info("fundList {}", fundList.size());
		
		int count = 0;
		for(var fund: fundList) {
//			if (fund.isinCode.compareTo("JP3046490003") != 0) continue;
			
			count++;
			if ((count % 10) == 1) logger.info("{}", String.format("%4d / %4d", count, fundList.size()));
			String isinCode = fund.isinCode;
			
			var priceList = Price.getList(isinCode);
			if (priceList.isEmpty()) continue;
			
			var divMap    = Dividend.getMap(isinCode);
			
			BigDecimal previousPrice           = null;
			BigDecimal previousReinvestedPrice = null;
			BigDecimal previousLogPrice        = null;
			BigDecimal totalDiv = BigDecimal.ZERO;

			List<PriceStats> statsList = new ArrayList<>();
			for(var e: priceList) {
				LocalDate  date  = e.date;
				BigDecimal nav   = e.nav;
				BigDecimal price = e.price;
				BigDecimal units = e.units;
				
				if (previousPrice == null) {
					previousPrice           = price;
					previousReinvestedPrice = price;
					previousLogPrice        = BigDecimalUtil.log(price, BigDecimalUtil.DEFAULT_INTERNAL_SCALE);
				}
				
				BigDecimal logPrice  = BigDecimalUtil.log(price, BigDecimalUtil.DEFAULT_INTERNAL_SCALE);
				BigDecimal logReturn = logPrice.subtract(previousLogPrice);

				BigDecimal div;
				if (divMap.containsKey(date)) {
					div      = divMap.get(date).amount;
					totalDiv = totalDiv.add(div);
				} else {
					div      = BigDecimal.ZERO;
				}
				
				// 分配金再投資基準価格 = 前営業日の分配金再投資基準価格 ×（基準価格＋分配金）÷ 前営業日の基準価格
				BigDecimal reinvestedPrice;
				if (div.compareTo(BigDecimal.ZERO) == 0 && previousPrice.compareTo(previousReinvestedPrice) == 0) {
					reinvestedPrice = price;
				} else {
					reinvestedPrice = previousReinvestedPrice.multiply(price.add(div)).divide(previousPrice, BigDecimalUtil.DEFAULT_INTERNAL_SCALE - 1, BigDecimalUtil.DEFAULT_ROUNDING_MODE);
				}
				
				statsList.add(new PriceStats(date, nav, price, units, totalDiv.stripTrailingZeros() , reinvestedPrice.stripTrailingZeros(), logReturn.stripTrailingZeros()));
				
				// update for next iteration
				previousPrice           = price;
				previousReinvestedPrice = reinvestedPrice;
				previousLogPrice        = logPrice;
			}
			PriceStats.save(isinCode, statsList);
		}
		
		logger.info("STOP");
	}
}
