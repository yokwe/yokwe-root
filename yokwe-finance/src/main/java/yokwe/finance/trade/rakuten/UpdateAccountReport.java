package yokwe.finance.trade.rakuten;

public class UpdateAccountReport {
	private static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();

	public static void main(String[] args) {
		logger.info("START");
		
		update();
		
		logger.info("STOP");
	}
	
	private static void update() {
		UpdateAccountReportJPY.update();
	}
}
