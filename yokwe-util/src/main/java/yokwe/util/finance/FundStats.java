package yokwe.util.finance;

import java.time.LocalDate;
import java.time.Period;
import java.time.temporal.ChronoField;
import java.time.temporal.TemporalAdjusters;
import java.time.temporal.TemporalField;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import yokwe.util.UnexpectedException;

public class FundStats {
	private static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();
	
	// https://www.nikkei.com/help/contents/markets/fund/
	
	public static class MonthlyStats {
		public final double[] rorPriceArray;
		public final double[] rorReinvestArray;
		
		public MonthlyStats(double[] rorPriceArray, double[] rorReinvestArray) {
			this.rorPriceArray    = rorPriceArray;
			this.rorReinvestArray = rorReinvestArray;
		}
	}

	public final String      code;                  // isinCode, stockCode or ticker symbol
	public final int         length;                // length of array
	public final LocalDate[] dateArray;             // 日付
	public final double[]    priceArray;            // 基準価格
	public final double[]    divArray;              // 分配金
	
	public final int         duration;              // duration in Month
	public final LocalDate   firstDate;             // logical first date
	public final LocalDate   lastDate;              // logical last date
	
	public final int[]       startIndexArray;       // array of startIndex
												    // startIndexArray[0] is startIndex of first month
												    // startIndexArray[1] is startIndex of second month also stopIndexPlusOne of first month
												    // startIndexArray[startIndexArray.length - 2] is startIndex of last month
												    // startIndexArray[startIndexArray.length - 1] is stopIndexPlusOne of last month

	public final int         startIndex;            // startIndex for firstDate
	public final int         stopIndexPlusOne;      // stopIndexPlusOne for lastDate
	
	public final MonthlyStats monthlyStats;         // monthlyStats.rorPriceArray.length == duration
	
	
	
	private FundStats(
		String code,
		LocalDate[] dateArray, double[] priceArray, double[] divArray,
		int duration, LocalDate firstDate, LocalDate lastDate,
		int startIndexArray[],
		MonthlyStats monthlyStats) {
		this.code              = code;
		this.length            = dateArray.length;
		this.dateArray         = dateArray;
		this.priceArray        = priceArray;
		this.divArray          = divArray;
		this.duration          = duration;
		this.firstDate         = firstDate;
		this.lastDate          = lastDate;
		
		this.startIndexArray   = startIndexArray;
		this.startIndex        = startIndexArray[0];
		this.stopIndexPlusOne  = startIndexArray[startIndexArray.length - 1];
		
		this.monthlyStats      = monthlyStats;
	}
	public static FundStats getInstance(String code, DailyPriceDiv[] array) {
		LocalDate firstDate = array[0].date.plusMonths(1).with(TemporalAdjusters.firstDayOfMonth());                // inclusive
		LocalDate lastDate  = array[array.length - 1].date.minusMonths(1).with(TemporalAdjusters.lastDayOfMonth()); // inclusive
		Period    period    = Period.between(firstDate, lastDate.plusDays(1));
		int       duration  = (int)period.toTotalMonths();
		if (duration < 1) {
			// return null if duration is less than 1
			logger.warn("FundStats getInstance  {}  {}  {}  {}", code, firstDate, lastDate, period.toString());
			return null;
		}
		
		final LocalDate[]  dateArray  = DailyPriceDiv.toDateArray(array);
		final double[]     priceArray = DailyPriceDiv.toPriceArray(array);
		final double[]     divArray   = DailyPriceDiv.toDivArray(array);
		
		final int[] startIndexArray = getStartIndexArray(dateArray, ChronoField.MONTH_OF_YEAR);
		if (startIndexArray.length - 1 != duration) {
			logger.error("startIndexArray.length != duration");
			logger.error("  startIndexArray  {}", startIndexArray.length);
			logger.error("  duration         {}", duration);
			throw new UnexpectedException("startIndexArray.length != duration");
		}

		MonthlyStats monthlyStats;
		{
			
			List<Double> rorPriceList      = new ArrayList<>();
			List<Double> rorReinvestList   = new ArrayList<>();
			
			for(int i = 1; i < startIndexArray.length; i++) {
				int startIndex       = startIndexArray[i - 1];
				int stopIndexPlusOne = startIndexArray[i];
				
				double rorPrice;
				{
					double   startValue   = priceArray[startIndex - 1];
					double   endValue     = priceArray[stopIndexPlusOne - 1];
					
					rorPrice = (endValue / startValue) - 1;
				}
				rorPriceList.add(rorPrice);
				
				double rorReinvest;
				{
					// https://www.nikkei.com/help/contents/markets/fund/#qf15
					//
					// リターン・リターン(１年)・リターン(年率)
					// 投資家が期間中に投資信託を保有して得られた収益を示します。
					// 分配金を受け取らずにその分を元本に加えて運用を続けた場合、
					// 基準価格（分配金再投資ベース）がどれだけ上昇または下落したかをパーセントで表示しています。
					// リターン(年率)は対象期間中のリターンを１年間に換算した年率で表示しています。
					// 【計算内容】
					// ・リターン　(＝累積リターン)
					// {nΠ1(1+月次リターンn)} - 1 　n=6,12,36,60,120,設定来月数
					// ・リターン(1年)、リターン(年率)　（＝年率累積リターン）
					// (1+上記累積リターン)^(12/n) - 1 　n=6,12,36,60,120,設定来月数
					
					double lastPrice           = priceArray[startIndex - 1];
					double lastReinvestedPrice = lastPrice;
					for(int j = startIndex; j < stopIndexPlusOne; j++) {
						double price = priceArray[j];
						double div   = divArray[j];
						
						double dailyReturn     = (price + div) / lastPrice;
						double reinvestedPrice = lastReinvestedPrice * dailyReturn;
						
						// update for next iteration
						lastPrice           = price;
						lastReinvestedPrice = reinvestedPrice;
					}
					double startValue   = priceArray[startIndex - 1];
					double endValue     = lastReinvestedPrice;
					
					rorReinvest = (endValue / startValue) - 1;
				}
				rorReinvestList.add(rorReinvest);
			}
			
			// reverse list. So first entry of list point to latest week
			Collections.reverse(rorPriceList);
			Collections.reverse(rorReinvestList);
			monthlyStats = new MonthlyStats(
				rorPriceList.stream().mapToDouble(o -> o).toArray(),
				rorReinvestList.stream().mapToDouble(o -> o).toArray()
			);
		}
		
		return new FundStats(code, dateArray, priceArray, divArray, duration, firstDate, lastDate, startIndexArray, monthlyStats);
	}
	
