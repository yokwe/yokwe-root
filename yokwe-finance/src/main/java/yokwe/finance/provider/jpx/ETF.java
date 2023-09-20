package yokwe.finance.provider.jpx;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import yokwe.finance.Storage;
import yokwe.util.ListUtil;
import yokwe.util.StringUtil;

public class ETF implements Comparable<ETF> {	
	private static final String PATH_FILE = Storage.Provider.JPX.getPath("etf.csv");
	public static String getPath() {
		return PATH_FILE;
	}
	
	public static List<ETF> getList() {
		return ListUtil.getList(ETF.class, getPath());
	}
	public static Map<String, ETF> getMap() {
		//            stockCode
		var list = getList();
		return ListUtil.checkDuplicate(list, o -> o.stockCode);
	}
	public static void save(Collection<ETF> collection) {
		ListUtil.save(ETF.class, getPath(), collection);
	}
	public static void save(List<ETF> list) {
		ListUtil.save(ETF.class, getPath(), list);
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
	public int compareTo(ETF that) {
		return this.stockCode.compareTo(that.stockCode);
	}
	@Override
	public boolean equals(Object o) {
		if (o == null) return false;
		if (o instanceof ETF) {
			ETF that = (ETF)o;
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
