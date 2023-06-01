package yokwe.util.finance;

import java.math.BigDecimal;
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
	public final BigDecimal   absoluteYield;                // 分配金利率
	public final BigDecimal   annualizedYield;              // 年率換算した分配金利率
	
	public final BigDecimal   absoluteReturn;               // 分配金受取ベースのリターン
	public final BigDecimal   annualizedReturn;             // 分配金受取ベースのリターン 年率換算
	
	public final BigDecimal   absoluteReturnWithReinvest;   // 分配金再投資ベースのリターン
	public final BigDecimal   annualizedReturnWithReinvest; // 分配金再投資ベースのリターン 年率換算
	
	public final BigDecimal   cumulativeReturn;             // 月統計の分配金再投資ベースのリターンより求めた累積リターン
	public final BigDecimal   annualizedCumulativeReturn;   // 月統計の分配金再投資ベースのリターンより求めた累積リターン 年率換算

	public final BigDecimal   annualizedStandardDeviationA;
	public final BigDecimal   annualizedStandardDeviationB;
	public final BigDecimal   annualizedStandardDeviationC;
	
	private AnnualStats(final MonthlyStats[] monthlyStatsArray, final int nYear) {
		final int nMonth = nYear * 12;
		
		this.isinCode = monthlyStatsArray[0].isinCode;
				
		MonthlyStats startMonth = monthlyStatsArray[nMonth - 1];
		MonthlyStats endMonth   = monthlyStatsArray[0];
		
		reinvestedPriceArray = startMonth.reinvestedPriceArray;
		priceArray           = startMonth.priceArray;
		startIndex           = startMonth.startIndex;
		stopIndexPlusOne     = endMonth.stopIndexPlusOne;
		
		startDate              = startMonth.startDate;
		startValue             = startMonth.startValue;
		startValueWithReinvest = startMonth.startValueWithReinvest;
		
		endDate              = endMonth.endDate;
		endValue             = endMonth.endValue;
		endValueWithReinvest = endMonth.endValueWithReinvest;
				
		div             = BigDecimalUtil.sum(monthlyStatsArray, 0, nMonth, o -> o.div);
		absoluteYield   = div.divide(endValue, BigDecimalUtil.DEFAULT_MATH_CONTEXT);
		annualizedYield = absoluteYield.divide(BigDecimal.valueOf(nYear), BigDecimalUtil.DEFAULT_MATH_CONTEXT);
		
		// https://www.nikkei.com/help/contents/markets/fund/
		// 分配金受取基準価格
		// 受け取った分配金の合計額を基準価格に足した値です。
		// 【計算内容】
		// <計算式>基準価格 + 分配金累計
		// <例>基準価格が15000、分配金累計が100の場合、15000+100=15100。分配金は税引き前。
		absoluteReturn = BigDecimalUtil.toSimpleReturn(startValue, endValue.add(div));
	
		// https://www.nikkei.com/help/contents/markets/fund/
		// 分配金受取ベースのリターン(年率)
		// 分配金を足した基準価格がどれだけ上昇または下落したかをパーセントで表示しています。
		// その投資信託を購入した投資家の保有期間中の損得の実感に近いリターンと言えます。
		// 対象期間中のリターンを１年間に換算した年率で表示しています。
		// 【計算内容】
		// {(計算期末基準価格＋計算期間分配金合計)／計算期初基準価格} ^ (12／n) - 1
		// n=6,12,36,60,120
		absoluteReturnWithReinvest = BigDecimalUtil.toSimpleReturn(startValueWithReinvest, endValueWithReinvest);
		
		// https://www.nikkei.com/help/contents/markets/fund/
		// リターン(年率)は対象期間中のリターンを１年間に換算した年率で表示しています。
		// 【計算内容】
		// ・リターン　(＝累積リターン)
		// {nΠ1(1+月次リターンn)} - 1 　n=6,12,36,60,120,設定来月数
		// ・リターン(1年)、リターン(年率)　（＝年率累積リターン）
		// (1+上記累積リターン)^(12/n) - 1 　n=6,12,36,60,120,設定来月数
		cumulativeReturn = Finance.cumulativeReturn(monthlyStatsArray, 0, nMonth, o -> o.absoluteReturnWithReinvest);
		
		annualizedReturn             = Finance.annualizeReturn(absoluteReturn, nYear);
		annualizedReturnWithReinvest = Finance.annualizeReturn(absoluteReturnWithReinvest, nYear);
		annualizedCumulativeReturn   = Finance.annualizeReturn(cumulativeReturn, nYear);
		
		
		// FIXME which method is better?
		{
			// calculate standard deviation from reinvestedPriceArray
			// NOTE using reinvestedPriceArray
			// NOTE using daily value
			BigDecimal[] array = BigDecimalUtil.toSimpleReturn(reinvestedPriceArray, startIndex, stopIndexPlusOne, o -> o.value);
			BigDecimal   mean  = BigDecimalArrays.mean(array);
			BigDecimal   sd    = BigDecimalArrays.standardDeviation(array, mean);
			annualizedStandardDeviationA = Finance.annualizeDailyStandardDeviation(sd);
			logger.info("## AA  {}  {}", nYear, sd.stripTrailingZeros().toPlainString());
		}
		{
			// calculate standard deviation from endValueWithReinvest of monthlyStatsArray
			// NOTE using reinvestedPriceArray
			// NOTE using monthly value
			BigDecimal[] array = BigDecimalUtil.toSimpleReturn(monthlyStatsArray, 0, nMonth, o -> o.endValueWithReinvest);
			BigDecimal   mean  = BigDecimalArrays.mean(array);
			BigDecimal   sd    = BigDecimalArrays.standardDeviation(array, mean);
			annualizedStandardDeviationB = Finance.annualizeMonthlyStandardDeviation(sd);
			logger.info("## BB  {}  {}", nYear, sd.stripTrailingZeros().toPlainString());
		}
		{
			// calculate standard deviation from variance of monthlyStatsArray
			// NOTE using reinvestedPriceArray
			// NOTE using daily value
			BigDecimal sd      = BigDecimalUtil.mean(monthlyStatsArray, 0, nMonth, o -> o.standardDeviation);
			annualizedStandardDeviationC = Finance.annualizeDailyStandardDeviation(sd);
			logger.info("## CC  {}  {}", nYear, sd.stripTrailingZeros().toPlainString());
		}

	}
}