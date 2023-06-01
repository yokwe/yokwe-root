package yokwe.util.finance;

import java.math.BigDecimal;
import java.util.Arrays;

public final class BigDecimalArrays {
	private static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();
	
	//
	// create simple ratio array from array
	//
	public static BigDecimal[] toSimpleReturn(BigDecimal[] array, int startIndex, int stopIndexPlusOne) {
		return BigDecimalUtil.toSimpleReturn(array, startIndex, stopIndexPlusOne, o -> o);
	}
	public static BigDecimal[] toSimpleReturn(BigDecimal[] array) {
		return BigDecimalUtil.toSimpleReturn(array, o -> o);
	}

	
	//
	// create log ratio array from array
	//
	public static BigDecimal[] toLogReturn(BigDecimal[] array, int startIndex, int stopIndexPlusOne) {
		return BigDecimalUtil.toLogReturn(array, startIndex, stopIndexPlusOne, o -> o);
	}
	public static BigDecimal[] toLogReturn(BigDecimal[] array) {
		return BigDecimalUtil.toLogReturn(array, o -> o);
	}
	
	
	//
	// sum
	//
	public static BigDecimal sum(BigDecimal[] array, int startIndex, int stopIndexPlusOne) {
		return BigDecimalUtil.sum(array, startIndex, stopIndexPlusOne, o -> o);
	}
	public static BigDecimal sum(BigDecimal[] array) {
		return BigDecimalUtil.sum(array, o -> o);
	}
	
	
	//
	// arithmetic mean
	//
	public static BigDecimal mean(BigDecimal[] array, int startIndex, int stopIndexPlusOne) {
		return BigDecimalUtil.mean(array, startIndex, stopIndexPlusOne, o -> o);
	}
	public static BigDecimal mean(BigDecimal[] array) {
		return BigDecimalUtil.mean(array, o -> o);
	}
	
	
	//
	// geometric mean  -- using logarithm and exponential
	//
	public static BigDecimal meanGeometric(BigDecimal[] array, int startIndex, int stopIndexPlusOne) {
		return BigDecimalUtil.meanGeometric(array, startIndex, stopIndexPlusOne, o -> o);
	}
	public static BigDecimal meanGeiometric(BigDecimal[] array) {
		return BigDecimalUtil.meanGeometric(array, o -> o);
	}
	
	
	//
	// variance
	//
	public static BigDecimal variance(BigDecimal[] array, int startIndex, int stopIndexPlusOne, BigDecimal mean) {
		return BigDecimalUtil.variance(array, startIndex, stopIndexPlusOne, o -> o);
	}
	public static BigDecimal variance(BigDecimal[] array, BigDecimal mean) {
		return BigDecimalUtil.variance(array, o -> o);
	}
	
		
	//
	// standard deviation
	//
	public static BigDecimal standardDeviation(BigDecimal[] array, int startIndex, int stopIndexPlusOne, BigDecimal mean) {
		return BigDecimalUtil.standardDeviation(array, startIndex, stopIndexPlusOne, mean, o -> o);
	}
	public static BigDecimal standardDeviation(BigDecimal[] array, BigDecimal mean) {
		return BigDecimalUtil.standardDeviation(array, mean, o -> o);
	}
	// without mean
	public static BigDecimal standardDeviation(BigDecimal[] array, int startIndex, int stopIndexPlusOne) {
		BigDecimal mean = mean(array, startIndex, stopIndexPlusOne);
		return BigDecimalUtil.standardDeviation(array, startIndex, stopIndexPlusOne, mean, o -> o);
	}
	public static BigDecimal standardDeviation(BigDecimal[] array) {
		BigDecimal mean = mean(array);
		return BigDecimalUtil.standardDeviation(array, mean, o -> o);
	}
	
	
	private static void test(int[] values) {
		BigDecimal[] array = Arrays.stream(values).mapToObj(o -> BigDecimal.valueOf(o)).toArray(BigDecimal[]::new);
		
		BigDecimal mean = BigDecimalArrays.mean(array);
		BigDecimal var  = BigDecimalArrays.variance(array, mean);
		BigDecimal sd   = BigDecimalArrays.standardDeviation(array, mean);
		
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
