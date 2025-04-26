package yokwe.finance.account.smbc;

import yokwe.finance.Storage;

public class UpdateAssetSMBC {
	private static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();
	
	private static final Storage storage = Storage.account.smbc;

	private static void update() {
		try(var browser = new WebBrowserSMBC()) {
			logger.info("login");
			browser.login();
			
			browser.savePage(storage.getFile("top.html"));
			
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
