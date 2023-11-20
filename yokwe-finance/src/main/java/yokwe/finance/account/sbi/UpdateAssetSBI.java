package yokwe.finance.account.sbi;

import yokwe.finance.Storage;

public class UpdateAssetSBI {
	private static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();
	
	private static void update() {
		Storage.initialize();
		
		try(var browser = new WebBrowserSBI()) {
			logger.info("login");
			browser.login();
			browser.savePage(StorageSBI.getPath("top.html"));
			
			logger.info("balance-jpy");
			browser.balanceJPY();
			browser.savePage(StorageSBI.getPath("balance-jpy.html"));
			
			logger.info("balance-foreign");
			browser.balanceForeign();
			browser.savePage(StorageSBI.getPath("balance-foreign.html"));
			
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
