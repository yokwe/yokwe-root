package yokwe.finance.provider.nasdaq;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import yokwe.finance.Storage;
import yokwe.util.ListUtil;
import yokwe.util.StringUtil;

public class ScreenerStock implements Comparable<ScreenerStock> {
	private static final String PATH_FILE = Storage.provider_nasdaq.getPath("screener-stock.csv");
	public static String getPath() {
		return PATH_FILE;
	}
	
	public static List<ScreenerStock> getList() {
		return ListUtil.getList(ScreenerStock.class, getPath());
	}
	public static Map<String, ScreenerStock> getMap() {
		//            stockCode
		var list = getList();
		return ListUtil.checkDuplicate(list, o -> o.stockCode);
	}
	public static void save(Collection<ScreenerStock> collection) {
		ListUtil.save(ScreenerStock.class, getPath(), collection);
	}
	public static void save(List<ScreenerStock> list) {
		ListUtil.save(ScreenerStock.class, getPath(), list);
	}
	
	public String stockCode;
	public String sector;
	public String industry;
	
	public String name;
	
	@Override
	public String toString() {
		return StringUtil.toString(this);
	}
	
	public String getKey() {
		return this.stockCode;
	}
	@Override
	public int compareTo(ScreenerStock that) {
		return this.getKey().compareTo(that.getKey());
	}
	@Override
	public int hashCode() {
		return this.getKey().hashCode();
	}
}
