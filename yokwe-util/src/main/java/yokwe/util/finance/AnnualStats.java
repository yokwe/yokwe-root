package yokwe.util.finance;

import java.math.BigDecimal;
import java.math.MathContext;
import java.time.LocalDate;

import yokwe.util.UnexpectedException;

public class AnnualStats {
	private static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();

	public final DailyValue[] dailyValueArray;
	public final int          startIndex;
	public final int          endIndexPlusOne;

	public final LocalDate    startDate;
	public final LocalDate    endDate;
	public final BigDecimal   startValue;
	public final BigDecimal   endValue;
	
	public final BigDecimal   returns;    // from statValue and endValue
	public final BigDecimal   annualReturn;
	
	public final BigDecimal   mean;
	public final BigDecimal   sd;
	
	public AnnualStats(MonthlyStats[] array, int nMonth, MathContext mathContext) {
		if (array == null) {
			logger.error("array == null");
			throw new UnexpectedException("array == null");
		}
		if (nMonth < 6 || array.length < nMonth) {
			logger.error("array.length < nMonth");
			logger.error("  array.length {}", array.length);
			logger.error("  nMonth       {}", nMonth);
			throw new UnexpectedException("array.length < nMonth");
		}
		
		int startIndex      = 0;
		int endIndexPlusOne = nMonth;
		
		MonthlyStats startMonth = array[nMonth - 1];
		MonthlyStats endMonth   = array[0];
		
		this.dailyValueArray = startMonth.dailyValueArray;
		this.startIndex      = startMonth.startIndex;
		this.endIndexPlusOne = endMonth.endIndexPlusOne;
		
		startDate  = startMonth.startDate;
		endDate    = endMonth.endDate;
		startValue = startMonth.startValue;
		endValue   = endMonth.endValue;
		
		returns    = endValue.divide(startValue, mathContext).subtract(BigDecimal.ONE);
		
		{
			BigDecimal[] returnsArray = BigDecimalArrays.toArray(array, startIndex, endIndexPlusOne, o -> o.returns);
			
			{
				BigDecimal value = BigDecimal.ONE;
				for(var e: returnsArray) {
					value = value.multiply(e.add(BigDecimal.ONE), mathContext);
				}
				BigDecimal k = BigDecimal.valueOf(12).divide(BigDecimal.valueOf(nMonth), mathContext);
				annualReturn = BigDecimalUtil.mathPow(value, k).subtract(BigDecimal.ONE);
			}
			
			{
				BigDecimal[] valueArray    = BigDecimalArrays.toArray(this.dailyValueArray, this.startIndex, this.endIndexPlusOne, o -> o.value);
				mean        = BigDecimalArrays.mean(valueArray, mathContext);
				var returnMean = BigDecimalArrays.mean(returnsArray, mathContext);
				sd          = BigDecimalArrays.sd(returnsArray, returnMean, mathContext);

			}
		}
		
	}
}