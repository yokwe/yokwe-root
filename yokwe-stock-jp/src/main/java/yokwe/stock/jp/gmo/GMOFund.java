package yokwe.stock.jp.gmo;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

import yokwe.stock.jp.Storage;
import yokwe.util.ListUtil;
import yokwe.util.ToString;

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
	
	private static Map<String, BigDecimal> salesFeeMap = null;
	private static void initSalesFeeMap() {
		salesFeeMap = new TreeMap<>();
		for(var e: getList()) {
			salesFeeMap.put(e.isinCode, e.salesFee);
		}
	}
	public static BigDecimal getSalesFee(String isinCode, BigDecimal defaultValue) {
		if (salesFeeMap == null) initSalesFeeMap();
		return salesFeeMap.getOrDefault(isinCode, defaultValue);
	}
	
	
	public String     isinCode;
	public String     fundCode;
	
    public BigDecimal salesFee;      // value contains consumption tax
    public BigDecimal expenseRatio;  // value contains consumption tax
	
	public String     name;
	
	public GMOFund(String isinCode, String fundCode, BigDecimal salesFee, BigDecimal expenseRatio, String name) {
		this.isinCode      = isinCode;
		this.fundCode      = fundCode;
		this.salesFee      = salesFee;
		this.expenseRatio  = expenseRatio;
		this.name          = name;
	}
	
	@Override
	public String toString() {
		return ToString.withFieldName(this);
	}
	
	@Override
	public int compareTo(GMOFund that) {
		return this.isinCode.compareTo(that.isinCode);
	}

}
