package yokwe.finance.account.smtb;

import yokwe.finance.Storage;

public class UpdateAssetSMTB {
	private static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();
	
	private static void update() {
		Storage.initialize();
		
		try(var browser = new WebBrowserSMTB()) {
			logger.info("login");
			browser.login();
			
			browser.savePage(StorageSMTB.getPath("top.html"));
			
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
