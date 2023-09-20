package yokwe.finance.provider.jpx;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import yokwe.finance.Storage;
import yokwe.util.ListUtil;
import yokwe.util.StringUtil;

public class StockDetail implements Comparable<StockDetail> {
	private static final String PATH_FILE = Storage.Provider.JPX.getPath("stock-detail.csv");
	public static String getPath() {
		return PATH_FILE;
	}
	
	public static List<StockDetail> getList() {
		return ListUtil.getList(StockDetail.class, getPath());
	}
	public static Map<String, StockDetail> getMap() {
		//            stockCode
		var list = getList();
		return ListUtil.checkDuplicate(list, o -> o.stockCode);
	}
	public static void save(Collection<StockDetail> collection) {
		ListUtil.save(StockDetail.class, getPath(), collection);
	}
	public static void save(List<StockDetail> list) {
		ListUtil.save(StockDetail.class, getPath(), list);
	}
	
	
	public final String stockCode;
	public final String isinCode;
	public final int    tradeUnit;
	public final long   issued;
	
	public StockDetail(String stockCode, String isinCode, int tradeUnit, long issued) {
		this.stockCode = stockCode;
		this.isinCode  = isinCode;
		this.tradeUnit = tradeUnit;
		this.issued    = issued;
	}
	
	
	@Override
	public String toString() {
		return StringUtil.toString(this);
	}
	
	@Override
	public int compareTo(StockDetail that) {
		return this.stockCode.compareTo(that.stockCode);
	}
	@Override
	public boolean equals(Object o) {
		if (o == null) return false;
		if (o instanceof StockDetail) {
			StockDetail that = (StockDetail)o;
			return 
				this.stockCode.equals(that.stockCode) &&
				this.isinCode.equals(that.isinCode) &&
				this.tradeUnit == that.tradeUnit &&
				this.issued == that.issued;
		} else {
			return false;
		}
	}
}
