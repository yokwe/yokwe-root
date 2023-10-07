package yokwe.finance.provider.jita;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import yokwe.finance.Storage;
import yokwe.finance.type.FundInfoJP;
import yokwe.util.ListUtil;

public class FundInfoJITA {
	private static final String PATH_FILE = Storage.provider_jita.getPath("fund-info-jita.csv");
	public static String getPath() {
		return PATH_FILE;
	}
	
	public static List<FundInfoJP> getList() {
		return ListUtil.getList(FundInfoJP.class, getPath());
	}
	public static Map<String, FundInfoJP> getMap() {
		//            stockCode
		return ListUtil.checkDuplicate(getList(), o -> o.stockCode);
	}
	public static void save(Collection<FundInfoJP> collection) {
		ListUtil.save(FundInfoJP.class, getPath(), collection);
	}
	public static void save(List<FundInfoJP> list) {
		ListUtil.save(FundInfoJP.class, getPath(), list);
	}
}
