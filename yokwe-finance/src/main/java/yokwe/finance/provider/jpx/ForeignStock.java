package yokwe.finance.provider.jpx;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import yokwe.finance.Storage;
import yokwe.util.ListUtil;
import yokwe.util.StringUtil;

public class ForeignStock implements Comparable<ForeignStock> {
	private static final String PATH_FILE = Storage.Provider.JPX.getPath("foreign-stock.csv");
	public static String getPath() {
		return PATH_FILE;
	}
	
	public static List<ForeignStock> getList() {
		return ListUtil.getList(ForeignStock.class, getPath());
	}
	public static Map<String, ForeignStock> getMap() {
		//            stockCode
		var list = getList();
		return ListUtil.checkDuplicate(list, o -> o.stockCode);
	}
	public static void save(Collection<ForeignStock> collection) {
		ListUtil.save(ForeignStock.class, getPath(), collection);
	}
	public static void save(List<ForeignStock> list) {
		ListUtil.save(ForeignStock.class, getPath(), list);
	}
	
	
	public String     stockCode;
	public String     name;
	
	@Override
	public String toString() {
		return StringUtil.toString(this);
	}
	
	@Override
	public int compareTo(ForeignStock that) {
		return this.stockCode.compareTo(that.stockCode);
	}
	@Override
	public boolean equals(Object o) {
		if (o == null) return false;
		if (o instanceof ForeignStock) {
			ForeignStock that = (ForeignStock)o;
			return 
				this.stockCode.equals(that.stockCode) &&
				this.name.equals(that.name);
		} else {
			return false;
		}
	}
}
