package yokwe.util.test;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import yokwe.util.ClassUtil;

class TestDoubleArray {
	public static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();
	
	private static final boolean OUTPUT_LOG = false;

	private static final String FORMAT_NAME = "%-28s";
	
	@Test
	void testAA() {
		String expected = "testAA";
		String actual   = ClassUtil.getCallerMethodName();
		if (OUTPUT_LOG) logger.info("{}  expected  {}  actual {}", String.format(FORMAT_NAME, ClassUtil.getCallerMethodName()), expected, actual);
		assertEquals(expected, actual);
	}
	@Test
	void testAAA() {
		var stackFrame = ClassUtil.getCallerStackFrame();
		
		String expected = "testAAA";
		String actual   = stackFrame.getMethodName();
		if (OUTPUT_LOG) logger.info("{}  expected  {}  actual {}", String.format(FORMAT_NAME, ClassUtil.getCallerMethodName()), expected, actual);
		assertEquals(expected, actual);
	}
	
}
