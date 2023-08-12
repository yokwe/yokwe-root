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
				
		DailyPriceDiv[] dailyPriceDivarray = getDailyPriceDiv(isinCode);
//		logger.info("dailyPriceDiv  {}  {}  {}", dailyPriceDivarray.length, dailyPriceDivarray[0].date, dailyPriceDivarray[dailyPriceDivarray.length - 1].date);

		FundStats fundStats = FundStats.getInstance(isinCode, dailyPriceDivarray);
		logger.info("fundStats      {}  {}  {}", fundStats.duration, fundStats.firstDate, fundStats.lastDate);
		
		logger.info("  rateOfReturn  {}", fundStats.rateOfReturn(nMonth));
		logger.info("  rateOfReturnX {}", fundStats.rateOfReturnX(nMonth));
		logger.info("  rateOfReturnNoReinvest {}", fundStats.rateOfReturnNoReinvest(nMonth));
	}
	
	
	public static void main(String[] args) {
		logger.info("START");
		
		// JP3046490003  01311078  ＮＥＸＴ　ＦＵＮＤＳ金価格連動型上場投信                            has no dividend
		// JP90C0008X42  53311133  フランクリン・テンプルトン・アメリカ高配当株ファンド（毎月分配型）  has monthly dividend
		testStats("JP3046490003", 12);
		testStats("JP90C0008X42", 12);
		
		logger.info("STOP");
	}
}
