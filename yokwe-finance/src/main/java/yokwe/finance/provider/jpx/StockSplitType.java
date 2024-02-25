package yokwe.finance.provider.jpx;

import java.time.LocalDate;

public class StockSplitType implements Comparable<StockSplitType> {
	public final LocalDate date;
	public final String    stockCode;
	public final int       before;
	public final int       after;
	public final String    name;
	
	public StockSplitType(LocalDate date, String stockCode, int before, int after, String name) {
		this.date      = date;
		this.stockCode = stockCode;
		this.before    = before;
		this.after     = after;
		this.name      = name;
	}
	
	public String getKey() {
		return date.toString() + stockCode;
	}
	
	@Override
	public int compareTo(StockSplitType that) {
		int ret = this.date.compareTo(that.date);
		if (ret == 0) ret = this.stockCode.compareTo(that.stockCode);
		return ret;
	};
	@Override
	public boolean equals(Object o) {
		if (o instanceof StockSplitType) {
			StockSplitType that = (StockSplitType)o;
			return
				this.date.equals(that.date) &&
				this.stockCode.equals(that.stockCode) &&
				this.before == that.before &&
				this.after == that.after &&
				this.name.equals(that.name);
		} else {
			return false;
		}
	}
	@Override
	public String toString() {
		return String.format("{%s  %s  %d  %d  %s}", date, stockCode, before, after, name);
	}
}
