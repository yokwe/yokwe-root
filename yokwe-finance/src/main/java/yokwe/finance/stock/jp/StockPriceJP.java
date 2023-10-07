package yokwe.finance.stock.jp;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import yokwe.finance.Storage;
import yokwe.finance.type.OHLCV;
import yokwe.util.ListUtil;

public class StockPriceJP {
	private static final String PREFIX = "stock-price-jp";
	
	private static final Storage storage = Storage.stock_jp;
	
	public static String getPath() {
		return storage.getPath(PREFIX);
	}
	public static String getPath(String stockCode) {
		return storage.getPath(PREFIX, stockCode + ".csv");
	}
	
	private static final String PREFIX_DELIST = PREFIX + "-delist";
	public static String getPathDelist() {
		return storage.getPath(PREFIX_DELIST);
	}
	
	public static void save(String stockCode, Collection<OHLCV> collection) {
		ListUtil.save(OHLCV.class, getPath(stockCode), collection);
	}
	public static void save(String stockCode, List<OHLCV> list) {
		ListUtil.save(OHLCV.class, getPath(stockCode), list);
	}
	
	public static List<OHLCV> getList(String stockCode) {
		return ListUtil.getList(OHLCV.class, getPath(stockCode));
	}
	public static Map<LocalDate, OHLCV> getMap(String stockCode) {
		return ListUtil.checkDuplicate(getList(stockCode), o -> o.date);
	}	
}
