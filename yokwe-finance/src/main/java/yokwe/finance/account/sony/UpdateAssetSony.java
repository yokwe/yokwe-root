package yokwe.finance.account.sony;

import java.io.File;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriverException;

import yokwe.finance.Storage;
import yokwe.finance.account.Asset;
import yokwe.finance.account.Asset.Company;
import yokwe.finance.account.Secret;
import yokwe.finance.account.UpdateAsset;
import yokwe.finance.fund.StorageFund;
import yokwe.finance.provider.sony.FundInfoSony;
import yokwe.finance.provider.sony.StorageSony;
import yokwe.finance.type.Currency;
import yokwe.finance.type.FundInfoJP;
import yokwe.util.FileUtil;
import yokwe.util.UnexpectedException;
import yokwe.util.selenium.WebDriverWrapper;

public final class UpdateAssetSony implements UpdateAsset {
	private static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();
	
	private static final Storage storage = Storage.account.sony;
	
	private static final File FILE_TOP                     = storage.getFile("top.html");
	private static final File FILE_BALANCE                 = storage.getFile("balance.html");
	private static final File FILE_BALANCE_DEPOSIT         = storage.getFile("balance-deposit.html");
	private static final File FILE_BALANCE_DEPOSIT_FOREIGN = storage.getFile("balance-deposit-foreign.html");
	private static final File FILE_BALANCE_FUND            = storage.getFile("balance-fund.html");
	
	@Override
	public Storage getStorage() {
		return storage;
	}
	
		
	@Override
	public void download() {
//		var driver = WebDriverWrapper.Factory.createChrome();
		var driver = WebDriverWrapper.Factory.createSafari();
		
		try {
			// login
			{
				logger.info("login");
				driver.getAndWait("https://sonybank.jp/pages/db/dbca0100/?lang=ja");
				
				if (driver.getTitle().contains("システムメンテナンス")) {
					logger.info("skip system maintenance");
					return;
				}
				
				var secret = Secret.read().sony;
				driver.sendKey(By.id("brchNum"),            secret.branch);
				driver.sendKey(By.id("accountNum"),         secret.account);
				driver.sendKey(By.id("loginPwd_inputPass"), secret.password);		
				driver.clickAndWait(By.id("loginBtn"));
				driver.savePage(FILE_TOP);
				
				if (driver.getTitle().contains("システムメンテナンス")) {
					logger.info("skip system maintenance");
					return;
				}
				
				// sanity check
				driver.check.titleContains("トップ - ソニー銀行");
			}
			
			// balance
			{
				logger.info("balance");
				// btnBalance
				driver.clickAndWait(By.id("btnBalance"));
				driver.savePage(FILE_BALANCE);
				
				logger.info("balance deposit");
				driver.getAndWait("https://sonybank.jp/pages/dc/dcba1200/");
				driver.savePage(FILE_BALANCE_DEPOSIT);

				logger.info("balance deposit foreign");
				driver.getAndWait("https://sonybank.jp/pages/dc/dcba1300/");
				driver.savePage(FILE_BALANCE_DEPOSIT_FOREIGN);

				logger.info("balance fund");
				driver.getAndWait("https://sonybank.jp/pages/ia/iaca6300/");
				if (driver.getPageSource().contains("現在メンテナンス中")) {
					logger.info("businessErrorCloseButton");
					driver.clickAndWait(By.xpath("//button[@data-testid='businessErrorCloseButton']"));
				} else {
					driver.savePage(FILE_BALANCE_FUND);
				}
			}
			
			// logout
			{
				logger.info("logout");
				logger.info("userIcon");
				driver.clickAndWait(By.id("userIcon"));
				logger.info("logout");
				driver.clickAndWait(By.id("logout"));
				logger.info("decisionBtn");
				driver.clickAndWait(By.id("decisionBtn"));
				logger.info("closeBtn");
				driver.clickAndWait(By.id("closeBtn"));
				
				logger.info("sleep");
				driver.sleep(Duration.ofSeconds(2));
			}
		} catch (WebDriverException e) {
			String exceptionName = e.getClass().getSimpleName();
			logger.warn("{} {}", exceptionName, e);
		} finally {
			driver.quit();
		}
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
					var units     = e.units;
					var unitPrice = e.unitPrice;
					var value     = e.value;
					var cost      = e.value.subtract(e.profit);
					var code      = fund.isinCode;
					var name      = fund.name;
					list.add(Asset.fund(dateTime, Company.SONY, Currency.JPY, units.intValue(), unitPrice, value, cost, code, name));
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
//		instance.update();
		
		logger.info("STOP");
	}
}
