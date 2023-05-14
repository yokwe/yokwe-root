package yokwe.util.finance;


import java.math.BigDecimal;
import java.time.LocalDate;

import yokwe.util.StringUtil;

public class DailyValue implements Comparable<DailyValue> {
	public static DailyValue getInstance(LocalDate date, BigDecimal value) {
		return new DailyValue(date, value);
	}
	
	public final LocalDate  date;
	public final BigDecimal value;

	public DailyValue(LocalDate date, BigDecimal value) {
		this.date  = date;
		this.value = value;
	}
	
	@Override
	public String toString() {
		return StringUtil.toString(this);
	}
	@Override
	public int compareTo(DailyValue that) {
		return this.date.compareTo(that.date);
	}
	@Override
	public boolean equals(Object o) {
		if (o == null) {
			return false;
		} else {
			if (o instanceof DailyValue) {
				DailyValue that = (DailyValue)o;
				return this.compareTo(that) == 0;
			} else {
				return false;
			}
		}
	}
	@Override
	public int hashCode() {
		return this.date.hashCode();
	}
}
