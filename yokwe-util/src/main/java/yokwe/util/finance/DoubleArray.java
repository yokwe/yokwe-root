package yokwe.util.finance;

import java.lang.reflect.Array;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.function.DoubleBinaryOperator;
import java.util.function.DoubleConsumer;
import java.util.function.DoubleFunction;
import java.util.function.DoubleUnaryOperator;
import java.util.function.IntFunction;
import java.util.function.ToDoubleFunction;

import yokwe.util.UnexpectedException;

public class DoubleArray {
	private static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();
	
	//
	// check index consistency with array
	//
	private static <T> void checkIndex(T[] array, int startIndex, int stopIndexPlusOne) {
		if (array == null) {
			logger.error("array == null");
			throw new UnexpectedException("array == null");
		}
		if (array.length == 0 && startIndex == 0 && stopIndexPlusOne == 0) return;
		
		if (!(0 <= startIndex && startIndex < array.length)) {
			logger.error("  array.length      {}", array.length);
			logger.error("  startIndex        {}", startIndex);
			logger.error("  stopIndexPlusOne  {}", stopIndexPlusOne);
			throw new UnexpectedException("offset is out of range");
		}
		if (!(startIndex < stopIndexPlusOne && stopIndexPlusOne <= array.length)) {
			logger.error("  array.length      {}", array.length);
			logger.error("  startIndex        {}", startIndex);
			logger.error("  stopIndexPlusOne  {}", stopIndexPlusOne);
			throw new UnexpectedException("offset is out of range");
		}
	}
	private static void checkIndex(double[] array, int startIndex, int stopIndexPlusOne) {
		if (array == null) {
			logger.error("array == null");
			throw new UnexpectedException("array == null");
		}
		if (array.length == 0 && startIndex == 0 && stopIndexPlusOne == 0) return;
		
		if (!(0 <= startIndex && startIndex < array.length)) {
			logger.error("  array.length      {}", array.length);
			logger.error("  startIndex        {}", startIndex);
			logger.error("  stopIndexPlusOne  {}", stopIndexPlusOne);
			throw new UnexpectedException("offset is out of range");
		}
		if (!(startIndex < stopIndexPlusOne && stopIndexPlusOne <= array.length)) {
			logger.error("  array.length      {}", array.length);
			logger.error("  startIndex        {}", startIndex);
			logger.error("  stopIndexPlusOne  {}", stopIndexPlusOne);
			throw new UnexpectedException("offset is out of range");
		}
	}
	private static void checkIndex(double[] a, double[] b, int startIndex, int stopIndexPlusOne) {
		// length of array a and b must be same
		if (a != null && b != null && a.length != b.length) {
			logger.error("  a.length          {}", a.length);
			logger.error("  b.length          {}", b.length);
			logger.error("  startIndex        {}", startIndex);
			logger.error("  stopIndexPlusOne  {}", stopIndexPlusOne);
			throw new UnexpectedException("array length is different");
		}
		
		checkIndex(a, startIndex, stopIndexPlusOne);
		checkIndex(b, startIndex, stopIndexPlusOne);
	}
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// T[] to double[]
	///////////////////////////////////////////////////////////////////////////
	private static <T> double[] toDoubleArray(T[] array, int startIndex, int stopIndexPlusOne, ToDoubleFunction<T> map) {
		checkIndex(array, startIndex, stopIndexPlusOne);
		return Arrays.stream(array, startIndex, stopIndexPlusOne).mapToDouble(map).toArray();
	}
	///////////////////////////////////////////////////////////////////////////
	// BigDecima[] to double[]
	///////////////////////////////////////////////////////////////////////////
	public static double[] toDoubleArray(BigDecimal[] array, int startIndex, int stopIndexPlusOne) {
		return toDoubleArray(array, startIndex, stopIndexPlusOne, o -> o.doubleValue());
	}
	public static double[] toDoubleArray(BigDecimal[] array) {
		return toDoubleArray(array, 0, array.length);
	}
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// double[] to R[]
	///////////////////////////////////////////////////////////////////////////
	private static final class Generator<R> implements IntFunction<R[]> {
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
	private static <R> R[] toArray(double[] array, int startIndex, int stopIndexPlusOne, DoubleFunction<R> map, Class<R> clazz) {
		checkIndex(array, startIndex, stopIndexPlusOne);
		IntFunction<R[]> generator = new Generator<R>(clazz);
		return Arrays.stream(array, startIndex, stopIndexPlusOne).mapToObj(map).toArray(generator);
	}
	///////////////////////////////////////////////////////////////////////////
	// double[] to BigDecima[]
	///////////////////////////////////////////////////////////////////////////
	public static BigDecimal[] toArray(double[] array, int startIndex, int stopIndexPlusOne) {
		return toArray(array, startIndex, stopIndexPlusOne, o -> BigDecimal.valueOf(o), BigDecimal.class);
	}
	public static BigDecimal[] toArray(double[] array) {
		return toArray(array, 0, array.length);
	}
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// double[] to double[]
	///////////////////////////////////////////////////////////////////////////
	public static double[] toDoubleArray(double[] array, int startIndex, int stopIndexPlusOne, DoubleUnaryOperator op) {
		checkIndex(array, startIndex, stopIndexPlusOne);
		//return Arrays.stream(array, startIndex, stopIndexPlusOne).map(op).toArray();

		int length = stopIndexPlusOne - startIndex;
		double[] result = new double[length];
		for(int i = startIndex, j = 0; i < stopIndexPlusOne; i++, j++) {
			result[j] = op.applyAsDouble(array[i]);
		}
		return result;
	}
	public static double[] toDoubleArray(double[] array, DoubleUnaryOperator op) {
		return toDoubleArray(array, 0, array.length, op);
	}
	//
	// simple return
	//
	private static class SimpleReturn implements DoubleUnaryOperator {
		private boolean firstTime = true;
		private double  lastValue = Double.NaN;
		
