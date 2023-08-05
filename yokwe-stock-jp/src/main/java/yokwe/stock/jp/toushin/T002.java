package yokwe.stock.jp.toushin;

import java.time.LocalDate;
import java.util.Map;

import yokwe.util.UnexpectedException;
import yokwe.util.finance.AnnualStats;
import yokwe.util.finance.DailyPriceDiv;
import yokwe.util.finance.MonthlyStats;
import yokwe.util.finance.Portofolio;

public class T002 {
	private static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();
	
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
		
		logger.info("fund {}  {}", isinCode, fund.name);
		
		final DailyPriceDiv[] dailyPriceDivArray;
		{
			Price[]    priceArray = Price.getList(isinCode).stream().toArray(Price[]::new);
			Dividend[] divArray   = Dividend.getList(isinCode).stream().toArray(Dividend[]::new);
			
			dailyPriceDivArray = DailyPriceDiv.toDailyPriceDivArray(
				priceArray, o -> o.date, o -> o.price.doubleValue(),
				divArray,   o -> o.date, o -> o.amount.doubleValue());
		}
		
		MonthlyStats[] monthlyStatsArray = MonthlyStats.monthlyStatsArray(isinCode, dailyPriceDivArray, 9999);
		logger.info("monthlyStatsArray  {}  {}  {}", monthlyStatsArray.length, monthlyStatsArray[monthlyStatsArray.length - 1].startDate, monthlyStatsArray[0].endDate);
		
		int nYear = 3;
		
		{
			AnnualStats  aStats = AnnualStats.getInstance(monthlyStatsArray, nYear);
			logger.info("aStats  rorReinvested   {}", aStats.rorReinvestment * 100);
			logger.info("aStats  anSD            {}", aStats.standardDeviation);
		}
		Portofolio portfolio = Portofolio.builder().
			add(isinCode, dailyPriceDivArray, 100).
			getInstance(nYear);		
		logger.info("portofolio  rorReinvestment   {}", portfolio.rorReinvestment() * 100);
		logger.info("portofolio  standardDeviation {}", portfolio.standardDeviation());
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
