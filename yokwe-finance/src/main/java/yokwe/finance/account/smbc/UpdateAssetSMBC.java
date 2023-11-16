package yokwe.finance.account.smbc;

import yokwe.finance.Storage;

public class UpdateAssetSMBC {
	private static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();
	
	private static void update() {
		Storage.initialize();
		
		try(var browser = new WebBrowserSMBC()) {
			logger.info("login");
			browser.login();
			
			browser.savePage(StorageSMBC.getPath("top.html"));
			
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
