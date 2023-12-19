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
import yokwe.finance.account.AssetRisk;
import yokwe.finance.account.Asset.Company;
import yokwe.finance.fund.StorageFund;
import yokwe.finance.type.Currency;
import yokwe.finance.type.FundInfoJP;
import yokwe.util.FileUtil;
import yokwe.util.UnexpectedException;

public class UpdateAssetSony {
	private static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();
	
	private static final Storage storage = Storage.account.sony;
	
	private static final File FILE_TOP                     = storage.getFile("top.html");
	private static final File FILE_BALANCE                 = storage.getFile("balance.html");
	private static final File FILE_BALANCE_DEPOSIT         = storage.getFile("balance-deposit.html");
	private static final File FILE_BALANCE_DEPOSIT_FOREIGN = storage.getFile("balance-deposit-foreign.html");
	private static final File FILE_BALANCE_FUND            = storage.getFile("balance-fund.html");
	
	
	private static final List<FundInfoJP> fundInfoList = StorageFund.FundInfo.getList();
	private static FundInfoJP getFundInfo(String name) {
		String isinCode = null;
		for(var e: fundInfoList) {
			if (e.name.equals(name)) return e;
		}
		if (isinCode == null) {
			if (name.contains("(")) {
				String nameB = name.substring(0, name.indexOf("("));
				for(var e: fundInfoList) {
					if (e.name.equals(nameB)) return e;
				}
			}
		}
		
		logger.error("Unexpected name");
		logger.error("  name  {}!", name);
		throw new UnexpectedException("Unexpected name");
	}
	
	
	public static void download() {
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
	
	public static void update() {
		var list = new ArrayList<Asset>();
		
		{
			LocalDateTime dateTime;
			String        page;
			{
				var file = FILE_BALANCE_DEPOSIT;
				
				var instant = FileUtil.getLastModified(file);
				dateTime = LocalDateTime.ofInstant(instant, ZoneId.systemDefault()).truncatedTo(ChronoUnit.SECONDS);
				page     = FileUtil.read().file(file);
				logger.info("  {}  {}  {}", dateTime, page.length(), file.getPath());
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
				var file = FILE_BALANCE_FUND;
				
				var instant = FileUtil.getLastModified(file);
				dateTime = LocalDateTime.ofInstant(instant, ZoneId.systemDefault()).truncatedTo(ChronoUnit.SECONDS);
				page     = FileUtil.read().file(file);
				logger.info("  {}  {}  {}", dateTime, page.length(), file.getPath());
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
				var file = FILE_BALANCE_DEPOSIT_FOREIGN;
				
				var instant = FileUtil.getLastModified(file);
				dateTime = LocalDateTime.ofInstant(instant, ZoneId.systemDefault()).truncatedTo(ChronoUnit.SECONDS);
				page     = FileUtil.read().file(file);
				logger.info("  {}  {}  {}", dateTime, page.length(), file.getPath());
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
				var file = FILE_BALANCE_FUND;
				
				var instant = FileUtil.getLastModified(file);
				dateTime = LocalDateTime.ofInstant(instant, ZoneId.systemDefault()).truncatedTo(ChronoUnit.SECONDS);
				page     = FileUtil.read().file(file);
				logger.info("  {}  {}  {}", dateTime, page.length(), file.getPath());
			}
			{
				var fundJPYList = BalancePage.FundJPY.getInstance(page);
				for(var e: fundJPYList) {
//					logger.info("fundJPYList {}", e);
					var fund  = getFundInfo(e.name.replace("　", ""));
					var name  = fund.name;
					var code  = fund.isinCode;
					var value = e.value;
					var risk  = AssetRisk.fundCode.getRisk(code); 
					list.add(Asset.fund(dateTime, Company.SONY, Currency.JPY, value, risk, code, name));
				}
			}
		}

		for(var e: list) logger.info("list {}", e);
		
		logger.info("save  {}  {}", list.size(), StorageSony.Asset.getPath());
		StorageSony.Asset.save(list);
	}
	
	public static void main(String[] args) {
		logger.info("START");
				
		download();
		update();
		
		logger.info("STOP");
	}
}
