package yokwe.stock.us.yahoo;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import yokwe.stock.us.Stock;
import yokwe.util.yahoo.finance.Search;
import yokwe.util.yahoo.finance.Symbol;

public class UpdateYahooSymbol {
	private static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();
	
	public static String toYahooSymbol(Stock stock) {
		return stock.symbol.replace("-", "-P").replace(".", "-");
	}
	public static void main(String[] args) {
		logger.info("START");
		
		List<Stock> stockList = Stock.getList();
		logger.info("stockList  {}", stockList.size());
		
		Map<String, Symbol> symbolMap = YahooSymbol.getMap();
		logger.info("symbolMap  {}", symbolMap.size());

		// shuffle
		Collections.shuffle(stockList);
		
		int countUpdate = 0;
		for(var stock: stockList) {
			// only for common
			String yahooSymbol = toYahooSymbol(stock);
			if (symbolMap.containsKey(yahooSymbol)) continue;
			
			Symbol symbol = Search.getSymbol(yahooSymbol);
			if (symbol != null) {
				symbolMap.put(symbol.symbol, symbol);
				countUpdate++;
				if (countUpdate == 10) {
					logger.info("symbolMap  {} / {}", symbolMap.size(), stockList.size());
					YahooSymbol.save(symbolMap.values());
					countUpdate = 0;
				}
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					//
				}
			}
		}
		
		logger.info("STOP");
	}
}
