package yokwe.finance.provider.nyse;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import yokwe.finance.Storage;
import yokwe.finance.type.StockInfoUS;
import yokwe.util.ListUtil;

public class StockInfo {
	private static final String PATH_FILE = Storage.Provider.NYSE.getPath("stock-info.csv");
	public static String getPath() {
		return PATH_FILE;
	}
	
	public static List<StockInfoUS> getList() {
		return ListUtil.getList(StockInfoUS.class, getPath());
	}
	public static Map<String, StockInfoUS> getMap() {
		//            stockCode
		var list = getList();
		return ListUtil.checkDuplicate(list, o -> o.stockCode);
	}
	public static void save(Collection<StockInfoUS> collection) {
		ListUtil.save(StockInfoUS.class, getPath(), collection);
	}
	public static void save(List<StockInfoUS> list) {
		ListUtil.save(StockInfoUS.class, getPath(), list);
	}

}
