package yokwe.util.finance.online;

import yokwe.util.UnexpectedException;

public final class Mean implements OnlineDoubleUnaryOperator {	
	private static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();
	
	private boolean firstTime = true;
	private int     count     = 0;
	private double  lastMean  = Double.NaN;
	
	@Override
	public double getAsDouble() {
		return lastMean;
	}
	
	// method for SMA
	// replace oldValue with value and update lastMean
	public double replace(double oldValue, double value) {
		// ((lastMean * count) - oldValue + value) / count
		// lastMean + (value - oldValue) / count
		lastMean += (value - oldValue) / count;
		return lastMean;
	}

	@Override
	public void accept(double value) {
		// sanity check
		if (Double.isInfinite(value)) {
			logger.error("value is infinite");
			logger.error("  value {}", Double.toString(value));
			throw new UnexpectedException("value is infinite");
		}

		count++;
		
		final double mean;
		{
			if (firstTime) {
				mean      = value;
				firstTime = false;
			} else {
				mean = lastMean + (value - lastMean) / count;
			}
		}
		
		// update for next iteration
		lastMean = mean;
	}

}
