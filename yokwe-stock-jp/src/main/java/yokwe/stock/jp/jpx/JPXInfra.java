package yokwe.stock.jp.jpx;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import yokwe.stock.jp.Storage;
import yokwe.util.ListUtil;
import yokwe.util.StringUtil;

public class JPXInfra implements Comparable<JPXInfra> {
	private static final String PATH_FILE = Storage.JPX.getPath("jpx-infra.csv");
	public static String getPath() {
		return PATH_FILE;
	}
	
	public static List<JPXInfra> getList() {
		return ListUtil.getList(JPXInfra.class, getPath());
	}
	public static Map<String, JPXInfra> getMap() {
		//            stockCode
		var list = getList();
		return ListUtil.checkDuplicate(list, o -> o.stockCode);
	}
	public static void save(Collection<JPXInfra> collection) {
		ListUtil.save(JPXInfra.class, getPath(), collection);
	}
	public static void save(List<JPXInfra> list) {
		ListUtil.save(JPXInfra.class, getPath(), list);
	}
	
	
	public String stockCode;
	public String name;
	
	@Override
	public String toString() {
		return StringUtil.toString(this);
	}
	
	@Override
	public int compareTo(JPXInfra that) {
		return this.stockCode.compareTo(that.stockCode);
	}
	@Override
	public boolean equals(Object o) {
		if (o == null) return false;
		if (o instanceof JPXInfra) {
			JPXInfra that = (JPXInfra)o;
			return 
				this.stockCode.equals(that.stockCode) &&
				this.name.equals(that.name);
		} else {
			return false;
		}
	}
}
