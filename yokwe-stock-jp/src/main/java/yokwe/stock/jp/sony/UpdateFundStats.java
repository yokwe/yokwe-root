package yokwe.stock.jp.sony;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import yokwe.util.StringUtil;
import yokwe.util.UnexpectedException;

public class UpdateFundStats {
	private static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();
	
	private static final String DATE_INFINITY = "無期限";
	
	private static class YearMonthDay {
		static final Pattern PAT = Pattern.compile("(?<yyyy>[12][0-9]{3})(年|\\/)(?<mm>[0-9]{1,2})(月|\\/)(?<dd>[0-9]{1,2})日?");
		static final StringUtil.MatcherFunction<YearMonthDay> OP = (m -> new YearMonthDay(m.group("yyyy"), m.group("mm"), m.group("dd")));
		
		public final int year;
		public final int month;
		public final int day;
		public YearMonthDay(String yyyy, String mm, String dd) {
			year  = Integer.parseInt(yyyy);
			month = Integer.parseInt(mm);
			day   = Integer.parseInt(dd);
		}
	}
	
	private static class MonthDay {
		static final Pattern PAT = Pattern.compile("(?<mm>[0-9]{1,2})(月|\\/)(?<dd>[0-9]{1,2})日?");
		static final StringUtil.MatcherFunction<MonthDay> OP = (m -> new MonthDay(m.group("mm"), m.group("dd")));
		
		public final int mm;
		public final int dd;
		public MonthDay(String mm, String dd) {
			this.mm = Integer.parseInt(mm);
			this.dd = Integer.parseInt(dd);
		}
	}

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
			stats.fundName = fund.fundName;
			stats.category = fund.category;
			stats.divFreq  = fund.divFreq;
		
			// from price
			{
				List<Price> priceLast1Y = priceList.stream().filter(m -> m.date.isAfter(TODAY_1Y)).collect(Collectors.toList());
				
				if (priceLast1Y.size() == 0) {
					stats.priceDate      = "-";
					stats.price1YCount   = BigDecimal.ZERO;
					stats.price          = BigDecimal.ZERO;
					stats.priceMinPCT    = BigDecimal.ZERO;
					stats.priceMaxPCT    = BigDecimal.ZERO;
					stats.netAsset       = BigDecimal.ZERO;
					stats.netAssetMinPCT = BigDecimal.ZERO;
					stats.netAssetMaxPCT = BigDecimal.ZERO;
					stats.unit           = BigDecimal.ZERO;
					stats.unitMinPCT     = BigDecimal.ZERO;
					stats.unitMaxPCT     = BigDecimal.ZERO;
				} else {
					Price lastPrice = priceLast1Y.get(priceLast1Y.size() - 1);
					stats.priceDate      = lastPrice.date.toString();
					stats.price1YCount   = new BigDecimal(priceLast1Y.size());
					stats.price          = lastPrice.price;
					var priceMin         = priceLast1Y.stream().map(m -> m.price).min(Comparator.naturalOrder()).get();
					var priceMax         = priceLast1Y.stream().map(m -> m.price).max(Comparator.naturalOrder()).get();
					stats.priceMinPCT    = stats.price.subtract(priceMin).divide(stats.price, 3, RoundingMode.HALF_DOWN);
					stats.priceMaxPCT    = priceMax.subtract(stats.price).divide(stats.price, 3, RoundingMode.HALF_DOWN);
					stats.netAsset       = lastPrice.netAsset;
					var netAssetMin      = priceLast1Y.stream().map(m -> m.netAsset).min(Comparator.naturalOrder()).get();
					var netAssetMax      = priceLast1Y.stream().map(m -> m.netAsset).max(Comparator.naturalOrder()).get();
					stats.netAssetMinPCT = stats.netAsset.subtract(netAssetMin).divide(stats.netAsset, 3, RoundingMode.HALF_DOWN);
					stats.netAssetMaxPCT = netAssetMax.subtract(stats.netAsset).divide(stats.netAsset, 3, RoundingMode.HALF_DOWN);
					stats.unit           = lastPrice.unit;
					var unitMin          = priceLast1Y.stream().map(m -> m.unit).min(Comparator.naturalOrder()).get();
					var unitMax          = priceLast1Y.stream().map(m -> m.unit).max(Comparator.naturalOrder()).get();
					stats.unitMinPCT     = stats.unit.subtract(unitMin).divide(stats.unit, 3, RoundingMode.HALF_DOWN);
					stats.unitMaxPCT     = unitMax.subtract(stats.unit).divide(stats.unit, 3, RoundingMode.HALF_DOWN);
				}
				
			}
			
			// from div
			{
				if (divList.isEmpty()) {
					stats.divFreq    = "0";
					//
					stats.div        = BigDecimal.ZERO;
					stats.div1YCount = BigDecimal.ZERO;
					stats.div1Y      = BigDecimal.ZERO;
					stats.yieldLast  = BigDecimal.ZERO;
					stats.yield1Y    = BigDecimal.ZERO;
				} else {
					Dividend lastDiv = divList.get(divList.size() - 1);
					stats.div = lastDiv.dividend;
					
					List<Dividend> divList1Y = divList.stream().filter(m -> m.date.isAfter(TODAY_1Y)).collect(Collectors.toList());
					stats.div1YCount = new BigDecimal(divList1Y.size());
					stats.div1Y = BigDecimal.ZERO;
					for(var e: divList1Y) {
						stats.div1Y = stats.div1Y.add(e.dividend);
					}
					stats.yieldLast = stats.div.multiply(stats.div1YCount).divide(stats.price, 8, RoundingMode.HALF_DOWN);
					stats.yield1Y   = stats.div1Y.divide(stats.price, 8, RoundingMode.HALF_DOWN);
					
					if (stats.div1Y.equals(BigDecimal.ZERO)) {
						stats.divFreq    = "0";
						//
						stats.div        = BigDecimal.ZERO;
						stats.div1YCount = BigDecimal.ZERO;
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
