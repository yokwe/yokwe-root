package yokwe.stock.jp.nikko;

import java.util.Collection;
import java.util.List;

import yokwe.stock.jp.Storage;
import yokwe.util.ListUtil;
import yokwe.util.StringUtil;

public class NikkoFund implements Comparable<NikkoFund>  {
	private static final String PATH_FILE = Storage.Nikko.getPath("nikko-fund.csv");
	public static String getPath() {
		return PATH_FILE;
	}
	
	public static List<NikkoFund> getList() {
		return ListUtil.getList(NikkoFund.class, getPath());
	}
	public static void save(Collection<NikkoFund> collection) {
		ListUtil.save(NikkoFund.class, getPath(), collection);
	}
	public static void save(List<NikkoFund> list) {
		ListUtil.save(NikkoFund.class, getPath(), list);
	}

	public String isinCode;
	public String fundCode;
	public String nikkoCode;
	
	public String fee;
	
	public String name;
	
	public NikkoFund(
		String isinCode,
		String fundCode,
		String nikkoCode,
		String fee,
		String name
		) {
		this.isinCode    = isinCode;
		this.fundCode    = fundCode;
		this.nikkoCode   = nikkoCode;
		this.fee         = fee;
		this.name        = name;
	}
	
	
	@Override
	public String toString() {
		return StringUtil.toString(this);
	}

	@Override
	public int compareTo(NikkoFund that) {
		return this.isinCode.compareTo(that.isinCode);
	}
}
