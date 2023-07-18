package yokwe.util.finance;

import yokwe.util.UnexpectedException;

public final class Stats {
	static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();

	private static void checkStats(Stats a, Stats b) {
		if (a == null) {
			logger.error("a is null");
			throw new UnexpectedException("a is null");
		}
		if (b == null) {
			logger.error("b is null");
			throw new UnexpectedException("b is null");
		}
		if (a.startIndex != b.startIndex) {
			logger.error("startIndex of a and b is different");
			logger.error("  a  {}", a.startIndex);
			logger.error("  b  {}", b.startIndex);
			throw new UnexpectedException("startIndex of a and b is different");
		}
		if (a.stopIndexPlusOne != b.stopIndexPlusOne) {
			logger.error("stopIndexPlusOne of a and b is different");
			logger.error("  a  {}", a.stopIndexPlusOne);
			logger.error("  b  {}", b.stopIndexPlusOne);
			throw new UnexpectedException("stopIndexPlusOne of a and b is different");
		}
		if (a.length != b.length) {
			logger.error("length of a and b is different");
			logger.error("  a  {}", a.length);
			logger.error("  b  {}", b.length);
			throw new UnexpectedException("length of a and b is different");
		}
	}
	
	public static double covariance(Stats a, Stats b) {
		// sanity check
		checkStats(a, b);
		
		double sumAB = 0;
		for(int i = a.startIndex; i < a.stopIndexPlusOne; i++) {
			sumAB += a.data[i] * b.data[i];
		}
		double sumA = a.sum();
		double sumB = b.sum();
		
		// Calculate unbiased value
		// E(a * b) - E(a) * E(b)
		//   sumAB / N - (sumA / N) * (sumB / N)
		// change N to (N - 1)
		//  (sumAB / N - (sumA / N) * (sumB / N)) * (N / (N - 1))
		//  (sumAB     - (sumA    ) * (sumB / N)) / (N - 1)
		//  (sumAB - (sumA * sumB / N)) / (N - 1)
		double N = a.length;
		return (sumAB - (sumA * sumB) / N) / (N - 1);
	}
	public static double correlation(Stats a, Stats b) {
		// sanity check
		checkStats(a, b);

		double covAB = covariance(a, b);
		double sdA   = a.standardDeviation();
		double sdB   = b.standardDeviation();
		// correlation = covariance(a, b) / statndardDeviation(a) * statndardDeviation(b)
		return covAB / (sdA * sdB);
	}
	
	
	
	private final double[] data;
	private final int      startIndex;
	private final int      stopIndexPlusOne;
	private final int      length;
	
	private double sum   = Double.NaN;
	private double sum2  = Double.NaN;
	private double mean  = Double.NaN;
	private double mean2 = Double.NaN;
	private double var   = Double.NaN;
	private double sd    = Double.NaN;
	
	public Stats(double[] data, int startIndex, int stopIndexPlusOne) {
		DoubleArray.checkIndex(data, startIndex, stopIndexPlusOne);
		
		this.data             = data;
		this.startIndex       = startIndex;
		this.stopIndexPlusOne = stopIndexPlusOne;
		this.length           = stopIndexPlusOne - startIndex;
		
		// sanity check
		for(int i = startIndex; i < stopIndexPlusOne; i++) {
			// sanity check
			if (Double.isInfinite(data[i])) {
				logger.error("value is infinite");
				logger.error("  data[{}} = {}", i, Double.toString(data[i]));
				throw new UnexpectedException("value is infinite");
			}
		}
	}
	public Stats(double[] data) {
		this(data, 0, data.length);
	}
	
	public double sum() {
		if (Double.isNaN(sum)) {
			sum = 0;
			for(int i = startIndex; i < stopIndexPlusOne; i++) {
				sum += data[i];
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
			for(int i = startIndex; i < stopIndexPlusOne; i++) {
				double t = data[i];
				sum2 += t * t;
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
			mean = sum() / length;
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
			mean2 = sum2() / length;
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
			double e = mean();
			double tsum = 0;
			for(int i = startIndex; i < stopIndexPlusOne; i++) {
				double t = data[i] - e;
				tsum += t * t;
			}
			// Calculate unbiased value
			var = tsum / (length - 1.0);
			// sanity check
			if (Double.isInfinite(var)) {
				logger.error("var is infinite");
				throw new UnexpectedException("var is infinite");
			}
		}
		return var;
	}
	public double standardDeviation() {
		if (Double.isNaN(sd)) {
			sd = Math.sqrt(variance());
			// sanity check
			if (Double.isInfinite(sd)) {
				logger.error("sd is infinite");
				throw new UnexpectedException("sd is infinite");
			}
		}
		return sd;
	}
	
}
