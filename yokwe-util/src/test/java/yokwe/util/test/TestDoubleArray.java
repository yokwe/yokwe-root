package yokwe.util.test;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.junit.jupiter.api.Test;

import yokwe.util.ClassUtil;
import yokwe.util.finance.DoubleArray;

class TestDoubleArray {
	public static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();
	
	private static final String FORMAT_NAME = "%-28s";
	private static final double DOUBLE_DELTA = 1e-14;
	
	@Test
	void testAA() {
		String expected = "testAA";
		String actual   = ClassUtil.getCallerMethodName();
//		logger.info("{}  expected  {}  actual {}", ClassUtil.getCallerMethodName(), expected, actual);
		assertEquals(expected, actual);
	}
	@Test
	void testAAA() {
		var stackFrame = ClassUtil.getCallerStackFrame();
		
		String expected = "testAAA";
		String actual   = stackFrame.getMethodName();
		logger.info("{}  expected  {}  actual {}", String.format(FORMAT_NAME, ClassUtil.getCallerMethodName()), expected, actual);
		assertEquals(expected, actual);
	}
	
	
	@Test
	void testSum() {
		double data[] = { 1, 2, 3, 4, 5};
		DescriptiveStatistics stats = new DescriptiveStatistics(data);
		
		double expected = stats.getSum();
		double actual   = DoubleArray.sum(data);
		logger.info("{}  expected  {}  actual {}", String.format(FORMAT_NAME, ClassUtil.getCallerMethodName()), expected, actual);
		assertEquals(expected, actual, DOUBLE_DELTA);
	}
	@Test
	void testMean() {
		double data[] = { 1, 2, 3, 4, 5};
		DescriptiveStatistics stats = new DescriptiveStatistics(data);
		
		double expected = stats.getMean();
		double actual   = DoubleArray.mean(data);
		logger.info("{}  expected  {}  actual {}", String.format(FORMAT_NAME, ClassUtil.getCallerMethodName()), expected, actual);
		assertEquals(expected, actual, DOUBLE_DELTA);
	}
	@Test
	void testGeometricMean() {
		double data[] = { 1, 2, 3, 4, 5};
		DescriptiveStatistics stats = new DescriptiveStatistics(data);
		
		double expected = stats.getGeometricMean();
		double actual   = DoubleArray.geometricMean(data);
		logger.info("{}  expected  {}  actual {}", String.format(FORMAT_NAME, ClassUtil.getCallerMethodName()), expected, actual);
		assertEquals(expected, actual, DOUBLE_DELTA);
	}
	@Test
	void testVariance() {
		double data[] = { 1, 2, 3, 4, 5};
		DescriptiveStatistics stats = new DescriptiveStatistics(data);

		double expected = stats.getPopulationVariance();
		double actual   = DoubleArray.variance(data);
		logger.info("{}  expected  {}  actual {}", String.format(FORMAT_NAME, ClassUtil.getCallerMethodName()), expected, actual);
		assertEquals(expected, actual, DOUBLE_DELTA);
	}
	@Test
	void testVariance2() {
		double data[] = { 1, 2, 3, 4, 5};
		DescriptiveStatistics stats = new DescriptiveStatistics(data);

		double expected = stats.getPopulationVariance();
		double actual   = DoubleArray.variance(data, DoubleArray.mean(data));
		logger.info("{}  expected  {}  actual {}", String.format(FORMAT_NAME, ClassUtil.getCallerMethodName()), expected, actual);
		assertEquals(expected, actual, DOUBLE_DELTA);
	}
	@Test
	void testStandardDeviation() {
		double data[] = { 1, 2, 3, 4, 5};
		DescriptiveStatistics stats = new DescriptiveStatistics(data);
		
		double expected = Math.sqrt(stats.getPopulationVariance());
		double actual   = DoubleArray.standardDeviation(data);
		logger.info("{}  expected  {}  actual {}", String.format(FORMAT_NAME, ClassUtil.getCallerMethodName()), expected, actual);
		assertEquals(expected, actual, DOUBLE_DELTA);
	}
	@Test
	void testStandardDeviation2() {
		double data[] = { 1, 2, 3, 4, 5};
		DescriptiveStatistics stats = new DescriptiveStatistics(data);

		double expected = Math.sqrt(stats.getPopulationVariance());
		double actual   = DoubleArray.standardDeviation(data, DoubleArray.mean(data));
		logger.info("{}  expected  {}  actual {}", String.format(FORMAT_NAME, ClassUtil.getCallerMethodName()), expected, actual);
		assertEquals(expected, actual, DOUBLE_DELTA);
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
