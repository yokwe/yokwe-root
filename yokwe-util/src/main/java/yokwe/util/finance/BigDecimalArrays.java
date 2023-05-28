package yokwe.util.finance;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.function.Function;

import yokwe.util.UnexpectedException;

public final class BigDecimalArrays {
	private static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();
	
	//
	// create BigDecimal array from other type
	//
	public static <T> BigDecimal[] toArray(T[] array, int startIndex, int stopIndexPlusOne, Function<T, BigDecimal> function) {
		checkIndex(array, startIndex, stopIndexPlusOne);
		
		BigDecimal[] ret = new BigDecimal[stopIndexPlusOne - startIndex];
		int j = 0;
		for(int i = startIndex; i < stopIndexPlusOne; i++, j++) {
			ret[j] = function.apply(array[i]);
		}
		return ret;
	}
	public static <T> BigDecimal[] toArray(T[] array, Function<T, BigDecimal> function) {
		return toArray(array, 0, array.length, function);
	}
	
	
	//
	// check index consistency with array
	//
	public static <T> void checkIndex(T[] array, int startIndex, int stopIndexPlusOne) {
		if (array == null) {
			logger.error("array == null");
			throw new UnexpectedException("array == null");
		}
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
	// sum
	//
	public static BigDecimal sum(BigDecimal[] array, int startIndex, int stopIndexPlusOne) {
		checkIndex(array, startIndex, stopIndexPlusOne);
		
		BigDecimal ret = BigDecimal.ZERO;
		for(int i = startIndex; i < stopIndexPlusOne; i++) {
			ret = ret.add(array[i]);
		}
		return ret;
	}
	public static BigDecimal sum(BigDecimal[] values) {
		return sum(values, 0, values.length);
	}
	
	
	//
	// arithmetic mean
	//
	public static BigDecimal mean(BigDecimal[] array, int startIndex, int stopIndexPlusOne, MathContext mathContext) {
		checkIndex(array, startIndex, stopIndexPlusOne);
		
		BigDecimal sum = sum(array, startIndex, stopIndexPlusOne);
		return sum.divide(BigDecimal.valueOf(stopIndexPlusOne - startIndex), mathContext);
	}
	public static BigDecimal mean(BigDecimal[] array, MathContext mathContext) {
		return mean(array, 0, array.length, mathContext);
	}
	
	//
	// geometric mean  -- using logarithm and exponential
	//
	public static BigDecimal geometricMean(BigDecimal[] array, int startIndex, int stopIndexPlusOne, MathContext mathContext) {
		checkIndex(array, startIndex, stopIndexPlusOne);
		
		BigDecimal value = BigDecimal.ZERO;
		for(int i = startIndex; i < stopIndexPlusOne; i++) {
			value = value.add(BigDecimalUtil.mathLog(array[i]));
		}
		
		return BigDecimalUtil.mathExp(value.divide(BigDecimal.valueOf(stopIndexPlusOne - startIndex), mathContext));
	}
	public static BigDecimal geometricMean(BigDecimal[] array, MathContext mathContext) {
		return geometricMean(array, 0, array.length, mathContext);
	}
	
	
	//
	// variance
	//
	public static BigDecimal variance(BigDecimal[] array, int startIndex, int stopIndexPlusOne, BigDecimal mean, MathContext mathContext) {
		checkIndex(array, startIndex, stopIndexPlusOne);
		
		BigDecimal var = BigDecimal.ZERO;
		for(int i = startIndex; i < stopIndexPlusOne; i++) {
			BigDecimal diff = mean.subtract(array[i]);
			var = var.add(diff.multiply(diff, mathContext));
		}
		// variance
		return var.divide(BigDecimal.valueOf(stopIndexPlusOne - startIndex), mathContext);
	}
	public static BigDecimal variance(BigDecimal[] array, BigDecimal mean, MathContext mathContext) {
		return variance(array, 0, array.length, mean, mathContext);
	}
	
	
	//
	// standard deviation
	//
	public static BigDecimal sd(BigDecimal[] array, int startIndex, int stopIndexPlusOne, BigDecimal mean, MathContext mathContext) {
		return variance(array, startIndex, stopIndexPlusOne, mean, mathContext).sqrt(mathContext);
	}
	public static BigDecimal sd(BigDecimal[] array, int startIndex, int stopIndexPlusOne, MathContext mathContext) {
		BigDecimal mean = mean(array, startIndex, stopIndexPlusOne, mathContext);
		return variance(array, startIndex, stopIndexPlusOne, mean, mathContext).sqrt(mathContext);
	}
	public static BigDecimal sd(BigDecimal[] array, BigDecimal mean, MathContext mathContext) {
		return sd(array, 0, array.length, mean, mathContext);
	}
	public static BigDecimal sd(BigDecimal[] array, MathContext mathContext) {
		BigDecimal mean = mean(array, mathContext);
		return sd(array, 0, array.length, mean, mathContext);
	}
}
