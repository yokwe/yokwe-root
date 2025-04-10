package yokwe.stock.jp.rakuten;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import yokwe.stock.jp.Storage;
import yokwe.util.ListUtil;
import yokwe.util.ToString;

public class RakutenFund implements Comparable<RakutenFund> {
	private static final String PATH_FILE = Storage.Rakuten.getPath("rakuten-fund.csv");
	public static final String getPath() {
		return PATH_FILE;
	}

	private static List<RakutenFund> list = null;
	public static List<RakutenFund> getList() {
		if (list == null) {
			list = ListUtil.getList(RakutenFund.class, getPath());
		}
		return list;
	}

	private static Map<String, RakutenFund> map = null;
	public static Map<String, RakutenFund> getMap() {
		if (map == null) {
			var list = getList();
			map = list.stream().collect(Collectors.toMap(o -> o.isinCode, o -> o));
		}
		return map;
	}

	public static void save(Collection<RakutenFund> collection) {
		ListUtil.save(RakutenFund.class, getPath(), collection);
	}
	public static void save(List<RakutenFund> list) {
		ListUtil.save(RakutenFund.class, getPath(), list);
	}
	
	public String     isinCode;
	public String     fundCode;
	
    public BigDecimal salesFee;      // value contains consumption tax
    public BigDecimal expenseRatio;  // value contains consumption tax
    
	public String     name;
	
	public RakutenFund(String isinCode, String fundCode, BigDecimal salesFee, BigDecimal expenseRatio, String name) {
			this.isinCode     = isinCode;
			this.fundCode     = fundCode;
			this.salesFee     = salesFee;
			this.expenseRatio = expenseRatio;
			this.name         = name;
	}
	
	@Override
	public String toString() {
		return ToString.withFieldName(this);
	}
	
	@Override
	public int compareTo(RakutenFund that) {
		return this.isinCode.compareTo(that.isinCode);
	}

}
