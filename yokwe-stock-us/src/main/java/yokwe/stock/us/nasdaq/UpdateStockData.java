package yokwe.stock.us.nasdaq;

import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

public class UpdateStockData {
	static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(UpdateStockData.class);
	
	public static void main(String[] args) {
		logger.info("START");		
		
		StockList.update();
		List<StockList> stockList = StockList.getList();
		{
			Set<String> symbolSet = Symbol.getList().stream().map(o -> o.symbol).collect(Collectors.toSet());
			logger.info("symbolSet {}", symbolSet.size());
						
			logger.info("stockList {}", stockList.size());
			var i = stockList.iterator();
			while(i.hasNext()) {
				var e = i.next();
				if (symbolSet.contains(e.symbol)) continue;
				i.remove();
			}
			logger.info("stockList {}", stockList.size());
			StockList.save(stockList);
			
			{
				Set<String> stockSet        = stockList.stream().map(o -> o.symbol).collect(Collectors.toSet());
				Set<String> onlyInStockSet  = new TreeSet<>(stockSet);
				Set<String> onlyInSymbolSet = new TreeSet<>(symbolSet);
				onlyInStockSet.removeAll(symbolSet);
				onlyInSymbolSet.removeAll(stockSet);
				logger.info("onlyInStockSet  {} {}", onlyInStockSet.size(), onlyInStockSet);
				logger.info("onlyInSymbolSet {} {}", onlyInSymbolSet.size(), onlyInSymbolSet);
			}
		}
		
		// update price using stockList
		
				
		logger.info("STOP");
	}
}