	private static int[] getStartIndexArray(LocalDate[] dateArray, TemporalField temporalField) {
	    // array[0] is startIndex of first month
	    // array[1] is startIndex of second month also stopIndexPlusOne of first month
	    // array[array.length - 2] is startIndex of last month
	    // array[array.length - 1] is stopIndexPlusOne of last month

		// first entry point to startIndex of first month
		// last  entry point to stopIndexPlusOne of last month
		List<Integer> list = new ArrayList<>((dateArray.length / 12) + 1);
		
		int lastValue = dateArray[0].get(temporalField);
		for(int i = 1; i < dateArray.length; i++) {
			int value = dateArray[i].get(temporalField);
			if (value == lastValue) continue;
			
			list.add(i);
			lastValue = value;
		}
		
		// list to array
		int[] array = list.stream().mapToInt(o -> o).toArray();
		return array;
	}
	
	private static Set<Integer> validMonthSet = new HashSet<>();
	static {
		validMonthSet.add(6);
		validMonthSet.add(12);
		validMonthSet.add(36);
		validMonthSet.add(60);
		validMonthSet.add(120);
	}
	private void checkMonthValue(int nMonth) {
		validMonthSet.contains(nMonth);
		
		if (duration < nMonth) {
			logger.error("Unexpected nMonth");
			logger.error("  nMonth    {}", nMonth);
			logger.error("  duration  {}", duration);
			throw new UnexpectedException("Unexpected nMonth");
		}
	}
	
	public double rateOfReturn(int nMonth) {
		// sanity check
		checkMonthValue(nMonth);
		
		double value = 1;
		for(int i = 0; i < nMonth; i++) {
			value *= (1 + monthlyStats.rorReinvestArray[i]);
		}
		return Math.pow(value, 12.0 / nMonth) - 1;
	}

	public double rateOfReturnNoReinvest(int nMonth) {
		// https://www.nikkei.com/help/contents/markets/fund/#qf16
		//
		// 分配金受取ベースのリターン(年率)
		// 分配金を足した基準価格がどれだけ上昇または下落したかをパーセントで表示しています。
		// その投資信託を購入した投資家の保有期間中の損得の実感に近いリターンと言えます。
		// 対象期間中のリターンを１年間に換算した年率で表示しています。
		// 【計算内容】
		// {(計算期末基準価格＋計算期間分配金合計)／計算期初基準価格} ^ (12／n) - 1
		// n=6,12,36,60,120
		
		// sanity check
		checkMonthValue(nMonth);

		int    startIndex       = startIndexArray[startIndexArray.length - 1 - nMonth];
		int    stopIndexPlusOne = startIndexArray[startIndexArray.length - 1];
		
		double startValue       = priceArray[startIndex - 1];
		double endValue         = priceArray[stopIndexPlusOne - 1];
		double divTotal         = Arrays.stream(divArray, startIndex, stopIndexPlusOne).sum();
		
		return Math.pow((endValue + divTotal) / startValue, 12.0 / nMonth) - 1;
	}
	
