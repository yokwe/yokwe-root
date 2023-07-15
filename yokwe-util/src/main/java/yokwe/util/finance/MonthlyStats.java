package yokwe.util.finance;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import yokwe.util.UnexpectedException;

public final class MonthlyStats {
	private static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();
	
	public static MonthlyStats[] monthlyStatsArray(String isinCode, DailyValue[] rawPriceArray, DailyValue[] divArray, int nMonth) {
		LocalDate[]  dateArray     = DailyValue.toDateArray(rawPriceArray);
		double[]     dividendArray = new double[rawPriceArray.length];
		{
			Map<LocalDate, Double> map = Arrays.stream(divArray).collect(Collectors.toMap(o -> o.date, o -> o.value));
			Double zero = Double.valueOf(0);
			for(int i = 0; i < rawPriceArray.length; i++) {
				LocalDate date = rawPriceArray[i].date;
				dividendArray[i] = map.getOrDefault(date, zero);
			}
		}
		double[] priceArray = new double[rawPriceArray.length];
		{
			ReinvestedPrice reinvestedPrice = new ReinvestedPrice();
			for(int i = 0; i < rawPriceArray.length; i++) {
				var price = rawPriceArray[i].value;
				var div   = dividendArray[i];
				
				priceArray[i] = reinvestedPrice.applyAsDouble(price, div);;
			}
		}
		double[] dailyReturnArray = DoubleArray.simpleReturn(priceArray);
		
		
		// array of index that point to stopIndexPlusOne of each month
		// stopIndexPlusOneArray[0] contains stopIndexPlusOne of newest month
		int[] stopIndexPlusOneArray;
		{			
			List<Integer> list = new ArrayList<>((priceArray.length / 12) + 1);
			int lastMonthValue = dateArray[0].getMonthValue();
			for(int i = 1; i < dateArray.length; i++) {
				int monthValue = dateArray[i].getMonthValue();
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

		List<MonthlyStats> list = new ArrayList<>();
		for(int i = 0; i < stopIndexPlusOneArray.length - 2; i++) {
			int stopIndexPlusOne = stopIndexPlusOneArray[i];
			int startIndex       = stopIndexPlusOneArray[i + 1];
			
			list.add(new MonthlyStats(isinCode, startIndex, stopIndexPlusOne, dateArray, priceArray, dailyReturnArray, dividendArray));
			if (list.size() == nMonth) break;
		}

		return list.toArray(MonthlyStats[]::new);
	}
	

	public final String      isinCode;
	public final int         startIndex;                   // この期間の取引初日への配列インデックス
	public final int         stopIndexPlusOne;             // 次の期間の取引初日への配列インデックス
	public final LocalDate[] dateArray;
	public final double[]    priceArray;                   // 分配金再投資基準価格
	public final double[]    dividendArray;                // 分配金
	public final double[]    dailyReturnArray;             // 分配金再投資基準価格の日次リターン
	
	// derived value		
	public final LocalDate   startDate;                    // この期間の取引初日
	public final LocalDate   endDate;                      // この期間の取引末日
	
	public final double      startValue;                   // 取引初日開始前 分配金再投資基準価格
	public final double      endValue;                     // 取引末日終了後 分配金再投資基準価格

	public final double      dividend;                     // この期間の分配金累計

	private MonthlyStats(String isinCode_, int startIndex_, int stopIndexPlusOne_, 
			LocalDate[] dateArray_, double[] priceArray_, double[] dailyReturnArray_, double[] dividendArray_) {
		if (priceArray_.length != dailyReturnArray_.length || priceArray_.length != dividendArray_.length) {
			logger.error("size of priceArray, dailytRetur and divArray not equal");
			logger.error("  isinCode_         {}", isinCode_);
			logger.error("  priceArray_       {}", priceArray_.length);
			logger.error("  dailyReturnArray_ {}", dailyReturnArray_.length);
			logger.error("  divArray_         {}", dividendArray_.length);
			throw new UnexpectedException("size of priceArray, dailytRetur and divArray not equal");
		}
		
		isinCode         = isinCode_;
		startIndex       = startIndex_;
		stopIndexPlusOne = stopIndexPlusOne_;
		
		dateArray        = dateArray_;
		priceArray       = priceArray_;
		dailyReturnArray = dailyReturnArray_;
		dividendArray    = dividendArray_;
		
		startDate  = dateArray[startIndex];
		endDate    = dateArray[stopIndexPlusOne - 1];
		
		startValue = priceArray[startIndex - 1];
		endValue   = priceArray[stopIndexPlusOne - 1];
		
		dividend   = DoubleArray.sum(dividendArray, startIndex, stopIndexPlusOne);
	}
}