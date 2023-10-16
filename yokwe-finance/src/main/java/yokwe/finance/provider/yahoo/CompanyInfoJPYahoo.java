package yokwe.finance.provider.yahoo;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import yokwe.finance.Storage;
import yokwe.finance.type.CompanyInfoType;
import yokwe.util.ListUtil;

public class CompanyInfoJPYahoo {
	private static final String PATH_FILE = Storage.provider_yahoo.getPath("company-info-jp-yahoo.csv");
	public static String getPath() {
		return PATH_FILE;
	}
	
	public static List<CompanyInfoType> getList() {
		return ListUtil.getList(CompanyInfoType.class, getPath());
	}
	public static Map<String, CompanyInfoType> getMap() {
		//            stockCode
		return ListUtil.checkDuplicate(getList(), o -> o.stockCode);
	}
	public static void save(Collection<CompanyInfoType> collection) {
		ListUtil.save(CompanyInfoType.class, getPath(), collection);
	}
	public static void save(List<CompanyInfoType> list) {
		ListUtil.save(CompanyInfoType.class, getPath(), list);
	}
}
