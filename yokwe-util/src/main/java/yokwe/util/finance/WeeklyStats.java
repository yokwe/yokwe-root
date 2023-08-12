package yokwe.util.finance;

import java.time.LocalDate;
import java.time.temporal.ChronoField;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import yokwe.util.UnexpectedException;

public class WeeklyStats {
	private static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();
	
	public static WeeklyStats[] weeklyStatsArray(String code, DailyPriceDiv[] array) {
		LocalDate[]  dateArray  = DailyPriceDiv.toDateArray(array);
		double[]     priceArray = DailyPriceDiv.toPriceArray(array);
		double[]     divArray   = DailyPriceDiv.toDivArray(array);
		
		return weeklyStatsArray(code, dateArray, priceArray, divArray);
	}
	
	public static WeeklyStats[] weeklyStatsArray(String code, LocalDate[] dateArray, double[] priceArray, double[] divArray) {
		// sanity check
		if (dateArray == null) {
			throw new UnexpectedException("dateArray is null");
		}
		if (priceArray == null) {
			throw new UnexpectedException("priceArray is null");
		}
		if (divArray == null) {
			throw new UnexpectedException("divArray is null");
		}
		if (dateArray.length != priceArray.length || dateArray.length != divArray.length) {
			logger.error("Unexpected array length");
			logger.error("  dateArray   {}", dateArray.length);
			logger.error("  priceArray  {}", priceArray.length);
			logger.error("  divArray    {}", divArray.length);
			throw new UnexpectedException("Unexpected array length");
		}
			
			
		// array of index that point to stopIndexPlusOne of each week
		// stopIndexPlusOneArray[0] contains stopIndexPlusOne of newest week (most recent)
		int[] stopIndexPlusOneArray;
		{
			List<Integer> list = new ArrayList<>((dateArray.length / 7) + 1);
			
			int lastWeekValue = dateArray[0].get(ChronoField.ALIGNED_WEEK_OF_YEAR);
			LocalDate lastDate = dateArray[dateArray.length - 1].minusMonths(1).with(TemporalAdjusters.lastDayOfMonth());
			for(int i = 1; i < dateArray.length; i++) {
				int weekValue = dateArray[i].get(ChronoField.ALIGNED_WEEK_OF_YEAR);
				if (weekValue == lastWeekValue) continue;
				if (dateArray[i].isAfter(lastDate)) break;
				
				// week has changed, treat i as stopIndexPlusOne
				int stopIndexPlusOne = i;
				list.add(stopIndexPlusOne);
				// update lastWeekValue
				lastWeekValue = weekValue;
			}
			// reverse list. So first entry of list point to latest week
			Collections.reverse(list);
			// list to array
			stopIndexPlusOneArray = list.stream().mapToInt(o -> o).toArray();
		}

		List<WeeklyStats> list = new ArrayList<>();
		for(int i = 0; i < stopIndexPlusOneArray.length - 2; i++) {
			int stopIndexPlusOne = stopIndexPlusOneArray[i];
			int startIndex       = stopIndexPlusOneArray[i + 1];
			
			list.add(new WeeklyStats(code, startIndex, stopIndexPlusOne, dateArray, priceArray, divArray));
		}

		return list.toArray(WeeklyStats[]::new);
	}

	public final String      code;               // isinCode, stockCode or ticker symbol
	public final int         startIndex;         // この期間の取引初日への配列インデックス
	public final int         stopIndexPlusOne;   // 次の期間の取引初日への配列インデックス
	public final LocalDate[] dateArray;
	public final double[]    priceArray;         // 基準価格
	public final double[]    divArray;           // 分配金
		
	// derived value		
	public final LocalDate   startDate;          // この期間の取引初日
	public final LocalDate   endDate;            // この期間の取引末日	
	
	private WeeklyStats(String code_, int startIndex_, int stopIndexPlusOne_, 
			LocalDate[] dateArray_, double[] priceArray_, double[] divArray_) {
		
		code             = code_;
		startIndex       = startIndex_;
		stopIndexPlusOne = stopIndexPlusOne_;
		
		dateArray        = dateArray_;
		priceArray       = priceArray_;
		divArray         = divArray_;
		
		startDate        = dateArray[startIndex];
		endDate          = dateArray[stopIndexPlusOne - 1];
	}

}
