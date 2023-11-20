package yokwe.finance.account.sony;

import yokwe.finance.Storage;

public class UpdateAssetSony {
	private static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();
	
	private static void update() {
		Storage.initialize();
		
		try(var browser = new WebBrowserSony()) {
			logger.info("login");
			browser.login();
			browser.savePage(StorageSony.getPath("top.html"));
			
			logger.info("balance");
			browser.balance();
			browser.savePage(StorageSony.getPath("balance.html"));
			
			logger.info("balance deposit");
			browser.balanceDeopsit();
			browser.savePage(StorageSony.getPath("balance-deposit.html"));
			
			logger.info("balance deposit foreign");
			browser.balanceDeopsitForeign();
			browser.savePage(StorageSony.getPath("balance-deposit-foreign.html"));
			
			logger.info("balance fund");
			browser.balanceFund();
			browser.savePage(StorageSony.getPath("balance-fund.html"));

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
