package yokwe.util.finance;

import yokwe.util.UnexpectedException;
import yokwe.util.finance.DoubleArray.DoubleReducer;

class Variance implements DoubleReducer {
	private int    count  = 0;
	private double sumA   = 0;
	private double sumAA  = 0;
	
	@Override
	public void accept(double a) {
		// sanity check
		if (Double.isInfinite(a)) {
			DoubleArray.logger.error("a is infinite");
			DoubleArray.logger.error("  a {}", Double.toString(a));
			throw new UnexpectedException("a is infinite");
		}
		
		count++;
		
		sumA  += a;
		sumAA += a * a;
	}

	@Override
	public double get() {
		if (count == 0) return Double.NaN;
		double eA  = sumA  / count;
		double eAA = sumAA / count;
		// variance = E(a * a) - E(a) * E(a)
		return eAA - eA * eA;
	}
}