package yokwe.stock.jp.toushin;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;
import java.util.List;

import yokwe.util.finance.BigDecimalUtil;

public class UpdateStats {
	private static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();
	
	private static record MyStats (
		BigDecimal sd, BigDecimal div, BigDecimal yield, BigDecimal reinvestedReturn) {}
	
private static MyStats getMyStats(List<PriceStats> priceStatsList, List<Dividend>divList, LocalDate targetDate, int years) {
		// years = 1  targetDate = 2010-01-02  lastDate = 2010-01-03  firstDate = 2009-01-02
		final LocalDate lastDate  = targetDate.plusDays(1);
		final LocalDate firstDate = targetDate.minusYears(years);
		
		BigDecimal[] logReturnArray = priceStatsList.stream().filter(o -> o.date.isAfter(firstDate) && o.date.isBefore(lastDate)).map(o -> o.logReturn).toArray(BigDecimal[]::new);
		BigDecimal[] reinvestedPriceArray = priceStatsList.stream().filter(o -> o.date.isAfter(firstDate) && o.date.isBefore(lastDate)).map(o -> o.reinvestedPrice).toArray(BigDecimal[]::new);
		BigDecimal[] priceArray = priceStatsList.stream().filter(o -> o.date.isAfter(firstDate) && o.date.isBefore(lastDate)).map(o -> o.price).toArray(BigDecimal[]::new);
		BigDecimal[] divArray = divList.stream().filter(o -> o.date.isAfter(firstDate) && o.date.isBefore(lastDate)).map(o -> o.amount).toArray(BigDecimal[]::new);

		final BigDecimal sd;
		final BigDecimal div;
		final BigDecimal yield;
		final BigDecimal reinvestedReturn;
		
		{
			var priceStats = BigDecimalUtil.stats(logReturnArray, BigDecimalUtil.DEFAULT_MATH_CONTEXT);
			sd = priceStats.sd(); // FIXME ANNUALIZE
		}
		{
			BigDecimal lastPrice = priceArray[priceArray.length - 1];
			BigDecimal totalDiv = BigDecimal.ZERO;
			for(var e: divArray) {
				totalDiv = totalDiv.add(e);
			}
			div   = totalDiv.divide(BigDecimal.valueOf(years), BigDecimalUtil.DEFAULT_MATH_CONTEXT);
			yield = div.divide(lastPrice, BigDecimalUtil.DEFAULT_MATH_CONTEXT);
		}
		{
			var first = reinvestedPriceArray[0];
			var last  = reinvestedPriceArray[reinvestedPriceArray.length - 1];
			reinvestedReturn = BigDecimalUtil.annualizedRetrun(first, last, years);
		}
		return new MyStats(sd, div, yield, reinvestedReturn);
	}
	
