package yokwe.stock.jp.nikko;

import java.util.Collection;
import java.util.List;

import yokwe.stock.jp.Storage;
import yokwe.util.ListUtil;

public class NikkoFundInfo implements Comparable<NikkoFundInfo> {
	private static final String PATH_FILE = Storage.Nikko.getPath("nikko-fund-info.csv");
	public static String getPath() {
		return PATH_FILE;
	}
	
	public static List<NikkoFundInfo> getList() {
		return ListUtil.getList(NikkoFundInfo.class, getPath());
	}
	public static void save(Collection<NikkoFundInfo> collection) {
		ListUtil.save(NikkoFundInfo.class, getPath(), collection);
	}
	public static void save(List<NikkoFundInfo> list) {
		ListUtil.save(NikkoFundInfo.class, getPath(), list);
	}

	public String isinCode;
	public String fundCode;
	public String nikkoCode;
	
	public String tradeDirect; // 1 means YES  0 means NO
	public String tradeSougou; // 1 means YES  0 means NO
	
	public String name;
	
	public NikkoFundInfo(
		String isinCode,
		String fundCode,
		String nikkoCode,
		String tradeDirect,
		String tradeSougou,
		String name
		) {
		this.isinCode    = isinCode;
		this.fundCode    = fundCode;
		this.nikkoCode   = nikkoCode;
		this.tradeDirect = tradeDirect;
		this.tradeSougou = tradeSougou;
		this.name        = name;
	}
	
	
	@Override
	public String toString() {
		return String.format("{%s %s %d %d %s}", isinCode, fundCode, tradeDirect, tradeSougou, name);
	}

	@Override
	public int compareTo(NikkoFundInfo that) {
		return this.isinCode.compareTo(that.isinCode);
	}
}
