package yokwe.util;

import java.math.BigDecimal;
import java.util.function.Function;
import java.util.function.UnaryOperator;

import yokwe.util.GenericArray.Collect;
import yokwe.util.GenericArray.CollectImpl;

public final class BigDecimalArray {
	//
	// GenericArray.toArray
	//
	
	
	//
	// create BigDecimal array from other type of array using map and op
	//
	public static <T> BigDecimal[] toArray(T[] array, int startIndex, int stopIndexPlusOne, Function<T, BigDecimal> map, Function<BigDecimal, BigDecimal> op) {
		return GenericArray.toArray(array, startIndex, stopIndexPlusOne, map, op, BigDecimal.class);
	}
	public static <T> BigDecimal[] toArray(T[] array, Function<T, BigDecimal> function, Function<BigDecimal, BigDecimal> op) {
		// call above method
		return toArray(array, 0, array.length, function, op);
	}

	
	//
	// create simple ratio BigDecimal array from other type of array using map
	//
	private static final class SimpleReturnOp implements UnaryOperator<BigDecimal> {
		private boolean    firstTime = true;
		private BigDecimal previous  = null;
		
		@Override
		public BigDecimal apply(BigDecimal value) {
			if (firstTime) {
				// use first value as previous
				firstTime = false;
				previous  = value;
			}
			
			// ret = (value / previous) - 1
			BigDecimal ret   = BigDecimalUtil.toSimpleReturn(previous, value);
			
			// update for next iteration
			previous = value;
			
			return ret;
		}
	}
	public static <T> BigDecimal[] toSimpleReturn(T[] array, int startIndex, int stopIndexPlusOne, Function<T, BigDecimal> map) {
		UnaryOperator<BigDecimal> op = new SimpleReturnOp();
		return toArray(array, startIndex, stopIndexPlusOne, map, op);
	}
	public static <T> BigDecimal[] toSimpleReturn(T[] array, Function<T, BigDecimal> function) {
		// call above method
		return toSimpleReturn(array, 0, array.length, function);
	}
	
	
	//
	// create log ratio BigDecimal array from other type of array using map
	//
	private static final class LogReturnOp implements UnaryOperator<BigDecimal> {
		private boolean    firstTime = true;
		private BigDecimal previousLog  = null;
		
		@Override
		public BigDecimal apply(BigDecimal value) {
			if (firstTime) {
				// use first value as previous
				firstTime   = false;
				previousLog = BigDecimalUtil.mathLog(value);
			}

			BigDecimal valueLog = BigDecimalUtil.mathLog(value);
			BigDecimal ret      = valueLog.subtract(previousLog);
			
			// update for next iteration
			previousLog = valueLog;
			
			return ret;
		}
	}
	public static <T> BigDecimal[] toLogReturn(T[] array, int startIndex, int stopIndexPlusOne, Function<T, BigDecimal> map) {
		UnaryOperator<BigDecimal> op = new LogReturnOp();
		return toArray(array, startIndex, stopIndexPlusOne, map, op);
	}
	public static <T> BigDecimal[] toLogReturn(T[] array, Function<T, BigDecimal> function) {
		// call above method
		return toLogReturn(array, 0, array.length, function);
	}
	
	
	//
	// GenericArray.collect
	//
	
	
	//
	// calculate BigDecimal sum from other type of array using map
	//
	private static final class SumImpl implements CollectImpl<BigDecimal> {
		private BigDecimal sum = BigDecimal.ZERO;
		@Override
		public void accept(BigDecimal value) {
			sum  = sum.add(value);
		}
		@Override
		public BigDecimal get() {
			return sum;
		}
	}
	public static <T> BigDecimal sum(T[] array, int startIndex, int stopIndexPlusOne, Function<T, BigDecimal> map) {
		var collector = new Collect<>(SumImpl::new);
		return GenericArray.collect(array, startIndex, stopIndexPlusOne, map, collector);
	}

