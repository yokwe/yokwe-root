package yokwe.finance.provider.yahoo;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import yokwe.util.ListUtil; // FIXME

public final class Search implements Comparable<Search> {
	private static final String PATH_FILE = StorageYahoo.getPath("search.csv");
	public static String getPath() {
		return PATH_FILE;
	}
	
	public static List<Search> getList() {
		return ListUtil.getList(Search.class, getPath());
	}
	public static Map<String, Search> getMap() {
		//            stockCode
		return ListUtil.checkDuplicate(getList(), o -> o.stockCode);
	}
	public static void save(Collection<Search> collection) {
		ListUtil.save(Search.class, getPath(), collection);
	}
	public static void save(List<Search> list) {
		ListUtil.save(Search.class, getPath(), list);
	}
	
	public String stockCode;
	public String type;
	public String exchange;
	public String sector;
	public String industry;
	public String name;
	
	public Search(
		String stockCode,
		String type,
		String exchange,
		String sector,
		String industry,
		String name
		) {
		this.stockCode = stockCode;
		this.type      = type;
		this.exchange  = exchange;
		this.sector    = sector;
		this.industry  = industry;
		this.name      = name;
	}
	
	@Override
	public String toString() {
		return String.format("{%s  %s  %s  \"%s\"  \"%s\" \"%s\"}", stockCode, type, exchange, sector, industry, name);
	}
	
	@Override
	public int compareTo(Search that) {
		return this.stockCode.compareTo(that.stockCode);
	}
}
