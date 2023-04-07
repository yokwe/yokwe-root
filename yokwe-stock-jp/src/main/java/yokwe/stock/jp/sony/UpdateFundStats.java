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
		
			// from price
			{
				List<Price> priceLast1Y = priceList.stream().filter(m -> m.date.isAfter(TODAY_1Y)).collect(Collectors.toList());
				
				if (priceLast1Y.size() == 0) {
					stats.date         = "-";
					stats.pricec      = 0;
					stats.price       = BigDecimal.ZERO;
					stats.priceMinPCT = BigDecimal.ZERO;
					stats.priceMaxPCT = BigDecimal.ZERO;
					stats.uam         = BigDecimal.ZERO;
					stats.uamMinPCT   = BigDecimal.ZERO;
					stats.uamMaxPCT   = BigDecimal.ZERO;
					stats.unit        = BigDecimal.ZERO;
					stats.unitMinPCT  = BigDecimal.ZERO;
					stats.unitMaxPCT  = BigDecimal.ZERO;
				} else {
					Price lastPrice   = priceLast1Y.get(priceLast1Y.size() - 1);
					stats.date        = lastPrice.date.toString();
					stats.pricec      = priceLast1Y.size();
					stats.price       = lastPrice.price;
					
					var priceMin      = priceLast1Y.stream().map(m -> m.price).min(Comparator.naturalOrder()).get();
					var priceMax      = priceLast1Y.stream().map(m -> m.price).max(Comparator.naturalOrder()).get();
					stats.priceMinPCT = stats.price.subtract(priceMin).divide(stats.price, 3, RoundingMode.HALF_UP);
					stats.priceMaxPCT = priceMax.subtract(stats.price).divide(stats.price, 3, RoundingMode.HALF_UP);
					
					var uam           = lastPrice.uam;
					var uamMin        = priceLast1Y.stream().map(m -> m.uam).min(Comparator.naturalOrder()).get();
					var uamMax        = priceLast1Y.stream().map(m -> m.uam).max(Comparator.naturalOrder()).get();
					stats.uam         = uam;
					stats.uamMinPCT   = uam.subtract(uamMin).divide(uam, 3, RoundingMode.HALF_UP);
					stats.uamMaxPCT   = uamMax.subtract(stats.uam).divide(uam, 3, RoundingMode.HALF_UP);
					stats.unit        = lastPrice.unit;
					
					var unitMin       = priceLast1Y.stream().map(m -> m.unit).min(Comparator.naturalOrder()).get();
					var unitMax       = priceLast1Y.stream().map(m -> m.unit).max(Comparator.naturalOrder()).get();
					stats.unitMinPCT  = stats.unit.subtract(unitMin).divide(stats.unit, 3, RoundingMode.HALF_UP);
					stats.unitMaxPCT  = unitMax.subtract(stats.unit).divide(stats.unit, 3, RoundingMode.HALF_UP);
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
					Dividend lastDiv = divList.get(divList.size() - 1);
					stats.divLast = lastDiv.dividend;
					
					List<Dividend> divList1Y = divList.stream().filter(m -> m.date.isAfter(TODAY_1Y)).collect(Collectors.toList());
					stats.divc = divList1Y.size();
					var divc = new BigDecimal(stats.divc);
					stats.div1Y = BigDecimal.ZERO;
					for(var e: divList1Y) {
						stats.div1Y = stats.div1Y.add(e.dividend);
					}
					stats.yieldLast = stats.divLast.multiply(divc).divide(stats.price, 8, RoundingMode.HALF_UP);
					stats.yield1Y   = stats.div1Y.divide(stats.price, 8, RoundingMode.HALF_UP);
					
					if (stats.div1Y.equals(BigDecimal.ZERO)) {
						stats.divc       = 0;
						stats.divLast    = BigDecimal.ZERO;
						stats.div1Y      = BigDecimal.ZERO;
						stats.yieldLast  = BigDecimal.ZERO;
						stats.yield1Y    = BigDecimal.ZERO;	
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
