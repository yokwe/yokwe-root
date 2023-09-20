package yokwe.finance.fund;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import yokwe.finance.Storage;
import yokwe.finance.type.DailyValue;
import yokwe.util.ListUtil;

public class JITAFundDivJP {
	private static final String PREFIX = "jita-fund-div-jp";
	public static String getPath() {
		return Storage.Fund.getPath(PREFIX);
	}
	public static String getPath(String stockCode) {
		return Storage.Fund.getPath(PREFIX, stockCode + ".csv");
	}
	
	private static final String PREFIX_DELIST = PREFIX + "-delist";
	public static String getPathDelist() {
		return Storage.Fund.getPath(PREFIX_DELIST);
	}

	public static void save(String stockCode, Collection<DailyValue> collection) {
		String path = getPath(stockCode);
		ListUtil.save(DailyValue.class, path, collection);
	}
	public static void save(String stockCode, List<DailyValue> list) {
		String path = getPath(stockCode);
		ListUtil.save(DailyValue.class, path, list);
	}
	
	public static List<DailyValue> getList(String stockCode) {
		String path = getPath(stockCode);
		return ListUtil.getList(DailyValue.class, path);
	}
	public static Map<LocalDate, DailyValue> getMap(String stockCode) {
		var list = getList(stockCode);
		return ListUtil.checkDuplicate(list, o -> o.date);
	}	
}
