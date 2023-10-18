package yokwe.finance.report;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import yokwe.finance.provider.yahoo.StorageYahoo;
import yokwe.finance.stock.StorageStock;
import yokwe.util.MarketHoliday;
import yokwe.util.StringUtil;
import yokwe.util.libreoffice.LibreOffice;
import yokwe.util.libreoffice.Sheet;
import yokwe.util.libreoffice.SpreadSheet;

public class UpdateStockStatsJP {
	private static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();
	
	private static final String URL_TEMPLATE  = StringUtil.toURLString("data/form/STOCK_STATS_JP.ods");
	
	private static List<StockStatsJP> getStatsList() {
		var dateStop  = MarketHoliday.JP.getLastTradingDate();
		var dateStart = dateStop.minusYears(1).plusDays(1);
		
		logger.info("date range  {}  -  {}", dateStart, dateStop);
		
		var list = new ArrayList<StockStatsJP>();
		{
			var companyInfoMap = StorageYahoo.CompanyInfoJPYahoo.getMap();
			
			for(var stockInfo: StorageStock.StockInfoJP.getList()) {
				var stockCode = stockInfo.stockCode;
				var priceList = StorageStock.StockPriceJP.getList(stockCode);
				var divList   = StorageStock.StockDivJP.getList(stockCode);
				
				if (priceList.size() < 10) {
					logger.info("skip  {}  {}", stockCode, priceList.size());
					continue;
				}
				
				var companyInfo = companyInfoMap.get(stockCode);
				
				StockStatsJP stats = new StockStatsJP();
				stats.stockCode = stockCode;
				stats.type      = stockInfo.type.simpleType.toString();
				stats.sector    = companyInfo != null ? companyInfo.sector   : "*Unknown*";
				stats.industry  = companyInfo != null ? companyInfo.industry : "*Unknown*";
				stats.name      = stockInfo.name;
				
				{
					StockStats stockStats = StockStats.getInstance(dateStart,  dateStop, priceList, divList);
					
					stats.date      = stockStats.date.toString();
					stats.price     = stockStats.price;
					stats.pricec    = stockStats.pricec;
					stats.last      = stockStats.last;

					stats.sd        = stockStats.sd;
					stats.hv        = stockStats.hv;
					stats.rsi       = stockStats.rsi;
					
					stats.min       = stockStats.min;
					stats.max       = stockStats.max;

					stats.divc      = stockStats.divc;
					stats.yield     = stockStats.yield;

					stats.vol       = stockStats.vol;
					stats.vol5      = stockStats.vol5;
					stats.vol21     = stockStats.vol21;
				}
				
				list.add(stats);
			}
		}

		return list;
	}
	
	
	private static void generateReport(List<StockStatsJP> statsList) {
		String urlReport;
		{
			String timestamp  = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss").format(LocalDateTime.now());
			String name       = String.format("stock-stats-jp-%s.ods", timestamp);
			String pathReport = StorageReport.getPath("stock-stats-jp", name);
			
			urlReport  = StringUtil.toURLString(pathReport);
		}

		logger.info("urlReport {}", urlReport);
		logger.info("docLoad   {}", URL_TEMPLATE);
		try {
			// start LibreOffice process
			LibreOffice.initialize();
			
			SpreadSheet docLoad = new SpreadSheet(URL_TEMPLATE, true);
			SpreadSheet docSave = new SpreadSheet();
			
			String sheetName = Sheet.getSheetName(StockStatsJP.class);
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
		logger.info("save {} {}", statsList.size(), StorageReport.StockStatsJP.getPath());
		StorageReport.StockStatsJP.save(statsList);
		
		generateReport(statsList);
	}
	
	public static void main(String[] args) {
		logger.info("START");
		
		update();
				
		logger.info("STOP");
	}
}
