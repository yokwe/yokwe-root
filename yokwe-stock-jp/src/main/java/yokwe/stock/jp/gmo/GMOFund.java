package yokwe.stock.jp.gmo;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import yokwe.stock.jp.Storage;
import yokwe.util.ListUtil;
import yokwe.util.StringUtil;

public class GMOFund implements Comparable<GMOFund> {
	private static final String PATH_FILE = Storage.GMO.getPath("gmo-fund.csv");
	public static final String getPath() {
		return PATH_FILE;
	}

	private static List<GMOFund> list = null;
	public static List<GMOFund> getList() {
		if (list == null) {
			list = ListUtil.getList(GMOFund.class, getPath());
		}
		return list;
	}

	private static Map<String, GMOFund> map = null;
	public static Map<String, GMOFund> getMap() {
		if (map == null) {
			var list = getList();
			map = list.stream().collect(Collectors.toMap(o -> o.isinCode, o -> o));
		}
		return map;
	}

	public static void save(Collection<GMOFund> collection) {
		ListUtil.save(GMOFund.class, getPath(), collection);
	}
	public static void save(List<GMOFund> list) {
		ListUtil.save(GMOFund.class, getPath(), list);
	}
	
	public String isinCode;
	public String fundCode;
	public String name;
	
	public GMOFund(String isinCode, String fundCode, String name) {
		this.isinCode = isinCode;
		this.fundCode = fundCode;
		this.name     = name;
	}
	
	@Override
	public String toString() {
		return StringUtil.toString(this);
	}
	
	@Override
	public int compareTo(GMOFund that) {
		return this.isinCode.compareTo(that.isinCode);
	}

}
