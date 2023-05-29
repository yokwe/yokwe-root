package yokwe.util.finance;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MonthlyStats {
	public static MonthlyStats[] monthlyStatsArray(DailyValue[] priceArray, DailyValue[] divArray, int limit) {
		// array of index that point to stopIndexPlusOne of each month
		// stopIndexPlusOneArray[0] contains stopIndexPlusOne of newest month
		int[] stopIndexPlusOneArray;
		{			
			List<Integer> list = new ArrayList<>((priceArray.length / 12) + 1);
			int lastMonthValue = priceArray[0].date.getMonthValue();
			for(int i = 1; i < priceArray.length; i++) {
				int monthValue = priceArray[i].date.getMonthValue();
				if (monthValue == lastMonthValue) continue;
				
				// monthValue has changed, add previous index date and index to list
				int stopIndexPlusOne = i;
				list.add(stopIndexPlusOne);
				// update lastMonthValue for next iteration
				lastMonthValue = monthValue;
			}
			
			// reverse list
			Collections.reverse(list);
			// list to array
			stopIndexPlusOneArray = list.stream().mapToInt(o -> o).toArray();
		}
		
		DailyValue[] reinvestedPriceArray = Finance.toReinvested(priceArray, divArray);
		
		List<MonthlyStats> list = new ArrayList<>();
		for(int i = 0; i < stopIndexPlusOneArray.length - 2; i++) {
			int stopIndexPlusOne = stopIndexPlusOneArray[i];
			int startIndex       = stopIndexPlusOneArray[i + 1];
			
			list.add(new MonthlyStats(reinvestedPriceArray, priceArray, startIndex, stopIndexPlusOne, divArray));
			// needs only limit entries
			if (list.size() == limit) break;
		}
		
		return list.toArray(MonthlyStats[]::new);
	}
	
	public final DailyValue[] reinvestedPriceArray;
	public final DailyValue[] priceArray;
	public final int          startIndex;
	public final int          stopIndexPlusOne;
	
	public final LocalDate    startDate;
	public final LocalDate    endDate;
	
	public final BigDecimal   startValue;
	public final BigDecimal   endValue;
	public final BigDecimal   returns;  // return ratio from previous period
	
	public final BigDecimal   div;
	
	public final BigDecimal   mean;
	public final BigDecimal   sd;
	
	public MonthlyStats(final DailyValue[] reinvestedPriceArray, final DailyValue[] priceArray, final int startIndex, final int stopIndexPlusOne, final DailyValue[] divArray) {
		this.reinvestedPriceArray = reinvestedPriceArray;
		this.priceArray           = priceArray;
		this.startIndex           = startIndex;
		this.stopIndexPlusOne     = stopIndexPlusOne;
		
		startDate = priceArray[startIndex].date;
		endDate   = priceArray[stopIndexPlusOne - 1].date;
		
		// startValue is price of startDate before trade
		startValue = priceArray[startIndex - 1].value;
		endValue   = priceArray[stopIndexPlusOne - 1].value;
		// percent change of startValue to endValue
		{
			// To compare return of fund with and without dividend, use reinvestedStartValue
			BigDecimal reinvestedStartValue = reinvestedPriceArray[startIndex - 1].value;
			BigDecimal reinvestedEendValue  = reinvestedPriceArray[stopIndexPlusOne - 1].value;
			returns    = reinvestedEendValue.divide(reinvestedStartValue, BigDecimalUtil.DEFAULT_MATH_CONTEXT).subtract(BigDecimal.ONE);
		}
		
		{
			BigDecimal[] divs = DailyValue.filterValue(divArray, startDate, endDate);
			div = BigDecimalArrays.sum(divs);
		}
		
		{
			BigDecimal[] valueArray = DailyValue.toValueArray(priceArray, startIndex, stopIndexPlusOne);
			mean = BigDecimalArrays.mean(valueArray);
		}

		{
			BigDecimal[] valueArray = DailyValue.toValueArray(priceArray, startIndex, stopIndexPlusOne);
			BigDecimal[] simpleRatioArray = BigDecimalArrays.toSimpleRatio(valueArray);
			sd   = BigDecimalArrays.sd(simpleRatioArray);
		}
	}
}