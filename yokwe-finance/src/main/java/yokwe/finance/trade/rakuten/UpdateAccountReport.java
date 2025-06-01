package yokwe.finance.trade.rakuten;

import java.math.BigDecimal;

import yokwe.util.StringUtil;

public class UpdateAccountReport {
	private static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();
	
	public static int toCentValue(BigDecimal value) {
		if (value.compareTo(BigDecimal.ZERO) == 0) return 0;
		return value.movePointRight(2).intValue();
	}
	public static BigDecimal toDollarValue(int value) {
		if (value == 0) return BigDecimal.ZERO;
		return BigDecimal.valueOf(value).movePointLeft(2);
	}
	
	public static final String URL_TEMPLATE  = StringUtil.toURLString("data/form/ACCOUNT.ods");


	public static void main(String[] args) {
		logger.info("START");
		
		update();
		
		logger.info("STOP");
	}
	
	private static void update() {
//		UpdateAccountReportJPY.update(); // FIXME
		UpdateAccountReportUSD.update();
	}
}
