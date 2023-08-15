package yokwe.util.test;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Random;

import org.apache.commons.math3.stat.correlation.Covariance;
import org.apache.commons.math3.stat.correlation.PearsonsCorrelation;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.junit.jupiter.api.Test;

import yokwe.util.ClassUtil;
import yokwe.util.finance.DoubleArray;

class TestDoubleArray {
	public static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();
	
	private static final boolean OUTPUT_LOG = false;
	
	private static final String FORMAT_NAME = "%-28s";
	private static final double DOUBLE_DELTA = 1e-13;
	
	private static final int DATA_SIZE = 1000;
	private static final double[] dataA;
	private static final double[] dataB;
	static {
		Random random = new Random(System.currentTimeMillis());
		dataA = random.doubles(DATA_SIZE).toArray();
		dataB = random.doubles(DATA_SIZE).toArray();
	}
	

	@Test
	void testStatsCor() {
		PearsonsCorrelation correlation = new PearsonsCorrelation();
		
		double expected = correlation.correlation(dataA, dataB);
		double actual   = DoubleArray.correlation(dataA, dataB);
		
		if (OUTPUT_LOG) logger.info("{}  expected  {}  actual {}", String.format(FORMAT_NAME, ClassUtil.getCallerMethodName()), expected, actual);
		assertEquals(expected, actual, DOUBLE_DELTA);
	}
	@Test
	void testStatsCov() {
		Covariance covariance = new Covariance();
		
		double expected = covariance.covariance(dataA, dataB);
		double actual   = DoubleArray.covariance(dataA, dataB);
		
		if (OUTPUT_LOG) logger.info("{}  expected  {}  actual {}", String.format(FORMAT_NAME, ClassUtil.getCallerMethodName()), expected, actual);
		assertEquals(expected, actual, DOUBLE_DELTA);
	}
	@Test
	void testStatsMean() {
		DescriptiveStatistics ds    = new DescriptiveStatistics(dataA);
		
		double expected = ds.getMean();
		double actual   = DoubleArray.mean(dataA);
		
		if (OUTPUT_LOG) logger.info("{}  expected  {}  actual {}", String.format(FORMAT_NAME, ClassUtil.getCallerMethodName()), expected, actual);
		assertEquals(expected, actual, DOUBLE_DELTA);
	}

}
