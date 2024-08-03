package yokwe.finance.report;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import yokwe.finance.provider.rakuten.StorageRakuten;
import yokwe.finance.provider.yahoo.StorageYahoo;
import yokwe.finance.stats.MonthlyStats;
import yokwe.finance.stock.StorageStock;
import yokwe.finance.type.DailyValue;
import yokwe.finance.type.StockInfoUSType;
import yokwe.finance.type.TradingStockType;
import yokwe.util.DoubleUtil;
import yokwe.util.MarketHoliday;
import yokwe.util.StringUtil;
import yokwe.util.finance.Finance;
import yokwe.util.libreoffice.LibreOffice;
import yokwe.util.libreoffice.Sheet;
import yokwe.util.libreoffice.SpreadSheet;

public class UpdateStockStatsUSMonthly {
	private static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();
	
	private static final String URL_TEMPLATE  = StringUtil.toURLString("data/form/STOCK_STATS_US_MONTHLY.ods");
	
	private static String tradingString(Map<String, TradingStockType> map, String stockCode) {
		return map.containsKey(stockCode) ? Integer.toString(map.get(stockCode).feeType.value): "";
	}
	
	
	private static List<StockStatsUSMonthly> getStatsList() {
		var dateStop  = MarketHoliday.US.getLastTradingDate();
		var dateStart = dateStop.minusYears(1).plusDays(1);
		
		logger.info("date range  {}  -  {}", dateStart, dateStop);
		
		var list = new ArrayList<StockStatsUSMonthly>();
		{
			var rakutenMap     = StorageRakuten.TradingStockRakuten.getMap();
			var companyInfoMap = StorageYahoo.CompanyInfoUSYahoo.getMap();
			
			for(var stockInfo: StorageStock.StockInfoUSTrading.getList()) {
				var stockCode = stockInfo.stockCode;
				
				MonthlyStats  monthlyStats;
				{
					var priceList = StorageStock.StockPriceUS.getList(stockCode).stream().map(o -> new DailyValue(o.date, o.close)).collect(Collectors.toList());
					var divList   = MonthlyStats.getDivList(priceList, StorageStock.StockDivUS.getList(stockCode));
					
					if (priceList.size() < 10) {
						logger.info("skip  {}  {}  {}", priceList.size(), stockCode, stockInfo.name);
						continue;
					}
					monthlyStats = MonthlyStats.getInstance(stockCode, priceList, divList);
				}
				if (monthlyStats == null) continue;

				var stats = new StockStatsUSMonthly();
				stats.stockCode = stockInfo.stockCode;
				stats.type      = stockInfo.type.toString();
				
				// set sector and industry
				if (stockInfo.type == StockInfoUSType.Type.PREF) {
					stats.sector    = "*" + stats.type + "*";
					stats.industry  = "*" + stats.type + "*";
				} else {
					var companyInfo = companyInfoMap.get(stockCode);
					
					if (companyInfo != null) {
						stats.sector    = companyInfo.sector;
						stats.industry  = companyInfo.industry;
					} else {
						stats.sector    = "*" + stats.type + "*";
						stats.industry  = "*" + stats.type + "*";
					}
				}
				
				stats.age       = Finance.durationInYearMonth(monthlyStats.firstDate, monthlyStats.lastDate);

				stats.name      = stockInfo.name;
				
				stats.nikko     = ""; // FIXME
				stats.rakuten   = tradingString(rakutenMap, stockCode);

				// 1 year
				{
					int nMonth  = 12;
					int nOffset = 0;
					
					stats.sd1Y    = (monthlyStats == null || !monthlyStats.contains(nMonth, nOffset)) ? null : DoubleUtil.toBigDecimal(monthlyStats.risk(nMonth, nOffset));
					stats.div1Y   = (monthlyStats == null || !monthlyStats.contains(nMonth, nOffset)) ? null : DoubleUtil.toBigDecimal(monthlyStats.dividend(nMonth, nOffset));
					stats.yield1Y = (monthlyStats == null || !monthlyStats.contains(nMonth, nOffset)) ? null : DoubleUtil.toBigDecimal(monthlyStats.yield(nMonth, nOffset));
					stats.ror1Y   = (monthlyStats == null || !monthlyStats.contains(nMonth, nOffset)) ? null : DoubleUtil.toBigDecimal(monthlyStats.rateOfReturn(nMonth, nOffset));
					stats.rsi     = (monthlyStats == null || !monthlyStats.contains(nMonth, nOffset)) ? null : DoubleUtil.toBigDecimal(monthlyStats.rsi(nMonth, nOffset));
				}
				// 3 year
				{
					int nMonth = 36;
					int nOffset = 0;
					
					stats.sd3Y    = (monthlyStats == null || !monthlyStats.contains(nMonth, nOffset)) ? null : DoubleUtil.toBigDecimal(monthlyStats.risk(nMonth, nOffset));
					stats.div3Y   = (monthlyStats == null || !monthlyStats.contains(nMonth, nOffset)) ? null : DoubleUtil.toBigDecimal(monthlyStats.dividend(nMonth, nOffset));
					stats.yield3Y = (monthlyStats == null || !monthlyStats.contains(nMonth, nOffset)) ? null : DoubleUtil.toBigDecimal(monthlyStats.yield(nMonth, nOffset));
					stats.ror3Y   = (monthlyStats == null || !monthlyStats.contains(nMonth, nOffset)) ? null : DoubleUtil.toBigDecimal(monthlyStats.rateOfReturn(nMonth, nOffset));
				}
				// 5 year
				{
					int nMonth = 60;
					int nOffset = 0;
					
					stats.sd5Y    = (monthlyStats == null || !monthlyStats.contains(nMonth, nOffset)) ? null : DoubleUtil.toBigDecimal(monthlyStats.risk(nMonth, nOffset));
					stats.div5Y   = (monthlyStats == null || !monthlyStats.contains(nMonth, nOffset)) ? null : DoubleUtil.toBigDecimal(monthlyStats.dividend(nMonth, nOffset));
					stats.yield5Y = (monthlyStats == null || !monthlyStats.contains(nMonth, nOffset)) ? null : DoubleUtil.toBigDecimal(monthlyStats.yield(nMonth, nOffset));
					stats.ror5Y   = (monthlyStats == null || !monthlyStats.contains(nMonth, nOffset)) ? null : DoubleUtil.toBigDecimal(monthlyStats.rateOfReturn(nMonth, nOffset));
				}
				// 10 year
				{
					int nMonth = 120;
					int nOffset = 0;
					
					stats.sd10Y    = (monthlyStats == null || !monthlyStats.contains(nMonth, nOffset)) ? null : DoubleUtil.toBigDecimal(monthlyStats.risk(nMonth, nOffset));
					stats.div10Y   = (monthlyStats == null || !monthlyStats.contains(nMonth, nOffset)) ? null : DoubleUtil.toBigDecimal(monthlyStats.dividend(nMonth, nOffset));
					stats.yield10Y = (monthlyStats == null || !monthlyStats.contains(nMonth, nOffset)) ? null : DoubleUtil.toBigDecimal(monthlyStats.yield(nMonth, nOffset));
					stats.ror10Y   = (monthlyStats == null || !monthlyStats.contains(nMonth, nOffset)) ? null : DoubleUtil.toBigDecimal(monthlyStats.rateOfReturn(nMonth, nOffset));
				}
				
				list.add(stats);
			}
		}
		
		logger.info("list   {}", list.size());
		return list;
	}
	
	
	private static void generateReport(List<StockStatsUSMonthly> statsList) {
		String urlReport;
		{
			String timestamp  = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss").format(LocalDateTime.now());
			String name       = String.format("stock-stats-us-monthly-%s.ods", timestamp);
			String pathReport = StorageReport.storage.getPath("stock-stats-us", name);
			urlReport  = StringUtil.toURLString(pathReport);
		}

		logger.info("urlReport {}", urlReport);
		logger.info("docLoad   {}", URL_TEMPLATE);
		try {
			// start LibreOffice process
			LibreOffice.initialize();
			
			SpreadSheet docLoad = new SpreadSheet(URL_TEMPLATE, true);
			SpreadSheet docSave = new SpreadSheet();
			
			String sheetName = Sheet.getSheetName(StockStatsUS.class);
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
	
	
	private static void update() {
		var statsList = getStatsList();		
		generateReport(statsList);
	}
	
	public static void main(String[] args) {
		logger.info("START");
		
		update();
				
		logger.info("STOP");
	}
}
