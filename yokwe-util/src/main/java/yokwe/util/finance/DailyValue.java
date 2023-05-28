package yokwe.util.finance;

import java.math.BigDecimal;
import java.math.MathContext;
import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import yokwe.util.StringUtil;
import yokwe.util.UnexpectedException;

public final class DailyValue implements Comparable<DailyValue> {
	private static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();
	
	public static class AnnualStats {
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
	public static class MonthlyStats {
		public final DailyValue[] dailyValueArray;
		public final int          startIndex;
		public final int          endIndexPlusOne;
		
		public final LocalDate    startDate;
		public final LocalDate    endDate;
		
		public final BigDecimal   startValue;
		public final BigDecimal   endValue;
		public final BigDecimal   returns;  // return ratio from previous period
		
		public final BigDecimal   mean;
		public final BigDecimal   sd;
		
		public MonthlyStats(DailyValue[] dailyValueArray, int startIndex, int endIndexPlusOne, MathContext mathContext) {
			this.dailyValueArray = dailyValueArray;
			this.startIndex      = startIndex;
			this.endIndexPlusOne = endIndexPlusOne;
			
			startDate = dailyValueArray[startIndex].date;
			endDate   = dailyValueArray[endIndexPlusOne - 1].date;
			
			// startValue is price of startDate before trade
			startValue = dailyValueArray[startIndex - 1].value;
			endValue   = dailyValueArray[endIndexPlusOne - 1].value;
			// percent change of startValue to endValue
			returns    = endValue.divide(startValue, mathContext).subtract(BigDecimal.ONE);
			
			BigDecimal[] valueArray = toValueArray(dailyValueArray);
			mean = BigDecimalArrays.mean(valueArray, startIndex, endIndexPlusOne, mathContext);
			sd   = BigDecimalArrays.sd(valueArray, startIndex, endIndexPlusOne, mean, mathContext);
		}
	}
	
	public static MonthlyStats[] monthlyStatsArray(DailyValue[] array, int limit, MathContext mathContext) {
		List<MonthlyStats> list = new ArrayList<>();
		
		int[] endIndexArray = monthlyEndIndexArray(array);
		for(int i = 0; i < 5; i++) {
			int endIndex = endIndexArray[i];
			logger.info("endIndexArray[{}]  {}  {}", i, endIndex, array[endIndex].date);
		}
		
		for(int i = 0; i < endIndexArray.length - 2; i++) {
			int endIndexPlusOne = endIndexArray[i] + 1;
			int startIndex      = endIndexArray[i + 1] + 1;
			
			list.add(new MonthlyStats(array, startIndex, endIndexPlusOne, mathContext));
			if (list.size() == limit) break;
		}
		
		return list.toArray(MonthlyStats[]::new);
	}
	
	public static int[] monthlyEndIndexArray(DailyValue[] array) {
		if (array == null) {
			logger.error("array == null");
			throw new UnexpectedException("array == null");
		}
		if (array.length == 0) {
			logger.error("array.length == 0");
			throw new UnexpectedException("array.length == 0");
		}
		
		List<Integer> list = new ArrayList<>((array.length / 12) + 1);
		
		int lastMonthValue = array[0].date.getMonthValue();
		for(int i = 1; i < array.length; i++) {
			int monthValue = array[i].date.getMonthValue();
			if (monthValue == lastMonthValue) continue;
			
			// monthValue has changed, add previous index date and index to list
			int endIndex = i - 1;
			list.add(endIndex);
			// update lastMonthValue for next iteration
			lastMonthValue = monthValue;
		}
		
		// reverse order of list
		Collections.reverse(list);
		
		//   list.get(0) contains endIndex of newest month
		//   list.get(1) contains endIndex of before newest month
		return list.stream().mapToInt(o -> o).toArray();
	}
	
	// value array
	public static BigDecimal[] toValueArray(List<DailyValue> list) {
		return list.stream().map(o -> o.value).toArray(BigDecimal[]::new);
	}
	public static BigDecimal[] toValueArray(DailyValue[] array, int startIndex, int endIndexPlusOne) {
		return BigDecimalArrays.toArray(array, startIndex, endIndexPlusOne, o -> o.value);
	}
	public static BigDecimal[] toValueArray(DailyValue[] array) {
		return toValueArray(array, 0, array.length);
	}
	
	// duration
	public static BigDecimal duration(DailyValue[] array) {
		LocalDate startDate = array[0].date;
		LocalDate endDate  = startDate;
		for(int i = 1; i < array.length; i++) {
			LocalDate date = array[i].date;
			if (date.isBefore(startDate)) startDate = date;
			if (date.isAfter(endDate))   endDate = date;
		}
		
		return duration(startDate, endDate);
	}
	public static BigDecimal duration(LocalDate startDate, LocalDate endDate) {
		Period period = startDate.until(endDate);
		String string = String.format("%d.%02d", period.getYears(), period.getMonths());
		return new BigDecimal(string);
	}
	
	
	public final LocalDate  date;
	public final BigDecimal value;

	public DailyValue(LocalDate date, BigDecimal value) {
		this.date  = date;
		this.value = value;
	}
	
	@Override
	public String toString() {
		return StringUtil.toString(this);
	}
	@Override
	public int compareTo(DailyValue that) {
		return this.date.compareTo(that.date);
	}
	@Override
	public boolean equals(Object o) {
		if (o == null) {
			return false;
		} else {
			if (o instanceof DailyValue) {
				DailyValue that = (DailyValue)o;
				return this.compareTo(that) == 0;
			} else {
				return false;
			}
		}
	}
	@Override
	public int hashCode() {
		return this.date.hashCode();
	}
}
