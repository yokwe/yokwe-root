package yokwe.util.finance;

import java.math.BigDecimal;
import java.time.LocalDate;

import yokwe.util.GenericArray;
import yokwe.util.StringUtil;
import yokwe.util.UnexpectedException;

public final class DailyValue implements Comparable<DailyValue> {
	private static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();
	
	///////////////////////////////////////////////////////////////////////////
	// DailyValue[] to double[]
	///////////////////////////////////////////////////////////////////////////
	public static double[] toValueArray(DailyValue[] array, int startIndex, int stopIndexPlusOne) {
		return DoubleArray.toDoubleArray(array, startIndex, stopIndexPlusOne, DailyValue::getValue);
	}
	public static double[] toValueArray(DailyValue[] array) {
		// call above method
		return toValueArray(array, 0, array.length);
	}
	
	
	///////////////////////////////////////////////////////////////////////////
	// DailyValue[] to LocalDate[]
	///////////////////////////////////////////////////////////////////////////
	public static LocalDate[] toDateArray(DailyValue[] array, int startIndex, int stopIndexPlusOne) {
		return GenericArray.toArray(array, startIndex, stopIndexPlusOne, DailyValue::getDate, LocalDate.class);
	}
	public static LocalDate[] toDateArray(DailyValue[] array) {
		return toDateArray(array, 0, array.length);
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
		
	
	public final LocalDate date;
	public final double    value;
	

	public DailyValue(LocalDate date, double value) {
		// sanity check
		if (Double.isInfinite(value)) {
			DoubleArray.logger.error("value is infinite");
			DoubleArray.logger.error("  value {}", Double.toString(value));
			throw new UnexpectedException("value is infinite");
		}

		this.date  = date;
		this.value = value;
	}
	public DailyValue(LocalDate date, BigDecimal value) {
		this(date, value.doubleValue());
	}
	
	public LocalDate getDate() {
		return date;
	}
	public double    getValue() {
		return value;
	}
	
	private LocalDate getKey() {
		return date;
	}

	@Override
	public String toString() {
		return StringUtil.toString(this);
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
