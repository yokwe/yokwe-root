package yokwe.finance.provider.jpx;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import yokwe.finance.Storage;
import yokwe.util.ListUtil;
import yokwe.util.StringUtil;

public class REIT implements Comparable<REIT> {
	private static final String PATH_FILE = Storage.provider_jpx.getPath("reit.csv");
	public static String getPath() {
		return PATH_FILE;
	}
	
	public static List<REIT> getList() {
		return ListUtil.getList(REIT.class, getPath());
	}
	public static Map<String, REIT> getMap() {
		//            stockCode
		var list = getList();
		return ListUtil.checkDuplicate(list, o -> o.stockCode);
	}
	public static void save(Collection<REIT> collection) {
		ListUtil.save(REIT.class, getPath(), collection);
	}
	public static void save(List<REIT> list) {
		ListUtil.save(REIT.class, getPath(), list);
	}
	
	
	public String stockCode;
	public String name;
	
	@Override
	public String toString() {
		return StringUtil.toString(this);
	}
	
	@Override
	public int compareTo(REIT that) {
		return this.stockCode.compareTo(that.stockCode);
	}
	@Override
	public boolean equals(Object o) {
		if (o == null) return false;
		if (o instanceof REIT) {
			REIT that = (REIT)o;
			return 
				this.stockCode.equals(that.stockCode) &&
				this.name.equals(that.name);
		} else {
			return false;
		}
	}
}
