package yokwe.util.finance;

import yokwe.util.UnexpectedException;
import yokwe.util.finance.DoubleArray.ToDoubleImpl;

class VarianceUsingMean implements ToDoubleImpl {
	private final double mean;
	
	private int    count  = 0;
	private double sum    = 0;
	
	VarianceUsingMean(double mean_) {
		mean = mean_;
	}
	
	@Override
	public void accept(double value) {
		// sanity check
		if (Double.isInfinite(value)) {
			DoubleArray.logger.error("value is infinite");
			DoubleArray.logger.error("  value {}", Double.toString(value));
			throw new UnexpectedException("value is infinite");
		}
		
		count++;
		
		double t = value - mean;
		sum  += (t * t);
	}

	@Override
	public double get() {
		return count == 0 ? Double.NaN : (sum / count);
	}
}