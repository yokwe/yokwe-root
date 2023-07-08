package yokwe.stock.jp.toushin;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import yokwe.stock.jp.Storage;
import yokwe.stock.jp.gmo.GMOFund;
import yokwe.stock.jp.nikko.NikkoFund;
import yokwe.stock.jp.nomura.NomuraFund;
import yokwe.stock.jp.rakuten.RakutenFund;
import yokwe.stock.jp.sbi.SBIFund;
import yokwe.stock.jp.sony.SonyFund;
import yokwe.util.BigDecimalArray;
import yokwe.util.StringUtil;
import yokwe.util.finance.AnnualStats;
import yokwe.util.finance.DailyValue;
import yokwe.util.finance.MonthlyStats;
import yokwe.util.libreoffice.Sheet;
import yokwe.util.libreoffice.SpreadSheet;

public class UpdateStats {
	private static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();
	
	private static final String PREFIX_REPORT = "report";
	private static final String URL_TEMPLATE  = StringUtil.toURLString(Storage.Toushin.getPath("TEMPLATE_TOUSHIN_STATS.ods"));

	public static final  BigDecimal CONSUMPTION_TAX_RATE  = new BigDecimal("1.1"); // 10 percent
	
	private static BigDecimal       MINUS_ONE = BigDecimal.ONE.negate();
	private static final int        MAX_YEARS = 10;
	
	private static List<Stats> getStatsList() {
		List<Stats> statsList = new ArrayList<>();
		
		List<Fund> fundList = Fund.getList();
		logger.info("fundList   {}", fundList.size());
		
		var nikkeiMap = yokwe.stock.jp.nikkei.Fund.getMap();
		logger.info("nikkeiMap  {}", nikkeiMap.size());
		
		var gmoSet     = GMOFund.getList().stream().map(o -> o.isinCode).collect(Collectors.toSet());
		var nikkoSet   = NikkoFund.getList().stream().map(o -> o.isinCode).collect(Collectors.toSet());
		var nomuraSet  = NomuraFund.getList().stream().map(o -> o.isinCode).collect(Collectors.toSet());
		var rakutenSet = RakutenFund.getList().stream().map(o -> o.isinCode).collect(Collectors.toSet());
		var sbiSet     = SBIFund.getList().stream().map(o -> o.isinCode).collect(Collectors.toSet());
		var sonySet    = SonyFund.getList().stream().map(o -> o.isinCode).collect(Collectors.toSet());
		logger.info("gmoSet     {}", gmoSet.size());
		logger.info("nikkoSet   {}", nikkoSet.size());
		logger.info("nomuraSet  {}", nomuraSet.size());
		logger.info("rakutenSet {}", rakutenSet.size());
		logger.info("sbiSet     {}", sbiSet.size());
		logger.info("sonySet    {}", sonySet.size());

		int countNoPrice  = 0;
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
				Period period = fund.inceptionDate.until(LocalDate.now());
				stats.age = new BigDecimal(String.format("%d.%02d", period.getYears(), period.getMonths()));
			}
			
			// Use toushin category
			stats.investingAsset = fund.investingAsset;
			stats.investingArea  = fund.investingArea;
			stats.indexFundType  = fund.indexFundType.replace("該当なし", "アクティブ型").replace("型", "");
			
			stats.expenseRatio = fund.expenseRatio.multiply(CONSUMPTION_TAX_RATE);
			stats.buyFeeMax    = fund.buyFeeMax.multiply(CONSUMPTION_TAX_RATE);
			stats.nav          = lastPrice.nav;
			stats.divc         = fund.divFreq;
			
			{
				// calculate latest RSI using priceArray
				LocalDate endDate    = lastPrice.date;
				LocalDate startDate  = endDate.minusYears(1).plusDays(1);
				var       indexRange = DailyValue.indexRange(priceArray, startDate, endDate);
				if (indexRange.isValid() && BigDecimalArray.RSI_PERIOD <= indexRange.size()) {
					BigDecimal[] rsiArray = BigDecimalArray.toRSI(priceArray, indexRange.startIndex, indexRange.stopIndexPlusOne, o -> o.value);
					stats.rsi = rsiArray[rsiArray.length - 1];
				} else {
					logger.info("rsi  !  {}  {}  {}  {}  {}", isinCode, startDate, endDate, indexRange, indexRange.size());
					stats.rsi = null;
				}
			}
			
