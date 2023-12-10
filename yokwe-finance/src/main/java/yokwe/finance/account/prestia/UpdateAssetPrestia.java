package yokwe.finance.account.prestia;

import java.io.File;

import yokwe.finance.Storage;

public class UpdateAssetPrestia {
	private static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();
	
	private static final Storage storage = Storage.account.prestia;
	
	private static final File FILE_TOP     = storage.getFile("top.html");
	private static final File FILE_BALANCE = storage.getFile("balance.html");
	private static final File FILE_FUND    = storage.getFile("fund.html");

	private static void download() {
		try(var browser = new WebBrowserPrestia()) {
			logger.info("login");
			browser.login();
			
			browser.savePage(FILE_TOP);
			
			logger.info("balance");
			browser.balance();
			browser.savePage(FILE_BALANCE);
			
			logger.info("fund");
			browser.fundEnter();
			browser.fundBalance();
			browser.savePage(FILE_FUND);
			browser.fundExit();
			
			logger.info("logout");
			browser.logout();
		}
	}
	
	private static void update() {
		
	}
	
	public static void main(String[] args) {
		logger.info("START");
				
		download();
		update();
		
		logger.info("STOP");
	}
}
