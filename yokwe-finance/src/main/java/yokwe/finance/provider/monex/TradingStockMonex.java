package yokwe.finance.provider.monex;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import yokwe.finance.Storage;
import yokwe.finance.type.TradingStockInfo;
import yokwe.util.ListUtil;

public class TradingStockMonex {
	private static final String PATH_FILE = Storage.provider_monex.getPath("trading-stock-monex.csv");
	public static String getPath() {
		return PATH_FILE;
	}
	
	public static List<TradingStockInfo> getList() {
		return ListUtil.getList(TradingStockInfo.class, getPath());
	}
	public static Map<String, TradingStockInfo> getMap() {
		//            stockCode
		return ListUtil.checkDuplicate(getList(), o -> o.stockCode);
	}
	public static void save(Collection<TradingStockInfo> collection) {
		ListUtil.save(TradingStockInfo.class, getPath(), collection);
	}
	public static void save(List<TradingStockInfo> list) {
		ListUtil.save(TradingStockInfo.class, getPath(), list);
	}
}
