package yokwe.util.finance;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;

import yokwe.util.StringUtil;

public final class Finance {
	public static final int          DEFAULT_INTERNAL_SCALE = 15;
	public static final RoundingMode DEFAULT_ROUNDING_MODE  = RoundingMode.HALF_UP;
	public static final MathContext  DEFAULT_MATH_CONTEXT   = new MathContext(DEFAULT_INTERNAL_SCALE, DEFAULT_ROUNDING_MODE);
	
	public static BigDecimal sum(BigDecimal[] values, MathContext mathContext) {
		BigDecimal ret = BigDecimal.ZERO;
		for(var e: values) {
			ret = ret.add(e);
		}
		return ret.round(mathContext);
	}
	public static BigDecimal mean(BigDecimal[] values, MathContext mathContext) {
		return sum(values, mathContext).divide(BigDecimal.valueOf(values.length), mathContext);
	}
	
	public static BigDecimal annualizedRetrun(BigDecimal first, BigDecimal last, int years) {
		// annualized return = (((last / first) ^ (1 / years)) - 1
		double power = 1.0 / years;
		double absoluteReturn = last.doubleValue() / first.doubleValue();
		double annualizedReturn = Math.pow(absoluteReturn, power) - 1.0;
		return BigDecimal.valueOf(annualizedReturn).round(DEFAULT_MATH_CONTEXT);
	}
	
	
	public static class Stats {
		public final BigDecimal mean;
		public final BigDecimal variance;
		public final BigDecimal sd;       // Standard Deviation
		public final BigDecimal cv;       // Coefficient of Variation
		public final BigDecimal diff[];
		
		public Stats(BigDecimal mean, BigDecimal variance, BigDecimal sd, BigDecimal cv, BigDecimal diff[]) {
			this.mean     = mean;
			this.variance = variance;
			this.sd       = sd;
			this.cv       = cv;
			this.diff     = diff;
		}
		
		@Override
		public String toString() {
			return StringUtil.toString(this);
		}
	}
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
		
		MathContext mathContext = new MathContext(4, DEFAULT_ROUNDING_MODE);
		Stats stats = stats(valueArray, mathContext);
		logger.info("stats  {}  {}  {}", stats.mean, stats.sd, stats.cv);
		
		logger.info("STOP");
	}
}
