package yokwe.finance.type;

import java.math.BigDecimal;
import java.time.LocalDate;

public class OHLCV implements Comparable<OHLCV> {
	public LocalDate  date;
	public BigDecimal open;
	public BigDecimal high;
	public BigDecimal low;
	public BigDecimal close;
	public long       volume;
	
	
	@Override
	public String toString() {
		return String.format("{%s %s %s %s %s %d}",
			date.toString(), open.toPlainString(), high.toPlainString(), low.toPlainString(), close.toPlainString(), volume);
	}
	@Override
	public int compareTo(OHLCV that) {
		return this.date.compareTo(that.date);
	}
	@Override
	public boolean equals(Object o) {
		if (o instanceof OHLCV) {
			OHLCV that = (OHLCV)o;
			return
				this.date.equals(that.date) &&
				this.open.compareTo(that.open) == 0 &&
				this.high.compareTo(that.high) == 0 &&
				this.low.compareTo(that.low) == 0 &&
				this.close.compareTo(that.close) == 0 &&
				this.volume == that.volume;
		} else {
			return false;
		}
	}
}
