package yokwe.finance.account.nikko;

import yokwe.finance.Storage;
import yokwe.finance.account.Secret;
import yokwe.finance.util.SeleniumUtil;
import yokwe.util.FileUtil;

public class UpdateAccountInfo {
	private static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();
	
	public static void main(String[] args) {
		logger.info("START");
		
		Storage.initialize();
		
		var secret = Secret.read();
		logger.info("branch    {}", secret.nikko.branch);
		logger.info("account   {}", secret.nikko.account);
//		logger.info("password  {}", secret.nikko.password);
		

		logger.info("driver");
		var driver = SeleniumUtil.getWebDriver();
		
		logger.info("login");
		AccountNikko.login(driver, secret.nikko.branch, secret.nikko.account, secret.nikko.password);
		
		logger.info("balance");
		AccountNikko.balance(driver);
		FileUtil.write().file(StorageNikko.getPath("balance.html"), driver.getPageSource());
				
		AccountNikko.logout(driver);
		
		logger.info("closeDriver");
		SeleniumUtil.closeDriver(driver);
		
		logger.info("STOP");
		
		System.exit(0);
	}
}
