package yokwe.util.finance;

import yokwe.util.UnexpectedException;

public class Stats {
	static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();

	private static double covariance(Stats a, Stats b) {		
		final int length = a.length;
		
		double sumAB = 0;
		for(int i = 0; i < length; i++) {
			sumAB += a.data[i] * b.data[i];
		}
		
		double eAB  = sumAB / length;
		double eA   = a.mean();
		double eB   = b.mean();
		// covariance = E(a * b) - E(a) * E(b)
		return eAB - (eA * eB);
	}
	
	public static double correlation(Stats a, Stats b) {
		// sanity check
		if (a == null) {
			logger.error("a is null");
			throw new UnexpectedException("a is null");
		}
		if (b == null) {
			logger.error("b is null");
			throw new UnexpectedException("b is null");
		}
		if (a.length != b.length) {
			logger.error("length of a and b is different");
			logger.error("  a  {}", a.length);
			logger.error("  b  {}", b.length);
			throw new UnexpectedException("length of a and b is different");
		}

		double covAB = covariance(a, b);
		double sdA   = a.statndardDeviation();
		double sdB   = b.statndardDeviation();
		// correlation = covariance(a, b) / statndardDeviation(a) * statndardDeviation(b)
		return covAB / (sdA * sdB);
	}
	
	
	
	private final double[] data;
	private final int      length;
	
	private double sum   = Double.NaN;
	private double sum2  = Double.NaN;
	private double mean  = Double.NaN;
	private double mean2 = Double.NaN;
	private double var   = Double.NaN;
	private double sd    = Double.NaN;
	
	public Stats(double[] data_, int startIndex, int stopIndexPlusOne) {
		DoubleArray.checkIndex(data_, startIndex, stopIndexPlusOne);
		
		length = stopIndexPlusOne - startIndex;
		data = new double[length];
		for(int i = 0, j = startIndex; i < length; i++, j++) {
			double value = data_[j];
			// sanity check
			if (Double.isInfinite(value)) {
				logger.error("value is infinite");
				logger.error("  data[{}} = {}", j, Double.toString(value));
				throw new UnexpectedException("value is infinite");
			}

			data[i] = data_[j];
		}
	}
	public Stats(double[] data) {
		this(data, 0, data.length);
	}
	
	public double sum() {
		if (Double.isNaN(sum)) {
			sum = 0;
			for(var e: data) {
				sum += e;
			}
			// sanity check
			if (Double.isInfinite(sum)) {
				logger.error("sum is infinite");
				throw new UnexpectedException("sum is infinite");
			}
		}
		return sum;
	}
	public double sum2() {
		if (Double.isNaN(sum2)) {
			sum2 = 0;
			for(var e: data) {
				sum2 += e * e;
			}
			// sanity check
			if (Double.isInfinite(sum2)) {
				logger.error("sum2 is infinite");
				throw new UnexpectedException("sum2 is infinite");
			}
		}
		return sum2;
	}
	public double mean() {
		if (Double.isNaN(mean)) {
			sum();
			//
			mean = sum / length;
			// sanity check
			if (Double.isInfinite(mean)) {
				logger.error("mean is infinite");
				throw new UnexpectedException("mean is infinite");
			}
		}
		return mean;
	}
	public double mean2() {
		if (Double.isNaN(mean2)) {
			sum2();
			//
			mean2 = sum2 / length;
			// sanity check
			if (Double.isInfinite(mean2)) {
				logger.error("mean2 is infinite");
				throw new UnexpectedException("mean2 is infinite");
			}
		}
		return mean2;
	}
	public double variance() {
		if (Double.isNaN(var)) {
			mean();
			//
			var = 0;
			for(var e: data) {
				double t = e - mean;
				var += t * t;
			}
			var = var / length;
			// sanity check
			if (Double.isInfinite(var)) {
				logger.error("var is infinite");
				throw new UnexpectedException("var is infinite");
			}
		}
		return var;
	}
	public double statndardDeviation() {
		if (Double.isNaN(sd)) {
			variance();
			//
			sd = Math.sqrt(var);
			// sanity check
			if (Double.isInfinite(sd)) {
				logger.error("sd is infinite");
				throw new UnexpectedException("sd is infinite");
			}
		}
		return sd;
	}
	
}
