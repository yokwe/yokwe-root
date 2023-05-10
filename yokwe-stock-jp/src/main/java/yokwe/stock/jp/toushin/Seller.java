package yokwe.stock.jp.toushin;

import java.math.BigDecimal;
import java.util.List;

import yokwe.stock.jp.Storage;
import yokwe.util.ListUtil;
import yokwe.util.StringUtil;

public class Seller implements Comparable<Seller> {
	private static final String PATH = Storage.Toushin.getPath("seller.csv");
	public static String getPath() {
		return PATH;
	}

	public static void save(List<Seller> list) {
		ListUtil.checkDuplicate(list, o -> o.isinCode + o.sellerName);
		ListUtil.save(Seller.class, getPath(), list);
	}
	
	public static List<Seller> load() {
		return ListUtil.load(Seller.class, getPath());
	}
	public static List<Seller> getList() {
		return ListUtil.getList(Seller.class, getPath());
	}

	public String     isinCode;
	public String     sellerName;
	public BigDecimal salesFee;
	
	public Seller(String isinCode, String name, BigDecimal salesFee) {
		this.isinCode   = isinCode;
		this.sellerName = name;
		this.salesFee   = salesFee;
	}
	public Seller() {
		isinCode   = "";
		sellerName = "";
		salesFee   = BigDecimal.ZERO;
	}
	
	@Override
	public String toString() {
		return StringUtil.toString(this);
	}

	@Override
	public int compareTo(Seller that) {
		int ret = this.isinCode.compareTo(that.isinCode);
		if (ret == 0) this.sellerName.compareTo(that.sellerName);
		if (ret == 0) this.salesFee.compareTo(that.salesFee);
		return ret;
	}
	@Override
	public boolean equals(Object o) {
		if (o == null) {
			return false;
		} else {
			if (o instanceof Seller) {
				Seller that = (Seller)o;
				return this.compareTo(that) == 0;
			} else {
				return false;
			}
		}
	}
	@Override
	public int hashCode() {
		int hashCode = 0;
		if (this.isinCode   != null) hashCode ^= this.isinCode.hashCode();
		if (this.sellerName != null) hashCode ^= this.sellerName.hashCode();
		if (this.salesFee   != null) hashCode ^= this.salesFee.hashCode();
		return hashCode;
	}
}
