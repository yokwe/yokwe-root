package yokwe.finance.provider.jpx;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

import yokwe.util.StringUtil;

public final class StockListType implements Comparable<StockListType> {
	public String     stockCode;
	public LocalDate  date; //?
	public LocalTime  time;
	public BigDecimal price;
	public long       volume;
	public String     name;
	
	public StockListType(
		String     stockCode,
		LocalDate  date,
		LocalTime  time,
		BigDecimal price,
		long       volume,
		String     name) {
		this.stockCode = stockCode;
		this.date      = date;
		this.time      = time;
		this.price     = price;
		this.volume    = volume;
		this.name      = name;
	}
	
	@Override
	public int compareTo(StockListType that) {
		return this.stockCode.compareTo(that.stockCode);
	}
	
	@Override
	public String toString() {
		return StringUtil.toString(this);
	}
	
	public String getKey() {
		return stockCode;
	}
}
