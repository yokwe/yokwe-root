package yokwe.finance.provider.nyse;

import java.util.Collection;
import java.util.List;

import yokwe.finance.Storage;
import yokwe.util.ListUtil;

public final class Filter implements Comparable<Filter> {
	private static final String PATH_FILE = Storage.Provider.NYSE.getPath("filter.csv");
	public static String getPath() {
		return PATH_FILE;
	}
	
	public static List<Filter> getList() {
		return ListUtil.getList(Filter.class, getPath());
	}
	public static void save(Collection<Filter> collection) {
		ListUtil.save(Filter.class, getPath(), collection);
	}
	public static void save(List<Filter> list) {
		ListUtil.save(Filter.class, getPath(), list);
	}
	
	
	public String normalizedTicker;
	public String exchangeId;
	public String instrumentName;
	public String instrumentType;
	public String micCode;
//	public String normalizedTicker;
	public String symbolEsignalTicker;
	public String symbolExchangeTicker;
	public String symbolTicker;
	public int    total;
	public String url;
	
	@Override
	public int compareTo(Filter that) {
		return this.normalizedTicker.compareTo(that.normalizedTicker);
	}

}
