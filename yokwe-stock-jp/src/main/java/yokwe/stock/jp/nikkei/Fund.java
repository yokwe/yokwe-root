package yokwe.stock.jp.nikkei;

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


	public String isinCode;
    public String fundCode;
    
    // CommonName
    public String name;
    
    // FundInfo
	public String category1;
	public String category2;
	public String category3;
	public String settlementFrequency;
	public String establishmentDate;
	public String redemptionDate;
	public String salesType;
	public String fundType;
	public String initialFee;
	public String trustFee;
	
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
	public String return6m;
	public String return1y;
	public String return3y;
	public String return5y;
	public String return10y;
	public String risk6m;
	public String risk1y;
	public String risk3y;
	public String risk5y;
	public String risk10y;
	public String sharpRatio6m;
	public String sharpRatio1y;
	public String sharpRatio3y;
	public String sharpRatio5y;
	public String sharpRatio10y;
	
	// DivScore
	public String divScore1Y;
	public String divScore3Y;
	public String divScore5Y;
	public String divScore10Y;
	
	// DivLast
	public String divLastDate;
	public String divLastAmount;
	public String divLastRate;
	public String divLastPrice;

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
