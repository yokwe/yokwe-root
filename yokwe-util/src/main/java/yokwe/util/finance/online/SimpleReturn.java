package yokwe.util.finance.online;

import yokwe.util.UnexpectedException;

public final class SimpleReturn implements OnlineDoubleUnaryOperator {
	public static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();
		
	public static final double getValue(double startValue, double endValue) {
		// (endValue - startValue) / startValue
		// (endValue / statValue) - 1
		return (endValue / startValue) - 1;
	}
	
	
	private double  simpleReturn = Double.NaN;
	private boolean hasLastValue;
	private double  lastValue;
	
	public SimpleReturn() {
		this.hasLastValue = false;
	}
	public SimpleReturn(double lastValue) {
		this.hasLastValue = true;
		this.lastValue    = lastValue;
	}
	
	@Override
	public void accept(double value) {
		// sanity check
		if (Double.isInfinite(value)) {
			logger.error("value is infinite");
			logger.error("  value {}", Double.toString(value));
			throw new UnexpectedException("value is infinite");
		}
		
		if (hasLastValue) {
			simpleReturn = getValue(lastValue, value);
		} else {
			// treat value as lastValue
			hasLastValue = true;
			simpleReturn = 0; // simpleReturn(value, value) == 0
		}
		
		// update for next iteration
		lastValue = value;
	}
	
	@Override
	public double getAsDouble() {
		return simpleReturn;
	}
}
