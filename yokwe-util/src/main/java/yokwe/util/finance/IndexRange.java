package yokwe.util.finance;

import java.time.LocalDate;
import java.util.function.Function;

import yokwe.util.UnexpectedException;

//
// IndexRange and indexRange
//
public final class IndexRange {
	private static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();
	
	public static <T> IndexRange getInstance(T[] array, LocalDate startDate, LocalDate endDate, Function<T, LocalDate> op) {
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
			LocalDate date = op.apply(array[i]);
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

	public static IndexRange getInstance(LocalDate[] array, LocalDate startDate, LocalDate endDate) {
		return getInstance(array, startDate, endDate, Function.identity());
	}
	
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