	public static <T> BigDecimal sum(T[] array, Function<T, BigDecimal> map) {
		// call above method
		return sum(array, 0, array.length, map);
	}
	
	
	//
	// calculate BigDecimal arithmetic mean from other type of array using map
	//
	private static final class MeanImpl implements CollectImpl<BigDecimal> {
		private int        count = 0;
		private BigDecimal total   = BigDecimal.ZERO;
		@Override
		public void accept(BigDecimal value) {
			count++;
			total  = total.add(value);
		}
		@Override
		public BigDecimal get() {
			return BigDecimalUtil.divide(total, BigDecimal.valueOf(count));
		}
	}
	public static <T> BigDecimal mean(T[] array, int startIndex, int stopIndexPlusOne, Function<T, BigDecimal> map) {
		var collector = new Collect<>(MeanImpl::new);
		return GenericArray.collect(array, startIndex, stopIndexPlusOne, map, collector);

	}
	public static <T> BigDecimal mean(T[] array, Function<T, BigDecimal> map) {
		// call above method
		return mean(array, 0, array.length, map);
	}
	
	
	//
	// calculate BigDecimal geometric mean from other type of array using map
	//
	private static final class GeometricMeanImpl implements CollectImpl<BigDecimal> {
		private int        count = 0;
		private BigDecimal total = BigDecimal.ZERO;
		
		@Override
		public void accept(BigDecimal value) {
			count++;
			total = total.add(BigDecimalUtil.mathLog(value));
		}

		@Override
		public BigDecimal get() {
			BigDecimal value = BigDecimalUtil.divide(total, BigDecimal.valueOf(count));;
			return BigDecimalUtil.mathExp(value);
		}
	}
	public static <T> BigDecimal geometricMean(T[] array, int startIndex, int stopIndexPlusOne, Function<T, BigDecimal> map) {
		var collector = new Collect<>(GeometricMeanImpl::new);
		return GenericArray.collect(array, startIndex, stopIndexPlusOne, map, collector);
	}
	public static <T> BigDecimal geometricMean(T[] array, Function<T, BigDecimal> map) {
		// call above method
		return geometricMean(array, 0, array.length, map);
	}
	
	
	//
	// calculate BigDecimal variance from other type of array using map
	//
	private static final class VarianceImpl implements CollectImpl<BigDecimal> {
		private int        count = 0;
		private BigDecimal total = BigDecimal.ZERO;
		private BigDecimal mean;
		
		VarianceImpl(BigDecimal mean) {
			this.mean = mean;
		}
		@Override
		public void accept(BigDecimal value) {
			count++;
			// total += (mean - value) ^2
			total = total.add(BigDecimalUtil.square(mean.subtract(value)));
		}

		@Override
		public BigDecimal get() {
			// return total / count;
			return BigDecimalUtil.divide(total, BigDecimal.valueOf(count));
		}
	}
	public static <T> BigDecimal variance(T[] array, int startIndex, int stopIndexPlusOne, BigDecimal mean, Function<T, BigDecimal> map) {
		var collector = new Collect<>(() -> new VarianceImpl(mean));
		return GenericArray.collect(array, startIndex, stopIndexPlusOne, map, collector);
	}
	public static <T> BigDecimal variance(T[] array, BigDecimal mean, Function<T, BigDecimal> map) {
		// call above method
		return variance(array, 0, array.length, mean, map);
	}
	
	
	//
	// calculate BigDecimal standard deviation from other type of array using map
	//
	public static <T> BigDecimal standardDeviation(T[] array, int startIndex, int stopIndexPlusOne, BigDecimal mean, Function<T, BigDecimal> map) {
		return BigDecimalUtil.mathSqrt(variance(array, startIndex, stopIndexPlusOne, mean, map));
	}
	public static <T> BigDecimal standardDeviation(T[] array, BigDecimal mean, Function<T, BigDecimal> map) {
		// call above method
		return standardDeviation(array, 0, array.length, mean, map);
	}

}
