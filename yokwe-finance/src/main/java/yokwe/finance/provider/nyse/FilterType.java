package yokwe.finance.provider.nyse;

import yokwe.util.StringUtil;

//import java.util.Collection;
//import java.util.List;

public final class FilterType implements Comparable<FilterType> {
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
	public int compareTo(FilterType that) {
		return this.normalizedTicker.compareTo(that.normalizedTicker);
	}
	
	@Override
	public String toString() {
		return StringUtil.toString(this);
	}
}
