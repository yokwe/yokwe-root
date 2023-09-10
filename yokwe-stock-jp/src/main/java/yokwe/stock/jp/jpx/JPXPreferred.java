package yokwe.stock.jp.jpx;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import yokwe.stock.jp.Storage;
import yokwe.util.ListUtil;
import yokwe.util.StringUtil;

public class JPXPreferred implements Comparable<JPXPreferred> {
	// Current
	// https://www.jpx.co.jp/equities/products/preferred-stocks/issues/index.html
	private static final String PATH_FILE = Storage.JPX.getPath("jpx-preferred.csv");
	public static String getPath() {
		return PATH_FILE;
	}
	
	public static List<JPXPreferred> getList() {
		return ListUtil.getList(JPXPreferred.class, getPath());
	}
	public static Map<String, JPXPreferred> getMap() {
		//            stockCode
		var list = getList();
		return ListUtil.checkDuplicate(list, o -> o.stockCode);
	}
	public static void save(Collection<JPXPreferred> collection) {
		ListUtil.save(JPXPreferred.class, getPath(), collection);
	}
	public static void save(List<JPXPreferred> list) {
		ListUtil.save(JPXPreferred.class, getPath(), list);
	}
	
	
	public String stockCode;
	public String name;
	
	@Override
	public String toString() {
		return StringUtil.toString(this);
	}
	
	@Override
	public int compareTo(JPXPreferred that) {
		return this.stockCode.compareTo(that.stockCode);
	}
	@Override
	public boolean equals(Object o) {
		if (o == null) return false;
		if (o instanceof JPXPreferred) {
			JPXPreferred that = (JPXPreferred)o;
			return 
				this.stockCode.equals(that.stockCode) &&
				this.name.equals(that.name);
		} else {
			return false;
		}
	}
}
