package yokwe.util.finance;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Period;
import java.util.List;

import yokwe.util.StringUtil;

public final class DailyValue implements Comparable<DailyValue> {
	// value array
	public static BigDecimal[] toValueArray(List<DailyValue> list) {
		return list.stream().map(o -> o.value).toArray(BigDecimal[]::new);
	}
	public static BigDecimal[] toValueArray(DailyValue[] array, int startIndex, int stopIndexPlusOne) {
		return BigDecimalArrays.toArray(array, startIndex, stopIndexPlusOne, o -> o.value);
	}
	public static BigDecimal[] toValueArray(DailyValue[] array) {
		return toValueArray(array, 0, array.length);
	}
	
	// duration
	public static BigDecimal duration(DailyValue[] array) {
		LocalDate startDate = array[0].date;
		LocalDate endDate  = startDate;
		for(int i = 1; i < array.length; i++) {
			LocalDate date = array[i].date;
			if (date.isBefore(startDate)) startDate = date;
			if (date.isAfter(endDate))   endDate = date;
		}
		
		return duration(startDate, endDate);
	}
	public static BigDecimal duration(LocalDate startDate, LocalDate endDate) {
		Period period = startDate.until(endDate);
		String string = String.format("%d.%02d", period.getYears(), period.getMonths());
		return new BigDecimal(string);
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
