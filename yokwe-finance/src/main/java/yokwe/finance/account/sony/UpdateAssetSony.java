package yokwe.finance.account.sony;

import java.io.File;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

import yokwe.finance.Storage;
import yokwe.finance.account.Asset;
import yokwe.finance.account.Asset.Company;
import yokwe.finance.account.UpdateAsset;
import yokwe.finance.fund.StorageFund;
import yokwe.finance.type.Currency;
import yokwe.finance.type.FundInfoJP;
import yokwe.util.FileUtil;
import yokwe.util.UnexpectedException;

public final class UpdateAssetSony implements UpdateAsset {
	private static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();
	
	private static final Storage storage = Storage.account.sony;
	
	private static final File FILE_TOP                     = storage.getFile("top.html");
	private static final File FILE_BALANCE                 = storage.getFile("balance.html");
	private static final File FILE_BALANCE_DEPOSIT         = storage.getFile("balance-deposit.html");
	private static final File FILE_BALANCE_DEPOSIT_FOREIGN = storage.getFile("balance-deposit-foreign.html");
	private static final File FILE_BALANCE_FUND            = storage.getFile("balance-fund.html");
	
	private static final File[] FILES = {
		FILE_TOP,
		FILE_BALANCE,
		FILE_BALANCE_DEPOSIT,
		FILE_BALANCE_DEPOSIT_FOREIGN,
		FILE_BALANCE_FUND,
	};
	
	
	@Override
	public Storage getStorage() {
		return storage;
	}
	
	private static final List<FundInfoJP> fundInfoList = StorageFund.FundInfo.getList();
	private static FundInfoJP getFundInfo(String name) {
		for(var e: fundInfoList) {
			if (e.name.equals(name)) return e;
		}
		if (name.contains("(")) {
			String nameB = name.substring(0, name.indexOf("("));
			for(var e: fundInfoList) {
				if (e.name.equals(nameB)) return e;
			}
		}
		{
			String nameStripped = name.replace("　", "");
			for(var e: fundInfoList) {
				String fundNameStripped = e.name.replace("　", "");
				if (fundNameStripped.equals(nameStripped)) return e;
			}
		}
		
		logger.error("Unexpected name");
		logger.error("  name  {}!", name);
		throw new UnexpectedException("Unexpected name");
	}
	
	
	@Override
	public void download() {
		deleteFile(FILES);
		
		try(var browser = new WebBrowserSony()) {
			logger.info("login");
			browser.login();
			browser.savePage(FILE_TOP);
			
			logger.info("balance");
			browser.balance();
			browser.savePage(FILE_BALANCE);
			
			logger.info("balance deposit");
			browser.balanceDeopsit();
			browser.savePage(FILE_BALANCE_DEPOSIT);
			
			logger.info("balance deposit foreign");
			browser.balanceDeopsitForeign();
			browser.savePage(FILE_BALANCE_DEPOSIT_FOREIGN);
			
			logger.info("balance fund");
			browser.balanceFund();
			browser.savePage(FILE_BALANCE_FUND);
			
			logger.info("logout");
			browser.logout();
		}
	}
	
	@Override
	public void update() {
		File file = getFile();
		file.delete();
		
		var list = new ArrayList<Asset>();
		
		{
			LocalDateTime dateTime;
			String        page;
			{
				var htmlFile = FILE_BALANCE_DEPOSIT;
				
				var instant = FileUtil.getLastModified(htmlFile);
				dateTime = LocalDateTime.ofInstant(instant, ZoneId.systemDefault()).truncatedTo(ChronoUnit.SECONDS);
				page     = FileUtil.read().file(htmlFile);
				logger.info("  {}  {}  {}", dateTime, page.length(), htmlFile.getPath());
			}
			
			{
				var depositJPYList = BalancePage.DepositJPY.getInstance(page);
				for(var e: depositJPYList) {
//					logger.info("depositJPYList {}", e);
					list.add(Asset.deposit(dateTime, Company.SONY, Currency.JPY, e.value, e.name));
				}
			}
		}
		{
			LocalDateTime dateTime;
			String        page;
			{
				var htmlFile = FILE_BALANCE_FUND;
				
				var instant = FileUtil.getLastModified(htmlFile);
				dateTime = LocalDateTime.ofInstant(instant, ZoneId.systemDefault()).truncatedTo(ChronoUnit.SECONDS);
				page     = FileUtil.read().file(htmlFile);
				logger.info("  {}  {}  {}", dateTime, page.length(), htmlFile.getPath());
			}
			{
				var depositForeignList = BalancePage.DepositForeign.getInstance(page);
				for(var e: depositForeignList) {
					logger.info("depositForeignList {}", e);
					var currency = Currency.valueOf(e.currency);
					var value    = e.value;
					if (value.compareTo(BigDecimal.ZERO) == 0) continue;
					list.add(Asset.deposit(dateTime, Company.SONY, currency, value, "外貨預金"));
				}
			}
		}
		{
			LocalDateTime dateTime;
			String        page;
			{
				var htmlFile = FILE_BALANCE_DEPOSIT_FOREIGN;
				
				var instant = FileUtil.getLastModified(htmlFile);
				dateTime = LocalDateTime.ofInstant(instant, ZoneId.systemDefault()).truncatedTo(ChronoUnit.SECONDS);
				page     = FileUtil.read().file(htmlFile);
				logger.info("  {}  {}  {}", dateTime, page.length(), htmlFile.getPath());
			}
			{
				var depositForeignList = BalancePage.DepositForeign.getInstance(page);
				for(var e: depositForeignList) {
//					logger.info("depositForeignList {}", e);
					var currency = Currency.valueOf(e.currency);
					var value    = e.value;
					if (value.compareTo(BigDecimal.ZERO) == 0) continue;
					list.add(Asset.deposit(dateTime, Company.SONY, currency, value, "外貨預金"));
				}
			}
		}
		{
			LocalDateTime dateTime;
			String        page;
			{
				var htmlFile = FILE_BALANCE_FUND;
				
				var instant = FileUtil.getLastModified(htmlFile);
				dateTime = LocalDateTime.ofInstant(instant, ZoneId.systemDefault()).truncatedTo(ChronoUnit.SECONDS);
				page     = FileUtil.read().file(htmlFile);
				logger.info("  {}  {}  {}", dateTime, page.length(), htmlFile.getPath());
			}
			{
				var fundJPYList = BalancePage.FundJPY.getInstance(page);
				for(var e: fundJPYList) {
//					logger.info("fundJPYList {}", e);
					var fund      = getFundInfo(e.name.replace("　", ""));
					var profit    = e.profit;

					var value     = e.value;
					var cost      = e.value.add(profit);
					var code      = fund.isinCode;
					var name      = fund.name;
					list.add(Asset.fund(dateTime, Company.SONY, Currency.JPY, value, cost, code, name));
				}
			}
		}

		for(var e: list) logger.info("list {}", e);
		
		logger.info("save  {}  {}", list.size(), file.getPath());
		save(list);
	}
	
	private static final UpdateAsset instance = new UpdateAssetSony();
	public static UpdateAsset getInstance() {
		return instance;
	}
	
	public static void main(String[] args) {
		logger.info("START");
				
//		instance.download();
		instance.update();
		
		logger.info("STOP");
	}
}
