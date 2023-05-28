package yokwe.util.finance;

import java.math.BigDecimal;
import java.math.MathContext;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import yokwe.util.UnexpectedException;

public class MonthlyStats {
	private static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();

	public static MonthlyStats[] monthlyStatsArray(DailyValue[] array, int limit, MathContext mathContext) {
		// array of index that point to stopIndexPlusOne of each month
		// stopIndexPlusOneArray[0] contains stopIndexPlusOne of newest month
		int[] stopIndexPlusOneArray;
		{			
			List<Integer> list = new ArrayList<>((array.length / 12) + 1);
			int lastMonthValue = array[0].date.getMonthValue();
			for(int i = 1; i < array.length; i++) {
				int monthValue = array[i].date.getMonthValue();
				if (monthValue == lastMonthValue) continue;
				
				// monthValue has changed, add previous index date and index to list
				int stopIndexPlusOne = i;
				list.add(stopIndexPlusOne);
				// update lastMonthValue for next iteration
				lastMonthValue = monthValue;
			}
			
			// reverse order of list
			Collections.reverse(list);
			// list to array
			stopIndexPlusOneArray = list.stream().mapToInt(o -> o).toArray();
		}
		
		List<MonthlyStats> list = new ArrayList<>();
		for(int i = 0; i < stopIndexPlusOneArray.length - 2; i++) {
			int stopIndexPlusOne = stopIndexPlusOneArray[i];
			int startIndex       = stopIndexPlusOneArray[i + 1];
			
			list.add(new MonthlyStats(array, startIndex, stopIndexPlusOne, mathContext));
			// needs only limit entries
			if (list.size() == limit) break;
		}
		
		return list.toArray(MonthlyStats[]::new);
	}
	

	public final DailyValue[] dailyValueArray;
	public final int          startIndex;
	public final int          stopIndexPlusOne;
	
	public final LocalDate    startDate;
	public final LocalDate    endDate;
	
	public final BigDecimal   startValue;
	public final BigDecimal   endValue;
	public final BigDecimal   returns;  // return ratio from previous period
	
	public final BigDecimal   mean;
	public final BigDecimal   sd;
	
	public MonthlyStats(DailyValue[] dailyValueArray, int startIndex, int stopIndexPlusOne, MathContext mathContext) {
		this.dailyValueArray = dailyValueArray;
		this.startIndex      = startIndex;
		this.stopIndexPlusOne = stopIndexPlusOne;
		
		startDate = dailyValueArray[startIndex].date;
		endDate   = dailyValueArray[stopIndexPlusOne - 1].date;
		
		// startValue is price of startDate before trade
		startValue = dailyValueArray[startIndex - 1].value;
		endValue   = dailyValueArray[stopIndexPlusOne - 1].value;
		// percent change of startValue to endValue
		returns    = endValue.divide(startValue, mathContext).subtract(BigDecimal.ONE);
		
		BigDecimal[] valueArray = DailyValue.toValueArray(dailyValueArray);
		mean = BigDecimalArrays.mean(valueArray, startIndex, stopIndexPlusOne, mathContext);
		sd   = BigDecimalArrays.sd(valueArray, startIndex, stopIndexPlusOne, mean, mathContext);
	}
}