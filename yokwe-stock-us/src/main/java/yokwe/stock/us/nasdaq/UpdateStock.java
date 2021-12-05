package yokwe.stock.us.nasdaq;

import java.util.Collections;
import java.util.List;

public class UpdateStock {
	static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(UpdateStock.class);
	
	public static void main(String[] args) {
		logger.info("START");		
		
		StockList.update();
		
		List<StockList> stockList = StockList.getList();
		
		Collections.shuffle(stockList);
		StockInfo.download(stockList);
		StockInfo.update(stockList);
		
		Collections.shuffle(stockList);
		StockSummary.download(stockList);
		StockSummary.update(stockList);
		
		Collections.shuffle(stockList);
		StockDividends.download(stockList);
		StockDividends.update(stockList);
		
		logger.info("STOP");
	}
}
