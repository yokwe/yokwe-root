package yokwe.finance.report;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import yokwe.finance.fund.StorageFund;
import yokwe.finance.provider.click.StorageClick;
import yokwe.finance.provider.nikkei.DivScoreType;
import yokwe.finance.provider.nikkei.StorageNikkei;
import yokwe.finance.provider.nikko.StorageNikko;
import yokwe.finance.provider.nomura.StorageNomura;
import yokwe.finance.provider.prestia.StoragePrestia;
import yokwe.finance.provider.rakuten.StorageRakuten;
import yokwe.finance.provider.sbi.StorageSBI;
import yokwe.finance.provider.sony.StorageSony;
import yokwe.finance.stats.MonthlyStats;
import yokwe.finance.type.DailyValue;
import yokwe.finance.type.FundInfoJP;
import yokwe.util.DoubleUtil;
import yokwe.util.StringUtil;
import yokwe.util.finance.Finance;
import yokwe.util.libreoffice.LibreOffice;
import yokwe.util.libreoffice.Sheet;
import yokwe.util.libreoffice.SpreadSheet;

public class UpdateFundStats {
	private static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();
	
	private static final String URL_TEMPLATE  = StringUtil.toURLString("data/form/FUND_STATS.ods");
	
	private static final LocalDate  LAST_DATE_OF_LAST_MONTH = LocalDate.now().withDayOfMonth(1).minusDays(1);
	private static final BigDecimal CONSUMPTION_TAX_RATE    = new BigDecimal("1.1"); // 10 percent


