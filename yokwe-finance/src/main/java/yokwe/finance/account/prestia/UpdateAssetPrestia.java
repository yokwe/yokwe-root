package yokwe.finance.account.prestia;

import yokwe.finance.Storage;

public class UpdateAssetPrestia {
	private static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();
	
	private static void update() {
		Storage.initialize();
		
		try(var browser = new WebBrowserPrestia()) {
			logger.info("login");
			browser.login();
			
			browser.savePage(StoragePrestia.getPath("top.html"));
			
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
