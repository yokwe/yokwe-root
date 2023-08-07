package yokwe.util.finance;

import java.time.LocalDate;
import java.util.function.DoubleUnaryOperator;

import yokwe.util.UnexpectedException;
import yokwe.util.finance.online.NoReinvestedValue;
import yokwe.util.finance.online.ReinvestedValue;
import yokwe.util.finance.online.SimpleReturn;

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
	
	
	public final String      code;                // isinCode, stockCode or ticker symbol
	public final LocalDate[] dateArray;
	public final double[]    priceArray;          // 基準価格
	public final double[]    divArray;            // 分配金
	
	// derived values
	public final double[]    retPrice;            // この期間の基準価格
	public final double[]    retReinvestment;     // この期間の分配金再投資基準価格
	public final double[]    retNoReinvestment;   // この期間の分配金受取基準価格

	public final int         startIndex;          // この期間の取引初日への配列インデックス
	public final LocalDate   startDate;           // この期間の取引初日
	public final double      startPrice;          // この期間の取引初日の基準価格
	public final double      startReinvestment;   // この期間の取引初日の分配金再投資基準価格
	public final double      startNoReinvestment; // この期間の取引初日の分配金受取基準価格

	public final int         stopIndexPlusOne;   // 次の期間の取引初日への配列インデックス
	public final LocalDate   endDate;            // この期間の取引末日
	public final double      endPrice;           // この期間の取引末日の基準価格
	public final double      endReinvestment;    // この期間の取引末日の分配金再投資基準価格
	public final double      endNoReinvestment;  // この期間の取引末日の分配金受取基準価格

	public final double      dividend;           // 分配金累計
	public final double      yield;              // 分配金利率 ※年率換算
	
	public final double      rorPrice;           // 基準価格のリターン割合             ※年率換算
	public final double      rorReinvestment;    // 分配金再投資基準価格のリターン割合 ※年率換算
	public final double      rorNoReinvestment;  // 分配金受取基準価格のリターン割合   ※年率換算
	
	public final double      standardDeviation;  // 基準価格日次リターンの標準偏差 ※年率換算
	
	private AnnualStats(final MonthlyStats[] monthlyStatsArray, final int nYear) {
		final int nMonth = nYear * 12;
		
		// this class represents last nMonth of data
		MonthlyStats startMonth = monthlyStatsArray[nMonth - 1];
		MonthlyStats endMonth   = monthlyStatsArray[0];
		
		code                 = startMonth.code;
		dateArray            = startMonth.dateArray;
		priceArray           = startMonth.priceArray;
		divArray             = startMonth.divArray;
		
		startIndex           = startMonth.startIndex;
		stopIndexPlusOne     = endMonth.stopIndexPlusOne;
		
		retPrice             = DoubleArray.toDoubleArray(priceArray, startIndex, stopIndexPlusOne, DoubleUnaryOperator.identity());
		retReinvestment      = DoubleArray.toDoubleArray(priceArray, divArray, startIndex, stopIndexPlusOne, new ReinvestedValue());
		retNoReinvestment    = DoubleArray.toDoubleArray(priceArray, divArray, startIndex, stopIndexPlusOne, new NoReinvestedValue());
		
		startDate            = dateArray[startIndex];
		startPrice           = retPrice[0];
		startReinvestment    = retReinvestment[0];
		startNoReinvestment  = retNoReinvestment[0];

		endDate              = dateArray[stopIndexPlusOne - 1];
		endPrice             = retPrice[retPrice.length - 1];
		endReinvestment      = retReinvestment[retReinvestment.length - 1];
		endNoReinvestment    = retNoReinvestment[retNoReinvestment.length - 1];
		
		dividend             = Stats.sum(divArray, startIndex, stopIndexPlusOne);
		yield                = (dividend / endPrice) / nYear; // yield per year
		
		rorPrice             = SimpleReturn.compoundAnnualReturn(SimpleReturn.getValue(startPrice, endPrice), nYear);
		rorReinvestment      = SimpleReturn.compoundAnnualReturn(SimpleReturn.getValue(startReinvestment, endReinvestment), nYear);
		rorNoReinvestment    = SimpleReturn.compoundAnnualReturn(SimpleReturn.getValue(startNoReinvestment, endNoReinvestment), nYear);
		
		{
			// calculate standard deviation from retPrice
			double[] array = DoubleArray.toDoubleArray(retPrice, new SimpleReturn());
			double sd = Stats.standardDeviation(array);
			standardDeviation = Finance.annualStandardDeviationFromDailyStandardDeviation(sd);
		}
	}
}