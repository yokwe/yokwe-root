package yokwe.util.test;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;

import yokwe.util.ClassUtil;
import yokwe.util.finance.Finance;

public class TestFinance {
	public static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();
	
	private static final boolean OUTPUT_LOG = false;
	
	private static final String FORMAT_NAME = "%-28s";
	private static final double DOUBLE_DELTA = 1e-14;

	@Test
	void testDurationInYear() {
		{
			LocalDate startDate = LocalDate.of(2020,  1,  1);
			LocalDate endDate   = LocalDate.of(2020, 12, 31);
			
			double expected = 1.0;
			double actual   = Finance.durationInYear(startDate, endDate);
			
			if (OUTPUT_LOG) logger.info("{}  {} - {}  expected  {}  actual {}", String.format(FORMAT_NAME, ClassUtil.getCallerMethodName()), startDate, endDate, expected, actual);
			assertEquals(expected, actual, DOUBLE_DELTA);
		}
		{
			LocalDate startDate = LocalDate.of(2021,  1,  1);
			LocalDate endDate   = LocalDate.of(2021, 12, 31);
			
			double expected = 1.0;
			double actual   = Finance.durationInYear(startDate, endDate);
			
			if (OUTPUT_LOG) logger.info("{}  {} - {}  expected  {}  actual {}", String.format(FORMAT_NAME, ClassUtil.getCallerMethodName()), startDate, endDate, expected, actual);
			assertEquals(expected, actual, DOUBLE_DELTA);
		}
		{
			LocalDate startDate = LocalDate.of(2020, 12,  2);
			LocalDate endDate   = LocalDate.of(2021, 12,  1);
			
			double expected = 1.0;
			double actual   = Finance.durationInYear(startDate, endDate);
			
			if (OUTPUT_LOG) logger.info("{}  {} - {}  expected  {}  actual {}", String.format(FORMAT_NAME, ClassUtil.getCallerMethodName()), startDate, endDate, expected, actual);
			assertEquals(expected, actual, DOUBLE_DELTA);
		}
		{
			LocalDate startDate = LocalDate.of(2020, 1,  1);
			LocalDate endDate   = LocalDate.of(2020, 1,  1);
			
			double expected = Finance.DURATION_PER_DAY;
			double actual   = Finance.durationInYear(startDate, endDate);
			
			if (OUTPUT_LOG) logger.info("{}  {} - {}  expected  {}  actual {}", String.format(FORMAT_NAME, ClassUtil.getCallerMethodName()), startDate, endDate, expected, actual);
			assertEquals(expected, actual, DOUBLE_DELTA);
		}

	}

}
