package yokwe.util.finance;

import java.math.BigDecimal;
import java.time.LocalDate;

import yokwe.util.BigDecimalArray;
import yokwe.util.BigDecimalUtil;
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
	
	
	public final String       isinCode;
	public final LocalDate[]  dateArray;
	public final BigDecimal[] priceArray;        // 分配金再投資基準価格
	public final BigDecimal[] dividendArray;     // 分配金
	public final BigDecimal[] dailyReturnArray;  // 分配金再投資基準価格の日次リターン

	public final int          startIndex;
	public final LocalDate    startDate;         // この期間の取引初日
	public final BigDecimal   startValue;        // 取引初日開始前 分配金再投資基準価格
	
	public final int          stopIndexPlusOne;
	public final LocalDate    endDate;           // この期間の取引末日
	public final BigDecimal   endValue;          // 取引末日終了後 分配金再投資基準価格
	
	public final BigDecimal   dividend;          // 分配金累計
	public final BigDecimal   yield;             // 分配金利率 ※年率換算
	
	public final BigDecimal   returns;           // 分配金再投資基準価格のリターン ※年率換算
	
	public final BigDecimal   standardDeviation; // 分配金再投資基準価格の日次リターンの標準偏差 ※年率換算
	public final BigDecimal   sharpeRatio;       // シャープレシオ ※年率換算
	
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
				
		dividend             = BigDecimalArray.sum(monthlyStatsArray, 0, nMonth, o -> o.dividend);
		yield                = BigDecimalUtil.divide(dividend, endValue.multiply(BigDecimal.valueOf(nYear)));
		
		returns              = Finance.annualizeReturn(BigDecimalUtil.toSimpleReturn(startValue, endValue), nYear);
		
		{
			// calculate standard deviation from dailyReturnArray
			BigDecimal mean   = BigDecimalArray.mean(dailyReturnArray, startIndex, stopIndexPlusOne, o -> o);
			BigDecimal sd     = BigDecimalArray.standardDeviation(dailyReturnArray, startIndex, stopIndexPlusOne, mean, o -> o);
			standardDeviation = Finance.annualizeDailyStandardDeviation(sd);
		}
		
		sharpeRatio = BigDecimalUtil.divide(returns, standardDeviation);
	}
}