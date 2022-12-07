package yokwe.stock.jp.toushin;

import java.util.List;
import java.util.Map;

import yokwe.stock.jp.Storage;
import yokwe.util.ListUtil;
import yokwe.util.StringUtil;

public final class FundData implements Comparable<FundData> {
	private static final String PATH = Storage.Toushin.getPath("fund-data.csv");
	public static String getPath() {
		return PATH;
	}

	public static void save(List<FundData> list) {
		ListUtil.checkDuplicate(list, o -> o.isinCode);
		ListUtil.save(FundData.class, getPath(), list);
	}
	
	public static List<FundData> load() {
		return ListUtil.load(FundData.class, getPath());
	}
	public static List<FundData> getList() {
		return ListUtil.getList(FundData.class, getPath());
	}
	public static Map<String, FundData> getMap() {
		//            stockCode
		var list = ListUtil.getList(FundData.class, getPath());
		return ListUtil.checkDuplicate(list, o -> o.isinCode);
	}

	public String isinCode;
	public String name;
	
	public FundData(String isin, String name) {
		this.isinCode = isin;
		this.name = name;
	}
	public FundData() {
		this(null, null);
	}
	
    @Override
    public String toString() {
        return StringUtil.toString(this);
    }
    
    @Override
    public int compareTo(FundData that) {
    	return this.isinCode.compareTo(that.isinCode);
    }
    @Override
    public boolean equals(Object o) {
    	if (o == null) {
    		return false;
    	} else {
    		if (o instanceof FundData) {
    			FundData that = (FundData)o;
    			return this.compareTo(that) == 0;
    		} else {
    			return false;
    		}
    	}
    }
    @Override
    public int hashCode() {
    	return this.isinCode.hashCode();
    }
}

