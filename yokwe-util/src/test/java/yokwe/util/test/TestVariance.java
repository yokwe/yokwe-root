package yokwe.util.test;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Random;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.junit.jupiter.api.Test;

import yokwe.util.ClassUtil;
import yokwe.util.finance.online.Variance;


public class TestVariance {
	public static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();
	
	private static final boolean OUTPUT_LOG = false;
	
	private static final String FORMAT_NAME = "%-28s";
	private static final double DOUBLE_DELTA = 1e-14;
	
	private static final int DATA_SIZE = 100000;
	private static final double[] dataA;
	static {
		Random random = new Random(System.currentTimeMillis());
		dataA = random.doubles(DATA_SIZE).toArray();
	}
	private static DescriptiveStatistics ds       = new DescriptiveStatistics(dataA);
	private static Variance              variance = new Variance();
	static {
		variance.accept(dataA);
	}

	@Test
	void testMean() {
		double expected = ds.getMean();
		double actual   = variance.mean();
		
		if (OUTPUT_LOG) logger.info("{}  expected  {}  actual {}", String.format(FORMAT_NAME, ClassUtil.getCallerMethodName()), expected, actual);
		assertEquals(expected, actual, DOUBLE_DELTA);
	}
	@Test
	void testVariance() {
		double expected = ds.getVariance();
		double actual   = variance.variance();
		
		if (OUTPUT_LOG) logger.info("{}  expected  {}  actual {}", String.format(FORMAT_NAME, ClassUtil.getCallerMethodName()), expected, actual);
		assertEquals(expected, actual, DOUBLE_DELTA);
	}
	@Test
	void testSD() {
		double expected = ds.getStandardDeviation();
		double actual   = variance.standardDeviation();
		
		if (OUTPUT_LOG) logger.info("{}  expected  {}  actual {}", String.format(FORMAT_NAME, ClassUtil.getCallerMethodName()), expected, actual);
		assertEquals(expected, actual, DOUBLE_DELTA);
	}

}