		@Override
		public double applyAsDouble(double value) {
			// sanity check
			if (Double.isInfinite(value)) {
				logger.error("value is infinite");
				logger.error("  value {}", Double.toString(value));
				throw new UnexpectedException("value is infinite");
			}
			
			final double result;
			if (firstTime) {
				firstTime = false;
				result    = 0;
			} else {
				result    = (value / lastValue) - 1.0;
			}
			lastValue = value;
			return result;
		}
	}
	public static double[] simpleReturn(double[] array, int startIndex, int stopIndexPlusOne) {
		DoubleUnaryOperator op = new SimpleReturn();
		return toDoubleArray(array, startIndex, stopIndexPlusOne, op);
	}
	public static double[] simpleReturn(double[] array) {
		return simpleReturn(array, 0, array.length);
	}
	//
	// log return
	//
	private static class LogReturn implements DoubleUnaryOperator {
		private boolean firstTime    = true;
		private double  lastLogValue = Double.NaN;
		
		@Override
		public double applyAsDouble(double value) {
			// sanity check
			if (Double.isInfinite(value)) {
				logger.error("value is infinite");
				logger.error("  value {}", Double.toString(value));
				throw new UnexpectedException("value is infinite");
			}
			
			double logValue = Math.log(value);
			
			final double result;
			if (firstTime) {
				firstTime = false;
				result    = 0;
			} else {
				result    = logValue - lastLogValue;
			}
			
			lastLogValue = logValue;
			return result;
		}
	}
	public static double[] logReturn(double[] array, int startIndex, int stopIndexPlusOne) {
		DoubleUnaryOperator op = new LogReturn();
		return toDoubleArray(array, startIndex, stopIndexPlusOne, op);
	}
	public static double[] logReturn(double[] array) {
		return logReturn(array, 0, array.length);
	}
	//
	// simple moving average
	//
	private static class SMA implements DoubleUnaryOperator {
		private final int      size;
		private final double[] data;

		private int      count = 0;
		private int      index = 0;
		private double   sum   = 0;
		
		public SMA(int size_) {
			size = size_;
			data = new double[size];
		}
		
