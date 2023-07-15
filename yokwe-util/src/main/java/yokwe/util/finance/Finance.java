package yokwe.util.finance;

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
	public static double annualizeReturn(double absoluteReturn, int nYear) {
		// ((1 + absoluteReturn) ^ (1 / nYear)) - 1
		double base     = 1.0 + absoluteReturn;
		double exponent = 1.0 / nYear;
		return Math.pow(base, exponent) - 1.0;
	}
	
	
	//
	// annualized standard deviation
	//
	// https://www.nomura.co.jp/terms/japan/hi/A02397.html
	// 標準偏差は求めた値を年率換算（年間での変化率を計算）して使うのが一般的だ。
	// 具体的には、日次、週次、月次騰落率から計測した標準偏差について、
	// 1年＝250（営業日）＝52（週）＝12（月）の各250、52、12の平方根を掛けた値が年率換算値になる。
	public static final double SQRT_12  = Math.sqrt(12);
	public static final double SQRT_250 = Math.sqrt(250);
	
	public static double annualizeDailyStandardDeviation(double dailyStandardDeviation) {
		// 1 year = 250 days
		// dailyStandardDeviation x sqrt(250)
		return dailyStandardDeviation * SQRT_250;
	}
	public static double annualizeMonthlyStandardDeviation(double montylyStandardDeviation) {
		// 1 year = 12 month
		// montylyStandardDeviation x sqrt(12)
		return montylyStandardDeviation * SQRT_12;
	}
	
}
