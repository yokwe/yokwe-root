package yokwe.util.finance;


import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import yokwe.util.StringUtil;

public final class DailyValue implements Comparable<DailyValue> {
	// filter
	public static List<DailyValue> filter(List<DailyValue> list, LocalDate firstDate, LocalDate lastDate) {
		return list.stream().filter(o -> !o.date.isBefore(firstDate) && !o.date.isAfter(lastDate)).collect(Collectors.toList());
	}
	public static List<DailyValue> filterPreviousYear(List<DailyValue> list, LocalDate lastDate, int years) {
		LocalDate firstDate = previousYear(lastDate, years);
		return filter(list, firstDate, lastDate);
	}
	
	// toValueArray
	public static BigDecimal[] toValueArray(List<DailyValue> list, LocalDate firstDate, LocalDate lastDate) {
		return list.stream().filter(o -> !o.date.isBefore(firstDate) && !o.date.isAfter(lastDate)).map(o -> o.value).toArray(BigDecimal[]::new);
	}
	public static BigDecimal[] toValueArrayPreviousYear(List<DailyValue> list, LocalDate lastDate, int years) {
		LocalDate firstDate = previousYear(lastDate, years);
		return toValueArray(list, firstDate, lastDate);
	}
	
	// previous year
	public static LocalDate previousYear(LocalDate date, int years) {
		return date.plusDays(1).minusYears(years);
	}
	
	// value array
	public static BigDecimal[] toValueArray(List<DailyValue> list) {
		return list.stream().map(o -> o.value).toArray(BigDecimal[]::new);
	}
	
	public static DailyValue getInstance(LocalDate date, BigDecimal value) {
		return new DailyValue(date, value);
	}
	public static DailyValue getInstance(String stringDate, double doubleValue) {
		LocalDate  date = LocalDate.parse(stringDate);
		BigDecimal value = doubleValue == 0 ? BigDecimal.ZERO : BigDecimal.valueOf(doubleValue);
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
