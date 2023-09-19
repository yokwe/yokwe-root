package yokwe.finance.stock;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import yokwe.finance.Storage;
import yokwe.finance.type.StockInfoJP;
import yokwe.util.ListUtil;

public final class JPXStockInfoJP {
	private static final String PATH_FILE = Storage.Stock.getPath("jpx-stock-info-jp.csv");
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
