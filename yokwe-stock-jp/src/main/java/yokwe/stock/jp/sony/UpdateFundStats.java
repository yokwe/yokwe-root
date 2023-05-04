package yokwe.stock.jp.sony;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import yokwe.util.UnexpectedException;

public class UpdateFundStats {
	private static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();
	
	private static final LocalDate TODAY = LocalDate.now();
	private static final LocalDate TODAY_1Y = TODAY.minusYears(1).minusDays(1);
	
	public static void main(String[] args) {
		logger.info("START");
		
		List<FundStats> statsList = new ArrayList<>();
		
		Map<String, Fund>     fundMap = Fund.getMap();
		Map<String, FundInfo> infoMap = FundInfo.getMap();
		logger.info("fundMap {}", fundMap.size());
		logger.info("infoMap {}", infoMap.size());
		
		for(var key: fundMap.keySet()) {
			var fund = fundMap.get(key);
			var info = infoMap.get(key);
			
			if (fund == null) {
				logger.error("Unexpected key for fund");
				logger.error("  key = {}", key);
				throw new UnexpectedException("Unexpected key for fund");
			}
			if (info == null) {
				logger.error("Unexpected key for info");
				logger.error("  key = {}", key);
				throw new UnexpectedException("Unexpected key for info");
			}
			
			List<Price>    priceList = Price.getList(info.isinCode);
			List<Dividend> divList   = Dividend.getList(fund.isinCode);
			
			if (priceList.isEmpty()) {
				logger.warn("Skip no price {}", info.isinCode);
				continue;
			}

			FundStats stats = new FundStats();
			
			// from fund
			stats.isinCode = fund.isinCode;
			stats.name     = fund.fundName;
			stats.category = fund.category;
			stats.rating   = fund.rating;
		
			// from price
			{
				List<Price> priceLast1Y = priceList.stream().filter(m -> m.date.isAfter(TODAY_1Y)).collect(Collectors.toList());
				
				if (priceLast1Y.size() == 0) {
					stats.date      = "-";
					stats.pricec   = 0;
					stats.price    = BigDecimal.ZERO;
					stats.priceMin = BigDecimal.ZERO;
					stats.priceMax = BigDecimal.ZERO;
					stats.uam      = BigDecimal.ZERO;
					stats.uamMin   = BigDecimal.ZERO;
					stats.uamMax   = BigDecimal.ZERO;
					stats.unit     = BigDecimal.ZERO;
					stats.unitMin  = BigDecimal.ZERO;
					stats.unitMax  = BigDecimal.ZERO;
				} else {
					Price lastPrice = priceLast1Y.get(priceLast1Y.size() - 1);
					stats.date      = lastPrice.date.toString();
					stats.pricec    = priceLast1Y.size();
					stats.price     = lastPrice.price;
					
					var priceMin    = priceLast1Y.stream().map(m -> m.price).min(Comparator.naturalOrder()).get();
					var priceMax    = priceLast1Y.stream().map(m -> m.price).max(Comparator.naturalOrder()).get();
					stats.priceMin  = stats.price.subtract(priceMin).divide(stats.price, 3, RoundingMode.HALF_UP);
					stats.priceMax  = priceMax.subtract(stats.price).divide(stats.price, 3, RoundingMode.HALF_UP);
					
					var uam         = lastPrice.uam;
					var uamMin      = priceLast1Y.stream().map(m -> m.uam).min(Comparator.naturalOrder()).get();
					var uamMax      = priceLast1Y.stream().map(m -> m.uam).max(Comparator.naturalOrder()).get();
					stats.uam       = uam;
					stats.uamMin    = uam.subtract(uamMin).divide(uam, 3, RoundingMode.HALF_UP);
					stats.uamMax    = uamMax.subtract(stats.uam).divide(uam, 3, RoundingMode.HALF_UP);
					stats.unit      = lastPrice.unit;
					
					var unitMin     = priceLast1Y.stream().map(m -> m.unit).min(Comparator.naturalOrder()).get();
					var unitMax     = priceLast1Y.stream().map(m -> m.unit).max(Comparator.naturalOrder()).get();
					stats.unitMin   = stats.unit.subtract(unitMin).divide(stats.unit, 3, RoundingMode.HALF_UP);
					stats.unitMax   = unitMax.subtract(stats.unit).divide(stats.unit, 3, RoundingMode.HALF_UP);
				}
				
			}
			
			// from div
			{
				if (divList.isEmpty()) {
					stats.divc       = 0;
					stats.divLast    = BigDecimal.ZERO;
					stats.div1Y      = BigDecimal.ZERO;
					stats.yieldLast  = BigDecimal.ZERO;
					stats.yield1Y    = BigDecimal.ZERO;
				} else {					
					var divList1Y = divList.stream().filter(m -> m.date.isAfter(TODAY_1Y)).map(o -> o.dividend).collect(Collectors.toList());
					var div1Y     = divList1Y.stream().reduce(BigDecimal.ZERO, BigDecimal::add);
					
					if (div1Y.equals(BigDecimal.ZERO)) {
						stats.divc       = 0;
						stats.divLast    = BigDecimal.ZERO;
						stats.div1Y      = BigDecimal.ZERO;
						stats.yieldLast  = BigDecimal.ZERO;
						stats.yield1Y    = BigDecimal.ZERO;	
					} else {
						int divc      = divList1Y.size();
						var divLast   = divList1Y.get(divc - 1);
						var divLast1Y = divLast.multiply(BigDecimal.valueOf(divc));
						
						stats.divc      = divc;
						stats.divLast   = divLast;
						stats.div1Y     = div1Y;
						stats.yieldLast = divLast1Y.divide(stats.price, 8, RoundingMode.HALF_UP);
						stats.yield1Y   = div1Y.divide(stats.price, 8, RoundingMode.HALF_UP);
					}
				}
			}
			
			statsList.add(stats);
		}
		
		logger.info("save {} {}", statsList.size(), FundStats.getPath());
		FundStats.save(statsList);		
		
		logger.info("STOP");
	}
}
