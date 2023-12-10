package yokwe.finance.account.prestia;

import java.io.File;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;

import yokwe.finance.Storage;
import yokwe.finance.account.Asset;
import yokwe.finance.account.nikko.StorageNikko;
import yokwe.finance.account.prestia.BalancePage.DepositJPY;
import yokwe.finance.account.prestia.BalancePage.DepositMultiMoney;
import yokwe.finance.account.prestia.BalancePage.DepositMultiMoneyJPY;
import yokwe.finance.account.prestia.BalancePage.DepositUSD;
import yokwe.finance.account.prestia.BalancePage.TermDepositForeign;
import yokwe.util.FileUtil;

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
		var list = new ArrayList<Asset>();
		
		// build assetList
		{
			LocalDateTime dateTime;
			String        page;
			{
				var file = FILE_BALANCE;
				
				var instant = FileUtil.getLastModified(file);
				dateTime = LocalDateTime.ofInstant(instant, ZoneId.systemDefault()).truncatedTo(ChronoUnit.SECONDS);
				logger.info("dateTime  {}  {}", dateTime, file.getName());
				
				page = FileUtil.read().file(file);
			}
			
			
			{
				var depositJPY = DepositJPY.getInstance(page);
				logger.info("depositJPY  {}", depositJPY);
			}
			{
				var depositMultiMoneyJPY = DepositMultiMoneyJPY.getInstance(page);
				logger.info("depositMultiMoneyJPY  {}", depositMultiMoneyJPY);
			}
			{
				var depositUSD = DepositUSD.getInstance(page);
				logger.info("depositUSD  {}", depositUSD);
			}
			{
				var depositMultiMoney = DepositMultiMoney.getInstance(page);
				logger.info("depositMultiMoney  {}", depositMultiMoney.size());
				for(var e: depositMultiMoney) {
					logger.info("depositMultiMoney  {}", e);
				}
			}
			// 外貨定期預金
			{
				var termDepositForeign = TermDepositForeign.getInstance(page);
				logger.info("termDepositForeign  {}", termDepositForeign.size());
				for(var e: termDepositForeign) {
					logger.info("termDepositForeign  {}", e);
				}
			}
		}
		
		for(var e: list) {
			logger.info("list {}", e);
		}
		
		logger.info("save  {}  {}", list.size(), StorageNikko.Asset.getPath());
		StorageNikko.Asset.save(list);
	}
	
	public static void main(String[] args) {
		logger.info("START");
				
//		download();
		update();
		
		logger.info("STOP");
	}
}
