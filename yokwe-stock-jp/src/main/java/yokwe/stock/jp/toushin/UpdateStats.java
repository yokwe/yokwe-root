package yokwe.stock.jp.toushin;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import yokwe.stock.jp.Storage;
import yokwe.stock.jp.gmo.GMOFund;
import yokwe.stock.jp.nikkei.NikkeiFund;
import yokwe.stock.jp.nikko.NikkoFund;
import yokwe.stock.jp.nomura.NomuraFund;
import yokwe.stock.jp.rakuten.RakutenFund;
import yokwe.stock.jp.sbi.SBIFund;
import yokwe.stock.jp.smbctb.SMBCTBFund;
import yokwe.stock.jp.sony.SonyFund;
import yokwe.util.DoubleUtil;
import yokwe.util.StringUtil;
import yokwe.util.finance.DailyPriceDiv;
import yokwe.util.finance.Finance;
import yokwe.util.finance.FundStats;
import yokwe.util.libreoffice.Sheet;
import yokwe.util.libreoffice.SpreadSheet;

public class UpdateStats {
	private static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();
	
	private static final String PREFIX_REPORT = "report";
	private static final String URL_TEMPLATE  = StringUtil.toURLString(Storage.Toushin.getPath("TEMPLATE_TOUSHIN_STATS.ods"));

	public static final  BigDecimal CONSUMPTION_TAX_RATE  = new BigDecimal("1.1"); // 10 percent
	
	private static final BigDecimal MINUS_ONE = BigDecimal.ONE.negate();
	
	private static final LocalDate LAST_DATE_OF_LAST_MONTH = LocalDate.now().withDayOfMonth(1).minusDays(1);
	
