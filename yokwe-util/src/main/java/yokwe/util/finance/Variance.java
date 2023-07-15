package yokwe.util.finance;

import yokwe.util.UnexpectedException;
import yokwe.util.finance.DoubleArray.ToDoubleImpl;

class Variance implements ToDoubleImpl {
	private int    count  = 0;
	private double sum    = 0;
	private double sum2   = 0;
	
	@Override
	public void accept(double value) {
		// sanity check
		if (Double.isInfinite(value)) {
			DoubleArray.logger.error("value is infinite");
			DoubleArray.logger.error("  value {}", Double.toString(value));
			throw new UnexpectedException("value is infinite");
		}
		
		count++;
		
		sum  += value;
		sum2 += value * value;
	}

	@Override
	public double get() {
		if (count == 0) return Double.NaN;
		double ex  = sum  / count;
		double ex2 = sum2 / count;
		// variance = E(X ^ 2) - E(X) ^ 2
		return ex2 - (ex * ex);
	}
}