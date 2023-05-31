package yokwe.util.finance;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.function.Function;

import yokwe.util.UnexpectedException;

public final class BigDecimalArrays {
	private static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();
	
	//
	// check index consistency with array
	//
	public static <T> void checkIndex(T[] array, int startIndex, int stopIndexPlusOne) {
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
	
	
	//
	// create BigDecimal array from other type
	//
	public static <T> BigDecimal[] toArray(T[] array, int startIndex, int stopIndexPlusOne, Function<T, BigDecimal> function) {
		checkIndex(array, startIndex, stopIndexPlusOne);
		
		BigDecimal[] ret = new BigDecimal[stopIndexPlusOne - startIndex];
		for(int i = startIndex, j = 0; i < stopIndexPlusOne; i++, j++) {
			ret[j] = function.apply(array[i]);
		}
		return ret;
	}
	public static <T> BigDecimal[] toArray(T[] array, Function<T, BigDecimal> function) {
		return toArray(array, 0, array.length, function);
	}
	
	
	//
	// create simple ratio array from array
	//
	public static BigDecimal toSimpleReturn(BigDecimal startValue, BigDecimal endValue) {
		return endValue.divide(startValue, BigDecimalUtil.DEFAULT_MATH_CONTEXT).subtract(BigDecimal.ONE);
	}
	public static <T> BigDecimal[] toSimpleReturn(T[] array, int startIndex, int stopIndexPlusOne, Function<T, BigDecimal> function) {
		checkIndex(array, startIndex, stopIndexPlusOne);
		
		BigDecimal[] ret = new BigDecimal[stopIndexPlusOne - startIndex];
		// use value of before startIndex if possible
		BigDecimal previous = function.apply(array[startIndex == 0 ? 0 : startIndex - 1]);
		for(int i = startIndex, j = 0; i < stopIndexPlusOne; i++, j++) {
			BigDecimal value = function.apply(array[i]);
			ret[j] = toSimpleReturn(previous, value);
			previous = value;
		}
		
		return ret;
	}
	public static <T> BigDecimal[] toSimpleReturn(T[] array, Function<T, BigDecimal> function) {
		return toSimpleReturn(array, 0, array.length, function);
	}
	public static BigDecimal[] toSimpleReturn(BigDecimal[] array, int startIndex, int stopIndexPlusOne) {
		return toSimpleReturn(array, startIndex, stopIndexPlusOne, o -> o);
	}
	public static BigDecimal[] toSimpleReturn(BigDecimal[] array) {
		return toSimpleReturn(array, 0, array.length, o -> o);
	}

	
	//
	// create log ratio array from array
	//
	public static <T> BigDecimal[] toLogReturn(T[] array, int startIndex, int stopIndexPlusOne, Function<T, BigDecimal> function) {
		checkIndex(array, startIndex, stopIndexPlusOne);
		
		BigDecimal[] ret = new BigDecimal[stopIndexPlusOne - startIndex];
		// use value of before startIndex if possible
		BigDecimal previous = BigDecimalUtil.mathLog(function.apply(array[startIndex == 0 ? 0 : startIndex - 1]));
		for(int i = startIndex, j = 0; i < stopIndexPlusOne; i++, j++) {
			BigDecimal element = function.apply(array[i]);
			if (element.signum() < 0) {
				logger.error("netagive value");
				logger.error("  {}  {}", i, element.toPlainString());
				logger.error("  array.length      {}", array.length);
				logger.error("  startIndex        {}", startIndex);
				logger.error("  stopIndexPlusOne  {}", stopIndexPlusOne);
				throw new UnexpectedException("netagive value");
			}
			BigDecimal value = BigDecimalUtil.mathLog(element);
			ret[j] = value.subtract(previous);
			previous = value;
		}
		
		return ret;
	}
	public static <T> BigDecimal[] toLogReturn(T[] array, Function<T, BigDecimal> function) {
		return toLogReturn(array, 0, array.length, function);
	}
	public static BigDecimal[] toLogReturn(BigDecimal[] array, int startIndex, int stopIndexPlusOne) {
		return toLogReturn(array, startIndex, stopIndexPlusOne, o -> o);
	}
	public static BigDecimal[] toLogReturn(BigDecimal[] array) {
		return toLogReturn(array, 0, array.length, o -> o);
	}
	
	
	//
	// sum
	//
	public static <T> BigDecimal sum(T[] array, int startIndex, int stopIndexPlusOne, Function<T, BigDecimal> function) {
		checkIndex(array, startIndex, stopIndexPlusOne);
		
		BigDecimal ret = BigDecimal.ZERO;
		for(int i = startIndex; i < stopIndexPlusOne; i++) {
			ret = ret.add(function.apply(array[i]));
		}
		
		return ret;
	}
	public static <T> BigDecimal sum(T[] array, Function<T, BigDecimal> function) {
		return sum(array, 0, array.length, function);
	}
	public static BigDecimal sum(BigDecimal[] array, int startIndex, int stopIndexPlusOne) {
		return sum(array, startIndex, stopIndexPlusOne, o -> o);
	}
	public static BigDecimal sum(BigDecimal[] array) {
		return sum(array, 0, array.length, o -> o);
	}
	
	
	//
	// arithmetic mean
	//
	public static <T> BigDecimal mean(T[] array, int startIndex, int stopIndexPlusOne, Function<T, BigDecimal> function) {
		checkIndex(array, startIndex, stopIndexPlusOne);
		
		BigDecimal value = sum(array, startIndex, stopIndexPlusOne, function);
		return value.divide(BigDecimal.valueOf(stopIndexPlusOne - startIndex), BigDecimalUtil.DEFAULT_MATH_CONTEXT);
	}
	public static <T> BigDecimal mean(T[] array, Function<T, BigDecimal> function) {
		return mean(array, 0, array.length, function);
	}
	public static BigDecimal mean(BigDecimal[] array, int startIndex, int stopIndexPlusOne) {
		return mean(array, startIndex, stopIndexPlusOne, o -> o);
	}
	public static BigDecimal mean(BigDecimal[] array) {
		return mean(array, 0, array.length, o -> o);
	}
	
	
	//
	// geometric mean  -- using logarithm and exponential
	//
	public static <T> BigDecimal geometricMean(T[] array, int startIndex, int stopIndexPlusOne, Function<T, BigDecimal> function) {
		checkIndex(array, startIndex, stopIndexPlusOne);
		
		BigDecimal value = BigDecimalArrays.sum(array, startIndex, stopIndexPlusOne, o -> BigDecimalUtil.mathLog(function.apply(o)));
		return BigDecimalUtil.mathExp(value.divide(BigDecimal.valueOf(stopIndexPlusOne - startIndex), BigDecimalUtil.DEFAULT_MATH_CONTEXT));
	}
	public static <T> BigDecimal geometricMean(T[] array, Function<T, BigDecimal> function) {
		return geometricMean(array, 0, array.length, function);
	}
	public static BigDecimal geometricMean(BigDecimal[] array, int startIndex, int stopIndexPlusOne) {
		return geometricMean(array, startIndex, stopIndexPlusOne, o -> o);
	}
	public static BigDecimal geometricMean(BigDecimal[] array) {
		return geometricMean(array, 0, array.length, o -> o);
	}
	
	
	//
	// variance
	//
	public static <T> BigDecimal variance(T[] array, int startIndex, int stopIndexPlusOne, BigDecimal mean, Function<T, BigDecimal> function) {
		checkIndex(array, startIndex, stopIndexPlusOne);
		
		BigDecimal var = BigDecimal.ZERO;
		for(int i = startIndex; i < stopIndexPlusOne; i++) {
			BigDecimal diff = mean.subtract(function.apply(array[i]));
			var = var.add(diff.multiply(diff, BigDecimalUtil.DEFAULT_MATH_CONTEXT));
		}
		
		return var.divide(BigDecimal.valueOf(stopIndexPlusOne - startIndex), BigDecimalUtil.DEFAULT_MATH_CONTEXT);
	}
	public static <T> BigDecimal variance(T[] array, BigDecimal mean, Function<T, BigDecimal> function) {
		return variance(array, 0, array.length, mean, function);
	}
	public static BigDecimal variance(BigDecimal[] array, int startIndex, int stopIndexPlusOne, BigDecimal mean) {
		return variance(array, startIndex, stopIndexPlusOne, mean, o -> o);
	}
	public static BigDecimal variance(BigDecimal[] array, BigDecimal mean) {
		return variance(array, 0, array.length, mean, o -> o);
	}
	
		
	//
	// standard deviation
	//
	public static <T> BigDecimal standardDeviation(T[] array, int startIndex, int stopIndexPlusOne, BigDecimal mean, Function<T, BigDecimal> function) {
		checkIndex(array, startIndex, stopIndexPlusOne);
		
		return variance(array, startIndex, stopIndexPlusOne, mean, function).sqrt(BigDecimalUtil.DEFAULT_MATH_CONTEXT);
	}
	public static <T> BigDecimal standardDeviation(T[] array, BigDecimal mean, Function<T, BigDecimal> function) {
		return standardDeviation(array, 0, array.length, mean, function);
	}
	public static BigDecimal standardDeviation(BigDecimal[] array, int startIndex, int stopIndexPlusOne, BigDecimal mean) {
		return standardDeviation(array, startIndex, stopIndexPlusOne, mean, o -> o);
	}
	public static BigDecimal standardDeviation(BigDecimal[] array, BigDecimal mean) {
		return standardDeviation(array, 0, array.length, mean, o -> o);
	}
	// without mean
	public static <T> BigDecimal standardDeviation(T[] array, int startIndex, int stopIndexPlusOne, Function<T, BigDecimal> function) {
		BigDecimal mean = mean(array, startIndex, stopIndexPlusOne, function);
		return standardDeviation(array, startIndex, stopIndexPlusOne, mean, function);
	}
	public static <T> BigDecimal standardDeviation(T[] array, Function<T, BigDecimal> function) {
		return standardDeviation(array, 0, array.length, function);
	}
	public static BigDecimal standardDeviation(BigDecimal[] array, int startIndex, int stopIndexPlusOne) {
		return standardDeviation(array, startIndex, stopIndexPlusOne, o -> o);
	}
	public static BigDecimal standardDeviation(BigDecimal[] array) {
		return standardDeviation(array, 0, array.length, o -> o);
	}
	
	
	private static void test(int[] values) {
		BigDecimal[] array = Arrays.stream(values).mapToObj(o -> BigDecimal.valueOf(o)).toArray(BigDecimal[]::new);
		
		BigDecimal mean = BigDecimalArrays.mean(array);
		BigDecimal var  = BigDecimalArrays.variance(array, mean);
		BigDecimal sd = BigDecimalArrays.standardDeviation(array, mean);
		
		logger.info("var {}", var);
		logger.info("sd  {}", sd);
	}
	public static void main(String[] args) {
		{
			//             14　2　13　20　16
			int array[] = {14, 2, 13, 20, 16};
			test(array);
		}
	}
}
