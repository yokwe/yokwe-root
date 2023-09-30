package yokwe.finance.provider.sbi;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import yokwe.finance.Storage;
import yokwe.finance.type.TradingStockInfo;
import yokwe.util.ListUtil;

public class TradingStock {
	private static final String PATH_FILE = Storage.provider_sbi.getPath("trading-stock.csv");
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
