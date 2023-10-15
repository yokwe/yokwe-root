package yokwe.finance.provider.prestia;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import yokwe.finance.Storage;
import yokwe.finance.type.TradingFundType;
import yokwe.util.ListUtil;

public class TradingFundPrestia {
	private static final String PATH_FILE = Storage.provider_prestia.getPath("trading-fund-prestia.csv");
	public static String getPath() {
		return PATH_FILE;
	}
	
	public static List<TradingFundType> getList() {
		return ListUtil.getList(TradingFundType.class, getPath());
	}
	public static Map<String, TradingFundType> getMap() {
		//            isinCode
		return ListUtil.checkDuplicate(getList(), o -> o.isinCode);
	}
	public static void save(Collection<TradingFundType> collection) {
		ListUtil.save(TradingFundType.class, getPath(), collection);
	}
	public static void save(List<TradingFundType> list) {
		ListUtil.save(TradingFundType.class, getPath(), list);
	}
}