package yokwe.util.finance;

import java.lang.reflect.Array;
import java.time.LocalDate;
import java.util.function.IntFunction;

import yokwe.util.UnexpectedException;

public final class Util {
	static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();

	///////////////////////////////////////////////////////////////////////////
	// check index consistency with array
	///////////////////////////////////////////////////////////////////////////
	//
	// one array
	//
	private static void checkindex(int length, int startIndex, int stopIndexPlusOne) {
		if (length == 0 && startIndex == 0 && stopIndexPlusOne == 0) return;
		
		if (!(0 <= startIndex && startIndex < length)) {
			logger.error("  alength           {}", length);
			logger.error("  startIndex        {}", startIndex);
			logger.error("  stopIndexPlusOne  {}", stopIndexPlusOne);
			throw new UnexpectedException("offset is out of range");
		}
		if (!(startIndex < stopIndexPlusOne && stopIndexPlusOne <= length)) {
			logger.error("  length            {}", length);
			logger.error("  startIndex        {}", startIndex);
			logger.error("  stopIndexPlusOne  {}", stopIndexPlusOne);
			throw new UnexpectedException("offset is out of range");
		}
	}
	// object array
	public static <T> void checkIndex(T[] array, int startIndex, int stopIndexPlusOne) {
		if (array == null) {
			logger.error("array == null");
			throw new UnexpectedException("array == null");
		}
		checkindex(array.length, startIndex, stopIndexPlusOne);
	}
	// double array
	public static void checkIndex(double[] array, int startIndex, int stopIndexPlusOne) {
		if (array == null) {
			logger.error("array == null");
			throw new UnexpectedException("array == null");
		}
		checkindex(array.length, startIndex, stopIndexPlusOne);
	}
	//
	// two array
	//
	// object array  object array
	public static <T, U> void checkIndex(T[] a, U[] b) {
		// length of array a and b must be same
		if (a == null) {
			throw new UnexpectedException("array a is null");
		}
		if (b == null) {
			throw new UnexpectedException("array b is null");
		}
		if (a.length != b.length) {
			logger.error("  a.length          {}", a.length);
			logger.error("  b.length          {}", b.length);
			throw new UnexpectedException("array length is different");
		}
	}
	
	public static <T, U> void checkIndex(T[] a, U[] b, int startIndex, int stopIndexPlusOne) {
		checkIndex(a, b);
		checkIndex(a, startIndex, stopIndexPlusOne);
		checkIndex(b, startIndex, stopIndexPlusOne);
	}
	// double array  double array
	public static void checkIndex(double[] a, double[] b) {
		// length of array a and b must be same
		if (a == null) {
			throw new UnexpectedException("array a is null");
		}
		if (b == null) {
			throw new UnexpectedException("array b is null");
		}
		if (a.length != b.length) {
			logger.error("  a.length          {}", a.length);
			logger.error("  b.length          {}", b.length);
			throw new UnexpectedException("array length is different");
		}
	}
	public static void checkIndex(double[] a, double[] b, int startIndex, int stopIndexPlusOne) {
		checkIndex(a, b);
		checkIndex(a, startIndex, stopIndexPlusOne);
		checkIndex(b, startIndex, stopIndexPlusOne);
	}

	
	
	///////////////////////////////////////////////////////////////////////////
	// IndexRange and indexRange
	///////////////////////////////////////////////////////////////////////////
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
	public static IndexRange indexRange(LocalDate[] array, LocalDate startDate, LocalDate endDate) {
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
			LocalDate date = array[i];
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
	
	
	///////////////////////////////////////////////////////////////////////////
	// Lambda to create object array
	///////////////////////////////////////////////////////////////////////////
	public static final class Generator<R> implements IntFunction<R[]> {
		private final Class<R> clazz;
		
		public Generator(Class<R> clazz) {
			this.clazz = clazz;
		}
		
		@Override
		@SuppressWarnings("unchecked")
		public R[] apply(int value) {
			return (R[]) Array.newInstance(clazz, value);
		}
	}

}
