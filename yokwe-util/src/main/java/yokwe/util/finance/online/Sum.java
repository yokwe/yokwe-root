package yokwe.util.finance.online;

import yokwe.util.UnexpectedException;

public class Sum implements OnlineDoubleUnaryOperator {
	private static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();
	
	private boolean firstTime = true;
	private double  sum       = Double.NaN;
	
	@Override
	public void accept(double value) {
		// sanity check
		if (Double.isInfinite(value)) {
			logger.error("value is infinite");
			logger.error("  value {}", Double.toString(value));
			throw new UnexpectedException("value is infinite");
		}
		
		if (firstTime) {
			sum       = value;
			firstTime = false;
		} else {
			sum += value;
		}
	}
	
	@Override
	public double getAsDouble() {
		return sum;
	}
}
