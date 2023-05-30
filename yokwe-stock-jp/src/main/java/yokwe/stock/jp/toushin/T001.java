package yokwe.stock.jp.toushin;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Map;

import yokwe.util.UnexpectedException;
import yokwe.util.finance.AnnualStats;
import yokwe.util.finance.BigDecimalUtil;
import yokwe.util.finance.DailyValue;
import yokwe.util.finance.MonthlyStats;

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
	
	private static void data(String isinCode, LocalDate targetDate) {
		Fund fund = getFund(isinCode);
		
		logger.info("fund        {}  {}", isinCode, fund.name);
		
		DailyValue[] priceArray = Price.getList(isinCode).stream().map(o ->new DailyValue(o.date, o.price)).toArray(DailyValue[]::new);
		DailyValue[] divArray   = Dividend.getList(isinCode).stream().map(o -> new DailyValue(o.date, o.amount)).toArray(DailyValue[]::new);
		
		{
			var startDate = priceArray[0].date;
			var stopDate  = priceArray[priceArray.length - 1].date;
			var duration  = DailyValue.duration(priceArray).doubleValue();
			logger.info("            {} - {}  {}", startDate, stopDate, String.format("%.2f", duration));
		}
		
		MonthlyStats[] monthlyStatsArray = MonthlyStats.monthlyStatsArray(isinCode, priceArray, divArray, 121);
		logger.info("monthlyStatsArray0  {}", monthlyStatsArray[0].endDate);
		
		for(int nYear: Arrays.asList(1, 3, 5)) {
			AnnualStats  aStats = AnnualStats.getInstance(monthlyStatsArray, nYear);
			if (aStats == null) continue;
			
			logger.info("nYear   {}", nYear);
			logger.info("  {} - {}  {} - {}", aStats.startDate, aStats.endDate, aStats.startValue.stripTrailingZeros(), aStats.endValue.stripTrailingZeros());
			
			logger.info("  abReturn  {}", aStats.absoluteReturn.multiply(N_100).setScale(2, BigDecimalUtil.DEFAULT_ROUNDING_MODE).toPlainString());
			logger.info("  anRetrun  {}", aStats.annualizedReturn.multiply(N_100).setScale(2, BigDecimalUtil.DEFAULT_ROUNDING_MODE).toPlainString());
			logger.info("  abReturnW {}", aStats.absoluteReturnWithReinvest.multiply(N_100).setScale(2, BigDecimalUtil.DEFAULT_ROUNDING_MODE).toPlainString());
			logger.info("  anRetrunW {}", aStats.annualizedReturnWithReinvest.multiply(N_100).setScale(2, BigDecimalUtil.DEFAULT_ROUNDING_MODE).toPlainString());
//			logger.info("  mean      {}", aStats.mean.setScale(4, BigDecimalUtil.DEFAULT_ROUNDING_MODE));
			logger.info("  sd        {}", aStats.sd.stripTrailingZeros().toPlainString());
			logger.info("  div       {}", aStats.div.stripTrailingZeros().toPlainString());
//			logger.info("  yield     {}", aStats.yield.setScale(4, BigDecimalUtil.DEFAULT_ROUNDING_MODE));
		}

	}
	
	public static void main(String[] args) {
		logger.info("START");
		
		// JP3046490003  01311078  ＮＥＸＴ　ＦＵＮＤＳ金価格連動型上場投信                            has no dividend
		// JP90C0008X42  53311133  フランクリン・テンプルトン・アメリカ高配当株ファンド（毎月分配型）  has monthly dividend

		data("JP3046490003", LocalDate.now());
		data("JP90C0008X42", LocalDate.now());
		
		logger.info("STOP");
	}
}
