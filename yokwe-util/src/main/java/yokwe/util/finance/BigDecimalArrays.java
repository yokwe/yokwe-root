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
	public static <T> BigDecimal[] toArray(T[] array, int startIndex, int endIndexPlusOne, Function<T, BigDecimal> function) {
		checkIndex(array, startIndex, endIndexPlusOne);
		
		BigDecimal[] ret = new BigDecimal[endIndexPlusOne - startIndex];
		int j = 0;
		for(int i = startIndex; i < endIndexPlusOne; i++, j++) {
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
	public static <T> void checkIndex(T[] array, int startIndex, int endIndexPlusOne) {
		if (array == null) {
			logger.error("array == null");
			throw new UnexpectedException("array == null");
		}
		if (!(0 <= startIndex && startIndex < array.length)) {
			logger.error("  array.length      {}", array.length);
			logger.error("  startIndex        {}", startIndex);
			logger.error("  endIndexPlusOne  {}", endIndexPlusOne);
			throw new UnexpectedException("offset is out of range");
		}
		if (!(startIndex < endIndexPlusOne && endIndexPlusOne <= array.length)) {
			logger.error("  array.length      {}", array.length);
			logger.error("  startIndex        {}", startIndex);
			logger.error("  endIndexPlusOne  {}", endIndexPlusOne);
			throw new UnexpectedException("offset is out of range");
		}
	}
	
	
	//
	// sum
	//
	public static BigDecimal sum(BigDecimal[] array, int startIndex, int endIndexPlusOne) {
		checkIndex(array, startIndex, endIndexPlusOne);
		
		BigDecimal ret = BigDecimal.ZERO;
		for(int i = startIndex; i < endIndexPlusOne; i++) {
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
	public static BigDecimal mean(BigDecimal[] array, int startIndex, int endIndexPlusOne, MathContext mathContext) {
		checkIndex(array, startIndex, endIndexPlusOne);
		
		BigDecimal sum = sum(array, startIndex, endIndexPlusOne);
		return sum.divide(BigDecimal.valueOf(endIndexPlusOne - startIndex), mathContext);
	}
	public static BigDecimal mean(BigDecimal[] array, MathContext mathContext) {
		return mean(array, 0, array.length, mathContext);
	}
	
	//
	// geometric mean  -- using logarithm and exponential
	//
	public static BigDecimal geometricMean(BigDecimal[] array, int startIndex, int endIndexPlusOne, MathContext mathContext) {
		checkIndex(array, startIndex, endIndexPlusOne);
		
		BigDecimal value = BigDecimal.ZERO;
		for(int i = startIndex; i < endIndexPlusOne; i++) {
			value = value.add(BigDecimalUtil.mathLog(array[i]));
		}
		
		return BigDecimalUtil.mathExp(value.divide(BigDecimal.valueOf(endIndexPlusOne - startIndex), mathContext));
	}
	public static BigDecimal geometricMean(BigDecimal[] array, MathContext mathContext) {
		return geometricMean(array, 0, array.length, mathContext);
	}
	
	
	//
	// variance
	//
	public static BigDecimal variance(BigDecimal[] array, int startIndex, int endIndexPlusOne, BigDecimal mean, MathContext mathContext) {
		checkIndex(array, startIndex, endIndexPlusOne);
		
		BigDecimal var = BigDecimal.ZERO;
		for(int i = startIndex; i < endIndexPlusOne; i++) {
			BigDecimal diff = mean.subtract(array[i]);
			var = var.add(diff.multiply(diff, mathContext));
		}
		// variance
		return var.divide(BigDecimal.valueOf(endIndexPlusOne - startIndex), mathContext);
	}
	public static BigDecimal variance(BigDecimal[] array, BigDecimal mean, MathContext mathContext) {
		return variance(array, 0, array.length, mean, mathContext);
	}
	
	
	//
	// standard deviation
	//
	public static BigDecimal sd(BigDecimal[] array, int startIndex, int endIndexPlusOne, BigDecimal mean, MathContext mathContext) {
		return variance(array, startIndex, endIndexPlusOne, mean, mathContext).sqrt(mathContext);
	}
	public static BigDecimal sd(BigDecimal[] array, BigDecimal mean, MathContext mathContext) {
		return sd(array, 0, array.length, mean, mathContext);
	}
}