	public static void main(String[] args) {
		logger.info("START");
		
//		final LocalDate today = LocalDate.now();
		final LocalDate today = LocalDate.parse("2023-04-30");

		logger.info("today {}", today);
		
		final BigDecimal minus1 = BigDecimal.ONE.negate();
		
		List<Stats> statsList = new ArrayList<>();
		
		List<Fund> fundList = Fund.getList();
		logger.info("fundList  {}", fundList.size());
		var nikkeiMap = yokwe.stock.jp.nikkei.Fund.getMap();
		logger.info("nikkeiMap {}", nikkeiMap.size());
		var sellerList = Seller.getList();
		logger.info("sellerList {}", sellerList.size());

		int countNoPrice = 0;
		int countNoNikkei = 0;
		
		int count = 0;
		for(var fund: fundList) {
			final String isinCode = fund.isinCode;
			
			count++;
			if ((count % 500) == 1) logger.info("{}", String.format("%4d / %4d", count, fundList.size()));
			
			List<PriceStats> priceStatsList = PriceStats.getList(isinCode);
			if (priceStatsList.isEmpty()) {
//				logger.warn("{}  priceStatsList is empty", isinCode);
				countNoPrice++;
				continue;
			}
			
//			List<DailyValue> priceList = priceStatsList.stream().map(o -> DailyValue.getInstance(o.date, o.reinvestedPrice)).toList();
			PriceStats lastPrice = priceStatsList.get(priceStatsList.size() - 1);

			var nikkei = nikkeiMap.get(isinCode);
			if (nikkei == null) {
				//logger.warn("{}  No Nikkei  --  {}", isinCode, fund.name);
				countNoNikkei++;
				continue;
			}
			
			var divList = Dividend.getList(isinCode);
			
			Stats stats = new Stats();
			
			stats.isinCode = fund.isinCode;
			stats.fundCode = fund.fundCode;
			
			stats.inception  = fund.inceptionDate;
			stats.redemption = fund.redemptionDate;
			{
				Period period = stats.inception.until(today);
				stats.age = new BigDecimal(String.format("%d.%02d", period.getYears(), period.getMonths()));
			}
			stats.qCat1  = nikkei.category1;
			stats.qCat2  = nikkei.category2;
			stats.forex = nikkei.category3.replace("為替リスク", "");
			stats.type   = nikkei.fundType.compareTo("アクティブ型") == 0 ? "ACTIVE" : "INDEX"; // アクティブ型 or インデックス型
			
			stats.date  = lastPrice.date;
			stats.price = lastPrice.price;
//			stats.price = lastPrice.reinvestedPrice;
			stats.nav   = lastPrice.nav;
			
			stats.divc  = fund.divFreq;

			// 1 year
			if (1.0 <= stats.age.doubleValue()) {
				MyStats myStats = getMyStats(priceStatsList, divList, today, 1);
				stats.sd1Y     = myStats.sd;
				stats.div1Y    = myStats.div;
				stats.yield1Y  = myStats.yield;
				stats.return1Y = myStats.reinvestedReturn;
			} else {
				stats.sd1Y     = minus1;
				stats.div1Y    = minus1;
				stats.yield1Y  = minus1;
				stats.return1Y = minus1;
			}
			// 3 year
			if (3.0 <= stats.age.doubleValue()) {
				MyStats myStats = getMyStats(priceStatsList, divList, today, 3);
				stats.sd3Y     = myStats.sd;
				stats.div3Y    = myStats.div;
				stats.yield3Y  = myStats.yield;
				stats.return3Y = myStats.reinvestedReturn;
			} else {
				stats.sd3Y     = minus1;
				stats.div3Y    = minus1;
				stats.yield3Y  = minus1;
				stats.return3Y = minus1;
			}
			// 5 year
			if (5.0 <= stats.age.doubleValue()) {
				MyStats myStats = getMyStats(priceStatsList, divList, today, 5);
				stats.sd5Y     = myStats.sd;
				stats.div5Y    = myStats.div;
				stats.yield5Y  = myStats.yield;
				stats.return5Y = myStats.reinvestedReturn;
			} else {
				stats.sd5Y     = minus1;
				stats.div5Y    = minus1;
				stats.yield5Y  = minus1;
				stats.return5Y = minus1;
			}
			
			stats.divQ1Y   = nikkei.divScore1Y;
			stats.divQ3Y   = nikkei.divScore3Y;
			stats.divQ5Y   = nikkei.divScore5Y;
			
			stats.name     = fund.name;
			stats.seller   = Seller.getSellerName(sellerList, stats.isinCode);
			
			statsList.add(stats);
		}
		
		
		logger.info("statsList  {}  {}", statsList.size(), Stats.getPath());
		Stats.save(statsList);
		
		logger.info("fundList       {}", fundList.size());
		logger.info("nikkeiMap      {}", nikkeiMap.size());
		logger.info("countNoPrice   {}", countNoPrice);
		logger.info("countNoNikkei  {}", countNoNikkei);
		
		logger.info("STOP");
	}
}
