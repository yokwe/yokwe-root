package yokwe.stock.jp.jpx;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import yokwe.stock.jp.Storage;
import yokwe.util.ListUtil;
import yokwe.util.StringUtil;

public class JPXETN implements Comparable<JPXETN> {	
	private static final String PATH_FILE = Storage.JPX.getPath("jpx-etn.csv");
	public static String getPath() {
		return PATH_FILE;
	}
	
	public static List<JPXETN> getList() {
		return ListUtil.getList(JPXETN.class, getPath());
	}
	public static Map<String, JPXETN> getMap() {
		//            stockCode
		var list = getList();
		return ListUtil.checkDuplicate(list, o -> o.stockCode);
	}
	public static void save(Collection<JPXETN> collection) {
		ListUtil.save(JPXETN.class, getPath(), collection);
	}
	public static void save(List<JPXETN> list) {
		ListUtil.save(JPXETN.class, getPath(), list);
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
	public int compareTo(JPXETN that) {
		return this.stockCode.compareTo(that.stockCode);
	}
	@Override
	public boolean equals(Object o) {
		if (o == null) return false;
		if (o instanceof JPXETN) {
			JPXETN that = (JPXETN)o;
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
