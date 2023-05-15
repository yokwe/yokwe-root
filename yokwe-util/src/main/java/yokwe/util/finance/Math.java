package yokwe.util.finance;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;

public final class Math {
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
	
	public static class Stats {
		public final BigDecimal mean;
		public final BigDecimal variance;
		public final BigDecimal sd;       // standard deviation
		public final BigDecimal diff[];
		
		public Stats(BigDecimal mean, BigDecimal variance, BigDecimal sd, BigDecimal diff[]) {
			this.mean     = mean;
			this.variance = variance;
			this.sd       = sd;
			this.diff     = diff;
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
		
		return new Stats(mean, variance, sd, diff);
	}
	
}
