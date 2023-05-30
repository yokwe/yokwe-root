package yokwe.util.finance;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MonthlyStats {
	public static MonthlyStats[] monthlyStatsArray(String isinCode, DailyValue[] priceArray, DailyValue[] divArray, int limit) {
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
		
		DailyValue[] reinvestedPriceArray = Finance.toReinvestedPrice(priceArray, divArray);
		
		List<MonthlyStats> list = new ArrayList<>();
		for(int i = 0; i < stopIndexPlusOneArray.length - 2; i++) {
			int stopIndexPlusOne = stopIndexPlusOneArray[i];
			int startIndex       = stopIndexPlusOneArray[i + 1];
			
			list.add(new MonthlyStats(isinCode, reinvestedPriceArray, priceArray, startIndex, stopIndexPlusOne, divArray));
			// needs only limit entries
			if (list.size() == limit) break;
		}
		
		return list.toArray(MonthlyStats[]::new);
	}
	
	public final String       isinCode;
	public final DailyValue[] reinvestedPriceArray;
	public final DailyValue[] priceArray;
	public final int          startIndex;
	public final int          stopIndexPlusOne;
	
	public final LocalDate    startDate;
	public final LocalDate    endDate;
	
	public final BigDecimal   startValue;
	public final BigDecimal   endValue;
	
	public final BigDecimal   absoluteReturn;
	public final BigDecimal   absoluteReturnReinvest;
	
	public final BigDecimal   div;
	
	public final BigDecimal   absoluteSD;
	
	public MonthlyStats(final String isinCode, final DailyValue[] reinvestedPriceArray, final DailyValue[] priceArray, final int startIndex, final int stopIndexPlusOne, final DailyValue[] divArray) {
		this.isinCode             = isinCode;
		this.reinvestedPriceArray = reinvestedPriceArray;
		this.priceArray           = priceArray;
		this.startIndex           = startIndex;
		this.stopIndexPlusOne     = stopIndexPlusOne;
		
		startDate = priceArray[startIndex].date;
		endDate   = priceArray[stopIndexPlusOne - 1].date;
		
		// startValue is price of startDate before trade
		startValue = priceArray[startIndex - 1].value;
		endValue   = priceArray[stopIndexPlusOne - 1].value;
		{
			BigDecimal[] divs = DailyValue.filterValue(divArray, startDate, endDate);
			div = BigDecimalArrays.sum(divs);
		}
		
		// https://www.nikkei.com/help/contents/markets/fund/
		// 分配金受取基準価格
		// 受け取った分配金の合計額を基準価格に足した値です。
		// 【計算内容】
		// <計算式>基準価格 + 分配金累計
		// <例>基準価格が15000、分配金累計が100の場合、15000+100=15100。分配金は税引き前。
		absoluteReturn = BigDecimalArrays.toSimpleReturn(startValue, endValue);

		// percent change of startValue to endValue
		{
			// To compare return of fund with and without dividend, use reinvestedStartValue
			BigDecimal start = reinvestedPriceArray[startIndex - 1].value;
			BigDecimal end   = reinvestedPriceArray[stopIndexPlusOne - 1].value;
			absoluteReturnReinvest = BigDecimalArrays.toSimpleReturn(start, end);
		}
		
		{
			BigDecimal[] simpleRatioArray = BigDecimalArrays.toSimpleReturn(reinvestedPriceArray, startIndex, stopIndexPlusOne, o -> o.value);
			absoluteSD = BigDecimalArrays.sd(simpleRatioArray);
		}

	}
}