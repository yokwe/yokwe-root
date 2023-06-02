package yokwe.util.finance;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.function.Function;

import yokwe.util.GenericArray;

public final class BigDecimalUtil {
	public static final int          DOUBLE_PRECISION       = 15; // precision of double type
	public static final int          DEFAULT_PRECISION      = DOUBLE_PRECISION;
	public static final RoundingMode DEFAULT_ROUNDING_MODE  = RoundingMode.HALF_EVEN;
	public static final MathContext  DEFAULT_MATH_CONTEXT   = new MathContext(DEFAULT_PRECISION, DEFAULT_ROUNDING_MODE);
	
	
	// 
	// ----------------------------
	// invoke static method of Math
	// ----------------------------
	//
	
	//
	// mathLog  -- Math.log
	//
	public static BigDecimal mathLog(BigDecimal x) {
		return BigDecimal.valueOf(Math.log(x.doubleValue())).round(DEFAULT_MATH_CONTEXT);
	}
	
	
	//
	// mathExp -- Math.exp
	//
	public static BigDecimal mathExp(BigDecimal x) {
		return BigDecimal.valueOf(Math.exp(x.doubleValue())).round(DEFAULT_MATH_CONTEXT);
	}
	
	
	//
	// mathPow  -- Math.pow
	//
	public static BigDecimal mathPow(BigDecimal x, BigDecimal y) {
		return BigDecimal.valueOf(Math.pow(x.doubleValue(), y.doubleValue())).round(DEFAULT_MATH_CONTEXT);
	}
	
	
	// 
	// ------------------
	// convenience method
	// ------------------
	//

	
	//
	// return simple return
	//
	public static BigDecimal toSimpleReturn(BigDecimal previous, BigDecimal value) {
		// return (value / previous) - 1
		return value.divide(previous, DEFAULT_MATH_CONTEXT).subtract(BigDecimal.ONE);
	}
	
	
	//
	// return value * value
	//
	public static BigDecimal square(BigDecimal value) {
		// return value * value
		return value.multiply(value, DEFAULT_MATH_CONTEXT);
	}


	
	//
	// ----------------------------------------------------------------------
	// create BigDecimal array from array type T with Function<T, BigDecimal>
	// ----------------------------------------------------------------------
	//
	
	
	//
	// create BigDecimal array from other type of array
	//
	public static <T> BigDecimal[] toArray(T[] array, int startIndex, int stopIndexPlusOne, Function<T, BigDecimal> function) {
		return GenericArray.toArray(BigDecimal.class, array, startIndex, stopIndexPlusOne, function);
	}
	public static <T> BigDecimal[] toArray(T[] array, Function<T, BigDecimal> function) {
		return toArray(array, 0, array.length, function);
	}

	
	//
	// create simple return array from other type of array
	//
	//
	// create log return ratio array from array
	//
	private static final class LogReturnImpl<T> implements Function<T, BigDecimal> {
		private Function<T, BigDecimal> function;
		private BigDecimal              previous;
		
		LogReturnImpl(Function<T, BigDecimal> function, T previous) {
			this.function = function;
			this.previous = mathLog(function.apply(previous));
		}
		
		@Override
		public BigDecimal apply(T t) {
			// value = log(t)
			BigDecimal value = mathLog(function.apply(t));
			
			// ret = value - previous
			BigDecimal ret   = value.subtract(previous);
			
			// update for next iteration
			previous = value;
			
			return ret;
		}
	}
	public static <T> BigDecimal[] toLogReturn(T[] array, int startIndex, int stopIndexPlusOne, Function<T, BigDecimal> function) {
		Function<T, BigDecimal> impl = new LogReturnImpl<>(function, array[Math.max(0, startIndex - 1)]);
		return toArray(array, startIndex, stopIndexPlusOne, impl);
	}
	public static <T> BigDecimal[] toLogReturn(T[] array, Function<T, BigDecimal> function) {
		return toLogReturn(array, 0, array.length, function);
	}

	
	//
	// create simple return ration array from array
	//
	//
	// create log return array from other type of array
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
			BigDecimal ret   = toSimpleReturn(previous, value);
			
			// update for next iteration
			previous = value;
			