		@Override
		public double applyAsDouble(double value) {
			// sanity check
			if (Double.isInfinite(value)) {
				logger.error("value is infinite");
				logger.error("  value {}", Double.toString(value));
				throw new UnexpectedException("value is infinite");
			}
			
			final double result;
			if (count < size) {
				// write data
				data[index] = value;
				// update sum
				sum += value;
				// set result
				result = sum / (index + 1);
			} else {
				// adjust sum
				sum -= data[index];
				// overwrite data
				data[index] = value;
				// update sum
				sum += value;
				// set result
				result = sum / size;
			}
			
			// update for next iteration
			count++;
			index++;
			if (index == size) index = 0;
			
			return result;
		}
	}
	public static double[] sma(double[] array, int startIndex, int stopIndexPlusOne, int size) {
		DoubleUnaryOperator op = new SMA(size);
		return toDoubleArray(array, startIndex, stopIndexPlusOne, op);
	}
	public static double[] sma(double[] array, int size) {
		return sma(array, 0, array.length, size);
	}
	//
	// FIXME exponential moving average
	//
	// public static double[] ema(double[] array, double alpha)
	// public static double[] ema(double[] array, int    size)
	//
	// FIXME RIS_Wilder
	//
	// public static double[] rsi(double[] array, int size)
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// double[] double[] to double[]
	///////////////////////////////////////////////////////////////////////////
	public static double[] toDoubleArray(double[] a, double[] b, int startIndex, int stopIndexPlusOne, DoubleBinaryOperator op) {
		checkIndex(a, b, startIndex, stopIndexPlusOne);

		int length = stopIndexPlusOne - startIndex;
		double[] result = new double[length];
		for(int i = startIndex, j = 0; i < stopIndexPlusOne; i++, j++) {
			result[j] = op.applyAsDouble(a[i], b[i]);
		}
		return result;
	}
	public static double[] toDoubleArray(double[] a, double[] b, DoubleBinaryOperator op) {
		return toDoubleArray(a, b, 0, a.length, op);
	}
	//
	// multiply
	//
	private static final DoubleBinaryOperator multiplyOp = (a, b) -> a * b;
	public static double[] multiply(double[] a, double[] b, int startIndex, int stopIndexPlusOne) {
		return toDoubleArray(a, b, startIndex, stopIndexPlusOne, multiplyOp);
	}
	public static double[] multiply(double[] a, double[] b) {
		return multiply(a, b, 0, a.length);
	}
	//
	// FIXME Reinvested Price
	//
	// public static double[] reinvestedPrices(double[] price, double[] div)
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// double[] to double
	///////////////////////////////////////////////////////////////////////////
	public static interface ToDoubleImpl extends DoubleConsumer {
		// extends DoubleConsumer for forEach() of DoubleStream
		public double get();
	}
	public static double toDouble(double[] array, int startIndex, int stopIndexPlusOne, ToDoubleImpl reduce) {
		checkIndex(array, startIndex, stopIndexPlusOne);
		// Arrays.stream(array, startIndex, stopIndexPlusOne).forEach(reduce);
		for(int i = startIndex; i < stopIndexPlusOne; i++) {
			reduce.accept(array[i]);
		}
		return reduce.get();
	}
	//
	// sum
	//
	private static class SumImpl implements ToDoubleImpl {
		private int    count  = 0;
		private double result = 0;
		
		@Override
		public void accept(double value) {
			// sanity check
			if (Double.isInfinite(value)) {
				logger.error("value is infinite");
				logger.error("  value {}", Double.toString(value));
				throw new UnexpectedException("value is infinite");
			}
			
			count++;
			result += value;
		}

		@Override
		public double get() {
			return count == 0 ? Double.NaN : result;
		}
	}
	public static double sum(double array[], int startIndex, int stopIndexPlusOne) {
		ToDoubleImpl reduce = new SumImpl();
		return toDouble(array, startIndex, stopIndexPlusOne, reduce);
	}
	public static double sum(double[] array) {
		return sum(array, 0, array.length);
	}
	//
	// mean
	//
	private static class MeanImpl implements ToDoubleImpl {
		private int    count  = 0;
		private double result = 0;
		
		@Override
		public void accept(double value) {
			// sanity check
			if (Double.isInfinite(value)) {
				logger.error("value is infinite");
				logger.error("  value {}", Double.toString(value));
				throw new UnexpectedException("value is infinite");
			}
			
			count++;
			result += value;
		}

