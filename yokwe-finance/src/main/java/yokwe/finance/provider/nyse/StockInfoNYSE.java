package yokwe.finance.provider.nyse;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import yokwe.finance.Storage;
import yokwe.finance.type.StockInfoUSType;
import yokwe.util.ListUtil;

public class StockInfoNYSE {
	private static final String PATH_FILE = Storage.provider_nyse.getPath("stock-info-nyse.csv");
	public static String getPath() {
		return PATH_FILE;
	}
	
	public static List<StockInfoUSType> getList() {
		return ListUtil.getList(StockInfoUSType.class, getPath());
	}
	public static Map<String, StockInfoUSType> getMap() {
		//            stockCode
		var list = getList();
		return ListUtil.checkDuplicate(list, o -> o.stockCode);
	}
	public static void save(Collection<StockInfoUSType> collection) {
		ListUtil.save(StockInfoUSType.class, getPath(), collection);
	}
	public static void save(List<StockInfoUSType> list) {
		ListUtil.save(StockInfoUSType.class, getPath(), list);
	}

}
