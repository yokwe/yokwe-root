package yokwe.util.finance.online;

import yokwe.util.UnexpectedException;

public final class LogReturn implements OnlineDoubleUnaryOperator {
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
	
	public LogReturn() {}
	
	private boolean firstTime    = true;
	private double  lastLogValue = 0;
	private double  logRun;
	
	@Override
	public void accept(double value) {
		// sanity check
		if (Double.isInfinite(value)) {
			logger.error("value is infinite");
			logger.error("  value {}", Double.toString(value));
			throw new UnexpectedException("value is infinite");
		}

		if (firstTime) {
			// use first value as previous
			firstTime   = false;
			lastLogValue = Math.log(value);
		}
		
		double logValue = Math.log(value);
		
		// save logRun for later use
		logRun = logValue - lastLogValue;
		
		// update for next iteration
		lastLogValue = logValue;
	}
	
	@Override
	public double getAsDouble() {
		return logRun;
	}
}
