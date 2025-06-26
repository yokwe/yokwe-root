package yokwe.finance.report;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import yokwe.finance.fund.StorageFund;
import yokwe.finance.provider.jreit.StorageJREIT;
import yokwe.finance.provider.manebu.StorageManebu;
import yokwe.finance.provider.yahoo.StorageYahoo;
import yokwe.finance.stats.StockStats;
import yokwe.finance.stock.StorageStock;
import yokwe.finance.type.StockCodeJP;
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
		logger.info("dateStop  {}", dateStop);
		
		var list = new ArrayList<StockStatsJP>();
		{
			var companyInfoMap = StorageYahoo.CompanyInfoJPYahoo.getMap();
			var etfMap         = StorageManebu.ETFInfo.getMap();
			var jreitMap       = StorageJREIT.JREITInfo.getMap();
			var nisaMap        = StorageFund.NISAInfo.getMap();

			for(var stockInfo: StorageStock.StockInfoJP.getList()) {
				var stockCode = stockInfo.stockCode;
				var priceList = StorageStock.StockPriceJP.getList(stockCode);
				var divList   = StorageStock.StockDivJP.getList(stockCode);
				
				if (priceList.size() < 10) {
					logger.info("skip  {}  {}  {}", priceList.size(), stockCode, stockInfo.name);
					continue;
				}
				
				StockStatsJP stats = new StockStatsJP();
				stats.stockCode = stockCode;
				stats.type      = stockInfo.type.simpleType.toString();
				
				stats.divc     = -1;
				// set sector and industry
				if (stockInfo.type.isETF() || stockInfo.type.isETN()) {
					stats.sector = stockInfo.type.simpleType.toString();
					
					var etf = etfMap.get(stockCode);
					if (etf != null) {
						stats.industry = "ETF-" + etf.category.replace("ETF", "");
						stats.divc = etf.divFreq;
					} else {
						stats.industry = stats.sector;
					}
				} else if (stockInfo.type.isREIT() || stockInfo.type.isInfra()) {
					stats.sector = stockInfo.type.simpleType.toString();
					
					var jreit = jreitMap.get(stockCode);
					if (jreit != null) {
						stats.industry = "REIT-" + jreit.category.replace(" ", "");
						stats.divc     = jreit.divFreq;
					} else {
						stats.industry = stats.sector;
					}
				} else if (StockCodeJP.isPreferredStock(stockCode)) {
					stats.sector    = "PREF";
					stats.industry  = "PREF";
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
				stats.marketCap = stockInfo.issued.multiply(priceList.get(priceList.size() - 1).close).longValue();
				
				{
					StockStats stockStats = StockStats.getInstance(stockCode, dateStop, priceList, divList);
					
					stats.price     = stockStats.price;
					stats.pricec    = stockStats.pricec;
					stats.invest    = (int)(stockStats.price * stockInfo.tradeUnit);
					stats.last      = stockStats.last;
					
					stats.rorNoReinvested = stockStats.rorNoReinvested;
					
					stats.sd        = stockStats.sd;
					stats.hv        = stockStats.hv;
					stats.rsi14     = stockStats.rsi14;
					stats.rsi7      = stockStats.rsi7;
					
					stats.min       = stockStats.min;
					stats.max       = stockStats.max;
					stats.minY3     = stockStats.minY3;
					stats.maxY3     = stockStats.maxY3;
					
//					if (stats.divc == -1) {
//						stats.divc          = stockStats.divc;
//					}
					stats.divc          = stockStats.divc;
					stats.lastDiv       = stockStats.lastDiv;
					stats.forwardYield  = stockStats.forwardYield;
					stats.annualDiv     = stockStats.annualDiv;
					stats.trailingYield = stockStats.trailingYield;

//					stats.vol       = (double)stockStats.vol / stockInfo.issued.doubleValue();
//					stats.vol5      = (double)stockStats.vol5 / stockInfo.issued.doubleValue();
//					stats.vol21     = (double)stockStats.vol21 / stockInfo.issued.doubleValue();
					stats.vol       = (double)stockStats.vol   * stats.price;
					stats.vol5      = (double)stockStats.vol5  * stats.price;
					stats.vol21     = (double)stockStats.vol21 * stats.price;
				}
				
				if (stockInfo.type.isETF()) {
					if (nisaMap.containsKey(stockInfo.isinCode)) {
						var nisaInfo = nisaMap.get(stockInfo.isinCode);
						stats.nisa = nisaInfo.tsumitate ? "1" : "0";
					} else {
						stats.nisa = "";
					}
				} else {
					stats.nisa = "0";
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
			String pathReport = StorageReport.storage.getPath("stock-stats-jp", name);
			
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
