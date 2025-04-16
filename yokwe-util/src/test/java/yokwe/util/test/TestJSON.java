package yokwe.util.test;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import yokwe.util.ClassUtil;
import yokwe.util.json.JSON;

public class TestJSON {
	private static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();
	
	private static final boolean OUTPUT_LOG = true;
	
	private static final String FORMAT_NAME = "%-28s";

	@Test
	void testBasic() {
		{
			var clazz = Integer.TYPE;
			var string = "123";
			int expected = 123;
			
			var actual = JSON.unmarshal(clazz, string);
			
			if (OUTPUT_LOG) logger.info("{}  {} - {}  expected  {}  actual {}",
					String.format(FORMAT_NAME, ClassUtil.getCallerMethodName()), clazz.getTypeName(), string, expected, actual);
//			assertEquals(clazz, actual.getClass());
			assertEquals(actual, expected);
		}
	}
}
