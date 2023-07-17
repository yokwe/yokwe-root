package yokwe.util.finance;

import yokwe.util.UnexpectedException;
import yokwe.util.finance.DoubleArray.BiDoubleReducer;

public class Covariance implements BiDoubleReducer{
	// Covariance(a, b) = E(a x b) - E(a) x E(b)
	
	int    count = 0;
	double sumA  = 0;
	double sumB  = 0;
	double sumAB = 0;
	
	@Override
	public void accept(double a, double b) {
		// sanity check
		if (Double.isInfinite(a)) {
			DoubleArray.logger.error("a is infinite");
			DoubleArray.logger.error("  a {}", Double.toString(a));
			throw new UnexpectedException("a is infinite");
		}
		if (Double.isInfinite(b)) {
			DoubleArray.logger.error("b is infinite");
			DoubleArray.logger.error("  b {}", Double.toString(b));
			throw new UnexpectedException("b is infinite");
		}
		
		count++;

		sumA  += a;
		sumB  += b;
		sumAB += a * b;
	}

	@Override
	public double get() {
		if (count == 0) return Double.NaN;
		double eA   = sumA  / count;
		double eB   = sumB  / count;
		double eAB  = sumAB / count;
		// covariance = E(a * b) - E(a) * E(b)
		return eAB - (eA * eB);
	}

}
