package yokwe.stock.jp.jpx;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import yokwe.stock.jp.Storage;
import yokwe.util.ListUtil;
import yokwe.util.ToString;

public class JPXETF implements Comparable<JPXETF> {	
	private static final String PATH_FILE = Storage.JPX.getPath("jpx-etf.csv");
	public static String getPath() {
		return PATH_FILE;
	}
	
	public static List<JPXETF> getList() {
		return ListUtil.getList(JPXETF.class, getPath());
	}
	public static Map<String, JPXETF> getMap() {
		//            stockCode
		var list = getList();
		return ListUtil.checkDuplicate(list, o -> o.stockCode);
	}
	public static void save(Collection<JPXETF> collection) {
		ListUtil.save(JPXETF.class, getPath(), collection);
	}
	public static void save(List<JPXETF> list) {
		ListUtil.save(JPXETF.class, getPath(), list);
	}
	
	
	public String     stockCode;
	public String     name;
	public BigDecimal expenseRatio;
	public String     indexName;
	
	@Override
	public String toString() {
		return ToString.withFieldName(this);
	}
	
	@Override
	public int compareTo(JPXETF that) {
		return this.stockCode.compareTo(that.stockCode);
	}
	@Override
	public boolean equals(Object o) {
		if (o == null) return false;
		if (o instanceof JPXETF) {
			JPXETF that = (JPXETF)o;
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
