package yokwe.finance.provider.jpx;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import yokwe.finance.Storage;
import yokwe.finance.type.StockInfoJP;
import yokwe.util.ListUtil;

public class StockInfoJPX {
	private static final String PATH_FILE = Storage.provider_jpx.getPath("stock-info-jpx.csv");
	public static String getPath() {
		return PATH_FILE;
	}
	
	public static List<StockInfoJP> getList() {
		return ListUtil.getList(StockInfoJP.class, getPath());
	}
	public static Map<String, StockInfoJP> getMap() {
		//            stockCode
		var list = getList();
		return ListUtil.checkDuplicate(list, o -> o.stockCode);
	}
	public static void save(Collection<StockInfoJP> collection) {
		ListUtil.save(StockInfoJP.class, getPath(), collection);
	}
	public static void save(List<StockInfoJP> list) {
		ListUtil.save(StockInfoJP.class, getPath(), list);
	}

}
