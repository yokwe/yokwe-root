package yokwe.util.yahoo.finance;

import java.math.BigDecimal;
import java.time.LocalDate;

public final class Price implements Comparable<Price> {
	public LocalDate  date;
	public BigDecimal open;
	public BigDecimal high;
	public BigDecimal low;
	public BigDecimal close;
	public BigDecimal adjClose;
	public long       volume;
	
	public Price(
		LocalDate  date,
		BigDecimal open,
		BigDecimal high,
		BigDecimal low,
		BigDecimal close,
		BigDecimal adjClose,
		long       volume
		) {
		this.date     = date;
		this.open     = open;
		this.high     = high;
		this.low      = low;
		this.close    = close;
		this.adjClose = adjClose;
		this.volume   = volume;
	}
	
	@Override
	public String toString() {
		return String.format(
			"{%s  %s  %s  %s  %s  %s  %d}",
			date,
			open.toPlainString(),
			high.toPlainString(),
			low.toPlainString(),
			close.toPlainString(),
			adjClose.toPlainString(),
			volume);
	}
	
	@Override
	public int compareTo(Price that) {
		return this.date.compareTo(that.date);
	}

}
