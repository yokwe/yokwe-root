package yokwe.stock.trade.data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import yokwe.stock.trade.Storage;
import yokwe.util.CSVUtil;

public class StockHistoryUtil {
	private static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();

	public static final String PATH_STOCK_HISTORY_GMO       = Storage.Data.getPath("stock-history-gmo.csv");
	public static final String PATH_STOCK_HISTORY_MONEX     = Storage.Data.getPath("stock-history-monex.csv");
	public static final String PATH_STOCK_HISTORY_FIRSTRADE = Storage.Data.getPath("stock-history-monex.csv");
	
	//                group
	public static Map<String, List<StockHistory>> getStockHistoryMap(String path) {
		logger.info("path     = {}!", path);
		
		List<StockHistory> stockHistoryList = CSVUtil.read(StockHistory.class).file(path);
		
		Map<String, List<StockHistory>> ret = new TreeMap<>();
		
		for(StockHistory stockHistory: stockHistoryList) {
			String key = stockHistory.group;
			if (!ret.containsKey(key)) {
				ret.put(key, new ArrayList<>());
			}
			ret.get(key).add(stockHistory);
		}
		
		for(Map.Entry<String, List<StockHistory>> entry: ret.entrySet()) {
			Collections.sort(entry.getValue());
		}
		
		return ret;
	}
}
