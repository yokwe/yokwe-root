package yokwe.util.finance;

import yokwe.util.UnexpectedException;
import yokwe.util.finance.DoubleArray.BiDoubleReducer;

public class Correlation implements BiDoubleReducer {
	int    count = 0;
	double sumA  = 0;
	double sumB  = 0;
	double sumAA = 0;
	double sumBB = 0;
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
		sumAA += a * a;
		sumBB += b * b;
		sumAB += a * b;
	}

	@Override
	public double get() {
		double eA  = sumA  / count;
		double eB  = sumB  / count;
		double eAA = sumAA / count;
		double eBB = sumBB / count;
		double eAB = sumAB / count;
		
		double covAB = eAB - eA * eB;
		double varA  = eAA - eA * eA;
		double varB  = eBB - eB * eB;
		
		return covAB / Math.sqrt(varA * varB);
	}

}
