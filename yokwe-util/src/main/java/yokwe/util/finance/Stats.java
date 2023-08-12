package yokwe.util.finance;

import yokwe.util.UnexpectedException;
import yokwe.util.finance.online.Covariance;
import yokwe.util.finance.online.GeometricMean;
import yokwe.util.finance.online.Max;
import yokwe.util.finance.online.Mean;
import yokwe.util.finance.online.Min;
import yokwe.util.finance.online.Sum;
import yokwe.util.finance.online.Variance;

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
		
		var op = new Covariance();
		double cov = op.applyAsDouble(a.data, b.data, a.startIndex, a.stopIndexPlusOne);
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

		double cov = covariance(a, b);
		double sdA = a.standardDeviation();
		double sdB = b.standardDeviation();
		// correlation = covariance(a, b) / statndardDeviation(a) * statndardDeviation(b)
		return cov / (sdA * sdB);
	}
	
	public static double sum(double[] data, int startIndex, int stopIndexPlusOne) {
		var op = new Sum();
		op.accept(data, startIndex, stopIndexPlusOne);
		return op.getAsDouble();
	}
	public static double standardDeviation(double[] data, int startIndex, int stopIndexPlusOne) {
		var op = new Variance();
		op.accept(data, startIndex, stopIndexPlusOne);
		return op.standardDeviation();
	}
	public static double standardDeviation(double[] data) {
		return standardDeviation(data, 0, data.length);
	}
	
	public static double variance(Stats[] statsArray, double[] weightArray) {
		// sanity check
		{
			if (statsArray == null) {
				logger.error("dataArray is null");
				throw new UnexpectedException("dataArray is null");
			}
			if (weightArray == null) {
				logger.error("weightArray is null");
				throw new UnexpectedException("weightArray is null");
			}
			if (statsArray.length == 0) {
				logger.error("dataArray.length == 0");
				throw new UnexpectedException("dataArray.length == 0");
			}
			if (weightArray.length == 0) {
				logger.error("weightArray.length == 0");
				throw new UnexpectedException("weightArray.length == 0");
			}
			if (statsArray.length != weightArray.length) {
				logger.error("Unexpected length");
				logger.error("  statsArray   {}", statsArray.length);
				logger.error("  weightArray  {}", weightArray.length);
				throw new UnexpectedException("Unexpected length");
			}
			int length = statsArray[0].length;
			for(int i = 0; i < statsArray.length; i++) {
				if (statsArray[i].length != length) {
					logger.error("Unexpected length");
					logger.error("  length                {}", length);
					logger.error("  statsArray[{}].length  {}", i, statsArray[i].length);
					throw new UnexpectedException("Unexpected length");
				}
			}
		}
		
		double variance = 0;
		int length = statsArray.length;
		
		// diagonal element
		for(int i = 0; i < length; i++) {
			// w1^2σ1^2 + w2^2σ2^2 + 2w1w2Cov1,2
			double w = weightArray[i];
			double v = statsArray[i].variance(); // sd = sqrt(variance) => sd ^ 2 = variance
			variance += (w * w) * v;
		}
		// not diagonal element
		for(int i = 0; i < length; i++) {
			for(int j = 0; j < length; j++) {
				if (i == j) break;
				
				double w1 = weightArray[i];
				double w2 = weightArray[j];
				Stats  s1 = statsArray[i];
				Stats  s2 = statsArray[j];
				double cov = covariance(s1, s2);
				
				variance += 2 * w1 * w2 * cov;
			}
		}
		
		return variance;
	}
	public static double standardDeviation(Stats[] statsArray, double[] weightArray) {
		return Math.sqrt(variance(statsArray, weightArray));
	}
	
	public static double[] standardMeasure(double[] data) {
		Stats stats = new Stats(data);
		double sd = stats.standardDeviation();
		double mean = stats.mean();
		
		return DoubleArray.toDoubleArray(data, o -> (o - mean) / sd);
	}
	
	public static double mean(double[] data) {
		Stats stats = new Stats(data);
		return stats.mean();
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
		Util.checkIndex(data, startIndex, stopIndexPlusOne);
		
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
			var op = new Sum();
			sum = op.applyAsDouble(data, startIndex, stopIndexPlusOne);
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
			var op = new Mean();
			mean = op.applyAsDouble(data, startIndex, stopIndexPlusOne);
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
			var op = new Variance();
			var = op.applyAsDouble(data, startIndex, stopIndexPlusOne);
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
			var op = new Min();
			min = op.applyAsDouble(data, startIndex, stopIndexPlusOne);
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
			var op = new Max();
			max = op.applyAsDouble(data, startIndex, stopIndexPlusOne);
			// sanity check
			if (Double.isInfinite(max)) {
				logger.error("max is infinite");
				throw new UnexpectedException("mean is infinite");
			}
		}
		return max;
	}
	public double geometricMean() {
		if (Double.isNaN(geoMean)) {
			var op = new GeometricMean();
			geoMean = op.applyAsDouble(data, startIndex, stopIndexPlusOne);
			// sanity check
			if (Double.isInfinite(geoMean)) {
				logger.error("geoMean is infinite");
				throw new UnexpectedException("geoMean is infinite");
			}
		}
		return geoMean;
	}
}
