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
		// assume array is not ordered by date
		LocalDate startDate = array[startIndex].date;
		LocalDate endDate   = startDate;
		for(int i = startIndex + 1; i < stopIndexPlusOne; i++) {
			LocalDate date = array[i].date;
			if (date.isBefore(startDate)) startDate = date; // find youngest date
			if (date.isAfter(endDate))    endDate   = date; // find oldest date
		}
		
		return duration(startDate, endDate);
	}
	public static BigDecimal duration(DailyValue[] array) {
		return duration(array, 0, array.length);
	}
	
	
	//
	// IndexRange and indexRange
	//
	public static final class IndexRange {
		public final int startIndex;
		public final int stopIndexPlusOne;
		
		public IndexRange(int startIndex, int stopIndexPlusOne) {
			this.startIndex       = startIndex;
			this.stopIndexPlusOne = stopIndexPlusOne;
		}
		
		public boolean isValid() {
			return 0 <= startIndex && 0 <= stopIndexPlusOne && startIndex < stopIndexPlusOne;
		}
		
		public int size() {
			return stopIndexPlusOne - startIndex;
		}
		
		@Override
		public String toString() {
			return String.format("{%d  %d}", startIndex, stopIndexPlusOne);
		}
	}
	public static IndexRange indexRange(DailyValue[] array, LocalDate startDate, LocalDate endDate) {
		// return index of array between startDaten inclusive and endDate inclusive
		
		// sanity check
		{
			if (startDate.isAfter(endDate)) {
				logger.error("Unexpeced date");
				logger.error("  startDate         {}", startDate);
				logger.error("  endDate           {}", endDate);
				throw new UnexpectedException("Unexpeced date");
			}
		}
		
		int       startIndex           = -1;		
		int       stopIndexPlusOne     = -1;
		LocalDate startIndexDate       = null;
		LocalDate stopIndexPlusOneDate = null;
		
		for(int i = 0; i < array.length; i++) {
			LocalDate date = array[i].date;
			if (date.isEqual(startDate) || date.isAfter(startDate)) {
				// youngest date equals to startDate or after startDate
				if (startIndexDate == null || date.isBefore(startIndexDate)) {
					startIndex     = i;
					startIndexDate = date;
				}
			}
			if (date.isEqual(endDate) || date.isAfter(endDate)) {
				// youngest date equals to endDate or after endDate
				if (stopIndexPlusOneDate == null || date.isBefore(stopIndexPlusOneDate)) {
					stopIndexPlusOne     = i + 1; // plus one for PluOne
					stopIndexPlusOneDate = date;
				}
			}
		}
		return new IndexRange(startIndex, stopIndexPlusOne);
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
