package yokwe.stock.jp.nikko;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import yokwe.stock.jp.Storage;
import yokwe.util.ListUtil;
import yokwe.util.ToString;

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
	public String     nikkoCode;
	
    public BigDecimal salesFee;      // value contains consumption tax
    public BigDecimal expenseRatio;  // value contains consumption tax
	
	public String     name;
	
	public NikkoFund(String isinCode, String fundCode, String nikkoCode, BigDecimal salesFee, BigDecimal expenseRatio, String name) {
		this.isinCode     = isinCode;
		this.fundCode     = fundCode;
		this.nikkoCode    = nikkoCode;
		this.salesFee     = salesFee;
		this.expenseRatio = expenseRatio;
		this.name         = name;
	}
	
	
	@Override
	public String toString() {
		return ToString.withFieldName(this);
	}

	@Override
	public int compareTo(NikkoFund that) {
		return this.isinCode.compareTo(that.isinCode);
	}
}
