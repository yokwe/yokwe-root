package yokwe.finance.provider.jpx;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import yokwe.finance.Storage;
import yokwe.util.ListUtil;
import yokwe.util.StringUtil;

public class JPXForeign implements Comparable<JPXForeign> {
	private static final String PATH_FILE = Storage.Provider.JPX.getPath("jpx-foreign.csv");
	public static String getPath() {
		return PATH_FILE;
	}
	
	public static List<JPXForeign> getList() {
		return ListUtil.getList(JPXForeign.class, getPath());
	}
	public static Map<String, JPXForeign> getMap() {
		//            stockCode
		var list = getList();
		return ListUtil.checkDuplicate(list, o -> o.stockCode);
	}
	public static void save(Collection<JPXForeign> collection) {
		ListUtil.save(JPXForeign.class, getPath(), collection);
	}
	public static void save(List<JPXForeign> list) {
		ListUtil.save(JPXForeign.class, getPath(), list);
	}
	
	
	public String     stockCode;
	public String     name;
	
	@Override
	public String toString() {
		return StringUtil.toString(this);
	}
	
	@Override
	public int compareTo(JPXForeign that) {
		return this.stockCode.compareTo(that.stockCode);
	}
	@Override
	public boolean equals(Object o) {
		if (o == null) return false;
		if (o instanceof JPXForeign) {
			JPXForeign that = (JPXForeign)o;
			return 
				this.stockCode.equals(that.stockCode) &&
				this.name.equals(that.name);
		} else {
			return false;
		}
	}
}
