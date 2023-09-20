package yokwe.finance.fund;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import yokwe.finance.Storage;
import yokwe.finance.type.FundInfoJP;
import yokwe.util.ListUtil;

public class JITAFundInfoJP {
	private static final String PATH_FILE = Storage.Fund.getPath("jita-fund-info-jp.csv");
	public static String getPath() {
		return PATH_FILE;
	}
	
	public static List<FundInfoJP> getList() {
		return ListUtil.getList(FundInfoJP.class, getPath());
	}
	public static Map<String, FundInfoJP> getMap() {
		//            stockCode
		var list = getList();
		return ListUtil.checkDuplicate(list, o -> o.stockCode);
	}
	public static void save(Collection<FundInfoJP> collection) {
		ListUtil.save(FundInfoJP.class, getPath(), collection);
	}
	public static void save(List<FundInfoJP> list) {
		ListUtil.save(FundInfoJP.class, getPath(), list);
	}
}
