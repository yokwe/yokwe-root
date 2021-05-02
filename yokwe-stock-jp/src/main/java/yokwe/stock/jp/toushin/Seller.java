package yokwe.stock.jp.toushin;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

import yokwe.util.CSVUtil;
import yokwe.util.StringUtil;

public class Seller implements Comparable<Seller> {
	public static final String getPath(String isinCode) {
		String path = String.format("seller/%s.csv", isinCode);
		return Toushin.getPath(path);
	}
	public static void save(String isinCode, List<Seller> list) {
		Collections.sort(list);
		CSVUtil.write(Seller.class).file(getPath(isinCode), list);
	}
	public static List<Seller> load(String isinCode) {
		return CSVUtil.read(Seller.class).file(getPath(isinCode));
	}

	public String     isinCode;   // isinCode
	public BigDecimal salesFee;   // 購入時手数料 販売価格に対する割合
	public String     sellerCode; // 販売会社
	public String     sellerName; // 販売会社名
	
	public Seller(String isinCode, BigDecimal salesFee, String sellerCode, String sellerName) {
		this.isinCode   = isinCode;
		this.salesFee   = salesFee;
		this.sellerCode = sellerCode;
		this.sellerName = sellerName;
	}
	public Seller() {
		this(null, null, null, null);
	}

	@Override
	public int compareTo(Seller that) {
		int ret = this.isinCode.compareTo(that.isinCode);
		if (ret == 0) ret = this.sellerCode.compareTo(that.sellerCode);
		return ret;
	}
	
	@Override
	public String toString() {
		return StringUtil.toString(this);
	}
}