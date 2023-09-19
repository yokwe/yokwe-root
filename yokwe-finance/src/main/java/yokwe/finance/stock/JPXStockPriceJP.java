package yokwe.finance.stock;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import yokwe.finance.Storage;
import yokwe.finance.type.OHLCV;
import yokwe.util.ListUtil;

public class JPXStockPriceJP {
	private static final String PREFIX = "jpx-stock-price-jp";
	public static String getPath() {
		return Storage.Stock.getPath(PREFIX);
	}
	public static String getPath(String stockCode) {
		return Storage.Stock.getPath(PREFIX, stockCode + ".csv");
	}
	
	private static final String PREFIX_DELIST = PREFIX + "-delist";
	public static String getPathDelist() {
		return Storage.Stock.getPath(PREFIX_DELIST);
	}

	public static void save(String stockCode, Collection<OHLCV> collection) {
		String path = getPath(stockCode);
		ListUtil.save(OHLCV.class, path, collection);
	}
	public static void save(String stockCode, List<OHLCV> list) {
		String path = getPath(stockCode);
		ListUtil.save(OHLCV.class, path, list);
	}
	
	public static List<OHLCV> getList(String stockCode) {
		String path = getPath(stockCode);
		return ListUtil.getList(OHLCV.class, path);
	}
	public static Map<LocalDate, OHLCV> getMap(String stockCode) {
		var list = getList(stockCode);
		return ListUtil.checkDuplicate(list, o -> o.date);
	}	
}
