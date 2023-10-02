package yokwe.finance.provider.jreit;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import yokwe.finance.Storage;
import yokwe.util.ListUtil;
import yokwe.util.StringUtil;

public class REITInfo implements Comparable<REITInfo> {
	private static final String PATH = Storage.provider_jreit.getPath("reit-info.csv");
	public static String getPath() {
		return PATH;
	}

	public static void save(List<REITInfo> list) {
		ListUtil.checkDuplicate(list, o -> o.stockCode);
		ListUtil.save(REITInfo.class, getPath(), list);
	}
	
	public static List<REITInfo> load() {
		return ListUtil.load(REITInfo.class, getPath());
	}
	public static List<REITInfo> getList() {
		return ListUtil.getList(REITInfo.class, getPath());
	}
	public static Map<String, REITInfo> getMap() {
		//            stockCode
		var list = ListUtil.getList(REITInfo.class, getPath());
		return ListUtil.checkDuplicate(list, o -> o.stockCode);
	}
	
	
	public static final String CATEGORY_INFRA_FUND = "INFRA FUND";

	// https://www.japan-reit.com/meigara/8954/info/
	// 上場日 2002/06/12
	
	public String    stockCode;
	public LocalDate listingDate;
	public int       divFreq;
	public String    category;
	public String    name;

	public REITInfo(String stockCode, LocalDate listingDate, int divFreq, String category, String name) {
		this.stockCode   = stockCode;
		this.listingDate = listingDate;
		this.divFreq     = divFreq;
		this.category    = category;
		this.name        = name;
	}
	public REITInfo() {}
	
	@Override
	public String toString() {
	    return StringUtil.toString(this);
	}

	@Override
	public int compareTo(REITInfo that) {
		return this.stockCode.compareTo(that.stockCode);
	}
	
	@Override
	public int hashCode() {
		return stockCode.hashCode();
	}
	
	@Override
	public boolean equals(Object o) {
		if (o != null) {
			if (o instanceof REITInfo) {
				REITInfo that = (REITInfo)o;
				return
					this.stockCode.equals(that.stockCode) &&
					this.listingDate.equals(that.listingDate) &&
					this.divFreq == that.divFreq &&
					this.category.equals(that.category) &&
					this.name.equals(that.name);
			} else {
				return false;
			}
		} else {
			return false;
		}
	}
}
