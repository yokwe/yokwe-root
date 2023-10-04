package yokwe.finance.provider.yahoo;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import yokwe.finance.Storage;
import yokwe.finance.type.DailyValue;
import yokwe.util.ListUtil;

public class YahooStockDiv {
	//
	// NOTE: dividend amount is split adjusted. Not raw amount.
	//
	
	private static final String PREFIX = "yahoo-stock-div";
	
	private static final Storage storage = Storage.provider_yahoo;
	
	// stockCode is yahoo stockCode
	
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
