package yokwe.util.finance;

import java.util.function.DoubleUnaryOperator;

import yokwe.util.UnexpectedException;

public final class LogReturn implements DoubleUnaryOperator {
	private boolean firstTime    = true;
	private double  lastLogValue = 0;
	
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
			firstTime   = false;
			lastLogValue = Math.log(value);
		}
		
		double logValue = Math.log(value);
		double ret      = logValue - lastLogValue;
		
		// update for next iteration
		lastLogValue = logValue;
		
		return ret;
	}
}
