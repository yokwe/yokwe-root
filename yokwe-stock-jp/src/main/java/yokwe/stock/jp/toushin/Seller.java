package yokwe.stock.jp.toushin;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import yokwe.stock.jp.Storage;
import yokwe.util.ListUtil;
import yokwe.util.StringUtil;
import yokwe.util.UnexpectedException;

public class Seller implements Comparable<Seller> {
	private static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();
	
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

	private static Map<String, Map<String, BigDecimal>> salesFeeMap = null;
	//                 isinCode    sellerName
	private static void initSellerMap() {
		salesFeeMap = new TreeMap<>();
		for(var e: getList()) {
			Map<String, BigDecimal> map;
			if (salesFeeMap.containsKey(e.isinCode)) {
				map = salesFeeMap.get(e.isinCode);
			} else {
				map = new TreeMap<>();
				salesFeeMap.put(e.isinCode, map);
			}
			if (map.containsKey(e.sellerName)) {
				logger.error("Unexpected sellerName");
				logger.error("  seller  {}", e.toString());
				throw new UnexpectedException("Unexpected sellerNamed");
			}
			map.put(e.sellerName, e.salesFee);
		}
	}
	
	public static BigDecimal getSalesFee(String isinCode, String sellerName) {
		if (salesFeeMap == null) initSellerMap();
		
		if (salesFeeMap.containsKey(isinCode)) {
			var map = salesFeeMap.get(isinCode);
			if (map.containsKey(sellerName)) {
				return map.get(sellerName);
			} else {
				logger.warn("Unexpected sellerName  {}  {}", isinCode, sellerName);
				return null;
			}
		} else {
			logger.warn("Unexpected sellerName  {}  {}", isinCode, sellerName);
			return null;
		}
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
