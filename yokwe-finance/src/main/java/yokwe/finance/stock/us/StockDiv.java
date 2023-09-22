package yokwe.finance.stock.us;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import yokwe.finance.Storage;
import yokwe.finance.type.DailyValue;
import yokwe.util.ListUtil;

public class StockDiv {
	private static final String PREFIX = "stock-div";
	public static String getPath() {
		return Storage.Stock.US.getPath(PREFIX);
	}
	public static String getPath(String stockCode) {
		return Storage.Stock.US.getPath(PREFIX, stockCode + ".csv");
	}
	
	private static final String PREFIX_DELIST = PREFIX + "-delist";
	public static String getPathDelist() {
		return Storage.Stock.US.getPath(PREFIX_DELIST);
	}

	public static void save(String stockCode, Collection<DailyValue> collection) {
		ListUtil.save(DailyValue.class, getPath(stockCode), collection);
	}
	public static void save(String stockCode, List<DailyValue> list) {
		ListUtil.save(DailyValue.class, getPath(stockCode), list);
	}
	
	public static List<DailyValue> getList(String stockCode) {
		return ListUtil.getList(DailyValue.class, getPath(stockCode));
	}
	public static Map<LocalDate, DailyValue> getMap(String stockCode) {
		return ListUtil.checkDuplicate(getList(stockCode), o -> o.date);
	}	
}
