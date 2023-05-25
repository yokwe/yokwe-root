package yokwe.util.finance;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;

public final class BigDecimalUtil {
	public static final int          DEFAULT_INTERNAL_SCALE = 15;
	public static final RoundingMode DEFAULT_ROUNDING_MODE  = RoundingMode.HALF_UP;
	public static final MathContext  DEFAULT_MATH_CONTEXT   = new MathContext(DEFAULT_INTERNAL_SCALE, DEFAULT_ROUNDING_MODE);
	
	public static BigDecimal sum(BigDecimal[] values) {
		BigDecimal ret = BigDecimal.ZERO;
		for(var e: values) {
			ret = ret.add(e);
		}
		return ret;
	}
	public static BigDecimal mean(BigDecimal[] values, MathContext mathContext) {
		return sum(values).divide(BigDecimal.valueOf(values.length), mathContext);
	}
	
	public static BigDecimal sd(BigDecimal[] values, BigDecimal mean, MathContext mathContext) {
		BigDecimal var = BigDecimal.ZERO;
		for(var e: values) {
			BigDecimal diff = mean.subtract(e);
			var = var.add(diff.multiply(diff, mathContext));
		}
		// variance
		BigDecimal variance = var.divide(BigDecimal.valueOf(values.length), mathContext);
		
		// standard deviation
		BigDecimal sd       = variance.sqrt(mathContext);
		
		return sd;
	}
	public static BigDecimal sd(BigDecimal[] values, MathContext mathContext) {
		return sd(values, mean(values, mathContext), mathContext);
	}

	public static BigDecimal log(BigDecimal x, int scale) {
		return BigFunctions.ln(x, scale);
	}	
	
	
	public static BigDecimal annualizedRetrun(BigDecimal first, BigDecimal last, int years) {
		// annualized return = (((last / first) ^ (1 / years)) - 1
		double power = 1.0 / years;
		double absoluteReturn = last.doubleValue() / first.doubleValue();
		double annualizedReturn = Math.pow(absoluteReturn, power) - 1.0;
		return BigDecimal.valueOf(annualizedReturn).round(DEFAULT_MATH_CONTEXT);
	}
	

	public record Stats (
		BigDecimal   mean,
		BigDecimal   variance,
		BigDecimal   sd,       // Standard Deviation
		BigDecimal   cv,       // Coefficient of Variation
		BigDecimal[] diff) {}

	public static Stats stats(BigDecimal[] values, MathContext mathContext) {
		int        size   = values.length;
		BigDecimal mean   = mean(values, mathContext);
		BigDecimal diff[] = new BigDecimal[size];
		
		BigDecimal var = BigDecimal.ZERO;
		for(int i = 0; i < size; i++) {
			BigDecimal diffi = mean.subtract(values[i], mathContext);
			var = var.add(diffi.multiply(diffi), mathContext);
			diff[i] = diffi;
		}
		BigDecimal variance = var.divide(BigDecimal.valueOf(size), mathContext);
		BigDecimal sd       = variance.sqrt(mathContext);
		BigDecimal cv       = sd.divide(mean, mathContext);
		
		return new Stats(mean, variance, sd, cv, diff);
	}

	
	public static void main(String[] args) {
		final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();

		logger.info("START");
		
		int[] intArray = {14, 2, 13, 20, 16};
		BigDecimal[] valueArray = new BigDecimal[intArray.length];
		for(int i = 0; i < intArray.length; i++) {
			valueArray[i] = BigDecimal.valueOf(intArray[i]);
		}
		
		MathContext mathContext = new MathContext(4, BigDecimalUtil.DEFAULT_ROUNDING_MODE);
		BigDecimalUtil.Stats stats = BigDecimalUtil.stats(valueArray, mathContext);
		logger.info("stats  {}  {}  {}", stats.mean(), stats.sd(), stats.cv());
		
		logger.info("STOP");
	}
}
