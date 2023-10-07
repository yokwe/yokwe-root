package yokwe.finance.provider.moomoo;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import yokwe.finance.Storage;
import yokwe.finance.type.TradingStockType;
import yokwe.util.ListUtil;

public class TradingStockMoomoo {
	private static final String PATH_FILE = Storage.provider_moomoo.getPath("trading-stock-moomoo.csv");
	public static String getPath() {
		return PATH_FILE;
	}
	
	public static List<TradingStockType> getList() {
		return ListUtil.getList(TradingStockType.class, getPath());
	}
	public static Map<String, TradingStockType> getMap() {
		//            stockCode
		return ListUtil.checkDuplicate(getList(), o -> o.stockCode);
	}
	public static void save(Collection<TradingStockType> collection) {
		ListUtil.save(TradingStockType.class, getPath(), collection);
	}
	public static void save(List<TradingStockType> list) {
		ListUtil.save(TradingStockType.class, getPath(), list);
	}
}
