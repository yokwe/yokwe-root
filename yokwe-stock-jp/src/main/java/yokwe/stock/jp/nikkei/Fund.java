package yokwe.stock.jp.nikkei;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import yokwe.stock.jp.Storage;
import yokwe.util.ListUtil;
import yokwe.util.StringUtil;

public class Fund implements Comparable<Fund> {
	private static final String PATH_FILE = Storage.Nikkei.getPath("fund.csv");
	public static final String getPath() {
		return PATH_FILE;
	}

	private static List<Fund> list = null;
	public static List<Fund> getList() {
		if (list == null) {
			list = ListUtil.getList(Fund.class, getPath());
		}
		return list;
	}
	
	private static Map<String, Fund> map = null;
	public static Map<String, Fund> getMap() {
		if (map == null) {
			var list = getList();
			map = list.stream().collect(Collectors.toMap(o -> o.isinCode, o -> o));
		}
		return map;
	}

	public static void save(Collection<Fund> collection) {
		ListUtil.save(Fund.class, getPath(), collection);
	}
	public static void save(List<Fund> list) {
		ListUtil.save(Fund.class, getPath(), list);
	}

	public static final LocalDate UNLIMITED_DATE = LocalDate.parse("2999-01-01");

	public String isinCode;
    public String fundCode;
    
    // CommonName
    public String name;
    
    // FundInfo
	public String category1;
	public String category2;
	public String category3;
	public String settlementFrequency;
	public LocalDate initiationDate;
	public LocalDate redemptionDate;
	public String salesType;
	public String fundType;
	public BigDecimal initialFee;
	public BigDecimal trustFee;
	
	// PerfScore
	public String scoreAsOf;
	public String scoreOverAll;
	public String scoreRisk;
	public String scoreReturn;
	public String scoreDownsideResistance;
	public String scoreCost;
	public String scoreDivHealth;
	
	// PerfValues
	public String valueAsOf;
	public BigDecimal return6m;
	public BigDecimal return1y;
	public BigDecimal return3y;
	public BigDecimal return5y;
	public BigDecimal return10y;
	public BigDecimal risk6m;
	public BigDecimal risk1y;
	public BigDecimal risk3y;
	public BigDecimal risk5y;
	public BigDecimal risk10y;
	public String sharpRatio6m;
	public String sharpRatio1y;
	public String sharpRatio3y;
	public String sharpRatio5y;
	public String sharpRatio10y;
	
	// DivScore
	public BigDecimal divScore1Y;
	public BigDecimal divScore3Y;
	public BigDecimal divScore5Y;
	public BigDecimal divScore10Y;
	
	// DivLast
	public LocalDate  divLastDate;
	public BigDecimal divLastAmount;
	public BigDecimal divLastRate;
	public BigDecimal divLastPrice;

	// FundPolicy
	public String policy;
    
	// Use default constructor
	
	@Override
	public String toString() {
		return StringUtil.toString(this);
	}
	@Override
	public int compareTo(Fund that) {
		int ret = this.isinCode.compareTo(that.isinCode);
		return ret;
	}
 }
