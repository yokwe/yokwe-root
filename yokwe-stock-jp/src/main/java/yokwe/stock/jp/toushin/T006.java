package yokwe.stock.jp.toushin;

import java.util.Map;

import yokwe.util.UnexpectedException;
import yokwe.util.finance.DailyPriceDiv;
import yokwe.util.finance.FundStats;

public class T006 {
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

	private static DailyPriceDiv[] getDailyPriceDiv(String isinCode) {
		Price[]    priceArray = Price.getList(isinCode).stream().toArray(Price[]::new);
		Dividend[] divArray   = Dividend.getList(isinCode).stream().toArray(Dividend[]::new);
		
		DailyPriceDiv[] dailyPriceDivArray = DailyPriceDiv.toDailyPriceDivArray(
			priceArray, o -> o.date, o -> o.price.doubleValue(),
			divArray,   o -> o.date, o -> o.amount.doubleValue());
		
		return dailyPriceDivArray;
	}
	
	static void testStats(String isinCode, int nMonth) {
		logger.info("testStats  {}  {}  {}", isinCode, nMonth, getFund(isinCode).name);
				
		DailyPriceDiv[] dailyPriceDivArray = getDailyPriceDiv(isinCode);
//		logger.info("dailyPriceDiv  {}  {}  {}", dailyPriceDivarray.length, dailyPriceDivarray[0].date, dailyPriceDivarray[dailyPriceDivarray.length - 1].date);

		FundStats fundStats = FundStats.getInstance(isinCode, dailyPriceDivArray);
		logger.info("fundStats      {}  {}  {}", fundStats.duration, fundStats.firstDate, fundStats.lastDate);
		
		logger.info("  rateOfReturn  {}", fundStats.rateOfReturn(nMonth));
		logger.info("  rateOfReturn2 {}", fundStats.rateOfReturnNoReinvest(nMonth));
		logger.info("  risk          {}", fundStats.risk(nMonth));
		logger.info("  riskDaily     {}", fundStats.riskDaily(nMonth));
		logger.info("  riskMonthly   {}", fundStats.riskMonthly(nMonth));
		
//		int[] a = {6, 12, 36, 60, 120};
//		for(var e: a) {
//			logger.info("{}", e);
//			logger.info("  rateOfReturn  {}", fundStats.rateOfReturn(e));
//			logger.info("  rateOfReturn2 {}", fundStats.rateOfReturnNoReinvest(e));
//			logger.info("  risk          {}", fundStats.risk(e));
//		}
	}
	
	
	public static void main(String[] args) {
		logger.info("START");
		
		// JP3046490003  01311078  ＮＥＸＴ　ＦＵＮＤＳ金価格連動型上場投信                            has no dividend
		// JP90C0008X42  53311133  フランクリン・テンプルトン・アメリカ高配当株ファンド（毎月分配型）  has monthly dividend
		int nMonth = 36;
		testStats("JP3046490003", nMonth);
		testStats("JP90C0008X42", nMonth);
		
		logger.info("STOP");
	}
}