			return ret;
		}
	}
	public static <T> BigDecimal[] toSimpleReturn(T[] array, int startIndex, int stopIndexPlusOne, Function<T, BigDecimal> function) {
		Function<T, BigDecimal> impl = new SimpleReturnImpl<>(function, array[Math.max(0, startIndex - 1)]);
		return toArray(array, startIndex, stopIndexPlusOne, impl);
	}
	public static <T> BigDecimal[] toSimpleReturn(T[] array, Function<T, BigDecimal> function) {
		return toSimpleReturn(array, 0, array.length, function);
	}
	
	//
	// -------------------------------------------------------------------------
	// calculate BigDecimal value from array type T with Function<T, BigDecimal>
	// -------------------------------------------------------------------------
	//

	//
	// calculate sum of array element
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
		return sum(array, 0, array.length, function);
	}
	
	//
	// calculate mean of array element  -- arithmetic mean
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
			return total.divide(BigDecimal.valueOf(count), DEFAULT_MATH_CONTEXT);
		}
	}
	public static <T> BigDecimal mean(T[] array, int startIndex, int stopIndexPlusOne, Function<T, BigDecimal> function) {
		GenericArray.ToValueImpl<T, BigDecimal> impl = new MeanImpl<>(function);
		return GenericArray.toValue(array, startIndex, stopIndexPlusOne, impl);
	}
	public static <T> BigDecimal mean(T[] array, Function<T, BigDecimal> function) {
		return mean(array, 0, array.length, function);
	}
	
	
	//
	// calculate geometric mean of array element  -- geometric mean
	//
	private static final class GeoMeanImpl<T> extends GenericArray.ToValueBase<T, BigDecimal> {		
		GeoMeanImpl(Function<T, BigDecimal> function) {
			super(function);
		}
		
		private int        count = 0;
		private BigDecimal total = BigDecimal.ZERO;
		
		@Override
		public void accept(BigDecimal value) {
			count++;
			total = total.add(mathLog(value));
		}

		@Override
		public BigDecimal get() {
			BigDecimal value = total.divide(BigDecimal.valueOf(count), DEFAULT_MATH_CONTEXT);
			return BigDecimalUtil.mathExp(value);
		}
	}
	public static <T> BigDecimal meanGeometric(T[] array, int startIndex, int stopIndexPlusOne, Function<T, BigDecimal> function) {
		GenericArray.ToValueImpl<T, BigDecimal> impl = new GeoMeanImpl<>(function);
		return GenericArray.toValue(array, startIndex, stopIndexPlusOne, impl);
	}
	public static <T> BigDecimal meanGeometric(T[] array, Function<T, BigDecimal> function) {
		return meanGeometric(array, 0, array.length, function);
	}
	
	//
	// calculate variance of array element
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
			total = total.add(square(mean.subtract(value)));
		}

		@Override
		public BigDecimal get() {
			// return total / count;
			return total.divide(BigDecimal.valueOf(count), DEFAULT_MATH_CONTEXT);
		}
	}
	public static <T> BigDecimal variance(T[] array, int startIndex, int stopIndexPlusOne, BigDecimal mean, Function<T, BigDecimal> function) {
		GenericArray.ToValueImpl<T, BigDecimal> impl = new VarianceImpl<>(function, mean);
		return GenericArray.toValue(array, startIndex, stopIndexPlusOne, impl);
	}
	public static <T> BigDecimal variance(T[] array, BigDecimal mean, Function<T, BigDecimal> function) {
		return variance(array, 0, array.length, mean, function);
	}
	// without mean
	public static <T> BigDecimal variance(T[] array, int startIndex, int stopIndexPlusOne, Function<T, BigDecimal> function) {
		BigDecimal mean = mean(array, startIndex, stopIndexPlusOne, function);
		return variance(array, startIndex, stopIndexPlusOne, mean, function);
	}
	public static <T> BigDecimal variance(T[] array, Function<T, BigDecimal> function) {
		return variance(array, 0, array.length, function);
	}

	
	//
	// standard deviation
	//
	public static <T> BigDecimal standardDeviation(T[] array, int startIndex, int stopIndexPlusOne, BigDecimal mean, Function<T, BigDecimal> function) {
		return variance(array, startIndex, stopIndexPlusOne, mean, function).sqrt(DEFAULT_MATH_CONTEXT);
	}
	public static <T> BigDecimal standardDeviation(T[] array, BigDecimal mean, Function<T, BigDecimal> function) {
		return standardDeviation(array, 0, array.length, mean, function);
	}
	// without mean
	public static <T> BigDecimal standardDeviation(T[] array, int startIndex, int stopIndexPlusOne, Function<T, BigDecimal> function) {
		BigDecimal mean = mean(array, startIndex, stopIndexPlusOne, function);
		return variance(array, startIndex, stopIndexPlusOne, mean, function).sqrt(DEFAULT_MATH_CONTEXT);
	}
	public static <T> BigDecimal standardDeviation(T[] array, Function<T, BigDecimal> function) {
		return standardDeviation(array, 0, array.length, function);
	}
	
}
