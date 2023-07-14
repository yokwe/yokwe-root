package yokwe.util.test;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.junit.jupiter.api.Test;

import yokwe.util.finance.DoubleArray;

class TestDoubleArray {
//	public static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();
	
	static double[] table52 = {
		 0.634,
		 0.115,
		-0.460,
		 0.094,
		 0.176,
		-0.088,
		-0.142,
		 0.324,
		-0.943,
		-0.528,
		-0.107,
		-0.160,
		-0.445,
	 	 0.053,
		 0.152,
		-0.318,
		 0.424,
		-0.708,
		-0.105,
		-0.257,
	};
	static double[] table53 = {
			 0.633,
			 0.115,
			-0.459,
			 0.093,
			 0.176,
			-0.087,
			-0.142,
			 0.324,
			-0.943,
			-0.528,
			-0.107,
			-0.159,
			-0.445,
		 	 0.053,
			 0.152,
			-0.318,
			 0.424,
			-0.708,
			-0.105,
			-0.257,
		};
	
	static double[] data  = table52;
	static double[] data2 = DoubleArray.toArray(data, o -> Math.abs(o));
	
	static DescriptiveStatistics stats  = new DescriptiveStatistics(data);
	static DescriptiveStatistics stats2 = new DescriptiveStatistics(data2);
	static double delta = 1e-14;
	
	@Test
	void testSum() {
		double expected = stats.getSum();
		double actual   = DoubleArray.sum(data);
//		logger.info("expected  {}  actual {}", expected, actual);
		assertEquals(expected, actual, delta);
	}
	@Test
	void testMean() {
		double expected = stats.getMean();
		double actual   = DoubleArray.mean(data);
//		logger.info("expected  {}  actual {}", expected, actual);
		assertEquals(expected, actual, delta);
	}
	@Test
	void testGeometricMean() {
		double expected = stats2.getGeometricMean();
		double actual   = DoubleArray.geometricMean(data2);
//		logger.info("expected  {}  actual {}", expected, actual);
		assertEquals(expected, actual, delta);
	}
	@Test
	void testVariance() {
		double expected = stats.getPopulationVariance();
		double actual   = DoubleArray.variance(data);
//		logger.info("expected  {}  actual {}", expected, actual);
		assertEquals(expected, actual, delta);
	}
	@Test
	void testVariance2() {
		double expected = stats.getPopulationVariance();
		double actual   = DoubleArray.variance(data, DoubleArray.mean(data));
//		logger.info("expected  {}  actual {}", expected, actual);
		assertEquals(expected, actual, delta);
	}
	@Test
	void testStandardDeviation() {
		double expected = Math.sqrt(stats.getPopulationVariance());
		double actual   = DoubleArray.standardDeviation(data);
//		logger.info("expected  {}  actual {}", expected, actual);
		assertEquals(expected, actual, delta);
	}
	@Test
	void testStandardDeviation2() {
		double expected = Math.sqrt(stats.getPopulationVariance());
		double actual   = DoubleArray.standardDeviation(data, DoubleArray.mean(data));
//		logger.info("expected  {}  actual {}", expected, actual);
		assertEquals(expected, actual, delta);
	}
	
	@Test
	void testSMA() {
		double[] data = {1, 2, 3, 4, 5, 6};
		double[] a = DoubleArray.sma(data, 3);
		
//		for(int i = 0; i < a.length; i++) {
//			logger.info("a[{}] = {}", i, a[i]);
//		}
		assertEquals(1.0, a[0]);
		assertEquals(1.5, a[1]);
		assertEquals(2.0, a[2]);
		assertEquals(3.0, a[3]);
		assertEquals(4.0, a[4]);
		assertEquals(5.0, a[5]);
	}
	
	@Test
	void testSimpleReturn() {
		double[] data = {1, 2, 3, 6, 3, 6};
		double[] a = DoubleArray.simpleReturn(data);
		
//		for(int i = 0; i < a.length; i++) {
//			logger.info("a[{}] = {}", i, a[i]);
//		}
		assertEquals( 0.0, a[0]);
		assertEquals( 1.0, a[1]);
		assertEquals( 0.5, a[2]);
		assertEquals( 1.0, a[3]);
		assertEquals(-0.5, a[4]);
		assertEquals( 1.0, a[5]);
	}
	
	@Test
	void testLogReturn() {
		double[] data = {1, 2, 3, 6, 3, 6};
		double[] a = DoubleArray.logReturn(data);
		
//		for(int i = 0; i < a.length; i++) {
//			logger.info("a[{}] = {}", i, a[i]);
//		}
		
		double d = 0.01;
		assertEquals( 0.00, a[0], d);
		assertEquals( 0.69, a[1], d);
		assertEquals( 0.40, a[2], d);
		assertEquals( 0.69, a[3], d);
		assertEquals(-0.69, a[4], d);
		assertEquals( 0.69, a[5], d);
	}
}
