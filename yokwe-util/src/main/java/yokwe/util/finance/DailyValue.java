package yokwe.util.finance;


import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import yokwe.util.StringUtil;

public class DailyValue implements Comparable<DailyValue> {
	public static BigDecimal[] toArray(List<DailyValue> list) {
		List<BigDecimal> ret = new ArrayList<>(list.size());
		for(var e: list) {
			ret.add(e.value);
		}
		return ret.toArray(new BigDecimal[0]);
	}
	public static BigDecimal[] toArray(List<DailyValue> list, LocalDate firstDate, LocalDate lastDate) {
		// firstDate and lastDate is inclusive
		LocalDate first = firstDate.minusDays(1);
		LocalDate last  = lastDate.plusDays(1);
		
		List<BigDecimal> ret = new ArrayList<>(list.size());
		for(var e: list) {
			if (e.date.isAfter(first) && e.date.isBefore(last)) {
				ret.add(e.value);
			}
		}
		return ret.toArray(new BigDecimal[0]);
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
