package yokwe.stock.jp.nikkei;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import yokwe.stock.jp.Storage;
import yokwe.util.ListUtil;
import yokwe.util.ToString;

public class NikkeiFund implements Comparable<NikkeiFund> {
	private static final String PATH_FILE = Storage.Nikkei.getPath("nikkei-fund.csv");
	public static final String getPath() {
		return PATH_FILE;
	}

	private static List<NikkeiFund> list = null;
	public static List<NikkeiFund> getList() {
		if (list == null) {
			list = ListUtil.getList(NikkeiFund.class, getPath());
		}
		return list;
	}
	
	private static Map<String, NikkeiFund> map = null;
	public static Map<String, NikkeiFund> getMap() {
		if (map == null) {
			var list = getList();
			map = list.stream().collect(Collectors.toMap(o -> o.isinCode, o -> o));
		}
		return map;
	}

	public static void save(Collection<NikkeiFund> collection) {
		ListUtil.save(NikkeiFund.class, getPath(), collection);
	}
	public static void save(List<NikkeiFund> list) {
		ListUtil.save(NikkeiFund.class, getPath(), list);
	}

	public static final LocalDate UNLIMITED_DATE = LocalDate.parse("2999-01-01");

	public String isinCode;
    public String fundCode;
    
	public String divScore1Y;
	public String divScore3Y;
	public String divScore5Y;
	public String divScore10Y;
    
    public String name;
	    
	// Use default constructor
	
	@Override
	public String toString() {
		return ToString.withFieldName(this);
	}
	@Override
	public int compareTo(NikkeiFund that) {
		int ret = this.isinCode.compareTo(that.isinCode);
		return ret;
	}
 }