	private static void generateReport(List<FundStatsType> statsList) {
		String urlReport;
		{
			String timestamp  = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss").format(LocalDateTime.now());
			String name       = String.format("fund-stats-%s.ods", timestamp);
			String pathReport = StorageReport.getPath("fund-stats", name);
			
			urlReport  = StringUtil.toURLString(pathReport);
		}

		logger.info("urlReport {}", urlReport);
		logger.info("docLoad   {}", URL_TEMPLATE);
		try {
			// start LibreOffice process
			LibreOffice.initialize();
			
			SpreadSheet docLoad = new SpreadSheet(URL_TEMPLATE, true);
			SpreadSheet docSave = new SpreadSheet();
			
			String sheetName = Sheet.getSheetName(FundStatsType.class);
			logger.info("sheet     {}", sheetName);
			docSave.importSheet(docLoad, sheetName, docSave.getSheetCount());
			Sheet.fillSheet(docSave, statsList);
			
			// remove first sheet
			docSave.removeSheet(docSave.getSheetName(0));

			docSave.store(urlReport);
			logger.info("output    {}", urlReport);
			
			docLoad.close();
			logger.info("close     docLoad");
			docSave.close();
			logger.info("close     docSave");
		} finally {
			// stop LibreOffice process
			LibreOffice.terminate();
		}
	}
	
	
	private static List<FundStatsType> getStatsList() {
		var statsList = new ArrayList<FundStatsType>();
		
		var fundList = StorageFund.FundInfo.getList();
		logger.info("fundList   {}", fundList.size());
		
		var nikkeiMap = StorageNikkei.DivScore.getMap();
		logger.info("nikkeiMap  {}", nikkeiMap.size());
		
		var clickMap   = StorageClick.TradingFundClick.getMap();
		var nikkoMap   = StorageNikko.TradingFundNikko.getMap();
		var nomuraMap  = StorageNomura.TradingFundNomura.getMap();
		var rakutenMap = StorageRakuten.TradingFundRakuten.getMap();
		var sbiMap     = StorageSBI.TradingFundSBI.getMap();
		var sonyMap    = StorageSony.TradingFundSony.getMap();
		var prestiaMap = StoragePrestia.TradingFundPrestia.getMap();
		logger.info("clickMap   {}", clickMap.size());
		logger.info("nikkoMap   {}", nikkoMap.size());
		logger.info("nomuraMap  {}", nomuraMap.size());
		logger.info("rakutenMap {}", rakutenMap.size());
		logger.info("sbiMapt    {}", sbiMap.size());
		logger.info("sonyMap    {}", sonyMap.size());
		logger.info("prestiaMap {}", prestiaMap.size());
		
		var nisaFundMap = StorageRakuten.NisaFundRakuten.getMap();
		logger.info("nisaFund   {}", nisaFundMap.size());
		var nisaETFMap = StorageRakuten.NisaETFJPRakuten.getMap();
		logger.info("nisaETF    {}", nisaETFMap.size());

		int countNoPrice  = 0;
		int countNoNikkei = 0;
		
		int count = 0;
		for(var fund: fundList) {
			final String isinCode = fund.isinCode;
			
			count++;
			if ((count % 500) == 1) logger.info("{}", String.format("%4d / %4d", count, fundList.size()));
			
			MonthlyStats  monthlyStats;
			BigDecimal    nav;
			{
				var fundPriceList = StorageFund.FundPrice.getList(isinCode);
				if (fundPriceList.isEmpty()) {
					countNoPrice++;
					continue;
				}

				var priceList = fundPriceList.stream().map(o -> new DailyValue(o.date, o.price)).collect(Collectors.toList());
				var divList   = MonthlyStats.getDivList(priceList, StorageFund.FundDiv.getList(isinCode));
				
				
				// use last element for nav
				nav = fundPriceList.get(fundPriceList.size() - 1).nav;
				
				monthlyStats = MonthlyStats.getInstance(isinCode, priceList, divList);
			}

			var nikkei = nikkeiMap.get(isinCode);
			if (nikkei == null) {
				countNoNikkei++;
			}
			
			var fundStats = new FundStatsType();
			
			fundStats.isinCode  = fund.isinCode;
			fundStats.fundCode  = fund.fundCode;
			fundStats.stockCode = fund.stockCode;
			
			fundStats.inception  = fund.inceptionDate;
			fundStats.redemption = fund.redemptionDate;
			fundStats.age        = Finance.durationInYearMonth(fundStats.inception, LAST_DATE_OF_LAST_MONTH);
			
			// Use toushin category
			fundStats.investingAsset = fund.investingAsset;
			fundStats.investingArea  = fund.investingArea;
			fundStats.indexFundType  = fund.indexFundType.replace("該当なし", "アクティブ型").replace("型", "");
			
			fundStats.expenseRatio = fund.expenseRatio.multiply(CONSUMPTION_TAX_RATE);
			fundStats.buyFeeMax    = fund.buyFeeMax.multiply(CONSUMPTION_TAX_RATE);
			fundStats.nav          = nav;
			fundStats.divc         = fund.divFreq;
			
			// 1 year
			{
				int nMonth  = 12;
				int nOffset = 0;
				
				fundStats.sd1Y    = (monthlyStats == null || !monthlyStats.contains(nMonth, nOffset)) ? null : DoubleUtil.toBigDecimal(monthlyStats.risk(nMonth, nOffset));
				fundStats.div1Y   = (monthlyStats == null || !monthlyStats.contains(nMonth, nOffset)) ? null : DoubleUtil.toBigDecimal(monthlyStats.dividend(nMonth, nOffset));
				fundStats.yield1Y = (monthlyStats == null || !monthlyStats.contains(nMonth, nOffset)) ? null : DoubleUtil.toBigDecimal(monthlyStats.yield(nMonth, nOffset));
				fundStats.ror1Y   = (monthlyStats == null || !monthlyStats.contains(nMonth, nOffset)) ? null : DoubleUtil.toBigDecimal(monthlyStats.rateOfReturn(nMonth, nOffset));
				fundStats.rsi     = (monthlyStats == null || !monthlyStats.contains(nMonth, nOffset)) ? null : DoubleUtil.toBigDecimal(monthlyStats.rsi(nMonth, nOffset));
			}
			// 3 year
			{
				int nMonth = 36;
				int nOffset = 0;
				
				fundStats.sd3Y    = (monthlyStats == null || !monthlyStats.contains(nMonth, nOffset)) ? null : DoubleUtil.toBigDecimal(monthlyStats.risk(nMonth, nOffset));
				fundStats.div3Y   = (monthlyStats == null || !monthlyStats.contains(nMonth, nOffset)) ? null : DoubleUtil.toBigDecimal(monthlyStats.dividend(nMonth, nOffset));
				fundStats.yield3Y = (monthlyStats == null || !monthlyStats.contains(nMonth, nOffset)) ? null : DoubleUtil.toBigDecimal(monthlyStats.yield(nMonth, nOffset));
				fundStats.ror3Y   = (monthlyStats == null || !monthlyStats.contains(nMonth, nOffset)) ? null : DoubleUtil.toBigDecimal(monthlyStats.rateOfReturn(nMonth, nOffset));
			}
			// 5 year
			{
				int nMonth = 60;
				int nOffset = 0;
				
				fundStats.sd5Y    = (monthlyStats == null || !monthlyStats.contains(nMonth, nOffset)) ? null : DoubleUtil.toBigDecimal(monthlyStats.risk(nMonth, nOffset));
				fundStats.div5Y   = (monthlyStats == null || !monthlyStats.contains(nMonth, nOffset)) ? null : DoubleUtil.toBigDecimal(monthlyStats.dividend(nMonth, nOffset));
				fundStats.yield5Y = (monthlyStats == null || !monthlyStats.contains(nMonth, nOffset)) ? null : DoubleUtil.toBigDecimal(monthlyStats.yield(nMonth, nOffset));
				fundStats.ror5Y   = (monthlyStats == null || !monthlyStats.contains(nMonth, nOffset)) ? null : DoubleUtil.toBigDecimal(monthlyStats.rateOfReturn(nMonth, nOffset));
			}
			// 10 year
			{
				int nMonth = 120;
				int nOffset = 0;
				
				fundStats.sd10Y    = (monthlyStats == null || !monthlyStats.contains(nMonth, nOffset)) ? null : DoubleUtil.toBigDecimal(monthlyStats.risk(nMonth, nOffset));
				fundStats.div10Y   = (monthlyStats == null || !monthlyStats.contains(nMonth, nOffset)) ? null : DoubleUtil.toBigDecimal(monthlyStats.dividend(nMonth, nOffset));
				fundStats.yield10Y = (monthlyStats == null || !monthlyStats.contains(nMonth, nOffset)) ? null : DoubleUtil.toBigDecimal(monthlyStats.yield(nMonth, nOffset));
				fundStats.ror10Y   = (monthlyStats == null || !monthlyStats.contains(nMonth, nOffset)) ? null : DoubleUtil.toBigDecimal(monthlyStats.rateOfReturn(nMonth, nOffset));
			}
			
			fundStats.divScore1Y  = (nikkei == null || !DivScoreType.hasValue(nikkei.score1Y))  ? null : nikkei.score1Y;
			fundStats.divScore3Y  = (nikkei == null || !DivScoreType.hasValue(nikkei.score3Y))  ? null : nikkei.score3Y;
			fundStats.divScore5Y  = (nikkei == null || !DivScoreType.hasValue(nikkei.score5Y))  ? null : nikkei.score5Y;
			fundStats.divScore10Y = (nikkei == null || !DivScoreType.hasValue(nikkei.score10Y)) ? null : nikkei.score10Y;
			
			fundStats.name     = fund.name;
			
			if (fundStats.stockCode.isEmpty()) {
				// FUND
				fundStats.click   = !clickMap.containsKey(fund.isinCode)   ? null: clickMap.get(fund.isinCode).salesFee;
				fundStats.nikko   = !nikkoMap.containsKey(fund.isinCode)   ? null: nikkoMap.get(fund.isinCode).salesFee;
				fundStats.nomura  = !nomuraMap.containsKey(fund.isinCode)  ? null: nomuraMap.get(fund.isinCode).salesFee;
				fundStats.rakuten = !rakutenMap.containsKey(fund.isinCode) ? null: rakutenMap.get(fund.isinCode).salesFee;
				fundStats.sbi     = !sbiMap.containsKey(fund.isinCode)     ? null: sbiMap.get(fund.isinCode).salesFee;
				fundStats.sony    = !sonyMap.containsKey(fund.isinCode)    ? null: sonyMap.get(fund.isinCode).salesFee;
				fundStats.prestia = !prestiaMap.containsKey(fund.isinCode) ? null: prestiaMap.get(fund.isinCode).salesFee;
				
				if (nisaFundMap.containsKey(isinCode)) {
					fundStats.nisa = BigDecimal.valueOf(nisaFundMap.get(isinCode).accumulable.value);
				} else {
					fundStats.nisa = null;
				}
			} else {
				// ETF
				fundStats.click   = BigDecimal.ZERO;;
				fundStats.nikko   = BigDecimal.ZERO;;
				fundStats.nomura  = BigDecimal.ZERO;;
				fundStats.rakuten = BigDecimal.ZERO;;
				fundStats.sbi     = BigDecimal.ZERO;;
				fundStats.sony    = null;
				fundStats.prestia = null;
				
				var stockCode = fundStats.stockCode;
				if (nisaETFMap.containsKey(stockCode)) {
					fundStats.nisa = BigDecimal.ZERO;
				} else {
					fundStats.nisa = null;
				}
			}
			
			// special case
			if (fund.redemptionDate.toString().compareTo(FundInfoJP.NO_REDEMPTION_DATE_STRING) == 0) fundStats.redemption = null;

			if (fundStats.div1Y  != null && fundStats.div1Y.compareTo(BigDecimal.ZERO) == 0)  fundStats.yield1Y  = fundStats.divScore1Y = null;
			if (fundStats.div3Y  != null && fundStats.div3Y.compareTo(BigDecimal.ZERO) == 0)  fundStats.yield3Y  = fundStats.divScore3Y = null;
			if (fundStats.div5Y  != null && fundStats.div5Y.compareTo(BigDecimal.ZERO) == 0)  fundStats.yield5Y  = fundStats.divScore5Y = null;
			if (fundStats.div10Y != null && fundStats.div10Y.compareTo(BigDecimal.ZERO) == 0) fundStats.yield10Y = fundStats.divScore10Y = null;
			
			statsList.add(fundStats);
		}
		
		// Cannot save with null value
//		logger.info("statsList  {}  {}", statsList.size(), Stats.getPath());
//		Stats.save(statsList);

		logger.info("fundList       {}", fundList.size());
		logger.info("nikkeiMap      {}", nikkeiMap.size());
		logger.info("countNoPrice   {}", countNoPrice);
		logger.info("countNoNikkei  {}", countNoNikkei);
		logger.info("statsList      {}", statsList.size());
		
		return statsList;
	}
	
	
	private static void update() {
		List<FundStatsType> statsList = getStatsList();
		
		// Cannot save with null value
//		logger.info("save {} {}", statsList.size(), StorageReport.FundStats.getPath());
//		StorageReport.FundStats.save(statsList);
		
		generateReport(statsList);
	}
	
	
	public static void main(String[] args) {
		logger.info("START");
		
		update();
				
		logger.info("STOP");
	}
}
