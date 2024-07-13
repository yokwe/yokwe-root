package yokwe.finance.account.sony;

import java.io.File;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.openqa.selenium.By;

import yokwe.finance.Storage;
import yokwe.finance.account.Asset;
import yokwe.finance.account.Secret;
import yokwe.finance.account.Asset.Company;
import yokwe.finance.account.UpdateAsset;
import yokwe.finance.fund.StorageFund;
import yokwe.finance.provider.sony.FundInfoSony;
import yokwe.finance.provider.sony.StorageSony;
import yokwe.finance.type.Currency;
import yokwe.finance.type.FundInfoJP;
import yokwe.finance.util.webbrowser.Target;
import yokwe.finance.util.webbrowser.WebBrowser;
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
	
	
	private static final Target LOGIN_A = new Target.Get("https://o2o.moneykit.net/NBG100001G01.html");
	private static final Target LOGIN_B = new Target.Click(By.linkText("ログイン"), "MONEYKit - ソニー銀行");
	
	private static final Target LOGOUT_A = new Target.Click(By.id("logout"));
	private static final Target LOGOUT_B = new Target.Javascript("subYes()", "THANK YOU");
	private static final Target LOGOUT_C = new Target.Javascript("allClose()");
	
	private static final Target TOP      = new Target.Javascript("hometop(1)");
	
	private static final Target BALANCE                 = new Target.Javascript("hometop(10)");
	private static final Target BALANCE_DEPOSIT         = new Target.Javascript("balancecommon(1)");
	private static final Target BALANCE_DEPOSIT_FOREIGN = new Target.Javascript("balancecommon(2)");
	private static final Target BALANCE_FUND            = new Target.Javascript("balancecommon(3)");
	
	public static boolean isSystemMaintenance(WebBrowser webBrowser) {
		LOGIN_A.action(webBrowser);
		return webBrowser.getTitle().contains("システムメンテナンス");
	}

	public static void login(WebBrowser browser) {
		var secret = Secret.read().sony;
		login(browser, secret.account, secret.password);
	}
	public static void login(WebBrowser browser, String account, String password) {
		LOGIN_A.action(browser);
		browser.sendKey(By.name("KozaNo"),   account);
		browser.sendKey(By.name("Password"), password);
		
		LOGIN_B.action(browser);
	}
	
	public static void logout(WebBrowser browser) {
		LOGOUT_A.action(browser);
		browser.switchToByTitleContains("ログアウト");
		
		LOGOUT_B.action(browser);
		LOGOUT_C.action(browser);
	}
	
	public static void top(WebBrowser browser) {
		TOP.action(browser);
	}
	
	public static void balance(WebBrowser browser) {
		BALANCE.action(browser);
	}
	public static void balanceDeopsit(WebBrowser browser) {
		BALANCE_DEPOSIT.action(browser);
	}
	public static void balanceDeopsitForeign(WebBrowser browser) {
		BALANCE_DEPOSIT_FOREIGN.action(browser);
	}
	public static void balanceFund(WebBrowser browser) {
		BALANCE_FUND.action(browser);
	}
	
	
	@Override
	public Storage getStorage() {
		return storage;
	}
	
	
	private static final Map<String, FundInfoSony> fundInfoSonyMap = StorageSony.FundInfoSony.getList().stream().collect(Collectors.toMap(o -> o.sbFundCode, Function.identity()));
	private static final Map<String, FundInfoJP>   fundInfoMap     = StorageFund.FundInfo.getMap();
	private static FundInfoJP getFundInfo(String fundCode, String fundName) {
		// 109020441 => 09020441
		// 123456789    12345678
		if (fundCode.length() != 9) {
			logger.error("Unexpected fundCode");
			logger.error("  fundCode  {}!", fundCode);
			logger.error("  fundName  {}!", fundName);
			throw new UnexpectedException("Unexpected fundCode");
		}
		var fundCode8 = fundCode.substring(1);
//		logger.info("getFundInfo  {}  {}", fundCode, fundName);
		var fundInfoSony = fundInfoSonyMap.get(fundCode8);
		if (fundInfoSony == null) {
			logger.error("Unexpected fundCode");
			logger.error("  fundCode  {}!", fundCode);
			logger.error("  fundName  {}!", fundName);
			throw new UnexpectedException("Unexpected fundCode");
		}
		var fundInfo = fundInfoMap.get(fundInfoSony.isinCode);
		if (fundInfo == null) {
			logger.error("Unexpected isinCode");
			logger.error("  fundCode      {}!", fundCode);
			logger.error("  fundName      {}!", fundName);
			logger.error("  fundInfoSony  {}", fundInfoSony);
			throw new UnexpectedException("Unexpected fundCode");
		}
		return fundInfo;
	}
	
	
	@Override
	public void download() {
process:
		try(var browser = new WebBrowser()) {
			// check system maintenance
			if (isSystemMaintenance(browser)) {
				logger.info("skip system maintenance");
				break process;
			}

			logger.info("login");
			login(browser);
			browser.savePage(FILE_TOP);
			
			logger.info("balance");
			balance(browser);
			browser.savePage(FILE_BALANCE);
			
			logger.info("balance deposit");
			balanceDeopsit(browser);
			browser.savePage(FILE_BALANCE_DEPOSIT);
			
			logger.info("balance deposit foreign");
			balanceDeopsitForeign(browser);
			browser.savePage(FILE_BALANCE_DEPOSIT_FOREIGN);
			
			logger.info("balance fund");
			balanceFund(browser);
			browser.savePage(FILE_BALANCE_FUND);
			
			logger.info("logout");
			logout(browser);
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
//				logger.info("fundJPYList  {}", fundJPYList.size());
				for(var e: fundJPYList) {
//					logger.info("fundJPYList {}", e);
					var fund      = getFundInfo(e.fundCode, e.name);
					var value     = e.value;
					var cost      = e.value.subtract(e.profit);
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
				
		instance.download();
		instance.update();
		
		logger.info("STOP");
	}
}
