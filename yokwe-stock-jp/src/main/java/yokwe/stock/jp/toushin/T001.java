package yokwe.stock.jp.toushin;

import java.math.BigDecimal;
import java.math.MathContext;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Map;

import yokwe.util.UnexpectedException;
import yokwe.util.finance.BigDecimalUtil;
import yokwe.util.finance.DailyValue;
import yokwe.util.finance.DailyValue.AnnualStats;

public final class T001 {
	private static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();
	
	public static final BigDecimal N_100 = BigDecimal.ONE.movePointRight(2);

	private static final Map<String, Fund> FUND_MAP = Fund.getMap();
	private static Fund getFund(String isinCode) {
		if (FUND_MAP.containsKey(isinCode)) {
			return FUND_MAP.get(isinCode);
		} else {
			logger.error("Unexpected isinCode");
			logger.error("  {}", isinCode);
			throw new UnexpectedException("Unexpected isinCode");
		}
	}
	
	private static void data(String isinCode, LocalDate targetDate, MathContext mathContext) {
		Fund fund = getFund(isinCode);
		var priceArray = PriceStats.getList(isinCode).stream().map(o -> new DailyValue(o.date, o.reinvestedPrice)).toArray(DailyValue[]::new);
//		var priceArray = PriceStats.getList(isinCode).stream().map(o -> new DailyValue(o.date, o.price)).toArray(DailyValue[]::new);
		logger.info("fund        {}  {}", isinCode, fund.name);
				
		double duration   = DailyValue.duration(priceArray).doubleValue();
		logger.info("            {} - {}  {}", priceArray[0].date, priceArray[priceArray.length - 1].date, String.format("%.2f", duration));
		
		DailyValue.MonthlyStats[] monthlyStatsArray = DailyValue.monthlyStatsArray(priceArray, 121, mathContext);
		logger.info("monthlyStatsArray0  {}", monthlyStatsArray[0].endDate);
		
		
		
		for(int e: Arrays.asList(12, 36, 60, 120)) {
			int nMonth = e;
			if (monthlyStatsArray.length <= nMonth) break;
			
			AnnualStats  aStats = new AnnualStats(monthlyStatsArray, nMonth, mathContext);
			
			logger.info("nMonth  {}", nMonth);
			logger.info("  {} - {}  {} - {}", aStats.startDate, aStats.endDate, aStats.startValue, aStats.endValue);
			
			logger.info("  returns  {}", aStats.returns.toPlainString());
			logger.info("  aRetruns {}", aStats.annualReturn.toPlainString());
			logger.info("  mean     {}", aStats.mean.setScale(4, mathContext.getRoundingMode()));
//			logger.info("  monthlyMean     {}", aStats.monthlyMean.setScale(4, mathContext.getRoundingMode()));
			logger.info("  sd       {}", aStats.sd.setScale(4, mathContext.getRoundingMode()));
		}

	}
	
	public static void main(String[] args) {
		logger.info("START");
		
		// JP90C0008X42  53311133  フランクリン・テンプルトン・アメリカ高配当株ファンド（毎月分配型）  has monthly dividend
		// JP3046490003  01311078  ＮＥＸＴ　ＦＵＮＤＳ金価格連動型上場投信                            has no dividend

		data("JP3046490003", LocalDate.now(), BigDecimalUtil.DEFAULT_MATH_CONTEXT);
		
		logger.info("STOP");
	}
}