	public double dividend(int nMonth) {
		// sanity check
		checkMonthValue(nMonth);
		
		int    startIndex       = startIndexArray[startIndexArray.length - 1 - nMonth];
		int    stopIndexPlusOne = startIndexArray[startIndexArray.length - 1];

		return Arrays.stream(divArray, startIndex, stopIndexPlusOne).sum();
	}
	
	public double yield(int nMonth) {
		// sanity check
		checkMonthValue(nMonth);
		
		int    startIndex       = startIndexArray[startIndexArray.length - 1 - nMonth];
		int    stopIndexPlusOne = startIndexArray[startIndexArray.length - 1];

		return Arrays.stream(divArray, startIndex, stopIndexPlusOne).sum() * 12.0 / nMonth;
	}
	
	public double risk(int nMonth) {
		// リスク・リスク(１年)・リスク(年率)
		// 基準価格のブレ幅の大きさ表します。
		// 過去の基準価格の一定間隔（日次、週次、月次）のリターンを統計処理した標準偏差の数値です。
		// この数値が大きな投資信託ほど大きく値上がりしたり、大きく値下がりしたりする可能性が高く、
		// 逆にリスクの小さい投信ほど値動きは緩やかになると推測できます。
		// 月次更新。6カ月は日次データ、1年は週次データ、3年超は月次データで算出しています。
		// リスク(年率)は対象期間中のリスクを１年間に換算した年率で表示しています。
		// 【計算内容】
		// ・リスク(1年)、リスク(年率)1年　（=年率標準偏差1年）
		// √(nΣ週次リターン^2 - (Σ週次リターン)^2) / n(n-1) × √52　n=52
		// ・リスク(年率)3年～設定来　（=年率標準偏差3年～設定来）
		// √(nΣ月次リターン^2 - (Σ月次リターン)^2) / n(n-1) × √12　n=36,60,120,設定来月数
		
		// sanity check
		checkMonthValue(nMonth);
		
		double value;
		if (nMonth == 6) {
			// use priceArray
			double[]  array            = new double[priceArray.length];
			int       startIndex       = startIndexArray[startIndexArray.length - 1 - nMonth];
			int       stopIndexPlusOne = startIndexArray[startIndexArray.length - 1];
			
			for(int i = startIndex; i < stopIndexPlusOne; i++) {
				double startValue = priceArray[i - 1];
				double endValue   = priceArray[i];
				array[i] = (endValue / startValue) - 1;
			}

			value = Stats.standardDeviation(array, startIndex, stopIndexPlusOne) * Math.sqrt(250);
		} else if (nMonth == 12) {
			// use weeklyStats
			double[] array;
			{
				List<Double> list = new ArrayList<>();
				
				LocalDate oneYearBeforeLastDate = lastDate.minusYears(1).plusDays(1); // inclusive
				int[] startIndexArray = getStartIndexArray(dateArray, ChronoField.ALIGNED_WEEK_OF_YEAR);
				for(int i = 1; i < startIndexArray.length; i++) {
					int       startIndex       = startIndexArray[i - 1];
					int       stopIndexPlusOne = startIndexArray[i];
					LocalDate startDate        = dateArray[startIndex];
					LocalDate endDate          = dateArray[stopIndexPlusOne - 1];
					double    startValue       = priceArray[startIndex - 1]; // previous day close value
					double    endValue         = priceArray[stopIndexPlusOne - 1];
					
					if (startDate.isBefore(oneYearBeforeLastDate)) continue;
					if (endDate.isAfter(lastDate)) break;
					
					list.add((endValue / startValue) - 1);
				}
				array = list.stream().mapToDouble(o -> o).toArray();
			}
			value = Stats.standardDeviation(array) * Math.sqrt(52);
		} else {
			// use monthlyStats
			value = Stats.standardDeviation(monthlyStats.rorPriceArray, 0, nMonth) * Math.sqrt(12);
		}
		
		return value;
	}
}
