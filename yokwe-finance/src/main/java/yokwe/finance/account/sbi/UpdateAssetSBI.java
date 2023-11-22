package yokwe.finance.account.sbi;

import yokwe.finance.Storage;

public class UpdateAssetSBI {
	private static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();
	
	private static final Storage storage = Storage.account.sbi;

	private static void update() {
		try(var browser = new WebBrowserSBI()) {
			logger.info("login");
			browser.login();
			browser.savePage(storage.getFile("top.html"));
			
			logger.info("balance-jpy");
			browser.balanceJPY();
			browser.savePage(storage.getFile("balance-jpy.html"));
			
			logger.info("balance-foreign");
			browser.balanceForeign();
			browser.savePage(storage.getFile("balance-foreign.html"));
			
			logger.info("logout");
			browser.logout();
		}
	}
	
	public static void main(String[] args) {
		logger.info("START");
				
		update();
		
		logger.info("STOP");
	}
}
