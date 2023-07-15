package yokwe.util.finance;

import java.time.LocalDate;

import yokwe.util.UnexpectedException;

public final class AnnualStats {
	private static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();
	
	public static AnnualStats getInstance(final MonthlyStats[] monthlyStatsArray, final int nYear) {
		if (monthlyStatsArray == null) {
			logger.error("monthlyStatsArray == null");
			throw new UnexpectedException("monthlyStatsArray == null");
		}
		
		int nMonth = nYear * 12;
		return (nMonth <= monthlyStatsArray.length) ? new AnnualStats(monthlyStatsArray, nYear) : null;
	}
	
	
	public final String      isinCode;
	public final LocalDate[] dateArray;
	public final double[]    priceArray;        // 分配金再投資基準価格
	public final double[]    dividendArray;     // 分配金
	public final double[]    dailyReturnArray;  // 分配金再投資基準価格の日次リターン

	public final int         startIndex;
	public final LocalDate   startDate;         // この期間の取引初日
	public final double      startValue;        // 取引初日開始前 分配金再投資基準価格
	
	public final int         stopIndexPlusOne;
	public final LocalDate   endDate;           // この期間の取引末日
	public final double      endValue;          // 取引末日終了後 分配金再投資基準価格
	
	public final double      dividend;          // 分配金累計
	public final double      yield;             // 分配金利率 ※年率換算
	
	public final double      returns;           // 分配金再投資基準価格のリターン ※年率換算
	
	public final double      standardDeviation; // 分配金再投資基準価格の日次リターンの標準偏差 ※年率換算
	public final double      sharpeRatio;       // シャープレシオ ※年率換算
	
	private AnnualStats(final MonthlyStats[] monthlyStatsArray, final int nYear) {
		final int nMonth = nYear * 12;
		
				
		MonthlyStats startMonth = monthlyStatsArray[nMonth - 1];
		MonthlyStats endMonth   = monthlyStatsArray[0];
		
		isinCode             = startMonth.isinCode;
		dateArray            = startMonth.dateArray;
		priceArray           = startMonth.priceArray;
		dividendArray        = startMonth.dividendArray;
		dailyReturnArray     = startMonth.dailyReturnArray;
		
		startIndex           = startMonth.startIndex;
		startDate            = startMonth.startDate;
		startValue           = startMonth.startValue;
		
		stopIndexPlusOne     = endMonth.stopIndexPlusOne;
		endDate              = endMonth.endDate;
		endValue             = endMonth.endValue;
		
		dividend             = DoubleArray.sum(dividendArray, startIndex, stopIndexPlusOne);
		yield                = (dividend / endValue) / nYear; // yield per year
		
		returns              = Finance.annualizeReturn(SimpleReturn.getValue(startValue, endValue), nYear);
		
		{
			// calculate standard deviation from dailyReturnArray
			double mean   = DoubleArray.mean(dailyReturnArray, startIndex, stopIndexPlusOne);
			double sd     = DoubleArray.standardDeviation(dailyReturnArray, startIndex, stopIndexPlusOne, mean);
			standardDeviation = Finance.annualizeDailyStandardDeviation(sd);
		}
		
		sharpeRatio = returns / standardDeviation;
	}
}