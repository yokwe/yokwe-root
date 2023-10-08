package yokwe.finance.provider.jpx;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import yokwe.finance.Storage;
import yokwe.finance.type.StockInfoJPType;
import yokwe.util.ListUtil;

public class StockInfoJPX {
	private static final String PATH_FILE = Storage.provider_jpx.getPath("stock-info-jpx.csv");
	public static String getPath() {
		return PATH_FILE;
	}
	
	public static List<StockInfoJPType> getList() {
		return ListUtil.getList(StockInfoJPType.class, getPath());
	}
	public static Map<String, StockInfoJPType> getMap() {
		//            stockCode
		var list = getList();
		return ListUtil.checkDuplicate(list, o -> o.stockCode);
	}
	public static void save(Collection<StockInfoJPType> collection) {
		ListUtil.save(StockInfoJPType.class, getPath(), collection);
	}
	public static void save(List<StockInfoJPType> list) {
		ListUtil.save(StockInfoJPType.class, getPath(), list);
	}

}