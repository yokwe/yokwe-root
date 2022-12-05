package yokwe.stock.jp.japanreit;

import java.util.Collection;
import java.util.List;

import yokwe.stock.jp.Storage;
import yokwe.util.ListUtil;
import yokwe.util.StringUtil;

public class REITDiv implements Comparable<REITDiv> {
	public static String getPath(String stockCode) {
		return Storage.JapanREIT.getPath("reit-div", stockCode + ".csv");
	}

	public static void save(String stockCode, Collection<REITDiv> collection) {
		// Sanity check
		ListUtil.checkDuplicate(collection, o -> o.date);
		ListUtil.save(REITDiv.class, getPath(stockCode), collection);
	}
	public static void save(String stockCode, List<REITDiv> list) {
		// Sanity check
		ListUtil.checkDuplicate(list, o -> o.date);
		ListUtil.save(REITDiv.class, getPath(stockCode), list);
	}

	public static List<REITDiv> getList(String stockCode) {
		String path = getPath(stockCode);
		var list = ListUtil.getList(REITDiv.class, path);
		
		// Sanity check
		ListUtil.checkDuplicate(list, o -> o.date);

		return list;
	}

	public static final int NO_VALUE = -1;
	
    public String date;
    public int    estimate;
    public int    actual;
    
    public REITDiv(String date, int estimate, int actual) {
    	this.date      = date;
    	this.estimate  = estimate;
    	this.actual    = actual;
    }
    
    public boolean hasValue() {
    	return actual != NO_VALUE;
    }

	@Override
	public String toString() {
		return StringUtil.toString(this);
	}

	@Override
	public int compareTo(REITDiv that) {
		return this.date.compareTo(that.date);
	}
	
	@Override
	public int hashCode() {
		return this.date.hashCode();
	}
	
	@Override
	public boolean equals(Object o) {
		if (o == null) {
			return false;
		} else {
			if (o instanceof REITDiv) {
				REITDiv that = (REITDiv)o;
				return this.compareTo(that) == 0;
			} else {
				return false;
			}
		}
	}

}
