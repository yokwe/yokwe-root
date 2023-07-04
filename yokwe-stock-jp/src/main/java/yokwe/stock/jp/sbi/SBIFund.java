package yokwe.stock.jp.sbi;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import yokwe.stock.jp.Storage;
import yokwe.util.ListUtil;
import yokwe.util.StringUtil;

public class SBIFund implements Comparable<SBIFund> {
	private static final String PATH_FILE = Storage.SBI.getPath("sbi-fund.csv");
	public static final String getPath() {
		return PATH_FILE;
	}

	private static List<SBIFund> list = null;
	public static List<SBIFund> getList() {
		if (list == null) {
			list = ListUtil.getList(SBIFund.class, getPath());
		}
		return list;
	}

	private static Map<String, SBIFund> map = null;
	public static Map<String, SBIFund> getMap() {
		if (map == null) {
			var list = getList();
			map = list.stream().collect(Collectors.toMap(o -> o.isinCode, o -> o));
		}
		return map;
	}

	public static void save(Collection<SBIFund> collection) {
		ListUtil.save(SBIFund.class, getPath(), collection);
	}
	public static void save(List<SBIFund> list) {
		ListUtil.save(SBIFund.class, getPath(), list);
	}
	
	public String     isinCode;
	public String     fundCode;
	
    public BigDecimal salesFee;      // value contains consumption tax
    public BigDecimal expenseRatio;  // value contains consumption tax
    
	public String     name;
	
	public SBIFund(String isinCode, String fundCode, BigDecimal salesFee, BigDecimal expenseRatio, String name) {
			this.isinCode     = isinCode;
			this.fundCode     = fundCode;
			this.salesFee     = salesFee;
			this.expenseRatio = expenseRatio;
			this.name         = name;
	}
	
	@Override
	public String toString() {
		return StringUtil.toString(this);
	}
	
	@Override
	public int compareTo(SBIFund that) {
		return this.isinCode.compareTo(that.isinCode);
	}

}
