package yokwe.util.stats;

import yokwe.util.UnexpectedException;

public final class BiStats {
	private static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();

	public final UniStats stats1;
	public final UniStats stats2;
	public final double   covariance;
	public final double   correlation;
	
	public BiStats(UniStats stats1, UniStats stats2, double covariance, double correlation) {
		this.stats1 = stats1;
		this.stats2 = stats2;
		this.covariance  = covariance;
		this.correlation = correlation;
	}
	
	public BiStats(double data1[], double data2[]) {
		if (data1.length != data2.length) {
			logger.error("data1.length = {}  data2.length = {}", data1.length, data2.length);
			throw new UnexpectedException("data1.length != data2.length");
		}
		final int size = data1.length;
		
		double mean1 = DoubleArray.mean(data1);
		double mean2 = DoubleArray.mean(data2);
		double cov   = 0;
		double var1  = 0;
		double var2  = 0;
		double diff1[] = new double[size];
		double diff2[] = new double[size];
		for(int i = 0; i < size; i++) {
			double d1 = data1[i] - mean1;
			double d2 = data2[i] - mean2;
			cov  += d1 * d2;
			var1 += d1 * d1;
			var2 += d2 * d2;
			diff1[i] = d1;
			diff2[i] = d2;
		}
		correlation = cov / (Math.sqrt(var1) * Math.sqrt(var2));
		covariance  = cov /size;
		stats1      = new UniStats(size, mean1, var1 / size, diff1);
		stats2      = new UniStats(size, mean2, var2 / size, diff2);
	}
	
	public BiStats(UniStats stats1, UniStats stats2) {
		if (stats1.size != stats2.size) {
			logger.error("stats1.size = {}  stats2.size = {}", stats1.size, stats2.size);
			throw new UnexpectedException("stats1.size != stats2.size");
		}
		final int size = stats1.size;
		this.stats1 = stats1;
		this.stats2 = stats2;
		
		double cov = 0;
		for(int i = 0; i < size; i++) {
			cov  += stats1.diff[i] * stats2.diff[i];
		}
		this.covariance  = cov / size;
		this.correlation = this.covariance / (stats1.sd * stats2.sd);
	}
	
	@Override
	public String toString() {
		return String.format("{%s %s %8.4f %8.4f", stats1, stats2, covariance, correlation);
	}
}