package yokwe.finance.account.smtb;

import yokwe.finance.Storage;

public class UpdateAssetSMTB {
	private static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();
	
	private static final Storage storage = Storage.account.smtb;
	
	private static void update() {
		try(var browser = new WebBrowserSMTB()) {
			logger.info("login");
			browser.login();
			browser.savePage(storage.getFile("top.html"));
			
			logger.info("balance");
			browser.balance();
			browser.savePage(storage.getFile("balance.html"));
			
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
