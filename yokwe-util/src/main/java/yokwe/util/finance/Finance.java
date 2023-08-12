package yokwe.util.finance;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Period;

import yokwe.util.UnexpectedException;

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
	public static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();

	public static final int WORKING_DAYS_IN_YEAR = 250;
	public static final int DAYS_IN_YEAR         = 365;
	public static final int WEEKS_IN_YEAR        =  52;
	public static final int MONTHS_IN_YEAR       =  12;
	
	public static final double DURATION_PER_DAY          = 1.0 / DAYS_IN_YEAR;
	public static final double SQRT_MONTHS_IN_YEAR       = Math.sqrt(MONTHS_IN_YEAR);
	public static final double SQRT_WEEKS_IN_YEAR        = Math.sqrt(WEEKS_IN_YEAR);
	public static final double SQRT_WORKING_DAYS_IN_YEAR = Math.sqrt(WORKING_DAYS_IN_YEAR);
	
	
	public static double durationInYear(LocalDate startDate, LocalDate endDate) {
		// sanity check
		if (startDate.isAfter(endDate)) {
			logger.error("Unexpected date ragne");
			logger.error("  {}  {}", startDate, endDate);
			throw new UnexpectedException("Unexpected date ragne");
		}
		
		// startDate and endDate is inclusive
		LocalDate endDatePlusOne = endDate.plusDays(1);		
		double    diffYear       = endDatePlusOne.getYear() - startDate.getYear();
		
		// if same month and same day, return diffYear
		double diffDays;
		{
			if (startDate.getMonthValue() == endDatePlusOne.getMonthValue() &&
				startDate.getDayOfMonth() == endDatePlusOne.getDayOfMonth()) {
				diffDays = 0;
			} else {
				diffDays = endDatePlusOne.getDayOfYear() - startDate.getDayOfYear();
			}
		}
		
		return diffYear + diffDays * DURATION_PER_DAY;
	}
	
	public static BigDecimal durationInYearMonth(LocalDate startDate, LocalDate endDate) {
		// startDate and endDate is inclusive
		if (startDate.isAfter(endDate)) {
			return new BigDecimal("0.00");
		} else {
			LocalDate endDatePlusOne = endDate.plusDays(1);		
			Period    period         = startDate.until(endDatePlusOne);
			return new BigDecimal(String.format("%d.%02d", period.getYears(), period.getMonths()));
		}
	}
	
	
	
	//
	// annual standard deviation
	//
	// https://www.nomura.co.jp/terms/japan/hi/A02397.html
	// 標準偏差は求めた値を年率換算（年間での変化率を計算）して使うのが一般的だ。
	// 具体的には、日次、週次、月次騰落率から計測した標準偏差について、
	// 1年＝250（営業日）＝52（週）＝12（月）の各250、52、12の平方根を掛けた値が年率換算値になる。	
	public static double annualStandardDeviationFromDailyStandardDeviation(double dailyStandardDeviation) {
		return dailyStandardDeviation * Finance.SQRT_WORKING_DAYS_IN_YEAR;
	}
	public static double annualStandardDeviationFromWeeklyStandardDeviation(double weeklyStandardDeviation) {
		return weeklyStandardDeviation * Finance.SQRT_WEEKS_IN_YEAR;
	}
	public static double annualStandardDeviationFromMonthlyStandardDeviation(double monthlyStandardDeviation) {
		return monthlyStandardDeviation * Finance.SQRT_MONTHS_IN_YEAR;
	}

}
