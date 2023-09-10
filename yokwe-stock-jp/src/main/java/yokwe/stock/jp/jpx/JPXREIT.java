package yokwe.stock.jp.jpx;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import yokwe.stock.jp.Storage;
import yokwe.util.ListUtil;
import yokwe.util.StringUtil;

public class JPXREIT implements Comparable<JPXREIT> {
	private static final String PATH_FILE = Storage.JPX.getPath("jpx-reit.csv");
	public static String getPath() {
		return PATH_FILE;
	}
	
	public static List<JPXREIT> getList() {
		return ListUtil.getList(JPXREIT.class, getPath());
	}
	public static Map<String, JPXREIT> getMap() {
		//            stockCode
		var list = getList();
		return ListUtil.checkDuplicate(list, o -> o.stockCode);
	}
	public static void save(Collection<JPXREIT> collection) {
		ListUtil.save(JPXREIT.class, getPath(), collection);
	}
	public static void save(List<JPXREIT> list) {
		ListUtil.save(JPXREIT.class, getPath(), list);
	}
	
	
	public String stockCode;
	public String name;
	
	@Override
	public String toString() {
		return StringUtil.toString(this);
	}
	
	@Override
	public int compareTo(JPXREIT that) {
		return this.stockCode.compareTo(that.stockCode);
	}
	@Override
	public boolean equals(Object o) {
		if (o == null) return false;
		if (o instanceof JPXREIT) {
			JPXREIT that = (JPXREIT)o;
			return 
				this.stockCode.equals(that.stockCode) &&
				this.name.equals(that.name);
		} else {
			return false;
		}
	}
}
