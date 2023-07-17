package yokwe.util.finance;

import yokwe.util.finance.DoubleArray.ToDoubleImpl;

public class StandardDeviation implements ToDoubleImpl {
	public static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();

	//
	// annual standard deviation
	//
	// https://www.nomura.co.jp/terms/japan/hi/A02397.html
	// 標準偏差は求めた値を年率換算（年間での変化率を計算）して使うのが一般的だ。
	// 具体的には、日次、週次、月次騰落率から計測した標準偏差について、
	// 1年＝250（営業日）＝52（週）＝12（月）の各250、52、12の平方根を掛けた値が年率換算値になる。	
	public static double annualStandardDeviationFromDailyStandardDeviation(double dailyStandardDeviation) {
		// 1 year = 250 days
		// dailyStandardDeviation x sqrt(250)
		return dailyStandardDeviation * Finance.SQRT_WORKING_DAYS_IN_YEAR;
	}
	public static double annualStandardDeviationFromMonthlyStandardDeviation(double monthlyStandardDeviation) {
		// 1 year = 12 month
		// montylyStandardDeviation x sqrt(12)
		return monthlyStandardDeviation * Finance.SQRT_MONTHS_IN_YEAR;
	}
	
	private ToDoubleImpl variance = new Variance();

	@Override
	public void accept(double value) {
		variance.accept(value);
	}
	@Override
	public double get() {
		return Math.sqrt(variance.get());
	}

}
