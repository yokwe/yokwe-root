package yokwe.stock.jp.japanreit;

import java.util.List;
import java.util.Map;

import yokwe.stock.jp.Storage;
import yokwe.util.ListUtil;
import yokwe.util.ToString;

public class REIT implements Comparable<REIT> {
	private static final String PATH = Storage.JapanREIT.getPath("reit.csv");
	public static String getPath() {
		return PATH;
	}

	public static void save(List<REIT> list) {
		ListUtil.checkDuplicate(list, o -> o.stockCode);
		ListUtil.save(REIT.class, getPath(), list);
	}
	
	public static List<REIT> load() {
		return ListUtil.load(REIT.class, getPath());
	}
	public static List<REIT> getList() {
		return ListUtil.getList(REIT.class, getPath());
	}
	public static Map<String, REIT> getMap() {
		//            stockCode
		var list = ListUtil.getList(REIT.class, getPath());
		return ListUtil.checkDuplicate(list, o -> o.stockCode);
	}


	// https://www.japan-reit.com/meigara/8954/info/
	// 上場日 2002/06/12
	
	public String stockCode;
	public String listingDate;
	public int    divFreq;
	public String category;
	public String name;

	public REIT(String stockCode, String listingDate, int divFreq, String category, String name) {
		this.stockCode   = stockCode;
		this.listingDate = listingDate;
		this.divFreq     = divFreq;
		this.category    = category;
		this.name        = name;
	}
	public REIT() {
		this(null, null, 0, null, null);
	}
	
	@Override
	public String toString() {
	    return ToString.withFieldName(this);
	}

	@Override
	public int compareTo(REIT that) {
		return this.stockCode.compareTo(that.stockCode);
	}
	
	@Override
	public int hashCode() {
		return stockCode.hashCode();
	}
	
	@Override
	public boolean equals(Object o) {
		if (o != null) {
			if (o instanceof REIT) {
				REIT that = (REIT)o;
				return this.compareTo(that) == 0;
			} else {
				return false;
			}
		} else {
			return false;
		}
	}
}