		@Override
		public double get() {
			return count == 0 ? Double.NaN : result / count;
		}
	}
	public static double mean(double array[], int startIndex, int stopIndexPlusOne) {
		ToDoubleImpl reduce = new MeanImpl();
		return toDouble(array, startIndex, stopIndexPlusOne, reduce);
	}
	public static double mean(double[] array) {
		return mean(array, 0, array.length);
	}
	//
	// geometric mean
	//
	private static class GeometricMeanImpl implements ToDoubleImpl {
		private int    count  = 0;
		private double result = 0;
		
		@Override
		public void accept(double value) {
			// sanity check
			if (Double.isInfinite(value)) {
				logger.error("value is infinite");
				logger.error("  value {}", Double.toString(value));
				throw new UnexpectedException("value is infinite");
			}
			
			count++;
			result += Math.log(value);
		}

		@Override
		public double get() {
			return count == 0 ? Double.NaN : Math.exp(result / count);
		}
	}
	public static double geometricMean(double array[], int startIndex, int stopIndexPlusOne) {
		ToDoubleImpl reduce = new GeometricMeanImpl();
		return toDouble(array, startIndex, stopIndexPlusOne, reduce);
	}
	public static double geometricMean(double[] array) {
		return geometricMean(array, 0, array.length);
	}

	
	
	//
	// variance
	//
	//
	// variance without mean
	//
	private static class VarianceImpl implements ToDoubleImpl {
		private int    count  = 0;
		private double sum    = 0;
		private double sum2   = 0;
		
		@Override
		public void accept(double value) {
			// sanity check
			if (Double.isInfinite(value)) {
				logger.error("value is infinite");
				logger.error("  value {}", Double.toString(value));
				throw new UnexpectedException("value is infinite");
			}
			
			count++;
			
			sum  += value;
			sum2 += value * value;
		}

		@Override
		public double get() {
			if (count == 0) return Double.NaN;
			double ex  = sum  / count;
			double ex2 = sum2 / count;
			// variance = E(X ^ 2) - E(X) ^ 2
			return ex2 - (ex * ex);
		}
	}
	public static double variance(double array[], int startIndex, int stopIndexPlusOne) {
		ToDoubleImpl reduce = new VarianceImpl();
		return toDouble(array, startIndex, stopIndexPlusOne, reduce);
	}
	public static double variance(double[] array) {
		return variance(array, 0, array.length);
	}
	//
	// variance - using precalculated mean
	//
	//
	// variance with mean
	//
	private static class Variance2Impl implements ToDoubleImpl {
		private final double mean;
		
		private int    count  = 0;
		private double sum    = 0;
		
		Variance2Impl(double mean_) {
			mean = mean_;
		}
		
		@Override
		public void accept(double value) {
			// sanity check
			if (Double.isInfinite(value)) {
				logger.error("value is infinite");
				logger.error("  value {}", Double.toString(value));
				throw new UnexpectedException("value is infinite");
			}
			
			count++;
			
			double t = value - mean;
			sum  += (t * t);
		}

		@Override
		public double get() {
			return count == 0 ? Double.NaN : (sum / count);
		}
	}
	public static double variance(double array[], int startIndex, int stopIndexPlusOne, double mean) {
		ToDoubleImpl reduce = new Variance2Impl(mean);
		return toDouble(array, startIndex, stopIndexPlusOne, reduce);
	}
	public static double variance(double[] array, double mean) {
		return variance(array, 0, array.length, mean);
	}
	//
	// standard deviation
	//
	//
	// standard deviation without mean
	//
	public static double standardDeviation(double array[], int startIndex, int stopIndexPlusOne) {
		// Math.sqrt(Double.Nan) == Double.NaN
		return Math.sqrt(variance(array, startIndex, stopIndexPlusOne));
	}
	public static double standardDeviation(double[] array) {
		return standardDeviation(array, 0, array.length);
	}
	//
	// standard deviation with mean
	//
	//
	// standardDeviation - using precalculated mean
	//
	public static double standardDeviation(double array[], int startIndex, int stopIndexPlusOne, double mean) {
		// Math.sqrt(Double.Nan) == Double.NaN
		return Math.sqrt(variance(array, startIndex, stopIndexPlusOne, mean));
	}
	public static double standardDeviation(double[] array, double mean) {
		return standardDeviation(array, 0, array.length, mean);
	}

}
