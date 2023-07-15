package yokwe.util.finance;

import java.lang.reflect.Array;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.OptionalDouble;
import java.util.function.DoubleBinaryOperator;
import java.util.function.DoubleConsumer;
import java.util.function.DoubleFunction;
import java.util.function.DoubleUnaryOperator;
import java.util.function.IntFunction;
import java.util.function.ToDoubleFunction;

import yokwe.util.DoubleUtil;
import yokwe.util.GenericArray;
import yokwe.util.UnexpectedException;

public final class DoubleArray {
	static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();
	
	// FIXME add test case for each methods
	
	///////////////////////////////////////////////////////////////////////////
	// check index consistency with array
	///////////////////////////////////////////////////////////////////////////
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
	public static <T> double[] toDoubleArray(T[] array, int startIndex, int stopIndexPlusOne, ToDoubleFunction<T> map) {
		GenericArray.checkIndex(array, startIndex, stopIndexPlusOne);
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

//		int length = stopIndexPlusOne - startIndex;
//		R[] result = generator.apply(length);
//		for(int i = 0, j = startIndex; i < length; i++, j++) {
//			result[i] = map.apply(array[j]);
//		}
//		return result;
	}
	///////////////////////////////////////////////////////////////////////////
	// double[] to BigDecima[]
	///////////////////////////////////////////////////////////////////////////
	private static DoubleFunction<BigDecimal> doubleToBigDecimal = o -> DoubleUtil.toBigDecimal(o);
	public static BigDecimal[] toArray(double[] array, int startIndex, int stopIndexPlusOne) {
		return toArray(array, startIndex, stopIndexPlusOne, doubleToBigDecimal, BigDecimal.class);
	}
	public static BigDecimal[] toArray(double[] array) {
		return toArray(array, 0, array.length);
	}
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// double[] to double[]
	///////////////////////////////////////////////////////////////////////////
	public static double[] toDoubleArray(double[] array, int startIndex, int stopIndexPlusOne, DoubleUnaryOperator op) {
		checkIndex(array, startIndex, stopIndexPlusOne);
		return Arrays.stream(array, startIndex, stopIndexPlusOne).map(op).toArray();
		
//		int length = stopIndexPlusOne - startIndex;
//		double[] result = new double[length];
//		for(int i = 0, j = startIndex; i < length; i++, j++) {
//			result[i] = op.applyAsDouble(array[j]);
//		}
//		return result;
	}
	public static double[] toDoubleArray(double[] array, DoubleUnaryOperator op) {
		return toDoubleArray(array, 0, array.length, op);
	}
	///////////////////////////////////////////////////////////////////////////
	// simple return
	///////////////////////////////////////////////////////////////////////////
	public static double[] simpleReturn(double[] array, int startIndex, int stopIndexPlusOne) {
		DoubleUnaryOperator op = new SimpleReturn();
		return toDoubleArray(array, startIndex, stopIndexPlusOne, op);
	}
	public static double[] simpleReturn(double[] array) {
		return simpleReturn(array, 0, array.length);
	}
	///////////////////////////////////////////////////////////////////////////
	// log return
	///////////////////////////////////////////////////////////////////////////
	public static double[] logReturn(double[] array, int startIndex, int stopIndexPlusOne) {
		DoubleUnaryOperator op = new LogReturn();
		return toDoubleArray(array, startIndex, stopIndexPlusOne, op);
	}
	public static double[] logReturn(double[] array) {
		return logReturn(array, 0, array.length);
	}
	///////////////////////////////////////////////////////////////////////////
	// simple moving average
	///////////////////////////////////////////////////////////////////////////
	public static double[] sma(double[] array, int startIndex, int stopIndexPlusOne, int size) {
		DoubleUnaryOperator op = new SMA(size);
		return toDoubleArray(array, startIndex, stopIndexPlusOne, op);
	}
	public static double[] sma(double[] array, int size) {
		return sma(array, 0, array.length, size);
	}
	///////////////////////////////////////////////////////////////////////////
	// exponential moving average
	///////////////////////////////////////////////////////////////////////////
	public static double[] ema(double[] array, int startIndex, int stopIndexPlusOne, double alpha) {
		DoubleUnaryOperator op = new EMA(alpha);
		return toDoubleArray(array, startIndex, stopIndexPlusOne, op);
	}
	public static double[] ema(double[] array, double alpha) {
		return ema(array, 0, array.length, alpha);
	}
	///////////////////////////////////////////////////////////////////////////
	// relative strength indicator
	///////////////////////////////////////////////////////////////////////////
	public static double[] rsi(double[] array, int startIndex, int stopIndexPlusOne, int size) {
		DoubleUnaryOperator op = new RSI_Wilder(size);
		return toDoubleArray(array, startIndex, stopIndexPlusOne, op);
	}
	public static double[] rsi(double[] array, int size) {
		return rsi(array, 0, array.length, size);
	}
	///////////////////////////////////////////////////////////////////////////
	// relative strength indicator with default size
	///////////////////////////////////////////////////////////////////////////
	public static double[] rsi(double[] array, int startIndex, int stopIndexPlusOne) {
		DoubleUnaryOperator op = new RSI_Wilder();
		return toDoubleArray(array, startIndex, stopIndexPlusOne, op);
	}
	public static double[] rsi(double[] array) {
		return rsi(array, 0, array.length);
	}
	
	
	
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
	///////////////////////////////////////////////////////////////////////////
	// multiply
	///////////////////////////////////////////////////////////////////////////
	private static final DoubleBinaryOperator multiplyOp = (a, b) -> a * b;
	public static double[] multiply(double[] a, double[] b, int startIndex, int stopIndexPlusOne) {
		return toDoubleArray(a, b, startIndex, stopIndexPlusOne, multiplyOp);
	}
	public static double[] multiply(double[] a, double[] b) {
		return multiply(a, b, 0, a.length);
	}
	///////////////////////////////////////////////////////////////////////////
	// reinvested Price
	///////////////////////////////////////////////////////////////////////////
	public static double[] reinvestedPrices(double[] price, double[] div, int startIndex, int stopIndexPlusOne) {
		DoubleBinaryOperator op = new ReinvestedPrice();
		return toDoubleArray(price, div, startIndex, stopIndexPlusOne, op);
	}
	public static double[] reinvestedPrices(double[] price, double[] div) {
		return reinvestedPrices(price, div, 0, price.length);
	}
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// double[] to double using ToDoubleImpl
	///////////////////////////////////////////////////////////////////////////
	public static interface ToDoubleImpl extends DoubleConsumer {
		// extends DoubleConsumer for forEach() of DoubleStream
		public double get();
	}
	public static double toDouble(double[] array, int startIndex, int stopIndexPlusOne, ToDoubleImpl toDouble) {
		checkIndex(array, startIndex, stopIndexPlusOne);
		Arrays.stream(array, startIndex, stopIndexPlusOne).forEach(toDouble);
		return toDouble.get();
	}
	///////////////////////////////////////////////////////////////////////////
	// double[] to double using DoubleBinaryOperator
	///////////////////////////////////////////////////////////////////////////
	public static double toDouble(double[] array, int startIndex, int stopIndexPlusOne, DoubleBinaryOperator op, double identity) {
		checkIndex(array, startIndex, stopIndexPlusOne);
		return Arrays.stream(array, startIndex, stopIndexPlusOne).reduce(identity, op);
	}
	public static double toDouble(double[] array, int startIndex, int stopIndexPlusOne, DoubleBinaryOperator op) {
		checkIndex(array, startIndex, stopIndexPlusOne);
		OptionalDouble opt = Arrays.stream(array, startIndex, stopIndexPlusOne).reduce(op);
		
		if (opt.isPresent()) return opt.getAsDouble();
		logger.error("opt is empty");
		throw new UnexpectedException("opt is empty");
	}
	///////////////////////////////////////////////////////////////////////////
	// sum
	///////////////////////////////////////////////////////////////////////////
	private static final DoubleBinaryOperator sumImpl = (a, b) -> a + b;
	public static double sum(double array[], int startIndex, int stopIndexPlusOne) {
		return toDouble(array, startIndex, stopIndexPlusOne, sumImpl, 0);
	}
	public static double sum(double[] array) {
		return sum(array, 0, array.length);
	}
	///////////////////////////////////////////////////////////////////////////
	// mean
	///////////////////////////////////////////////////////////////////////////
	public static double mean(double array[], int startIndex, int stopIndexPlusOne) {
		ToDoubleImpl reduce = new Mean();
		return toDouble(array, startIndex, stopIndexPlusOne, reduce);
	}
	public static double mean(double[] array) {
		return mean(array, 0, array.length);
	}
	///////////////////////////////////////////////////////////////////////////
	// geometric mean
	///////////////////////////////////////////////////////////////////////////
	public static double geometricMean(double array[], int startIndex, int stopIndexPlusOne) {
		ToDoubleImpl reduce = new GeometricMean();
		return toDouble(array, startIndex, stopIndexPlusOne, reduce);
	}
	public static double geometricMean(double[] array) {
		return geometricMean(array, 0, array.length);
	}
	///////////////////////////////////////////////////////////////////////////
	//variance
	///////////////////////////////////////////////////////////////////////////
	public static double variance(double array[], int startIndex, int stopIndexPlusOne) {
		ToDoubleImpl reduce = new Variance();
		return toDouble(array, startIndex, stopIndexPlusOne, reduce);
	}
	public static double variance(double[] array) {
		return variance(array, 0, array.length);
	}
	///////////////////////////////////////////////////////////////////////////
	//variance using mean
	///////////////////////////////////////////////////////////////////////////
	public static double variance(double array[], int startIndex, int stopIndexPlusOne, double mean) {
		ToDoubleImpl reduce = new VarianceUsingMean(mean);
		return toDouble(array, startIndex, stopIndexPlusOne, reduce);
	}
	public static double variance(double[] array, double mean) {
		return variance(array, 0, array.length, mean);
	}
	///////////////////////////////////////////////////////////////////////////
	// standard deviation
	///////////////////////////////////////////////////////////////////////////
	public static double standardDeviation(double array[], int startIndex, int stopIndexPlusOne) {
		return Math.sqrt(variance(array, startIndex, stopIndexPlusOne));
	}
	public static double standardDeviation(double[] array) {
		return standardDeviation(array, 0, array.length);
	}
	///////////////////////////////////////////////////////////////////////////
	// standard deviation using mean
	///////////////////////////////////////////////////////////////////////////
	public static double standardDeviation(double array[], int startIndex, int stopIndexPlusOne, double mean) {
		// Math.sqrt(Double.Nan) == Double.NaN
		return Math.sqrt(variance(array, startIndex, stopIndexPlusOne, mean));
	}
	public static double standardDeviation(double[] array, double mean) {
		return standardDeviation(array, 0, array.length, mean);
	}

}
