package yokwe.util;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;

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
	// return simple return from previous and price
	//
	public static BigDecimal toSimpleReturn(BigDecimal previous, BigDecimal value) {
		// return (value / previous) - 1
		return value.divide(previous, DEFAULT_MATH_CONTEXT).subtract(BigDecimal.ONE);
	}
	
	
	//
	// return square of value -- value * value
	//
	public static BigDecimal square(BigDecimal value) {
		// return value * value
		return value.multiply(value, DEFAULT_MATH_CONTEXT);
	}
	
}
