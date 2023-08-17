package yokwe.util.test;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Random;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.apache.commons.math3.stat.correlation.Covariance;
import org.apache.commons.math3.stat.correlation.PearsonsCorrelation;
import org.junit.jupiter.api.Test;

import yokwe.util.ClassUtil;
import yokwe.util.finance.Stats;

public class TestStats {
	private static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();
	
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
			
//	@Test
//	void testDummy() {
//		double expected = 1.0;
//		double actual   = 1.0;
//		
//		if (OUTPUT_LOG) logger.info("{}  expected  {}  actual {}", String.format(FORMAT_NAME, ClassUtil.getCallerMethodName()), expected, actual);
//		assertEquals(expected, actual, DOUBLE_DELTA);
//	}

	@Test
	void testSum() {
		DescriptiveStatistics ds    = new DescriptiveStatistics(dataA);
		Stats                 stats = new Stats(dataA);
		
		double expected = ds.getSum();
		double actual   = stats.sum();
		
		if (OUTPUT_LOG) logger.info("{}  expected  {}  actual {}", String.format(FORMAT_NAME, ClassUtil.getCallerMethodName()), expected, actual);
		assertEquals(expected, actual, DOUBLE_DELTA);
	}
	@Test
	void testMean() {
		DescriptiveStatistics ds    = new DescriptiveStatistics(dataA);
		Stats                 stats = new Stats(dataA);
		
		double expected = ds.getMean();
		double actual   = stats.mean();
		
		if (OUTPUT_LOG) logger.info("{}  expected  {}  actual {}", String.format(FORMAT_NAME, ClassUtil.getCallerMethodName()), expected, actual);
		assertEquals(expected, actual, DOUBLE_DELTA);
	}
	@Test
	void testVar() {
		DescriptiveStatistics ds    = new DescriptiveStatistics(dataA);
		Stats                 stats = new Stats(dataA);
		
		double expected = ds.getVariance();
		double actual   = stats.variance();
		
		if (OUTPUT_LOG) logger.info("{}  expected  {}  actual {}", String.format(FORMAT_NAME, ClassUtil.getCallerMethodName()), expected, actual);
		assertEquals(expected, actual, DOUBLE_DELTA);
	}
	@Test
	void testSD() {
		DescriptiveStatistics ds    = new DescriptiveStatistics(dataA);
		Stats                 stats = new Stats(dataA);
		
		double expected = ds.getStandardDeviation();
		double actual   = stats.standardDeviation();
		
		if (OUTPUT_LOG) logger.info("{}  expected  {}  actual {}", String.format(FORMAT_NAME, ClassUtil.getCallerMethodName()), expected, actual);
		assertEquals(expected, actual, DOUBLE_DELTA);
	}
	@Test
	void testCov() {
		Covariance covariance = new Covariance();
		
		Stats statsA = new Stats(dataA);
		Stats statsB = new Stats(dataB);
		
		double expected = covariance.covariance(dataA, dataB);
		double actual   = Stats.covariance(statsA, statsB);
		
		if (OUTPUT_LOG) logger.info("{}  expected  {}  actual {}", String.format(FORMAT_NAME, ClassUtil.getCallerMethodName()), expected, actual);
		assertEquals(expected, actual, DOUBLE_DELTA);
	}
	@Test
	void testCor() {
		PearsonsCorrelation correlation = new PearsonsCorrelation();
		
		Stats statsA = new Stats(dataA);
		Stats statsB = new Stats(dataB);
		
		double expected = correlation.correlation(dataA, dataB);
		double actual   = Stats.correlation(statsA, statsB);
		
		if (OUTPUT_LOG) logger.info("{}  expected  {}  actual {}", String.format(FORMAT_NAME, ClassUtil.getCallerMethodName()), expected, actual);
		assertEquals(expected, actual, DOUBLE_DELTA);
	}
	@Test
	void testSubrange() {
		PearsonsCorrelation correlation = new PearsonsCorrelation();
		
		int startIndex       = 5;
		int stopIndexPlusOne = 105;
		
		int length = stopIndexPlusOne - startIndex;
		double[] subA = new double[length];
		double[] subB = new double[length];
		for(int i = 0; i < length; i++) subA[i] = dataA[startIndex + i];
		for(int i = 0; i < length; i++) subB[i] = dataB[startIndex + i];

		Stats statsA = new Stats(dataA, startIndex, stopIndexPlusOne);
		Stats statsB = new Stats(dataB, startIndex, stopIndexPlusOne);
		
		double expected = correlation.correlation(subA, subB);
		double actual   = Stats.correlation(statsA, statsB);
		
		if (OUTPUT_LOG) logger.info("{}  expected  {}  actual {}", String.format(FORMAT_NAME, ClassUtil.getCallerMethodName()), expected, actual);
		assertEquals(expected, actual, DOUBLE_DELTA);
	}
	@Test
	void testMin() {
		DescriptiveStatistics ds    = new DescriptiveStatistics(dataA);
		Stats                 stats = new Stats(dataA);
		
		double expected = ds.getMin();
		double actual   = stats.min();
		
		if (OUTPUT_LOG) logger.info("{}  expected  {}  actual {}", String.format(FORMAT_NAME, ClassUtil.getCallerMethodName()), expected, actual);
		assertEquals(expected, actual, DOUBLE_DELTA);
	}
	@Test
	void testMax() {
		DescriptiveStatistics ds    = new DescriptiveStatistics(dataA);
		Stats                 stats = new Stats(dataA);
		
		double expected = ds.getMax();
		double actual   = stats.max();
		
		if (OUTPUT_LOG) logger.info("{}  expected  {}  actual {}", String.format(FORMAT_NAME, ClassUtil.getCallerMethodName()), expected, actual);
		assertEquals(expected, actual, DOUBLE_DELTA);
	}

	@Test
	void testGeoMean() {
		DescriptiveStatistics ds    = new DescriptiveStatistics(dataA);
		Stats                 stats = new Stats(dataA);
		
		double expected = ds.getGeometricMean();
		double actual   = stats.geometricMean();
		
		if (OUTPUT_LOG) logger.info("{}  expected  {}  actual {}", String.format(FORMAT_NAME, ClassUtil.getCallerMethodName()), expected, actual);
		assertEquals(expected, actual, DOUBLE_DELTA);
	}

}
