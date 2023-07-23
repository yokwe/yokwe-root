package yokwe.util.finance.online;

import yokwe.util.UnexpectedException;

public class Sum implements OnlineDoubleUnaryOperator {
	private static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();
	
	public static double apply(double[] value) {
		var op = new Sum();
		return op.applyAsDouble(value);
	}
	
	
	private double sum = 0;
	public Sum() {}
	
	@Override
	public void accept(double value) {
		// sanity check
		if (Double.isInfinite(value)) {
			logger.error("value is infinite");
			logger.error("  value {}", Double.toString(value));
			throw new UnexpectedException("value is infinite");
		}

		sum += value;
	}
	
	@Override
	public double getAsDouble() {
		return sum;
	}
}
