package yokwe.finance.fund;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import yokwe.finance.Storage;
import yokwe.finance.type.DailyValue;
import yokwe.util.ListUtil;

public class FundDiv {
	private static final String PREFIX = "fund-div";
	
	private static final Storage storage = Storage.fund;
	
	public static String getPath() {
		return storage.getPath(PREFIX);
	}
	public static String getPath(String isinCode) {
		return storage.getPath(PREFIX, isinCode + ".csv");
	}
	
	private static final String PREFIX_DELIST = PREFIX + "-delist";
	public static String getPathDelist() {
		return storage.getPath(PREFIX_DELIST);
	}

	public static void save(String isinCode, Collection<DailyValue> collection) {
		ListUtil.save(DailyValue.class, getPath(isinCode), collection);
	}
	public static void save(String isinCode, List<DailyValue> list) {
		ListUtil.save(DailyValue.class, getPath(isinCode), list);
	}
	
	public static List<DailyValue> getList(String isinCode) {
		return ListUtil.getList(DailyValue.class, getPath(isinCode));
	}
	public static Map<LocalDate, DailyValue> getMap(String isinCode) {
		return ListUtil.checkDuplicate(getList(isinCode), o -> o.date);
	}	
}
