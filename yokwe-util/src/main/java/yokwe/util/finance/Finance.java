package yokwe.util.finance;

import java.math.BigDecimal;

import yokwe.util.BigDecimalUtil;

//Morningstar
//https://web.stanford.edu/~wfsharpe/art/stars/stars2.htm

//GIPS
//https://www.pwc.ch/en/publications/2020/PwC-GIPS-2020.pdf
//https://www.gipsstandards.org/wp-content/uploads/2021/03/calculation_methodology_gs_2011.pdf
//https://www.nbim.no/contentassets/98750345a7e641558d820573c52e4a8e/2020-gips-manual-in-english.pdf
//https://www.cfainstitute.org/-/media/documents/code/gips/gips-standards-for-firms-explanation-of-provisions-section-2.ashx

//Modified Dietz method
//https://en.wikipedia.org/wiki/Modified_Dietz_method

public final class Finance {
	//
	// annualized return
	//
	public static BigDecimal annualizeReturn(BigDecimal absoluteReturn, int nYear) {
		// ((1 + absoluteReturn) ^ (1 / nYear)) - 1
		BigDecimal base     = BigDecimal.ONE.add(absoluteReturn);
		BigDecimal exponent = BigDecimalUtil.divide(BigDecimal.ONE, BigDecimal.valueOf(nYear));
		
		return BigDecimalUtil.mathPow(base, exponent).subtract(BigDecimal.ONE);
	}
	
	
	//
	// annualized standard deviation
	//
	// https://www.nomura.co.jp/terms/japan/hi/A02397.html
	// 標準偏差は求めた値を年率換算（年間での変化率を計算）して使うのが一般的だ。
	// 具体的には、日次、週次、月次騰落率から計測した標準偏差について、
	// 1年＝250（営業日）＝52（週）＝12（月）の各250、52、12の平方根を掛けた値が年率換算値になる。
	public static final BigDecimal SQRT_12  = BigDecimalUtil.mathSqrt(BigDecimal.valueOf(12));
	public static final BigDecimal SQRT_250 = BigDecimalUtil.mathSqrt(BigDecimal.valueOf(250));
	
	public static BigDecimal annualizeDailyStandardDeviation(BigDecimal dailyStandardDeviation) {
		// 1 year = 250 days
		// dailyStandardDeviation x sqrt(250)
		return BigDecimalUtil.multiply(dailyStandardDeviation, SQRT_250);
	}

	public static BigDecimal annualizeMonthlyStandardDeviation(BigDecimal montylyStandardDeviation) {
		// 1 year = 12 month
		// montylyStandardDeviation x sqrt(12)
		return BigDecimalUtil.multiply(montylyStandardDeviation, SQRT_12);
	}
	
}
