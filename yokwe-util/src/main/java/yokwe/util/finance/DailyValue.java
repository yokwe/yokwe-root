package yokwe.util.finance;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Function;

import yokwe.util.StringUtil;
import yokwe.util.UnexpectedException;

public final class DailyValue implements Comparable<DailyValue> {
	private static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();
	
	//
	// create DailyValue array from array of other type
	//
	public static <T> DailyValue[] toArray(T[] array, Function<T, LocalDate> toDate, Function<T, BigDecimal> toValue) {
		DailyValue[] ret = new DailyValue[array.length];
		
		for(int i = 0; i < array.length; i++) {
			T element = array[i];
			ret[i] = new DailyValue(toDate.apply(element), toValue.apply(element));
		}
		
		return ret;
	}
	
	
	//
	// filter array using firstDate and lastDate
	//
	public static DailyValue[] filter(DailyValue[] array, LocalDate firstDate, LocalDate lastDate) {
		LocalDate afterDate  = firstDate.minusDays(1);
		LocalDate beforeDate = lastDate.plusDays(1);
		
		List<DailyValue> list = new ArrayList<>();
		for(var e: array) {
			if (e.date.isAfter(afterDate) && e.date.isBefore(beforeDate)) list.add(e);
		}
		return list.toArray(DailyValue[]::new);
	}
	public static BigDecimal[] filterValue(DailyValue[] array, LocalDate firstDate, LocalDate lastDate) {
		LocalDate afterDate  = firstDate.minusDays(1);
		LocalDate beforeDate = lastDate.plusDays(1);
		
		List<BigDecimal> list = new ArrayList<>();
		for(var e: array) {
			if (e.date.isAfter(afterDate) && e.date.isBefore(beforeDate)) list.add(e.value);
		}
		return list.toArray(BigDecimal[]::new);
	}
	
	
	//
	// create value array from DailyValue array
	//
	public static BigDecimal[] toValueArray(DailyValue[] array, int startIndex, int stopIndexPlusOne) {
		return BigDecimalArrays.toArray(array, startIndex, stopIndexPlusOne, o -> o.value);
	}
	public static BigDecimal[] toValueArray(DailyValue[] array) {
		return BigDecimalArrays.toArray(array, o -> o.value);
	}
	
	
	//
	// get duration of DailyValue array
	//
	public static BigDecimal duration(LocalDate startDate, LocalDate endDate) {
		Period period = startDate.until(endDate);
		String string = String.format("%d.%02d", period.getYears(), period.getMonths());
		return new BigDecimal(string);
	}
	public static BigDecimal duration(DailyValue[] array, int startIndex, int stopIndexPlusOne) {
		LocalDate startDate = array[startIndex].date;
		LocalDate endDate  = startDate;
		for(int i = startIndex + 1; i < stopIndexPlusOne; i++) {
			LocalDate date = array[i].date;
			if (date.isBefore(startDate)) startDate = date;
			if (date.isAfter(endDate))   endDate = date;
		}
		
		return duration(startDate, endDate);
	}
	public static BigDecimal duration(DailyValue[] array) {
		return duration(array, 0, array.length);
	}
	
	
	//
	// reverse element of array
	//
	public static void reverse(DailyValue[] array) {
		for(int i = 0, j = array.length - 1; i < array.length / 2; i++, j--) {
			DailyValue save = array[j];
			array[j] = array[i];
			array[i] = save;
		}
	}
	
	
	//
	// toMap
	//
	public static Map<LocalDate, BigDecimal> toMap(DailyValue[] array) {
		Map<LocalDate, BigDecimal> map = new TreeMap<>();
		
		for(var e: array) {
			var oldValue = map.put(e.date, e.value);
			if (oldValue != null) {
				logger.error("Duplicate date");
				logger.error("  date  {}", e.date);
				logger.error("  new  {}",  e.value);
				logger.error("  old  {}",  oldValue);
				throw new UnexpectedException("Duplicate date");
			}
		}
		return map;
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
	LocalDate getKey() {
		return date;
	}
	@Override
	public int compareTo(DailyValue that) {
		return this.getKey().compareTo(that.getKey());
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
		return this.getKey().hashCode();
	}
}
