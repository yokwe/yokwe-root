package yokwe.util.test;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.junit.jupiter.api.Test;

import yokwe.util.ClassUtil;
import yokwe.util.finance.Finance;

public class TestFinance {
	private static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();
	
	private static final boolean OUTPUT_LOG = false;
	
	private static final String FORMAT_NAME = "%-28s";

	@Test
	void testDurationInYear() {
		{
			LocalDate startDate = LocalDate.of(2020,  1,  1);
			LocalDate endDate   = LocalDate.of(2020, 12, 31);
			
			BigDecimal expected = new BigDecimal("1.00");
			BigDecimal actual   = Finance.durationInYearMonth(startDate, endDate);
			
			if (OUTPUT_LOG) logger.info("{}  {} - {}  expected  {}  actual {}", String.format(FORMAT_NAME, ClassUtil.getCallerMethodName()), startDate, endDate, expected, actual);
			assertEquals(expected, actual);
		}
		{
			LocalDate startDate = LocalDate.of(2021,  1,  1);
			LocalDate endDate   = LocalDate.of(2021, 12, 31);
			
			BigDecimal expected = new BigDecimal("1.00");
			BigDecimal actual   = Finance.durationInYearMonth(startDate, endDate);
			
			if (OUTPUT_LOG) logger.info("{}  {} - {}  expected  {}  actual {}", String.format(FORMAT_NAME, ClassUtil.getCallerMethodName()), startDate, endDate, expected, actual);
			assertEquals(expected, actual);
		}
		{
			LocalDate startDate = LocalDate.of(2020, 12,  2);
			LocalDate endDate   = LocalDate.of(2021, 12,  1);
			
			BigDecimal expected = new BigDecimal("1.00");
			BigDecimal actual   = Finance.durationInYearMonth(startDate, endDate);
			
			if (OUTPUT_LOG) logger.info("{}  {} - {}  expected  {}  actual {}", String.format(FORMAT_NAME, ClassUtil.getCallerMethodName()), startDate, endDate, expected, actual);
			assertEquals(expected, actual);
		}
		{
			LocalDate startDate = LocalDate.of(2020, 1,  1);
			LocalDate endDate   = LocalDate.of(2020, 1,  1);
			
			BigDecimal expected = new BigDecimal("0.00");
			BigDecimal actual   = Finance.durationInYearMonth(startDate, endDate);
			
			if (OUTPUT_LOG) logger.info("{}  {} - {}  expected  {}  actual {}", String.format(FORMAT_NAME, ClassUtil.getCallerMethodName()), startDate, endDate, expected, actual);
			assertEquals(expected, actual);
		}

	}

}
