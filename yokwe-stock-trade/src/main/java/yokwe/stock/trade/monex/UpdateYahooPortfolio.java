package yokwe.stock.trade.monex;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.slf4j.LoggerFactory;

import yokwe.stock.trade.Storage;
import yokwe.stock.trade.data.StockHistory;
import yokwe.stock.trade.data.StockHistoryUtil;
import yokwe.stock.trade.data.YahooPortfolio;
import yokwe.util.CSVUtil;
import yokwe.util.DoubleUtil;

public class UpdateYahooPortfolio {
	private static final org.slf4j.Logger logger = LoggerFactory.getLogger(UpdateYahooPortfolio.class);
	
	public static final String PATH_YAHOO_PORTFOLIO = Storage.getPath("data", "yahoo-portfolio-monex.csv");

	public static void main(String[] args) {
		logger.info("START");
		
		List<YahooPortfolio> yahooPortfolioList = new ArrayList<>();
		
		Map<String, List<StockHistory>> stockHistoryMap = StockHistoryUtil.getStockHistoryMap(StockHistoryUtil.PATH_STOCK_HISTORY_MONEX);
		logger.info("stockHistoryMap {}", stockHistoryMap.size());
		
		for(Map.Entry<String, List<StockHistory>> entry: stockHistoryMap.entrySet()) {
			List<StockHistory> stockHistoryList = entry.getValue();
			
			StockHistory lastStockHistory = stockHistoryList.get(stockHistoryList.size() - 1);
			if (lastStockHistory.totalQuantity == 0) continue;
			
			// Change symbol style from iex to yahoo
			String symbol        = entry.getKey().replace("-", "-P").replace(".", "-");
			double quantity      = lastStockHistory.totalQuantity;
			double purchasePrice = DoubleUtil.round(lastStockHistory.totalCost / lastStockHistory.totalQuantity, 2);
			
			yahooPortfolioList.add(new YahooPortfolio(symbol, purchasePrice, quantity));
		}
		
		CSVUtil.write(YahooPortfolio.class).file(PATH_YAHOO_PORTFOLIO, yahooPortfolioList);
		logger.info("yahooPortfolioList {}  {}", PATH_YAHOO_PORTFOLIO, yahooPortfolioList.size());
		
		logger.info("STOP");
	}
}
