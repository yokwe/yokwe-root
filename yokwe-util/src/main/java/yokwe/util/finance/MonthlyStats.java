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
	public final DailyValue[] reinvestedPriceArray;         // 分配金再投資基準価格
	public final DailyValue[] priceArray;                   // 基準価格
	public final int          startIndex;
	public final int          stopIndexPlusOne;
	
	public final LocalDate    startDate;                    // この期間の取引初日
	public final BigDecimal   startValue;                   // 取引初日開始前 基準価格
	public final BigDecimal   startValueWithReinvest;       // 取引初日開始前 分配金再投資基準価格
	
	public final LocalDate    endDate;                      // この期間の取引末日
	public final BigDecimal   endValue;                     // 取引末日終了後 基準価格
	public final BigDecimal   endValueWithReinvest;         // 取引末日終了後 分配金再投資基準価格

	public final BigDecimal   div;                          // 分配金累計

	public final BigDecimal   absoluteReturn;               // 分配金受取ベースのリターン
	public final BigDecimal   absoluteReturnWithReinvest;   // 分配金再投資ベースのリターン
	
	public MonthlyStats(final String isinCode, final DailyValue[] reinvestedPriceArray, final DailyValue[] priceArray, final int startIndex, final int stopIndexPlusOne, final DailyValue[] divArray) {
		this.isinCode             = isinCode;
		this.reinvestedPriceArray = reinvestedPriceArray;
		this.priceArray           = priceArray;
		this.startIndex           = startIndex;
		this.stopIndexPlusOne     = stopIndexPlusOne;
		
		startDate              = priceArray[startIndex].date;
		// startValue is a price of startDate before trade
		startValue             = priceArray[startIndex - 1].value;
		startValueWithReinvest = reinvestedPriceArray[startIndex - 1].value;
		
		endDate              = priceArray[stopIndexPlusOne - 1].date;
		// endValue is a price of endValue after trade
		endValue             = priceArray[stopIndexPlusOne - 1].value;
		endValueWithReinvest = reinvestedPriceArray[stopIndexPlusOne - 1].value;

		{
			BigDecimal[] array = DailyValue.filterValue(divArray, startDate, endDate);
			div = BigDecimalArrays.sum(array);
		}
		
		absoluteReturn             = BigDecimalArrays.toSimpleReturn(startValue, endValue.add(div));
		absoluteReturnWithReinvest = BigDecimalArrays.toSimpleReturn(startValueWithReinvest, endValueWithReinvest);
		
	}
}