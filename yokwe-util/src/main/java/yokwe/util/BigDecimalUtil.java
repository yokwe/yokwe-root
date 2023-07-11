package yokwe.util;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;

public final class BigDecimalUtil {
	private static final int          DOUBLE_PRECISION       = 15; // precision of double type
	private static final int          DEFAULT_PRECISION      = DOUBLE_PRECISION;
	private static final RoundingMode DEFAULT_ROUNDING_MODE  = RoundingMode.HALF_EVEN;
	private static final MathContext  DEFAULT_MATH_CONTEXT   = new MathContext(DEFAULT_PRECISION, DEFAULT_ROUNDING_MODE);
	
	public static final BigDecimal MINUS_1  = BigDecimal.valueOf(-1);
	public static final BigDecimal PLUS_100 = BigDecimal.valueOf(100);
	
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
	// mathSqrt -- Math.sqrt
	//
	public static BigDecimal mathSqrt(BigDecimal x) {
		return BigDecimal.valueOf(Math.sqrt(x.doubleValue())).round(DEFAULT_MATH_CONTEXT);
	}

	
	// 
	// ------------------
	// convenience method
	// ------------------
	//
	
	//
	// setScale
	//
	public static BigDecimal setScale(BigDecimal value, int newScale) {
		return value.setScale(newScale, DEFAULT_ROUNDING_MODE);
	}
	
	
	//
	// round
	//
	public static BigDecimal round(BigDecimal value) {
		return value.round(DEFAULT_MATH_CONTEXT);
	}
	
	
	//
	// return a / b
	//
	public static BigDecimal divide(BigDecimal a, BigDecimal b) {
		return a.divide(b, DEFAULT_MATH_CONTEXT);
	}
	
	
	//
	// return a * b
	//
	public static BigDecimal multiply(BigDecimal a, BigDecimal b) {
		return a.multiply(b, DEFAULT_MATH_CONTEXT);
	}
	
	
	//
	// return a + b
	//
	public static BigDecimal add(BigDecimal a, BigDecimal b) {
		return a.add(b, DEFAULT_MATH_CONTEXT);
	}
	
	
	//
	// return a - b
	//
	public static BigDecimal subtract(BigDecimal a, BigDecimal b) {
		return a.subtract(b, DEFAULT_MATH_CONTEXT);
	}
	
	
	//
	// return simple return from previous and price
	//
	public static BigDecimal toSimpleReturn(BigDecimal previous, BigDecimal value) {
		// return (value / previous) - 1
		return divide(value, previous).subtract(BigDecimal.ONE);
	}
	
	
	//
	// return square of value -- value * value
	//
	public static BigDecimal square(BigDecimal value) {
		// return value * value
		return multiply(value, value);
	}
	
}
