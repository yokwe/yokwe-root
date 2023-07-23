package yokwe.util.test;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import yokwe.util.ClassUtil;
import yokwe.util.finance.online.SimpleReturn;

public class TestSimpleReturn {
	public static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();
	
	private static final boolean OUTPUT_LOG = false;

	private static final String FORMAT_NAME = "%-28s";
	private static final double DOUBLE_DELTA = 1e-14;

	@Test
	void testGetValue() {
		double startValue = 2.0;
		double endValue   = 3.0;
				
		double expected = (endValue / startValue) - 1.0;
		double actual   = SimpleReturn.getValue(startValue, endValue);
		if (OUTPUT_LOG) logger.info("{}  expected  {}  actual {}", String.format(FORMAT_NAME, ClassUtil.getCallerMethodName()), expected, actual);
		assertEquals(expected, actual, DOUBLE_DELTA);
	}

}
