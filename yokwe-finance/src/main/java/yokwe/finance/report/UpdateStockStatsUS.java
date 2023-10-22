package yokwe.finance.report;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import yokwe.finance.provider.monex.StorageMonex;
import yokwe.finance.provider.moomoo.StorageMoomoo;
import yokwe.finance.provider.rakuten.StorageRakuten;
import yokwe.finance.provider.sbi.StorageSBI;
import yokwe.finance.provider.yahoo.StorageYahoo;
import yokwe.finance.stats.StockStats;
import yokwe.finance.stock.StorageStock;
import yokwe.finance.type.StockInfoUSType;
import yokwe.finance.type.TradingStockType;
import yokwe.util.MarketHoliday;
import yokwe.util.StringUtil;
import yokwe.util.libreoffice.LibreOffice;
import yokwe.util.libreoffice.Sheet;
import yokwe.util.libreoffice.SpreadSheet;

public class UpdateStockStatsUS {
	private static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();
	
	private static final String URL_TEMPLATE  = StringUtil.toURLString("data/form/STOCK_STATS_US.ods");
	
	private static String tradingString(Map<String, TradingStockType> map, String stockCode) {
		return map.containsKey(stockCode) ? Integer.toString(map.get(stockCode).feeType.value): "";
	}
	
	
	private static List<StockStatsUS> getStatsList() {
		var dateStop  = MarketHoliday.US.getLastTradingDate();
		var dateStart = dateStop.minusYears(1).plusDays(1);
		
		logger.info("date range  {}  -  {}", dateStart, dateStop);
		
		var list = new ArrayList<StockStatsUS>();
		{
			var monexMap       = StorageMonex.TradingStockMonex.getMap();
			var sbiMap         = StorageSBI.TradingStockSBI.getMap();
			var rakutenMap     = StorageRakuten.TradingStockRakuten.getMap();
			var moomooMap      = StorageMoomoo.TradingStockMoomoo.getMap();
			var companyInfoMap = StorageYahoo.CompanyInfoUSYahoo.getMap();
			
			for(var stockInfo: StorageStock.StockInfoUS.getList()) {
				var stockCode = stockInfo.stockCode;
				var priceList = StorageStock.StockPriceUS.getList(stockCode);
				var divList   = StorageStock.StockDivUS.getList(stockCode);
				
				if (priceList.size() < 10) {
					logger.info("skip  {}  {}  {}", priceList.size(), stockCode, stockInfo.name);
					continue;
				}
				
				StockStatsUS stats = new StockStatsUS();
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
				
				stats.name      = stockInfo.name;
				
				stats.monex     = tradingString(monexMap, stockCode);
				stats.nikko     = ""; // FIXME
				stats.sbi       = tradingString(sbiMap, stockCode);
				stats.rakuten   = tradingString(rakutenMap, stockCode);
				stats.moomoo    = tradingString(moomooMap, stockCode);

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
	
	
	private static void generateReport(List<StockStatsUS> statsList) {
		String urlReport;
		{
			String timestamp  = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss").format(LocalDateTime.now());
			String name       = String.format("stock-stats-us-%s.ods", timestamp);
			String pathReport = StorageReport.getPath("stock-stats-us", name);
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
		logger.info("save {} {}", statsList.size(), StorageReport.StockStatsUS.getPath());
		StorageReport.StockStatsUS.save(statsList);
		
		generateReport(statsList);
	}
	
	public static void main(String[] args) {
		logger.info("START");
		
		update();
				
		logger.info("STOP");
	}
}
