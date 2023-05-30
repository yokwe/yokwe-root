package yokwe.util.finance;

import java.math.BigDecimal;
import java.time.LocalDate;

import yokwe.util.UnexpectedException;

public final class AnnualStats {
	private static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();
	
	public static AnnualStats getInstance(final MonthlyStats[] monthlyStatsArray, final int nYear) {
		if (monthlyStatsArray == null) {
			logger.error("array == null");
			throw new UnexpectedException("array == null");
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
	public final BigDecimal   yield;                        // 年率換算した分配金利率
	
	public final BigDecimal   absoluteReturn;               // 分配金受取ベースのリターン
	public final BigDecimal   absoluteReturnWithReinvest;   // 分配金再投資ベースのリターン
	
	public final BigDecimal   annualizedReturn;             // 年率換算した分配金受取ベースのリターン
	public final BigDecimal   annualizedReturnWithReinvest; // 年率換算した分配金再投資ベースのリターン
	
	public final BigDecimal   sd;
	
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
				
		div   = BigDecimalArrays.sum(monthlyStatsArray, 0, nMonth, o -> o.div);
		yield = div.divide(endValue.multiply(BigDecimal.valueOf(nYear)), BigDecimalUtil.DEFAULT_MATH_CONTEXT); // FIXME annualization
		
		// https://www.nikkei.com/help/contents/markets/fund/
		// 分配金受取基準価格
		// 受け取った分配金の合計額を基準価格に足した値です。
		// 【計算内容】
		// <計算式>基準価格 + 分配金累計
		// <例>基準価格が15000、分配金累計が100の場合、15000+100=15100。分配金は税引き前。
		absoluteReturn = BigDecimalArrays.toSimpleReturn(startValue, endValue.add(div));
	
		// https://www.nikkei.com/help/contents/markets/fund/
		// 分配金受取ベースのリターン(年率)
		// 分配金を足した基準価格がどれだけ上昇または下落したかをパーセントで表示しています。
		// その投資信託を購入した投資家の保有期間中の損得の実感に近いリターンと言えます。
		// 対象期間中のリターンを１年間に換算した年率で表示しています。
		// 【計算内容】
		// {(計算期末基準価格＋計算期間分配金合計)／計算期初基準価格} ^ (12／n) - 1
		// n=6,12,36,60,120
		annualizedReturn = Finance.annualizeReturn(absoluteReturn, nYear);
	
		absoluteReturnWithReinvest   = BigDecimalArrays.toSimpleReturn(startValueWithReinvest, endValueWithReinvest);
		annualizedReturnWithReinvest = Finance.annualizeReturn(absoluteReturnWithReinvest, nYear);
		
		// リスク・リスク(１年)・リスク(年率)
		// 基準価格のブレ幅の大きさ表します。過去の基準価格の一定間隔（日次、週次、月次）のリターンを統計処理した標準偏差の数値です。この数値が大きな投資信託ほど大きく値上がりしたり、大きく値下がりしたりする可能性が高く、逆にリスクの小さい投信ほど値動きは緩やかになると推測できます。月次更新。6カ月は日次データ、1年は週次データ、3年超は月次データで算出しています。
		// リスク(年率)は対象期間中のリスクを１年間に換算した年率で表示しています。
		// 【計算内容】
		// ・リスク(1年)、リスク(年率)1年　（=年率標準偏差1年）
		// √(nΣ週次リターン^2 - (Σ週次リターン)^2) / n(n-1) × √52　n=52
		// ・リスク(年率)3年～設定来　（=年率標準偏差3年～設定来）
		// √(nΣ月次リターン^2 - (Σ月次リターン)^2) / n(n-1) × √12　n=36,60,120,設定来月数
		{
			// FIXME
			// absoluteSD
			// annualizedSD
			BigDecimal[] simpleReturnArray = BigDecimalArrays.toSimpleReturn(reinvestedPriceArray, startIndex, stopIndexPlusOne, o -> o.value);
//			BigDecimal[] logReturnArray = BigDecimalArrays.toLogReturn(reinvestedPriceArray, startIndex, stopIndexPlusOne, o -> o.value);

			sd   = BigDecimalArrays.sd(simpleReturnArray);
		}

	}
}