package yokwe.util.finance;

import java.util.function.DoubleUnaryOperator;

import yokwe.util.UnexpectedException;

public final class SimpleReturn implements DoubleUnaryOperator {
	private boolean firstTime = true;
	private double  lastValue = 0;
	
	public static double getValue(double startValue, double endValue) {
		// (endValue - startValue) / startValue
		// (endValue / statValue) - 1
		return (endValue / startValue) - 1;
	}
	
	@Override
	public double applyAsDouble(double value) {
		// sanity check
		if (Double.isInfinite(value)) {
			DoubleArray.logger.error("value is infinite");
			DoubleArray.logger.error("  value {}", Double.toString(value));
			throw new UnexpectedException("value is infinite");
		}
		
		if (firstTime) {
			// use first value as previous
			firstTime = false;
			lastValue = value;
		}
		
		double ret = getValue(lastValue, value);
		
		// update for next iteration
		lastValue = value;
		
		return ret;
	}
}
