package yokwe.util;

import java.math.BigDecimal;
import java.util.function.Function;

public final class BigDecimalArray {
	//
	// create BigDecimal array from other type of array using function
	//
	public static <T> BigDecimal[] toArray(T[] array, int startIndex, int stopIndexPlusOne, Function<T, BigDecimal> function) {
		return GenericArray.toArray(array, startIndex, stopIndexPlusOne, function, BigDecimal.class);
	}
	public static <T> BigDecimal[] toArray(T[] array, Function<T, BigDecimal> function) {
		// call above method
		return toArray(array, 0, array.length, function);
	}

	
	//
	// create simple ratio BigDecimal array from other type of array using function
	//
	private static class SimpleReturnImpl<T> implements Function<T, BigDecimal> {
		private Function<T, BigDecimal> function;
		private BigDecimal              previous;
		
		SimpleReturnImpl(Function<T, BigDecimal> function, T previous) {
			this.function = function;
			this.previous = function.apply(previous);
		}
		
		@Override
		public BigDecimal apply(T t) {
			BigDecimal value = function.apply(t);
			
			// ret = (value / previous) - 1
			BigDecimal ret   = BigDecimalUtil.toSimpleReturn(previous, value);
			
			// update for next iteration
			previous = value;
			
			return ret;
		}
	}
	public static <T> BigDecimal[] toSimpleReturn(T[] array, int startIndex, int stopIndexPlusOne, Function<T, BigDecimal> function) {
		Function<T, BigDecimal> impl = new SimpleReturnImpl<>(function, array[Math.max(0, startIndex - 1)]);
		return GenericArray.toArray(array, startIndex, stopIndexPlusOne, impl, BigDecimal.class);
	}
	public static <T> BigDecimal[] toSimpleReturn(T[] array, Function<T, BigDecimal> function) {
		// call above method
		return toSimpleReturn(array, 0, array.length, function);
	}
	
	
	//
	// create log ratio BigDecimal array from other type of array using function
	//
	private static final class LogReturnImpl<T> implements Function<T, BigDecimal> {
		private Function<T, BigDecimal> function;
		private BigDecimal              previous;
		
		LogReturnImpl(Function<T, BigDecimal> function, T previous) {
			this.function = function;
			this.previous = BigDecimalUtil.mathLog(function.apply(previous));
		}
		
		@Override
		public BigDecimal apply(T t) {
			// value = log(t)
			BigDecimal value = BigDecimalUtil.mathLog(function.apply(t));
			
			// ret = value - previous
			BigDecimal ret   = value.subtract(previous);
			
			// update for next iteration
			previous = value;
			
			return ret;
		}
	}
	public static <T> BigDecimal[] toLogReturn(T[] array, int startIndex, int stopIndexPlusOne, Function<T, BigDecimal> function) {
		Function<T, BigDecimal> impl = new LogReturnImpl<>(function, array[Math.max(0, startIndex - 1)]);
		return GenericArray.toArray(array, startIndex, stopIndexPlusOne, impl, BigDecimal.class);
	}
	public static <T> BigDecimal[] toLogReturn(T[] array, Function<T, BigDecimal> function) {
		// call above method
		return toLogReturn(array, 0, array.length, function);
	}
	
	
	//
	// calculate BigDecimal sum from other type of array using function
	//
	private static final class SumImpl<T> extends GenericArray.ToValueBase<T, BigDecimal> {
		SumImpl(Function<T, BigDecimal> function) {
			super(function);
		}
		
		private BigDecimal total = BigDecimal.ZERO;
		
		@Override
		public void accept(BigDecimal value) {
			total = total.add(value);
		}

		@Override
		public BigDecimal get() {
			return total;
		}
	}
	public static <T> BigDecimal sum(T[] array, int startIndex, int stopIndexPlusOne, Function<T, BigDecimal> function) {
		GenericArray.ToValueImpl<T, BigDecimal> impl = new SumImpl<>(function);
		return GenericArray.toValue(array, startIndex, stopIndexPlusOne, impl);
	}
	public static <T> BigDecimal sum(T[] array, Function<T, BigDecimal> function) {
		// call above method
		return sum(array, 0, array.length, function);
	}
	
	
	//
	// calculate BigDecimal arithmetic mean from other type of array using function
	//
	private static final class MeanImpl<T> extends GenericArray.ToValueBase<T, BigDecimal> {
		MeanImpl(Function<T, BigDecimal> function) {
			super(function);
		}
		
