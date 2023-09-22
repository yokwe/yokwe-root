package yokwe.finance.provider.jpx;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import yokwe.finance.Storage;
import yokwe.util.ListUtil;
import yokwe.util.StringUtil;

public class ETN implements Comparable<ETN> {	
	private static final String PATH_FILE = Storage.provider_jpx.getPath("etn.csv");
	public static String getPath() {
		return PATH_FILE;
	}
	
	public static List<ETN> getList() {
		return ListUtil.getList(ETN.class, getPath());
	}
	public static Map<String, ETN> getMap() {
		//            stockCode
		var list = getList();
		return ListUtil.checkDuplicate(list, o -> o.stockCode);
	}
	public static void save(Collection<ETN> collection) {
		ListUtil.save(ETN.class, getPath(), collection);
	}
	public static void save(List<ETN> list) {
		ListUtil.save(ETN.class, getPath(), list);
	}
	
	
	public String     stockCode;
	public String     name;
	public BigDecimal expenseRatio;
	public String     indexName;
	
	@Override
	public String toString() {
		return StringUtil.toString(this);
	}
	
	@Override
	public int compareTo(ETN that) {
		return this.stockCode.compareTo(that.stockCode);
	}
	@Override
	public boolean equals(Object o) {
		if (o == null) return false;
		if (o instanceof ETN) {
			ETN that = (ETN)o;
			return 
				this.stockCode.equals(that.stockCode) &&
				this.indexName.equals(that.indexName) &&
				this.expenseRatio.equals(that.expenseRatio) &&
				this.name.equals(that.name);
		} else {
			return false;
		}
	}
	
}
