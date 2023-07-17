package yokwe.util.finance;

import java.util.function.DoubleUnaryOperator;

import yokwe.util.UnexpectedException;

public final class LogReturn implements DoubleUnaryOperator {
	public static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();
	
	//
	// annual rate of return
	//
	public static double annualReturn(double logRetrun, double durationInYear) {
		// From https://en.wikipedia.org/wiki/Rate_of_return
		//   According to the CFA Institute's Global Investment Performance Standards (GIPS),[3]
		//   Returns for periods of less than one year must not be annualized.
		if (durationInYear < 1.0) {
			logger.warn("durationInYear is less than one");
			logger.warn("  durationInYear  {}", durationInYear);
//			throw new UnexpectedException("durationInYear is less than one");
		}
		return logRetrun / durationInYear;
	}
	
	
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
