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
		
		double eA = a.mean();
		double eB = b.mean();
		
		double tsum = 0;
		for(int i = a.startIndex; i < a.stopIndexPlusOne; i++) {
			tsum += (a.data[i] - eA) * (b.data[i] - eB);
		}
		// Calculate unbiased value
		double cov = tsum / (a.length - 1.0);
		// sanity check
		if (Double.isInfinite(cov)) {
			logger.error("var is infinite");
			throw new UnexpectedException("var is infinite");
		}
		return cov;
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
	private double mean  = Double.NaN;
	private double var   = Double.NaN;
	private double sd    = Double.NaN;
	//
	private double min   = Double.NaN;
	private double max   = Double.NaN;
	//
	private double geoMean = Double.NaN;
	
	
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
	public double min() {
		if (Double.isNaN(min)) {
			min = data[startIndex];
			for(int i = startIndex + 1; i < stopIndexPlusOne; i++) {
				double t = data[i];
				if (t < min) min = t;
			}
			// sanity check
			if (Double.isInfinite(min)) {
				logger.error("min is infinite");
				throw new UnexpectedException("min is infinite");
			}
		}
		return min;
	}
	public double max() {
		if (Double.isNaN(max)) {
			max = data[startIndex];
			for(int i = startIndex + 1; i < stopIndexPlusOne; i++) {
				double t = data[i];
				if (max < t) max = t;
			}
			// sanity check
			if (Double.isInfinite(max)) {
				logger.error("max is infinite");
				throw new UnexpectedException("max is infinite");
			}
		}
		return max;
	}
	public double geometricMean() {
		if (Double.isNaN(geoMean)) {
			double t = 0;
			for(int i = startIndex; i < stopIndexPlusOne; i++) {
				double v = data[i];
				if (v < 0) {
					logger.error("negative value");
					throw new UnexpectedException("negative value");
				}
				t += Math.log(v);
			}
			geoMean = Math.exp(t / length);
			// sanity check
			if (Double.isInfinite(geoMean)) {
				logger.error("geoMean is infinite");
				throw new UnexpectedException("geoMean is infinite");
			}
		}
		return geoMean;
	}
}
