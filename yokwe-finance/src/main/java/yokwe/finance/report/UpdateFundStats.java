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
import yokwe.finance.stats.FundStats;
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
				var fundPriceList = StorageFund.FundPrice.getList(isinCode);
				
				var priceList = fundPriceList.stream().map(o -> new DailyValue(o.date, o.price)).collect(Collectors.toList());
				if (priceList.size() == 0) {
					countNoPrice++;
					continue;
				}
				
				// use latest value for nav
				nav = null;
				for(var e: fundPriceList) {
					if (e.date.isAfter(LAST_DATE_OF_LAST_MONTH)) break;
					nav = e.nav;
				}
				
				var divMap = StorageFund.FundDiv.getMap(isinCode);
				var divList = new ArrayList<DailyValue>(priceList.size());
				
				for(int i = 0; i < priceList.size(); i++) {
					var date = priceList.get(i).date;
					var value = divMap.containsKey(date) ? divMap.get(date).value : BigDecimal.ZERO;
					divList.add(new DailyValue(date, value));
				}
				
				fundStats = FundStats.getInstance(isinCode, priceList, divList);
			}

			var nikkei = nikkeiMap.get(isinCode);
			if (nikkei == null) {
				countNoNikkei++;
			}
			
			var stats = new FundStatsType();
			
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
			
			stats.divScore1Y  = (nikkei == null || !DivScoreType.hasValue(nikkei.score1Y))  ? null : nikkei.score1Y;
			stats.divScore3Y  = (nikkei == null || !DivScoreType.hasValue(nikkei.score3Y))  ? null : nikkei.score3Y;
			stats.divScore5Y  = (nikkei == null || !DivScoreType.hasValue(nikkei.score5Y))  ? null : nikkei.score5Y;
			stats.divScore10Y = (nikkei == null || !DivScoreType.hasValue(nikkei.score10Y)) ? null : nikkei.score10Y;
			
			stats.name     = fund.name;
			
			if (stats.stockCode.isEmpty()) {
				stats.click   = !clickMap.containsKey(fund.isinCode)   ? null: clickMap.get(fund.isinCode).salesFee;
				stats.nikko   = !nikkoMap.containsKey(fund.isinCode)   ? null: nikkoMap.get(fund.isinCode).salesFee;
				stats.nomura  = !nomuraMap.containsKey(fund.isinCode)  ? null: nomuraMap.get(fund.isinCode).salesFee;
				stats.rakuten = !rakutenMap.containsKey(fund.isinCode) ? null: rakutenMap.get(fund.isinCode).salesFee;
				stats.sbi     = !sbiMap.containsKey(fund.isinCode)     ? null: sbiMap.get(fund.isinCode).salesFee;
				stats.sony    = !sonyMap.containsKey(fund.isinCode)    ? null: sonyMap.get(fund.isinCode).salesFee;
				stats.prestia = !prestiaMap.containsKey(fund.isinCode) ? null: prestiaMap.get(fund.isinCode).salesFee;
			} else {
				stats.click   = null;
				stats.nikko   = null;
				stats.nomura  = null;
				stats.rakuten = null;
				stats.sbi     = null;
				stats.sony    = null;
				stats.prestia = null;
			}
			
			// special case
			if (fund.redemptionDate.toString().compareTo(FundInfoJP.NO_REDEMPTION_DATE_STRING) == 0) stats.redemption = null;

			if (stats.div1Y  != null && stats.div1Y.compareTo(BigDecimal.ZERO) == 0)  stats.yield1Y  = stats.divScore1Y = null;
			if (stats.div3Y  != null && stats.div3Y.compareTo(BigDecimal.ZERO) == 0)  stats.yield3Y  = stats.divScore3Y = null;
			if (stats.div5Y  != null && stats.div5Y.compareTo(BigDecimal.ZERO) == 0)  stats.yield5Y  = stats.divScore5Y = null;
			if (stats.div10Y != null && stats.div10Y.compareTo(BigDecimal.ZERO) == 0) stats.yield10Y = stats.divScore10Y = null;
			
			statsList.add(stats);
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
