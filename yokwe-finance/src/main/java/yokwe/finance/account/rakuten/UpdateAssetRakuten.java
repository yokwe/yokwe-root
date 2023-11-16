package yokwe.finance.account.rakuten;

import yokwe.finance.Storage;

public class UpdateAssetRakuten {
	private static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();
	
	private static void update() {
		Storage.initialize();
		
		try(var browser = new WebBrowserRakuten()) {
			logger.info("login");
			browser.login();
			
			browser.savePage(StorageRakuten.getPath("top.html"));
			
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
