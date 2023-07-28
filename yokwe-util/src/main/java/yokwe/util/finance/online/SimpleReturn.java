package yokwe.util.finance.online;

import yokwe.util.UnexpectedException;

public final class SimpleReturn implements OnlineDoubleUnaryOperator {
	public static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();
	
	//
	// annual rate of return
	//
	public static double annualReturn(double absoluteRateOfReturn, double durationInYear) {
		// From https://en.wikipedia.org/wiki/Rate_of_return
		//   According to the CFA Institute's Global Investment Performance Standards (GIPS),[3]
		//   Returns for periods of less than one year must not be annualized.
		if (durationInYear < 1.0) {
			logger.warn("durationInYear is less than one");
			logger.warn("  durationInYear  {}", durationInYear);
//			throw new UnexpectedException("durationInYear is less than one");
		}
		
		return absoluteRateOfReturn / durationInYear;
	}
	public static double compoundAnnualReturn(double simpleReturn, double durationInYear) {
		// From https://en.wikipedia.org/wiki/Rate_of_return
		//   According to the CFA Institute's Global Investment Performance Standards (GIPS),[3]
		//   Returns for periods of less than one year must not be annualized.
		if (durationInYear < 1.0) {
			logger.warn("durationInYear is less than one");
			logger.warn("  durationInYear  {}", durationInYear);
//			throw new UnexpectedException("durationInYear is less than one");
		}
		
		// if simpleReturn is for 3 month, durationInYear is 3 / 12 = 0.25
		// if simpleReturn is for 2 year,  durationInYear is 2
		// ((1 + simpleReturn) ^ (1 / durationInYear)) - 1
		double base     = 1.0 + simpleReturn;
		double exponent = 1.0 / durationInYear;
		return Math.pow(base, exponent) - 1.0;
	}
	
	public static double getValue(double startValue, double endValue) {
		// (endValue - startValue) / startValue
		// (endValue / statValue) - 1
		return (endValue / startValue) - 1;
	}
	
	
	private boolean firstTime    = true;
	private double  lastValue    = 0;
	private double  simpleReturn = Double.NaN;
	
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
			firstTime = false;
			lastValue = value;
		}
		
		simpleReturn = getValue(lastValue, value);
		
		// update for next iteration
		lastValue = value;
	}
	
	@Override
	public double getAsDouble() {
		return simpleReturn;
	}
}
