package yokwe.stock.jp.toushin;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import yokwe.stock.jp.gmo.GMOFund;
import yokwe.stock.jp.nikko.NikkoFund;
import yokwe.stock.jp.nomura.NomuraFund;
import yokwe.stock.jp.rakuten.RakutenFund;
import yokwe.stock.jp.sony.SonyFund;
import yokwe.util.finance.AnnualStats;
import yokwe.util.finance.DailyValue;
import yokwe.util.finance.MonthlyStats;

public class UpdateStats {
	private static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();
	
	private static final BigDecimal MINUS_1   = BigDecimal.ONE.negate();
	private static final int        MAX_YEARS = 10;

	public static void main(String[] args) {
		logger.info("START");
		
		List<Stats> statsList = new ArrayList<>();
		
		List<Fund> fundList = Fund.getList();
		logger.info("fundList   {}", fundList.size());
		
		var nikkeiMap = yokwe.stock.jp.nikkei.Fund.getMap();
		logger.info("nikkeiMap  {}", nikkeiMap.size());
		
		var gmoSet     = GMOFund.getList().stream().map(o -> o.isinCode).collect(Collectors.toSet());
		var nikkoSet   = NikkoFund.getList().stream().map(o -> o.isinCode).collect(Collectors.toSet());
		var nomuraSet  = NomuraFund.getList().stream().map(o -> o.isinCode).collect(Collectors.toSet());
		var rakutenSet = RakutenFund.getList().stream().map(o -> o.isinCode).collect(Collectors.toSet());
		var sonySet    = SonyFund.getList().stream().map(o -> o.isinCode).collect(Collectors.toSet());
		logger.info("gmoSet     {}", gmoSet.size());
		logger.info("nikkoSet   {}", nikkoSet.size());
		logger.info("nomuraSet  {}", nomuraSet.size());
		logger.info("rakutenSet {}", rakutenSet.size());
		logger.info("sonySet    {}", sonySet.size());

		int countNoPrice = 0;
		int countNoNikkei = 0;
		
		int count = 0;
		for(var fund: fundList) {
			final String isinCode = fund.isinCode;
			
			count++;
			if ((count % 500) == 1) logger.info("{}", String.format("%4d / %4d", count, fundList.size()));
			
			Price[] rawPriceArray = Price.getList(isinCode).stream().toArray(Price[]::new);
			if (rawPriceArray.length == 0) {
				countNoPrice++;
				continue;
			}
			Price lastPrice = rawPriceArray[rawPriceArray.length - 1];
			
			DailyValue[] priceArray = Arrays.stream(rawPriceArray).map(o -> new DailyValue(o.date, o.price)).toArray(DailyValue[]::new);
			DailyValue[] divArray   = Dividend.getList(isinCode).stream().map(o -> new DailyValue(o.date, o.amount)).toArray(DailyValue[]::new);
			
			MonthlyStats[] monthlyStatsArray = MonthlyStats.monthlyStatsArray(isinCode, priceArray, divArray, MAX_YEARS * 12 + 1);

			var nikkei = nikkeiMap.get(isinCode);
			if (nikkei == null) {
				countNoNikkei++;
			}
			
			Stats stats = new Stats();
			
			stats.isinCode  = fund.isinCode;
			stats.fundCode  = fund.fundCode;
			stats.stockCode = fund.stockCode;
			
			stats.inception  = fund.inceptionDate;
			stats.redemption = fund.redemptionDate;
			{
				LocalDate today = LocalDate.now();
				Period period = stats.inception.until(today);
				stats.age = new BigDecimal(String.format("%d.%02d", period.getYears(), period.getMonths()));
			}
			
			// Use toushin category
			stats.investingAsset = fund.investingAsset;
			stats.investingArea  = fund.investingArea;
			stats.indexFundType  = fund.indexFundType;
			
			stats.expenseRatio = fund.expenseRatio;
			stats.nav          = lastPrice.nav;
			stats.divc         = fund.divFreq;
			
			// 1 year
			{
				int nYear = 1;
				AnnualStats  aStats = AnnualStats.getInstance(monthlyStatsArray, nYear);
				stats.sd1Y     = aStats == null ? MINUS_1 : aStats.standardDeviation;
				stats.div1Y    = aStats == null ? MINUS_1 : aStats.dividend;
				stats.yield1Y  = aStats == null ? MINUS_1 : aStats.yield;
				stats.return1Y = aStats == null ? MINUS_1 : aStats.returns;
			}
			// 3 year
			{
				int nYear = 3;
				AnnualStats  aStats = AnnualStats.getInstance(monthlyStatsArray, nYear);
				stats.sd3Y     = aStats == null ? MINUS_1 : aStats.standardDeviation;
				stats.div3Y    = aStats == null ? MINUS_1 : aStats.dividend;
				stats.yield3Y  = aStats == null ? MINUS_1 : aStats.yield;
				stats.return3Y = aStats == null ? MINUS_1 : aStats.returns;
			}
			// 5 year
			{
				int nYear = 5;
				AnnualStats  aStats = AnnualStats.getInstance(monthlyStatsArray, nYear);
				stats.sd5Y     = aStats == null ? MINUS_1 : aStats.standardDeviation;
				stats.div5Y    = aStats == null ? MINUS_1 : aStats.dividend;
				stats.yield5Y  = aStats == null ? MINUS_1 : aStats.yield;
				stats.return5Y = aStats == null ? MINUS_1 : aStats.returns;
			}
			// 10 year
			{
				int nYear = 10;
				AnnualStats  aStats = AnnualStats.getInstance(monthlyStatsArray, nYear);
				stats.sd10Y     = aStats == null ? MINUS_1 : aStats.standardDeviation;
				stats.div10Y    = aStats == null ? MINUS_1 : aStats.dividend;
				stats.yield10Y  = aStats == null ? MINUS_1 : aStats.yield;
				stats.return10Y = aStats == null ? MINUS_1 : aStats.returns;
			}
			
			stats.divQ1Y   = nikkei == null ? MINUS_1 : nikkei.divScore1Y;
			stats.divQ3Y   = nikkei == null ? MINUS_1 : nikkei.divScore3Y;
			stats.divQ5Y   = nikkei == null ? MINUS_1 : nikkei.divScore5Y;
			stats.divQ10Y  = nikkei == null ? MINUS_1 : nikkei.divScore10Y;
			
			stats.name     = fund.name;
			
			stats.gmo      = gmoSet.contains(fund.isinCode) ? "1" : "0";
			stats.nikko    = nikkoSet.contains(fund.isinCode) ? "1" : "0";
			stats.nomura   = nomuraSet.contains(fund.isinCode) ? "1" : "0";
			stats.rakuten  = rakutenSet.contains(fund.isinCode) ? "1" : "0";
			stats.sony     = sonySet.contains(fund.isinCode) ? "1" : "0";
			
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
