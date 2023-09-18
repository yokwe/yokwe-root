package yokwe.finance.type;

import java.math.BigDecimal;
import java.time.LocalDate;

public class DailyValue implements Comparable<DailyValue> {
	public LocalDate  date;
	public BigDecimal value;
	
	@Override
	public String toString() {
		return String.format("{%s %s}", date.toString(), value.toPlainString());
	}
	@Override
	public int compareTo(DailyValue that) {
		return this.date.compareTo(that.date);
	}
	@Override
	public boolean equals(Object o) {
		if (o instanceof DailyValue) {
			DailyValue that = (DailyValue)o;
			return
				this.date.equals(that.date) &&
				this.value.compareTo(that.value) == 0;
		} else {
			return false;
		}
	}
}
