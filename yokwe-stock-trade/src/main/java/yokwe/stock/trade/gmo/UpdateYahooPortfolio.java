package yokwe.stock.trade.gmo;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import yokwe.stock.trade.Storage;
import yokwe.stock.trade.data.StockHistory;
import yokwe.stock.trade.data.StockHistoryUtil;
import yokwe.stock.trade.data.YahooPortfolio;
import yokwe.util.CSVUtil;

public class UpdateYahooPortfolio {
	private static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();
	
	public static final String PATH_YAHOO_PORTFOLIO = Storage.Data.getPath("yahoo-portfolio-gmo.csv");

	public static void main(String[] args) {
		logger.info("START");
		
		List<YahooPortfolio> yahooPortfolioList = new ArrayList<>();
		
		Map<String, List<StockHistory>> stockHistoryMap = StockHistoryUtil.getStockHistoryMap(StockHistoryUtil.PATH_STOCK_HISTORY_GMO);
		logger.info("stockHistoryMap {}", stockHistoryMap.size());
		
		for(Map.Entry<String, List<StockHistory>> entry: stockHistoryMap.entrySet()) {
			List<StockHistory> stockHistoryList = entry.getValue();
			
			StockHistory lastStockHistory = stockHistoryList.get(stockHistoryList.size() - 1);
			if (lastStockHistory.totalQuantity == 0) continue;
			
			String symbol        = entry.getKey();
			double quantity      = lastStockHistory.totalQuantity;
			double purchasePrice = Math.ceil(lastStockHistory.totalCost / lastStockHistory.totalQuantity);
			
			yahooPortfolioList.add(new YahooPortfolio(symbol, purchasePrice, quantity));
		}
		
		CSVUtil.write(YahooPortfolio.class).file(PATH_YAHOO_PORTFOLIO, yahooPortfolioList);
		logger.info("yahooPortfolioList {}  {}", PATH_YAHOO_PORTFOLIO, yahooPortfolioList.size());
		
		logger.info("STOP");
	}
}
