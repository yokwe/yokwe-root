package yokwe.finance.account.nikko;

import yokwe.finance.Storage;

public class UpdateAssetNikko {
	private static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();
	
	private static void update() {
		Storage.initialize();
		
		try(var browser = new WebBrowserNikko()) {
			logger.info("login");
			browser.login();
			
			browser.savePage(StorageNikko.getPath("top.html"));
			
			logger.info("balance");
			browser.balance();
			browser.savePage(StorageNikko.getPath("balance.html"));
			
			logger.info("balance bank");
			browser.balanceBank();
			browser.savePage(StorageNikko.getPath("balance-bank.html"));
						
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
