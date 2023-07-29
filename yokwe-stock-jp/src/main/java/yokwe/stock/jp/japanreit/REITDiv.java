package yokwe.stock.jp.japanreit;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import yokwe.stock.jp.Storage;
import yokwe.util.ListUtil;
import yokwe.util.StringUtil;

public class REITDiv implements Comparable<REITDiv> {
	private static final String PREFIX = "reit-div";
	public static String getPath() {
		return Storage.JapanREIT.getPath(PREFIX);
	}
	public static String getPath(String stockCode) {
		return Storage.JapanREIT.getPath(PREFIX, stockCode + ".csv");
	}
	
	private static final String PREFIX_DELIST = "reit-div-delist";
	public static String getPathDelist() {
		return Storage.JapanREIT.getPath(PREFIX_DELIST);
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

	public static List<REITDiv> getListAll(String stockCode) {
		String path = getPath(stockCode);
		var list = ListUtil.getList(REITDiv.class, path);
		
		// Sanity check
		ListUtil.checkDuplicate(list, o -> o.date);

		return list;
	}
	public static List<REITDiv> getList(String stockCode) {
		return getListAll(stockCode).stream().filter(o -> o.hasValue()).collect(Collectors.toList());
	}

	public static double getAnnual(REIT reit) {
		String stockCode = reit.stockCode;
		int    divFreq   = reit.divFreq;
		
		if (divFreq == 0) return 0;
		
		var divList = getList(stockCode);
		REITDiv[] divArray = divList.toArray(new REITDiv[0]);
		int divCount = divArray.length;
		
		if (divCount == 0) return 0;
		
		double annual = 0;

		if (divFreq <= divCount) {
			// use latest amount in divList for one year
			int offset = divCount - divFreq;
			for(int i = 0; i < divFreq; i++) {
				annual += divArray[offset + i].actual;
			}
		} else {
			// estimate amount
			for(int i = 0; i < divCount; i++) {
				annual += divArray[i].actual;
			}
			// calculate average dividend
			annual /= divCount;
			// estimate annual from average dividend
			annual *= divFreq;
		}

		return annual;
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
    public REITDiv() {
    	this(null, NO_VALUE, NO_VALUE);
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
