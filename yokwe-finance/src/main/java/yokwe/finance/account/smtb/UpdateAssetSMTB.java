package yokwe.finance.account.smtb;

import java.io.File;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;

import yokwe.finance.Storage;
import yokwe.finance.account.Asset;
import yokwe.finance.account.Asset.Company;
import yokwe.finance.account.smtb.BalancePage.DepositJPY;
import yokwe.finance.account.smtb.BalancePage.TermDepositJPY;
import yokwe.finance.type.Currency;
import yokwe.util.FileUtil;

public class UpdateAssetSMTB {
	private static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();
	
	private static final Storage storage          = Storage.account.smtb;
	
	private static final File    FILE_TOP         = storage.getFile("top.html");
	private static final File    FILE_BALANCE     = storage.getFile("balance.html");
	
	public static void download() {
		try(var browser = new WebBrowserSMTB()) {
			logger.info("login");
			browser.login();
			browser.savePage(FILE_TOP);
			
			logger.info("balance");
			browser.balance();
			browser.savePage(FILE_BALANCE);
			
			logger.info("logout");
			browser.logout();
		}
	}
	
	public static void update() {
		var list = new ArrayList<Asset>();
		
		// build assetList
		{
			LocalDateTime dateTime;
			String        page;
			{
				var file = FILE_BALANCE;
				
				var instant = FileUtil.getLastModified(file);
				dateTime = LocalDateTime.ofInstant(instant, ZoneId.systemDefault()).truncatedTo(ChronoUnit.SECONDS);
				page     = FileUtil.read().file(file);
				logger.info("  {}  {}  {}", dateTime, page.length(), file.getPath());
			}
			
			{
				var depositJPY = DepositJPY.getInstance(page);
//				logger.info("depositJPY  {}", depositJPY);
				list.add(Asset.deposit(dateTime, Company.SMTB, Currency.JPY, depositJPY.value, "円普通預金"));
			}
			{
				var termDepositJPY = TermDepositJPY.getInstance(page);
//				logger.info("depositJPY  {}", depositJPY);
				list.add(Asset.depositTime(dateTime, Company.SMTB, Currency.JPY, termDepositJPY.value, "円定期預金"));
			}
		}
		
		for(var e: list) logger.info("list {}", e);
		
		logger.info("save  {}  {}", list.size(), StorageSMTB.Asset.getPath());
		StorageSMTB.Asset.save(list);
	}
	
	public static void main(String[] args) {
		logger.info("START");
				
		download();
		update();
		
		logger.info("STOP");
	}
}
