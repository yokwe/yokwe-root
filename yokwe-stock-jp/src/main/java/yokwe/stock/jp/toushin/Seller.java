package yokwe.stock.jp.toushin;

import java.math.BigDecimal;
import java.util.List;

import yokwe.stock.jp.Storage;
import yokwe.util.ListUtil;
import yokwe.util.StringUtil;

public class Seller implements Comparable<Seller> {
	public static final String PREFIX = "seller";
	
	public static final String getPath(String isinCode) {
		return Storage.Toushin.getPath(PREFIX, isinCode + ".csv");
	}
	public static void save(String isinCode, List<Seller> list) {
		ListUtil.save(Seller.class, getPath(isinCode), list);
	}
	public static List<Seller> load(String isinCode) {
		return ListUtil.load(Seller.class, getPath(isinCode));
	}

	public String     name;
	public BigDecimal salesFee;
	
	public Seller(String name, BigDecimal salesFee) {
		this.name     = name;
		this.salesFee = salesFee;
	}
	public Seller() {
		name     = "";
		salesFee = BigDecimal.ZERO;
	}
	
	@Override
	public String toString() {
		return StringUtil.toString(this);
	}

	@Override
	public int compareTo(Seller that) {
		return this.name.compareTo(that.name);
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
		return this.name.hashCode();
	}
}
