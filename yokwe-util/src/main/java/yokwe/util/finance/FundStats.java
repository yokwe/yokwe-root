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
import yokwe.util.finance.online.NoReinvestedValue;
import yokwe.util.finance.online.ReinvestedValue;

public class FundStats {
	private static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();
	
	// https://www.nikkei.com/help/contents/markets/fund/
	
	public static class MonthlyStats {
		public final double rorReinvest;
		public final double rorNoReinvest;
		
		public MonthlyStats(double rorReinvest, double rorNoReinvest) {
			this.rorReinvest   = rorReinvest;
			this.rorNoReinvest = rorNoReinvest;
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
	
	public final MonthlyStats[] monthlyStatsArray;  // monthlyStatsArray.length == duration
	
	
	
	private FundStats(
		String code,
		LocalDate[] dateArray, double[] priceArray, double[] divArray,
		int duration, LocalDate firstDate, LocalDate lastDate,
		int startIndexArray[],
		MonthlyStats[] monthlyStatsArray) {
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
		
		this.monthlyStatsArray = monthlyStatsArray;
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


		MonthlyStats[] monthlyStatsArray;
		{
			
			List<MonthlyStats> list = new ArrayList<>();
			
			for(int i = 1; i < startIndexArray.length; i++) {
				int startIndex       = startIndexArray[i - 1];
				int stopIndexPlusOne = startIndexArray[i];
				
				double rorReinvest;
				{
					double[] valueArray   = DoubleArray.toDoubleArray(priceArray, divArray, startIndex, stopIndexPlusOne, new ReinvestedValue());
					double   startValue   = priceArray[startIndex - 1];
					double   endValue     = valueArray[valueArray.length - 1];
					
					rorReinvest = (endValue / startValue) - 1;
				}
				
				double rorNoReinvest;
				{
					double[] valueArray   = DoubleArray.toDoubleArray(priceArray, divArray, startIndex, stopIndexPlusOne, new NoReinvestedValue());
					double   startValue   = priceArray[startIndex - 1];
					double   endValue     = valueArray[valueArray.length - 1];
					
					rorNoReinvest = (endValue / startValue) - 1;
				}

				MonthlyStats monthlyStats = new MonthlyStats(rorReinvest, rorNoReinvest);
				list.add(monthlyStats);
			}
			
			// reverse list. So first entry of list point to latest week
			Collections.reverse(list);
			// list to array
			monthlyStatsArray = list.stream().toArray(MonthlyStats[]::new);
		}
		
		return new FundStats(code, dateArray, priceArray, divArray, duration, firstDate, lastDate, startIndexArray, monthlyStatsArray);
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
	private static boolean checkMonthValue(int nMonth) {
		return validMonthSet.contains(nMonth);
	}
	
	public double rateOfReturn(int nMonth) {
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
		
		// sanity check
		checkMonthValue(nMonth);
		if (duration < nMonth) {
			logger.error("Unexpected nMonth");
			logger.error("  nMonth    {}", nMonth);
			logger.error("  duration  {}", duration);
			throw new UnexpectedException("Unexpected nMonth");
		}
		
		double value = 1;
		for(int i = 0; i < nMonth; i++) {
			value *= (1 + monthlyStatsArray[i].rorReinvest);
		}
		return Math.pow(value, 12.0 / nMonth) - 1;
	}
	public double rateOfReturnX(int nMonth) {
		// https://www.nikkei.com/help/contents/markets/fund/#qf15
		//
		// リターン・リターン(１年)・リターン(年率)
		// 投資家が期間中に投資信託を保有して得られた収益を示します。
		// 分配金を受け取らずにその分を元本に加えて運用を続けた場合、
		// 基準価格（分配金再投資ベース）がどれだけ上昇または下落したかをパーセントで表示しています。
		// リターン(年率)は対象期間中のリターンを１年間に換算した年率で表示しています。
		// ・リターン(1年)、リターン(年率)　（＝年率累積リターン）
		// (1+上記累積リターン)^(12/n) - 1 　n=6,12,36,60,120,設定来月数
		
		// sanity check
		checkMonthValue(nMonth);

		int       startIndex       = startIndexArray[startIndexArray.length - 1 - nMonth];
		int       stopIndexPlusOne = startIndexArray[startIndexArray.length - 1];
		
		double value;
		{
			double[] valueArray   = DoubleArray.toDoubleArray(priceArray, divArray, startIndex, stopIndexPlusOne, new ReinvestedValue());
			double   startValue   = priceArray[startIndex - 1];
			double   endValue     = valueArray[valueArray.length - 1];
			
			value = (endValue / startValue);
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

		int       startIndex       = startIndexArray[startIndexArray.length - 1 - nMonth];
		int       stopIndexPlusOne = startIndexArray[startIndexArray.length - 1];
		
		double    startValue       = priceArray[startIndex - 1];
		double    endValue         = priceArray[stopIndexPlusOne - 1];
		double    divTotal         = Arrays.stream(divArray, startIndex, stopIndexPlusOne).sum();
		
		return Math.pow((endValue + divTotal) / startValue, 12.0 / nMonth) - 1;
	}

}