	private static List<Stats> getStatsList() {
		List<Stats> statsList = new ArrayList<>();
		
		List<Fund> fundList = Fund.getList();
		logger.info("fundList   {}", fundList.size());
		
		var nikkeiMap = NikkeiFund.getMap();
		logger.info("nikkeiMap  {}", nikkeiMap.size());
		
		var gmoSet     = GMOFund.getList().stream().map(o -> o.isinCode).collect(Collectors.toSet());
		var nikkoSet   = NikkoFund.getList().stream().map(o -> o.isinCode).collect(Collectors.toSet());
		var nomuraSet  = NomuraFund.getList().stream().map(o -> o.isinCode).collect(Collectors.toSet());
		var rakutenSet = RakutenFund.getList().stream().map(o -> o.isinCode).collect(Collectors.toSet());
		var sbiSet     = SBIFund.getList().stream().map(o -> o.isinCode).collect(Collectors.toSet());
		var sonySet    = SonyFund.getList().stream().map(o -> o.isinCode).collect(Collectors.toSet());
		var smbctbSet  = SMBCTBFund.getList().stream().map(o -> o.isinCode).collect(Collectors.toSet());
		logger.info("gmoSet     {}", gmoSet.size());
		logger.info("nikkoSet   {}", nikkoSet.size());
		logger.info("nomuraSet  {}", nomuraSet.size());
		logger.info("rakutenSet {}", rakutenSet.size());
		logger.info("sbiSet     {}", sbiSet.size());
		logger.info("sonySet    {}", sonySet.size());
		logger.info("smbctbSet  {}", smbctbSet.size());

		int countNoPrice  = 0;
		int countNoNikkei = 0;
		
		int count = 0;
		for(var fund: fundList) {
			final String isinCode = fund.isinCode;
			
			count++;
			if ((count % 500) == 1) logger.info("{}", String.format("%4d / %4d", count, fundList.size()));
			
			FundStats  fundStats;
			BigDecimal nav;
			{
				Price[] priceArray = Price.getList(isinCode).stream().toArray(Price[]::new);
				if (priceArray.length == 0) {
					countNoPrice++;
					continue;
				}
				
				// use latest value for nav
				nav = null;
				for(var e: priceArray) {
					if (e.date.isAfter(LAST_DATE_OF_LAST_MONTH)) break;
					nav = e.nav;
				}
				
				Dividend[]      divArray           = Dividend.getList(isinCode).stream().toArray(Dividend[]::new);
				DailyPriceDiv[] dailyPriceDivArray = DailyPriceDiv.toDailyPriceDivArray(
					priceArray, o -> o.date, o -> o.price.doubleValue(),
					divArray,   o -> o.date, o -> o.amount.doubleValue());
				fundStats = FundStats.getInstance(isinCode, dailyPriceDivArray);
			}

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
			stats.age        = Finance.durationInYearMonth(stats.inception, LAST_DATE_OF_LAST_MONTH);
			
			// Use toushin category
			stats.investingAsset = fund.investingAsset;
			stats.investingArea  = fund.investingArea;
			stats.indexFundType  = fund.indexFundType.replace("該当なし", "アクティブ型").replace("型", "");
			
			stats.expenseRatio = fund.expenseRatio.multiply(CONSUMPTION_TAX_RATE);
			stats.buyFeeMax    = fund.buyFeeMax.multiply(CONSUMPTION_TAX_RATE);
			stats.nav          = nav;
			stats.divc         = fund.divFreq;
			
			// 1 year
			{
				int nMonth  = 12;
				int nOffset = 0;
				
				stats.sd1Y    = (fundStats == null || !fundStats.contains(nMonth, nOffset)) ? null : DoubleUtil.toBigDecimal(fundStats.risk(nMonth, nOffset));
				stats.div1Y   = (fundStats == null || !fundStats.contains(nMonth, nOffset)) ? null : DoubleUtil.toBigDecimal(fundStats.dividend(nMonth, nOffset));
				stats.yield1Y = (fundStats == null || !fundStats.contains(nMonth, nOffset)) ? null : DoubleUtil.toBigDecimal(fundStats.yield(nMonth, nOffset));
				stats.ror1Y   = (fundStats == null || !fundStats.contains(nMonth, nOffset)) ? null : DoubleUtil.toBigDecimal(fundStats.rateOfReturn(nMonth, nOffset));
				stats.rsi     = (fundStats == null || !fundStats.contains(nMonth, nOffset)) ? null : DoubleUtil.toBigDecimal(fundStats.rsi(nMonth, nOffset));
			}
			// 3 year
			{
				int nMonth = 36;
				int nOffset = 0;
				
				stats.sd3Y    = (fundStats == null || !fundStats.contains(nMonth, nOffset)) ? null : DoubleUtil.toBigDecimal(fundStats.risk(nMonth, nOffset));
				stats.div3Y   = (fundStats == null || !fundStats.contains(nMonth, nOffset)) ? null : DoubleUtil.toBigDecimal(fundStats.dividend(nMonth, nOffset));
				stats.yield3Y = (fundStats == null || !fundStats.contains(nMonth, nOffset)) ? null : DoubleUtil.toBigDecimal(fundStats.yield(nMonth, nOffset));
				stats.ror3Y   = (fundStats == null || !fundStats.contains(nMonth, nOffset)) ? null : DoubleUtil.toBigDecimal(fundStats.rateOfReturn(nMonth, nOffset));
			}
			// 5 year
			{
				int nMonth = 60;
				int nOffset = 0;
				
				stats.sd5Y    = (fundStats == null || !fundStats.contains(nMonth, nOffset)) ? null : DoubleUtil.toBigDecimal(fundStats.risk(nMonth, nOffset));
				stats.div5Y   = (fundStats == null || !fundStats.contains(nMonth, nOffset)) ? null : DoubleUtil.toBigDecimal(fundStats.dividend(nMonth, nOffset));
				stats.yield5Y = (fundStats == null || !fundStats.contains(nMonth, nOffset)) ? null : DoubleUtil.toBigDecimal(fundStats.yield(nMonth, nOffset));
				stats.ror5Y   = (fundStats == null || !fundStats.contains(nMonth, nOffset)) ? null : DoubleUtil.toBigDecimal(fundStats.rateOfReturn(nMonth, nOffset));
			}
			// 10 year
			{
				int nMonth = 120;
				int nOffset = 0;
				
				stats.sd10Y    = (fundStats == null || !fundStats.contains(nMonth, nOffset)) ? null : DoubleUtil.toBigDecimal(fundStats.risk(nMonth, nOffset));
				stats.div10Y   = (fundStats == null || !fundStats.contains(nMonth, nOffset)) ? null : DoubleUtil.toBigDecimal(fundStats.dividend(nMonth, nOffset));
				stats.yield10Y = (fundStats == null || !fundStats.contains(nMonth, nOffset)) ? null : DoubleUtil.toBigDecimal(fundStats.yield(nMonth, nOffset));
				stats.ror10Y   = (fundStats == null || !fundStats.contains(nMonth, nOffset)) ? null : DoubleUtil.toBigDecimal(fundStats.rateOfReturn(nMonth, nOffset));
			}
			
			stats.divQ1Y  = (nikkei == null || nikkei.divScore1Y.isEmpty()) ? null : new BigDecimal(nikkei.divScore1Y);
			stats.divQ3Y  = (nikkei == null || nikkei.divScore3Y.isEmpty()) ? null : new BigDecimal(nikkei.divScore3Y);
			stats.divQ5Y  = (nikkei == null || nikkei.divScore5Y.isEmpty()) ? null : new BigDecimal(nikkei.divScore5Y);
			stats.divQ10Y = (nikkei == null || nikkei.divScore10Y.isEmpty()) ? null : new BigDecimal(nikkei.divScore10Y);
			
			stats.name     = fund.name;
			
			if (stats.stockCode.isEmpty()) {
				stats.gmo     = gmoSet.contains(fund.isinCode)     ? GMOFund.getSalesFee(isinCode, MINUS_ONE)     : null;
				stats.nikko   = nikkoSet.contains(fund.isinCode)   ? NikkoFund.getSalesFee(isinCode, MINUS_ONE)   : null;
				stats.nomura  = nomuraSet.contains(fund.isinCode)  ? NomuraFund.getSalesFee(isinCode, MINUS_ONE)  : null;
				stats.rakuten = rakutenSet.contains(fund.isinCode) ? BigDecimal.ZERO : null;
				stats.sbi     = sbiSet.contains(fund.isinCode)     ? BigDecimal.ZERO : null;
				stats.sony    = sonySet.contains(fund.isinCode)    ? BigDecimal.ZERO : null;
				stats.smbctb  = smbctbSet.contains(fund.isinCode)  ? BigDecimal.ONE : null;
			} else {
				stats.gmo     = null;
				stats.nikko   = null;
				stats.nomura  = null;
				stats.rakuten = null;
				stats.sbi     = null;
				stats.sony    = null;
				stats.smbctb  = null;
			}
			
			// special case
			if (fund.redemptionDate.compareTo(Fund.NO_REDEMPTION_DATE) == 0) stats.redemption = null;

			if (stats.div1Y  != null && stats.div1Y.compareTo(BigDecimal.ZERO) == 0)  stats.yield1Y  = stats.divQ1Y = null;
			if (stats.div3Y  != null && stats.div3Y.compareTo(BigDecimal.ZERO) == 0)  stats.yield3Y  = stats.divQ3Y = null;
			if (stats.div5Y  != null && stats.div5Y.compareTo(BigDecimal.ZERO) == 0)  stats.yield5Y  = stats.divQ5Y = null;
			if (stats.div10Y != null && stats.div10Y.compareTo(BigDecimal.ZERO) == 0) stats.yield10Y = stats.divQ10Y = null;
			
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
		
		logger.info("LAST_DATE_OF_LAST_MONTH  {}", LAST_DATE_OF_LAST_MONTH);		
		
		List<Stats> statsList = getStatsList();
		
		logger.info("statsList  {}  {}", statsList.size(), Stats.getPath());
//		Stats.save(statsList);
		
		saveStatsReport(statsList);
		
		logger.info("STOP");
		System.exit(0);
	}
}
