package yokwe.finance.account.sony;

import yokwe.finance.Storage;

public class UpdateAssetSony {
	private static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();
	
	private static final Storage storage = Storage.account.sony;
	
	private static void update() {
		try(var browser = new WebBrowserSony()) {
			logger.info("login");
			browser.login();
			browser.savePage(storage.getFile("top.html"));
			
			logger.info("balance");
			browser.balance();
			browser.savePage(storage.getFile("balance.html"));
			
			logger.info("balance deposit");
			browser.balanceDeopsit();
			browser.savePage(storage.getFile("balance-deposit.html"));
			
			logger.info("balance deposit foreign");
			browser.balanceDeopsitForeign();
			browser.savePage(storage.getFile("balance-deposit-foreign.html"));
			
			logger.info("balance fund");
			browser.balanceFund();
			browser.savePage(storage.getFile("balance-fund.html"));

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
