package yokwe.finance.report;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import yokwe.finance.Storage;
import yokwe.finance.provider.monex.TradingStockMonex;
import yokwe.finance.provider.moomoo.TradingStockMoomoo;
import yokwe.finance.provider.rakuten.TradingStockRakuten;
import yokwe.finance.provider.sbi.TradingStockSBI;
import yokwe.finance.stock.StockDivUS;
import yokwe.finance.stock.StockInfoUS;
import yokwe.finance.stock.StockPriceUS;
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
			var monexMap   = TradingStockMonex.getMap();
			var sbiMap     = TradingStockSBI.getMap();
			var rakutenMap = TradingStockRakuten.getMap();
			var moomooMap  = TradingStockMoomoo.getMap();
			
			for(var stockInfo: StockInfoUS.getList()) {
				var stockCode = stockInfo.stockCode;
				var priceList = StockPriceUS.getList(stockCode);
				var divList   = StockDivUS.getList(stockCode);
				
				if (priceList.size() < 10) {
					logger.info("skip  {}  {}", stockCode, priceList.size());
					continue;
				}
				
				StockStats stockStats = StockStats.getInstance(dateStart,  dateStop, priceList, divList);
				
				StockStatsUS stats = new StockStatsUS();
				stats.stockCode = stockCode;
				
				stats.type      = stockInfo.type.simpleType.toString();
				stats.name      = stockInfo.name;
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

				stats.monex     = tradingString(monexMap, stockCode);
				stats.nikko     = ""; // FIXME
				stats.sbi       = tradingString(sbiMap, stockCode);
				stats.rakuten   = tradingString(rakutenMap, stockCode);
				stats.moomoo    = tradingString(moomooMap, stockCode);
				
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
			String pathReport = Storage.report_stock_stats_us.getPath(name);
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
		logger.info("save {} {}", statsList.size(), StockStatsUS.getPath());
		StockStatsUS.save(statsList);
		
		generateReport(statsList);
	}
	
	public static void main(String[] args) {
		logger.info("START");
		
		update();
				
		logger.info("STOP");
	}
}
