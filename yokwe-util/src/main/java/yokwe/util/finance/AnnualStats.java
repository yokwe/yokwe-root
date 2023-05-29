package yokwe.util.finance;

import java.math.BigDecimal;
import java.time.LocalDate;

import yokwe.util.UnexpectedException;

public class AnnualStats {
	private static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();

	public final DailyValue[] reinvestedPriceArray;
	public final DailyValue[] priceArray;
	public final int          startIndex;
	public final int          stopIndexPlusOne;

	public final LocalDate    startDate;
	public final LocalDate    endDate;
	public final BigDecimal   startValue;
	public final BigDecimal   endValue;
	
	public final BigDecimal   returns;    // from statValue and endValue
	public final BigDecimal   annualReturn;
	
	public final BigDecimal   mean;
	public final BigDecimal   sd;
	
	public final BigDecimal   div;
	public final BigDecimal   yield;
	
	
	public AnnualStats(final MonthlyStats[] monthlyStatsArray, final int nYear) {
		final int nMonth = nYear * 12;
		
		if (monthlyStatsArray == null) {
			logger.error("array == null");
			throw new UnexpectedException("array == null");
		}
		if (monthlyStatsArray.length < nMonth) {
			logger.error("array.length < nMonth");
			logger.error("  array.length {}", monthlyStatsArray.length);
			logger.error("  nMonth       {}", nMonth);
			throw new UnexpectedException("array.length < nMonth");
		}
				
		MonthlyStats startMonth = monthlyStatsArray[nMonth - 1];
		MonthlyStats endMonth   = monthlyStatsArray[0];
		
		reinvestedPriceArray = startMonth.reinvestedPriceArray;
		priceArray           = startMonth.priceArray;
		startIndex           = startMonth.startIndex;
		stopIndexPlusOne     = endMonth.stopIndexPlusOne;
		
		startDate  = startMonth.startDate;
		endDate    = endMonth.endDate;
		startValue = startMonth.startValue;
		endValue   = endMonth.endValue;
		
		returns    = endValue.divide(startValue, BigDecimalUtil.DEFAULT_MATH_CONTEXT).subtract(BigDecimal.ONE);
		
		{
			BigDecimal[] array = BigDecimalArrays.toArray(monthlyStatsArray, 0, nMonth, o -> o.returns);
			BigDecimal value = BigDecimal.ONE;
			for(var e: array) {
				value = value.multiply(e.add(BigDecimal.ONE), BigDecimalUtil.DEFAULT_MATH_CONTEXT);
			}
			BigDecimal k = BigDecimal.valueOf(12).divide(BigDecimal.valueOf(nMonth), BigDecimalUtil.DEFAULT_MATH_CONTEXT);
			annualReturn = BigDecimalUtil.mathPow(value, k).subtract(BigDecimal.ONE);
		}
		
		mean  = BigDecimalArrays.geometricMean(monthlyStatsArray, o -> o.mean);
		sd    = BigDecimalArrays.sd(monthlyStatsArray, o -> o.sd);
		div   = BigDecimalArrays.sum(monthlyStatsArray, o -> o.div);
		yield = div.divide(endValue.multiply(BigDecimal.valueOf(nYear))); // FIXME annualization		
	}
}