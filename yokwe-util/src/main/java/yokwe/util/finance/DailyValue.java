package yokwe.util.finance;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Period;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Function;

import yokwe.util.GenericArray;
import yokwe.util.StringUtil;
import yokwe.util.UnexpectedException;

public final class DailyValue implements Comparable<DailyValue> {
	private static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();
	
	//
	// create value array from DailyValue array
	//
	public static BigDecimal[] toValueArray(DailyValue[] array, int startIndex, int stopIndexPlusOne) {		
		return GenericArray.toArray(array, startIndex, stopIndexPlusOne, DailyValue::getValue, Function.identity(), BigDecimal.class);
	}
	public static BigDecimal[] toValueArray(DailyValue[] array) {
		// call above method
		return toValueArray(array, 0, array.length);
	}
	
	
	//
	// create date array from DailyValue array
	//
	public static LocalDate[] toDateArray(DailyValue[] array, int startIndex, int stopIndexPlusOne) {
		return GenericArray.toArray(array, startIndex, stopIndexPlusOne, DailyValue::getDate, Function.identity(), LocalDate.class);
	}
	public static LocalDate[] toDateArray(DailyValue[] array) {
		return toDateArray(array, 0, array.length);
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
	
	public LocalDate getDate() {
		return date;
	}
	public BigDecimal getValue() {
		return value;
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