			// 1 year
			{
				int nYear = 1;
				AnnualStats  aStats = AnnualStats.getInstance(monthlyStatsArray, nYear);
				stats.sd1Y     = aStats == null ? null : aStats.standardDeviation;
				stats.div1Y    = aStats == null ? null : aStats.dividend;
				stats.yield1Y  = aStats == null ? null : aStats.yield;
				stats.return1Y = aStats == null ? null : aStats.returns;
			}
			// 3 year
			{
				int nYear = 3;
				AnnualStats  aStats = AnnualStats.getInstance(monthlyStatsArray, nYear);
				stats.sd3Y     = aStats == null ? null : aStats.standardDeviation;
				stats.div3Y    = aStats == null ? null : aStats.dividend;
				stats.yield3Y  = aStats == null ? null : aStats.yield;
				stats.return3Y = aStats == null ? null : aStats.returns;
			}
			// 5 year
			{
				int nYear = 5;
				AnnualStats  aStats = AnnualStats.getInstance(monthlyStatsArray, nYear);
				stats.sd5Y     = aStats == null ? null : aStats.standardDeviation;
				stats.div5Y    = aStats == null ? null : aStats.dividend;
				stats.yield5Y  = aStats == null ? null : aStats.yield;
				stats.return5Y = aStats == null ? null : aStats.returns;
			}
			// 10 year
			{
				int nYear = 10;
				AnnualStats  aStats = AnnualStats.getInstance(monthlyStatsArray, nYear);
				stats.sd10Y     = aStats == null ? null : aStats.standardDeviation;
				stats.div10Y    = aStats == null ? null : aStats.dividend;
				stats.yield10Y  = aStats == null ? null : aStats.yield;
				stats.return10Y = aStats == null ? null : aStats.returns;
			}
			
			stats.divQ1Y   = (nikkei == null || nikkei.divScore1Y.isEmpty()) ? null : new BigDecimal(nikkei.divScore1Y);
			stats.divQ3Y   = (nikkei == null || nikkei.divScore3Y.isEmpty()) ? null : new BigDecimal(nikkei.divScore3Y);
			stats.divQ5Y   = (nikkei == null || nikkei.divScore5Y.isEmpty()) ? null : new BigDecimal(nikkei.divScore5Y);
			stats.divQ10Y  = (nikkei == null || nikkei.divScore10Y.isEmpty()) ? null : new BigDecimal(nikkei.divScore10Y);
			
			stats.name     = fund.name;
			
			if (stats.stockCode.isEmpty()) {
				stats.gmo      = gmoSet.contains(fund.isinCode)     ? GMOFund.getSalesFee(isinCode, MINUS_ONE)     : null;
				stats.nikko    = nikkoSet.contains(fund.isinCode)   ? NikkoFund.getSalesFee(isinCode, MINUS_ONE)   : null;
				stats.nomura   = nomuraSet.contains(fund.isinCode)  ? NomuraFund.getSalesFee(isinCode, MINUS_ONE)  : null;
				stats.rakuten  = rakutenSet.contains(fund.isinCode) ? BigDecimal.ZERO : null;
				stats.sbi      = sbiSet.contains(fund.isinCode)     ? BigDecimal.ZERO : null;
				stats.sony     = sonySet.contains(fund.isinCode)    ? BigDecimal.ZERO : null;
			} else {
				stats.gmo      = BigDecimal.ZERO;
				stats.nikko    = BigDecimal.ZERO;
				stats.nomura   = BigDecimal.ZERO;
				stats.rakuten  = BigDecimal.ZERO;
				stats.sbi      = BigDecimal.ZERO;
				stats.sony     = BigDecimal.ZERO;
			}
			
			statsList.add(stats);
		}
		
		// Cannot save with null value
//		logger.info("statsList  {}  {}", statsList.size(), Stats.getPath());
//		Stats.save(statsList);

		logger.info("fundList       {}", fundList.size());
		logger.info("nikkeiMap      {}", nikkeiMap.size());
		logger.info("countNoPrice   {}", countNoPrice);
		logger.info("countNoNikkei  {}", countNoNikkei);
		
		return statsList;
	}
	
	private static void saveStatsReport(List<Stats> statsList) {
		String urlReport;
		{
			String timestamp  = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss").format(LocalDateTime.now());
			String name       = String.format("stats-%s.ods", timestamp);
			String pathReport = Storage.Toushin.getPath(PREFIX_REPORT, name);
			urlReport  = StringUtil.toURLString(pathReport);
		}

		logger.info("urlReport {}", urlReport);
		logger.info("docLoad   {}", URL_TEMPLATE);
		try (
			SpreadSheet docLoad = new SpreadSheet(URL_TEMPLATE, true);
			SpreadSheet docSave = new SpreadSheet();) {				
			String sheetName = Sheet.getSheetName(Stats.class);
			logger.info("sheet {}", sheetName);
			docSave.importSheet(docLoad, sheetName, docSave.getSheetCount());
			Sheet.fillSheet(docSave, statsList);
			
			// remove first sheet
			docSave.removeSheet(docSave.getSheetName(0));

			docSave.store(urlReport);
			logger.info("output {}", urlReport);
		}
	}
	
	public static void main(String[] args) {
		logger.info("START");
		
		List<Stats> statsList = getStatsList();
		
		logger.info("statsList  {}  {}", statsList.size(), Stats.getPath());
//		Stats.save(statsList);
		
		saveStatsReport(statsList);
		
		logger.info("STOP");
		System.exit(0);
	}
}
