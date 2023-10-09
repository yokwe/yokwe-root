package yokwe.finance.stock;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import yokwe.finance.Storage;
import yokwe.finance.type.StockInfoJPType;
import yokwe.util.ListUtil;

public final class StockInfoJP {
	private static final String PATH_FILE = Storage.stock.getPath("stock-info-jp.csv");
	public static String getPath() {
		return PATH_FILE;
	}
	
	public static List<StockInfoJPType> getList() {
		return ListUtil.getList(StockInfoJPType.class, getPath());
	}
	public static Map<String, StockInfoJPType> getMap() {
		//            stockCode
		return ListUtil.checkDuplicate(getList(), o -> o.stockCode);
	}
	public static void save(Collection<StockInfoJPType> collection) {
		ListUtil.save(StockInfoJPType.class, getPath(), collection);
	}
	public static void save(List<StockInfoJPType> list) {
		ListUtil.save(StockInfoJPType.class, getPath(), list);
	}
}
