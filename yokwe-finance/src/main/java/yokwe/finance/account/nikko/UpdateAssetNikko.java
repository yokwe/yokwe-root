package yokwe.finance.account.nikko;

import java.io.File;

import org.openqa.selenium.By;

import yokwe.finance.Storage;

public class UpdateAssetNikko {
	private static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();
	
	private static final Storage storage = Storage.account.nikko;
	
	private static final File FILE_TOP          = storage.getFile("top.html");
	private static final File FILE_BALANCE      = storage.getFile("balance.html");
	private static final File FILE_BALANCE_BANK = storage.getFile("balance-bank.html");
	
	private static void download() {		
		try(var browser = new WebBrowserNikko()) {
			logger.info("login");
			browser.login();
			browser.savePage(FILE_TOP);
			
			logger.info("balance");
			browser.balance();
			browser.savePage(FILE_BALANCE);
			
			logger.info("balance bank");
			browser.balanceBank();
			browser.savePage(FILE_BALANCE_BANK);
						
			logger.info("logout");
			browser.logout();
		}
	}
	
	private static void update() {
		try(var browser = new WebBrowserNikko()) {
			logger.info("get  balance");
			browser.get(FILE_BALANCE);
			
			
			// ※本日の残高は
			// //span[contains(text(), '本日の残高')]/span
			logger.info("a !{}!", browser.findElement(By.xpath("//span[@class='txt_01_1']/span[@class='txt_b01']")).getText());
			logger.info("a !{}!", browser.findElement(By.xpath("//span[contains(text(), '本日の残高')]/span")).getText());
			
			//
			logger.info("b !{}!", browser.findElement(By.xpath("//span[contains(text(), '予定残高')]/./../../../../../../following-sibling::td/div/span/font")).getText());
			
			
			logger.info("get  balance bank");
			browser.get(FILE_BALANCE_BANK);
			browser.sleep(5000);
		}
		
	}
	
	public static void main(String[] args) {
		logger.info("START");
		
		download();
		update();
		
		logger.info("STOP");
	}
}
