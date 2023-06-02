package yokwe.stock.jp.toushin;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import yokwe.util.finance.AnnualStats;
import yokwe.util.finance.DailyValue;
import yokwe.util.finance.MonthlyStats;

public class UpdateStats {
	private static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();
	
	public static void main(String[] args) {
		logger.info("START");
		
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
			
			Price[] rawPriceArray = Price.getList(isinCode).stream().toArray(Price[]::new);
			if (rawPriceArray.length == 0) {
//				logger.warn("{}  rawPriceArray is empty", isinCode);
				countNoPrice++;
				continue;
			}
			Price lastPrice = rawPriceArray[rawPriceArray.length - 1];
			
			DailyValue[] priceArray = Arrays.stream(rawPriceArray).map(o -> new DailyValue(o.date, o.price)).toArray(DailyValue[]::new);
			DailyValue[] divArray   = Dividend.getList(isinCode).stream().map(o -> new DailyValue(o.date, o.amount)).toArray(DailyValue[]::new);
			
			MonthlyStats[] monthlyStatsArray = MonthlyStats.monthlyStatsArray(isinCode, priceArray, divArray, 10 * 12 + 1);

			var nikkei = nikkeiMap.get(isinCode);
			if (nikkei == null) {
				//logger.warn("{}  No Nikkei  --  {}", isinCode, fund.name);
				countNoNikkei++;
				continue;
			}
			
			Stats stats = new Stats();
			
			stats.isinCode = fund.isinCode;
			stats.fundCode = fund.fundCode;
			
			stats.inception  = fund.inceptionDate;
			stats.redemption = fund.redemptionDate;
			{
				LocalDate today = LocalDate.now();
				Period period = stats.inception.until(today);
				stats.age = new BigDecimal(String.format("%d.%02d", period.getYears(), period.getMonths()));
			}
			stats.qCat1  = nikkei.category1;
			stats.qCat2  = nikkei.category2;
			stats.forex  = nikkei.category3.replace("為替リスク", "");
			stats.type   = nikkei.fundType.compareTo("アクティブ型") == 0 ? "ACTIVE" : "INDEX"; // アクティブ型 or インデックス型
			
			stats.date  = lastPrice.date;
			stats.price = lastPrice.price;
			stats.nav   = lastPrice.nav;
			
			stats.divc  = fund.divFreq;

			// 1 year
			{
				int nYear = 1;
				AnnualStats  aStats = AnnualStats.getInstance(monthlyStatsArray, nYear);
				if (aStats != null) {
					stats.sd1Y     = aStats.standardDeviation;
					stats.div1Y    = aStats.dividend;
					stats.yield1Y  = aStats.yield;
					stats.return1Y = aStats.returns;
					stats.sharpe1Y = aStats.sharpeRatio;
				} else {
					stats.sd1Y     = minus1;
					stats.div1Y    = minus1;
					stats.yield1Y  = minus1;
					stats.return1Y = minus1;
					stats.sharpe1Y = minus1;
				}
			}
			// 3 year
			{
				int nYear = 3;
				AnnualStats  aStats = AnnualStats.getInstance(monthlyStatsArray, nYear);
				if (aStats != null) {
					stats.sd3Y     = aStats.standardDeviation;
					stats.div3Y    = aStats.dividend;
					stats.yield3Y  = aStats.yield;
					stats.return3Y = aStats.returns;
					stats.sharpe3Y = aStats.sharpeRatio;
				} else {
					stats.sd3Y     = minus1;
					stats.div3Y    = minus1;
					stats.yield3Y  = minus1;
					stats.return3Y = minus1;
					stats.sharpe3Y = minus1;
				}
			}
			// 5 year
			{
				int nYear = 5;
				AnnualStats  aStats = AnnualStats.getInstance(monthlyStatsArray, nYear);
				if (aStats != null) {
					stats.sd5Y     = aStats.standardDeviation;
					stats.div5Y    = aStats.dividend;
					stats.yield5Y  = aStats.yield;
					stats.return5Y = aStats.returns;
					stats.sharpe5Y = aStats.sharpeRatio;
				} else {
					stats.sd5Y     = minus1;
					stats.div5Y    = minus1;
					stats.yield5Y  = minus1;
					stats.return5Y = minus1;
					stats.sharpe5Y = minus1;
				}
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
