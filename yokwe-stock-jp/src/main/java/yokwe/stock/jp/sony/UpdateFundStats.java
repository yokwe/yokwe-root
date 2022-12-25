package yokwe.stock.jp.sony;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
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
	
	private static BigDecimal toBigDecimalFromPercent(String string) {
		string = string.replace("＋運用報酬", "");
		string = string.replace("＋成功報酬", "");
		
		if (string.endsWith("%")) {
			return new BigDecimal(string.substring(0, string.length() - 1)).scaleByPowerOfTen(-2);
		} else if (string.length() == 0) {
			return BigDecimal.ZERO;
		} else {
			logger.error("Unexpected string");
			logger.error("  string = {}", string);
			throw new UnexpectedException("Unexpected string");
		}
	}
	
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
			stats.category = fund.category;
			stats.fundName = fund.fundName;
			stats.company  = fund.company;
			stats.divFreq  = fund.divFreq;
			stats.region   = fund.region;
			stats.target   = fund.target;
			stats.currency = fund.currency;
		
			// from info
			{
				List<YearMonthDay> list = StringUtil.find(info.inceptionDate, YearMonthDay.PAT, YearMonthDay.OP).collect(Collectors.toList());
				
				if (list.size() == 1) {
					var e = list.get(0);
					stats.inceptionDate = String.format("%s-%02d-%02d", e.year, e.month, e.day);
				} else {
					logger.error("Unexpected inceptionDate");
					logger.error("  inceptionDate = {}!", info.inceptionDate);
					throw new UnexpectedException("Unexpected inceptionDate");
				}
			}
					
			{
				if (info.redemptionDate.equals(DATE_INFINITY)) {
					stats.redemptionDate = "-";
				} else {
					List<YearMonthDay> list = StringUtil.find(info.redemptionDate, YearMonthDay.PAT, YearMonthDay.OP).collect(Collectors.toList());
					
					if (list.size() == 1) {
						var e = list.get(0);
						stats.redemptionDate = String.format("%s-%02d-%02d", e.year, e.month, e.day);
					} else {
						logger.error("Unexpected redemptionDate");
						logger.error("  redemptionDate = {}!", info.redemptionDate);
						throw new UnexpectedException("Unexpected redemptionDate");
					}
				}
			}
			{
				List<String> list = new ArrayList<>();
				for(var e: StringUtil.find(info.closingDate, MonthDay.PAT, MonthDay.OP).collect(Collectors.toList())) {
					list.add(String.format("%d/%d", e.mm, e.dd));
				}
				stats.closingDate = String.join(", ", list);
			}
			
			stats.trustFee     = toBigDecimalFromPercent(info.trustFee);
			stats.realTrustFee = toBigDecimalFromPercent(info.realTrustFee);
			stats.cancelFee    = toBigDecimalFromPercent(info.cancelFee);
			
			// from price
			{
				Price lastPrice = priceList.get(priceList.size() - 1);
				stats.priceDate = lastPrice.date.toString();
				stats.price     = lastPrice.price;
			}
			
			// from div
			{
				if (divList.isEmpty()) {
					stats.divFreq    = "0";
					//
					stats.divDate    = "-";
					stats.div        = BigDecimal.ZERO;
					stats.div1YCount = BigDecimal.ZERO;
					stats.div1Y      = BigDecimal.ZERO;
					stats.yieldLast  = BigDecimal.ZERO;
					stats.yield1Y    = BigDecimal.ZERO;
				} else {
					Dividend lastDiv = divList.get(divList.size() - 1);
					stats.divDate = lastDiv.date.toString();
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
						stats.divDate    = "-";
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
