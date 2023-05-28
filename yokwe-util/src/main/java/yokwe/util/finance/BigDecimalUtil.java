package yokwe.util.finance;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;

public final class BigDecimalUtil {
	public static final int          DOUBLE_PRECISION       = 15; // precision of double type
	public static final int          DEFAULT_PRECISION      = DOUBLE_PRECISION;
	public static final RoundingMode DEFAULT_ROUNDING_MODE  = RoundingMode.HALF_EVEN;
	public static final MathContext  DEFAULT_MATH_CONTEXT   = new MathContext(DEFAULT_PRECISION, DEFAULT_ROUNDING_MODE);
	
	// return log value using Math.log
	public static BigDecimal mathLog(BigDecimal x) {
		return BigDecimal.valueOf(Math.log(x.doubleValue()));
	}
	public static BigDecimal mathExp(BigDecimal x) {
		return BigDecimal.valueOf(Math.exp(x.doubleValue()));
	}
	public static BigDecimal mathPow(BigDecimal x, BigDecimal y) {
		return BigDecimal.valueOf(Math.pow(x.doubleValue(), y.doubleValue()));
	}
	
	public static BigDecimal annualizedRetrun(BigDecimal first, BigDecimal last, int years) {
		// annualized return = (((last / first) ^ (1 / years)) - 1
		double power = 1.0 / years;
		double absoluteReturn = last.doubleValue() / first.doubleValue();
		double annualizedReturn = Math.pow(absoluteReturn, power) - 1.0;
		return BigDecimal.valueOf(annualizedReturn).round(DEFAULT_MATH_CONTEXT);
	}
}
