package yokwe.stock.jp.nikko;

import java.util.Collection;
import java.util.List;

import yokwe.stock.jp.Storage;
import yokwe.util.ListUtil;

public class Fund implements Comparable<Fund> {
	private static final String PATH_FILE = Storage.Nikko.getPath("fund.csv");
	public static String getPath() {
		return PATH_FILE;
	}
	
	public static List<Fund> getList() {
		return ListUtil.getList(Fund.class, getPath());
	}
	public static void save(Collection<Fund> collection) {
		ListUtil.save(Fund.class, getPath(), collection);
	}
	public static void save(List<Fund> list) {
		ListUtil.save(Fund.class, getPath(), list);
	}

	public String isinCode;
	public String fundCode;
	
	public String tradeDirect; // 1 means YES  0 means NO
	public String tradeSougou; // 1 means YES  0 means NO
	
	public String name;
	
	public Fund(
		String isinCode,
		String fundCode,
		String tradeDirect,
		String tradeSougou,
		String name
		) {
		this.isinCode    = isinCode;
		this.fundCode    = fundCode;
		this.tradeDirect = tradeDirect;
		this.tradeSougou = tradeSougou;
		this.name        = name;
	}
	
	
	@Override
	public String toString() {
		return String.format("{%s %s %d %d %s}", isinCode, fundCode, tradeDirect, tradeSougou, name);
	}

	@Override
	public int compareTo(Fund that) {
		return this.isinCode.compareTo(that.isinCode);
	}
}