		private int        count = 0;
		private BigDecimal total = BigDecimal.ZERO;
		
		@Override
		public void accept(BigDecimal value) {
			count++;
			total = total.add(value);
		}

		@Override
		public BigDecimal get() {
			return total.divide(BigDecimal.valueOf(count), BigDecimalUtil.DEFAULT_MATH_CONTEXT);
		}
	}
	public static <T> BigDecimal mean(T[] array, int startIndex, int stopIndexPlusOne, Function<T, BigDecimal> function) {
		GenericArray.ToValueImpl<T, BigDecimal> impl = new MeanImpl<>(function);
		return GenericArray.toValue(array, startIndex, stopIndexPlusOne, impl);
	}
	public static <T> BigDecimal mean(T[] array, Function<T, BigDecimal> function) {
		// call above method
		return mean(array, 0, array.length, function);
	}
	
	
	//
	// calculate BigDecimal geometric mean from other type of array using function
	//
	private static final class GeometricMeanImpl<T> extends GenericArray.ToValueBase<T, BigDecimal> {
		GeometricMeanImpl(Function<T, BigDecimal> function) {
			super(function);
		}
		
		private int        count = 0;
		private BigDecimal total = BigDecimal.ZERO;
		
		@Override
		public void accept(BigDecimal value) {
			count++;
			total = total.add(BigDecimalUtil.mathLog(value));
		}

		@Override
		public BigDecimal get() {
			BigDecimal value = total.divide(BigDecimal.valueOf(count), BigDecimalUtil.DEFAULT_MATH_CONTEXT);
			return BigDecimalUtil.mathExp(value);
		}
	}
	public static <T> BigDecimal geometricMean(T[] array, int startIndex, int stopIndexPlusOne, Function<T, BigDecimal> function) {
		GenericArray.ToValueImpl<T, BigDecimal> impl = new GeometricMeanImpl<>(function);
		return GenericArray.toValue(array, startIndex, stopIndexPlusOne, impl);
	}
	public static <T> BigDecimal geometricMean(T[] array, Function<T, BigDecimal> function) {
		// call above method
		return geometricMean(array, 0, array.length, function);
	}
	
	
	//
	// calculate BigDecimal variance from other type of array using function
	//
	private static final class VarianceImpl<T> extends GenericArray.ToValueBase<T, BigDecimal> {
		VarianceImpl(Function<T, BigDecimal> function, BigDecimal mean) {
			super(function);
			this.mean = mean;
		}
		
		private int        count = 0;
		private BigDecimal total = BigDecimal.ZERO;
		private BigDecimal mean;
		
		@Override
		public void accept(BigDecimal value) {
			count++;
			// total += (mean - value) ^2
			total = total.add(BigDecimalUtil.square(mean.subtract(value)));
		}

		@Override
		public BigDecimal get() {
			// return total / count;
			return total.divide(BigDecimal.valueOf(count), BigDecimalUtil.DEFAULT_MATH_CONTEXT);
		}
	}
	public static <T> BigDecimal variance(T[] array, int startIndex, int stopIndexPlusOne, BigDecimal mean, Function<T, BigDecimal> function) {
		GenericArray.ToValueImpl<T, BigDecimal> impl = new VarianceImpl<>(function, mean);
		return GenericArray.toValue(array, startIndex, stopIndexPlusOne, impl);
	}
	public static <T> BigDecimal variance(T[] array, BigDecimal mean, Function<T, BigDecimal> function) {
		// call above method
		return variance(array, 0, array.length, mean, function);
	}
	
	
	//
	// calculate BigDecimal standard deviation from other type of array using function
	//
	public static <T> BigDecimal standardDeviation(T[] array, int startIndex, int stopIndexPlusOne, BigDecimal mean, Function<T, BigDecimal> function) {
		return variance(array, startIndex, stopIndexPlusOne, mean, function).sqrt(BigDecimalUtil.DEFAULT_MATH_CONTEXT);
	}
	public static <T> BigDecimal standardDeviation(T[] array, BigDecimal mean, Function<T, BigDecimal> function) {
		// call above method
		return standardDeviation(array, 0, array.length, mean, function);
	}

}
