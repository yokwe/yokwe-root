package yokwe.stock.jp.toushin;

import yokwe.util.finance.DailyPriceDiv;
import yokwe.util.finance.Portfolio;

public class T005 {
	private static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();
		
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
		
		int nMonth = 36;
		int nOffset = 0;
		
		// JP3046490003  01311078  ＮＥＸＴ　ＦＵＮＤＳ金価格連動型上場投信                            has no dividend
		// JP90C0008X42  53311133  フランクリン・テンプルトン・アメリカ高配当株ファンド（毎月分配型）  has monthly dividend		
		{
			Portfolio portfolio = new Portfolio();
			
			portfolio.
				add("JP3046490003", getDairyPriceDiv("JP3046490003")).
				add("JP90C0008X42", getDairyPriceDiv("JP90C0008X42")).
				duration(nMonth, nOffset);
			
			logger.info("{}  rateOfReturn {}", "JP3046490003", portfolio.rateOfReturn("JP3046490003"));
//			logger.info("{}  risk         {}", "JP3046490003", portfolio.risk("JP3046490003"));
			logger.info("{}  riskDialy    {}", "JP3046490003", portfolio.riskDaily("JP3046490003"));
//			logger.info("{}  riskMonthly  {}", "JP3046490003", portfolio.riskMonthly("JP3046490003"));
			logger.info("{}  rateOfReturn {}", "JP90C0008X42", portfolio.rateOfReturn("JP90C0008X42"));
//			logger.info("{}  risk         {}", "JP90C0008X42", portfolio.risk("JP90C0008X42"));
			logger.info("{}  riskDaily    {}", "JP90C0008X42", portfolio.riskDaily("JP90C0008X42"));
//			logger.info("{}  riskMonthly  {}", "JP90C0008X42", portfolio.riskMonthly("JP90C0008X42"));

			portfolio.
				quantity("JP3046490003", 100).
				quantity("JP90C0008X42", 0);
			logger.info("portfolio               {}", portfolio);
			logger.info("portofolio rateOfReturn {}", portfolio.rateOfReturn());
			logger.info("portofolio risk         {}", portfolio.risk());
			
			portfolio.
				quantity("JP3046490003", 0).
				quantity("JP90C0008X42", 100);
			logger.info("portfolio               {}", portfolio);
			logger.info("portofolio rateOfReturn {}", portfolio.rateOfReturn());
			logger.info("portofolio risk         {}", portfolio.risk());
			
			portfolio.
				quantity("JP3046490003", 100).
				quantity("JP90C0008X42", 100);
			logger.info("portfolio               {}", portfolio);
			logger.info("portofolio rateOfReturn {}", portfolio.rateOfReturn());
			logger.info("portofolio risk         {}", portfolio.risk());
		}
		
		logger.info("STOP");
	}
}
