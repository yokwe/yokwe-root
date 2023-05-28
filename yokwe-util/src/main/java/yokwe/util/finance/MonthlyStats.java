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
		// array of index that point to endIndexPlusOne of each month
		// endIndexPlusOneArray[0] contains endIndexPlusOne of newest month
		int[] endIndexPlusOneArray;
		{			
			List<Integer> list = new ArrayList<>((array.length / 12) + 1);
			int lastMonthValue = array[0].date.getMonthValue();
			for(int i = 1; i < array.length; i++) {
				int monthValue = array[i].date.getMonthValue();
				if (monthValue == lastMonthValue) continue;
				
				// monthValue has changed, add previous index date and index to list
				int endIndexPlusOne = i;
				list.add(endIndexPlusOne);
				// update lastMonthValue for next iteration
				lastMonthValue = monthValue;
			}
			
			// reverse order of list
			Collections.reverse(list);
			// list to array
			endIndexPlusOneArray = list.stream().mapToInt(o -> o).toArray();
		}
		
		List<MonthlyStats> list = new ArrayList<>();
		for(int i = 0; i < endIndexPlusOneArray.length - 2; i++) {
			int endIndexPlusOne = endIndexPlusOneArray[i];
			int startIndex      = endIndexPlusOneArray[i + 1];
			
			list.add(new MonthlyStats(array, startIndex, endIndexPlusOne, mathContext));
			// needs only limit entries
			if (list.size() == limit) break;
		}
		
		return list.toArray(MonthlyStats[]::new);
	}
	
	//
	// return array of index that point to endIndexPlusOne of each month
	//
	private static int[] monthlyEndIndexPlusOneArray(DailyValue[] array) {
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
			int endIndexPlusOne = i;
			list.add(endIndexPlusOne);
			// update lastMonthValue for next iteration
			lastMonthValue = monthValue;
		}
		
		// reverse order of list
		Collections.reverse(list);
		
		//   list.get(0) contains endIndex of newest month
		//   list.get(1) contains endIndex of before newest month
		return list.stream().mapToInt(o -> o).toArray();
	}
	

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
		
		BigDecimal[] valueArray = DailyValue.toValueArray(dailyValueArray);
		mean = BigDecimalArrays.mean(valueArray, startIndex, endIndexPlusOne, mathContext);
		sd   = BigDecimalArrays.sd(valueArray, startIndex, endIndexPlusOne, mean, mathContext);
	}
}