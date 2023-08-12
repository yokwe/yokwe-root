package yokwe.stock.jp.toushin;

import java.util.Map;

import yokwe.util.UnexpectedException;
import yokwe.util.finance.AnnualStats;
import yokwe.util.finance.DailyPriceDiv;
import yokwe.util.finance.MonthlyStats;
import yokwe.util.finance.Portfolio;

public class T005 {
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

	private static void data(String isinCode, int nYear) {
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
		
		AnnualStats  aStats = AnnualStats.getInstance(monthlyStatsArray, nYear);
		logger.info("aStats  rorReinvested   {}", aStats.rorReinvestment * 100);
		logger.info("aStats  anSD            {}", aStats.standardDeviation * 100);
	}
	
	private static DailyPriceDiv[] getDairyPriceDiv(String isinCode) {
		Price[]    priceArray = Price.getList(isinCode).stream().toArray(Price[]::new);
		Dividend[] divArray   = Dividend.getList(isinCode).stream().toArray(Dividend[]::new);
		
		DailyPriceDiv[] dailyPriceDivArray = DailyPriceDiv.toDailyPriceDivArray(
			priceArray, o -> o.date, o -> o.price.doubleValue(),
			divArray,   o -> o.date, o -> o.amount.doubleValue());
		
		return dailyPriceDivArray;
	}
	
	public static void main(String[] args) {
		logger.info("START");
		
		// JP3046490003  01311078  ＮＥＸＴ　ＦＵＮＤＳ金価格連動型上場投信                            has no dividend
		// JP90C0008X42  53311133  フランクリン・テンプルトン・アメリカ高配当株ファンド（毎月分配型）  has monthly dividend
		data("JP3046490003", 1);
		data("JP90C0008X42", 1);
		
		{
			Portfolio portfolio = new Portfolio();
			
			portfolio.
				add("JP3046490003", getDairyPriceDiv("JP3046490003")).
				add("JP90C0008X42", getDairyPriceDiv("JP90C0008X42")).
				setDuration(1);
			logger.info("portfolio  {}  {}", portfolio, portfolio.durationInYear());
			
			portfolio.
				setQuantity("JP3046490003", 100).
				setQuantity("JP90C0008X42", 0);
			logger.info("portofolio rorReinvestment    {}", portfolio.rorReinvestment() * 100);
			logger.info("portofolio standardDeviation  {}", portfolio.standardDeviation() * 100);
			
			portfolio.
				setQuantity("JP3046490003", 0).
				setQuantity("JP90C0008X42", 100);
			logger.info("portofolio rorReinvestment    {}", portfolio.rorReinvestment() * 100);
			logger.info("portofolio standardDeviation  {}", portfolio.standardDeviation() * 100);
			
			portfolio.
				setQuantity("JP3046490003", 100).
				setQuantity("JP90C0008X42", 100);
			logger.info("portofolio rorReinvestment    {}", portfolio.rorReinvestment() * 100);
			logger.info("portofolio standardDeviation  {}", portfolio.standardDeviation() * 100);
		}
		
		logger.info("STOP");
	}
}
