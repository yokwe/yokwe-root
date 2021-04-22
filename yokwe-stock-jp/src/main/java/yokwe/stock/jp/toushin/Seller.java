package yokwe.stock.jp.toushin;

import java.util.Collections;
import java.util.List;

import yokwe.stock.jp.Storage;
import yokwe.util.CSVUtil;

public class Seller implements Comparable<Seller> {
	public static final String getPath(String isinCode) {
		String name = String.format("seller/%s.csv", isinCode);
		return Storage.getPath(Fund.PREFIX, name);
	}
	public static void save(String isinCode, List<Seller> list) {
		Collections.sort(list);
		CSVUtil.write(Seller.class).file(getPath(isinCode), list);
	}
	public static List<Seller> load(String isinCode) {
		return CSVUtil.read(Seller.class).file(getPath(isinCode));
	}

	public String isinCode;
	public String name;
	public String initialFee;
	
	@Override
	public int compareTo(Seller that) {
		int ret = this.isinCode.compareTo(that.isinCode);
		if (ret == 0) ret = this.name.compareTo(that.name);
		return ret;
	}
}