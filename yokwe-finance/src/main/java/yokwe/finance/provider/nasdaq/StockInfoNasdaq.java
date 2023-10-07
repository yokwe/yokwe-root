package yokwe.finance.provider.nasdaq;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import yokwe.finance.Storage;
import yokwe.finance.type.StockInfoUSType;
import yokwe.util.ListUtil;

public class StockInfoNasdaq {
	private static final String PATH_FILE = Storage.provider_nasdaq.getPath("stock-info-nasdaq.csv");
	public static String getPath() {
		return PATH_FILE;
	}
	
	public static List<StockInfoUSType> getList() {
		return ListUtil.getList(StockInfoUSType.class, getPath());
	}
	public static Map<String, StockInfoUSType> getMap() {
		//            stockCode
		return ListUtil.checkDuplicate(getList(), o -> o.stockCode);
	}
	public static void save(Collection<StockInfoUSType> collection) {
		ListUtil.save(StockInfoUSType.class, getPath(), collection);
	}
	public static void save(List<StockInfoUSType> list) {
		ListUtil.save(StockInfoUSType.class, getPath(), list);
	}
}
