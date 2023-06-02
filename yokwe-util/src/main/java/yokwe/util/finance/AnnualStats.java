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
	public final BigDecimal[] priceArray;                   // 分配金再投資基準価格
	public final BigDecimal[] dividendArray;                // 分配金
	public final BigDecimal[] dailyReturnArray;             // 分配金再投資基準価格の日次リターン

	public final int          startIndex;
	public final LocalDate    startDate;                    // この期間の取引初日
	public final BigDecimal   startValue;                   // 取引初日開始前 分配金再投資基準価格
	
	public final int          stopIndexPlusOne;
	public final LocalDate    endDate;                      // この期間の取引末日
	public final BigDecimal   endValue;                     // 取引末日終了後 分配金再投資基準価格
	
	public final BigDecimal   dividend;                     // 分配金累計
	public final BigDecimal   absoluteYield;                // 分配金利率
	public final BigDecimal   annualizedYield;              // 年率換算した分配金利率
	
	public final BigDecimal   absoluteReturn;               // 分配金再投資ベースのリターン
	public final BigDecimal   annualizedReturn;             // 分配金再投資ベースのリターン 年率換算
	
	public final BigDecimal   annualizedStandardDeviation;
	
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
		absoluteYield        = dividend.divide(endValue, BigDecimalUtil.DEFAULT_MATH_CONTEXT);
		annualizedYield      = absoluteYield.divide(BigDecimal.valueOf(nYear), BigDecimalUtil.DEFAULT_MATH_CONTEXT);
		
		absoluteReturn       = BigDecimalUtil.toSimpleReturn(startValue, endValue);
		annualizedReturn     = Finance.annualizeReturn(absoluteReturn, nYear);
		
		// FIXME which method is better?
		{
			// calculate standard deviation from dailyReturnArray
			BigDecimal   mean  = BigDecimalArray.mean(dailyReturnArray, startIndex, stopIndexPlusOne, o -> o);
			BigDecimal   sd    = BigDecimalArray.standardDeviation(dailyReturnArray, startIndex, stopIndexPlusOne, mean, o -> o);
			annualizedStandardDeviation = Finance.annualizeDailyStandardDeviation(sd);
		}

	}
}