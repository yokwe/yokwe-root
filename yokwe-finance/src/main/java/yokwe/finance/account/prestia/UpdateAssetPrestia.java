package yokwe.finance.account.prestia;

import java.io.File;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.interactions.Actions;

import yokwe.finance.Storage;
import yokwe.finance.account.Asset;
import yokwe.finance.account.Asset.Company;
import yokwe.finance.account.Secret;
import yokwe.finance.account.UpdateAsset;
import yokwe.finance.account.prestia.BalancePage.DepositJPY;
import yokwe.finance.account.prestia.BalancePage.DepositMultiMoney;
import yokwe.finance.account.prestia.BalancePage.DepositMultiMoneyJPY;
import yokwe.finance.account.prestia.BalancePage.DepositUSD;
import yokwe.finance.account.prestia.BalancePage.TermDepositForeign;
import yokwe.finance.account.prestia.FundPage.FundReturns;
import yokwe.finance.provider.prestia.StoragePrestia;
import yokwe.finance.type.Currency;
import yokwe.util.FileUtil;
import yokwe.util.UnexpectedException;
import yokwe.util.selenium.ChromeWebDriver;

public final class UpdateAssetPrestia implements UpdateAsset {
	private static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();
	
	private static final Storage storage = Storage.account.prestia;
	
	private static final File FILE_LOGIN_A         = storage.getFile("login-a.html");
	private static final File FILE_LOGIN_B         = storage.getFile("login-b.html");
	private static final File FILE_LOGOUT          = storage.getFile("logout.html");

	private static final File FILE_BALANCE         = storage.getFile("balance.html");
	private static final File FILE_FUND_RETURNS    = storage.getFile("fund-returns.html");
	
	
	@Override
	public Storage getStorage() {
		return storage;
	}
	
	@Override
	public void download() {
		var builder = ChromeWebDriver.builder();
//		builder.withArguments("--headless");
		var driver = builder.build();
		try {
			// login
			{
				logger.info("login");
				driver.get("https://login.smbctb.co.jp/ib/portal/POSNIN1prestiatop.prst");
				driver.wait.untilPageTansitionFinish();
				driver.savePageSource(FILE_LOGIN_A);
				// sanity check
				driver.check.titleContains("プレスティア オンライン");
				
				var secret = Secret.read().prestia;
				driver.wait.untilPresenceOfElement(By.id("dispuserId")).sendKeys(secret.account);
				driver.wait.untilPresenceOfElement(By.id("disppassword")).sendKeys(secret.password);
				driver.wait.untilPresenceOfElement(By.linkText("サインオン")).click();
				driver.wait.untilPageTansitionFinish();
				driver.savePageSource(FILE_LOGIN_B);
				// sanity check
				driver.check.pageContains("代表口座");
			}
			
			// balance
			{
				logger.info("balance");
				driver.wait.untilPresenceOfElement(By.id("header-nav-label-0")).click();
				driver.wait.untilPageTansitionFinish();
				driver.wait.untilPresenceOfElement(By.linkText("口座残高")).click();
				driver.wait.untilPageTansitionFinish();
				driver.savePageSource(FILE_BALANCE);
			}
			
			// fund
			{
				logger.info("fund enter");
				driver.wait.untilPresenceOfElement(By.id("header-nav-label-3")).click();
				driver.wait.untilPageTansitionFinish();
				driver.wait.untilPresenceOfElement(By.linkText("投資信託サービス")).click();
				driver.wait.untilPageTansitionFinish();
				driver.check.titleContains("インターネットバンキング投資信託");
				
				logger.info("fund return");
				// hover mouse to navi02_03_active
				new Actions(driver).moveToElement(driver.findElement(By.id("navi02_03_active"))).perform();
				driver.wait.untilPageTansitionFinish();
				driver.wait.untilPresenceOfElement(By.xpath("//*[@id=\"navi02_03\"]/li[4]/a")).click();
				driver.wait.untilPageTansitionFinish();
				driver.savePageSource(FILE_FUND_RETURNS);
				
				logger.info("fund exit");
				driver.wait.untilPresenceOfElement(By.xpath("//*[@id=\"header\"]/img[1]")).click();
				driver.wait.untilPageTansitionFinish();
				driver.check.titleContains("プレスティア オンライン");
			}
			
			// logout
			{
				logger.info("logout");
				// By.linkText("サインオフ")
				driver.wait.untilPresenceOfElement(By.linkText("サインオフ")).click();
				driver.wait.untilPageTansitionFinish();
				driver.savePageSource(FILE_LOGOUT);
				// sanity check
				driver.check.pageContains("サインオフが完了しました");
			}
		} catch (WebDriverException e){
			String exceptionName = e.getClass().getSimpleName();
			logger.warn("{} {}", exceptionName, e);
		} finally {
			driver.quit();
		}
	}
	
	@Override
	public void update() {
		File file = getFile();
		file.delete();
		
		var list = new ArrayList<Asset>();
		
		// build assetList
		{
			LocalDateTime dateTime;
			String        page;
			{
				var htmlFile = FILE_BALANCE;
				
				var instant = FileUtil.getLastModified(htmlFile);
				dateTime = LocalDateTime.ofInstant(instant, ZoneId.systemDefault()).truncatedTo(ChronoUnit.SECONDS);
				logger.info("dateTime  {}  {}", dateTime, htmlFile.getName());
				
				page = FileUtil.read().file(htmlFile);
			}
			
			// 円普通預金
			{
				var depositJPY = DepositJPY.getInstance(page);
//				logger.info("depositJPY  {}", depositJPY);
				list.add(Asset.deposit(dateTime, Company.PRESTIA, Currency.JPY, BigDecimal.valueOf(depositJPY.value), "円普通預金"));
			}
			// マルチマネー口座円普通預金
			{
				var depositMultiMoneyJPY = DepositMultiMoneyJPY.getInstance(page);
//				logger.info("depositMultiMoneyJPY  {}", depositMultiMoneyJPY);
				var value = BigDecimal.valueOf(depositMultiMoneyJPY.value);
				if (value.compareTo(BigDecimal.ZERO) != 0) {
					list.add(Asset.deposit(dateTime, Company.PRESTIA, Currency.JPY, BigDecimal.valueOf(depositMultiMoneyJPY.value), "マルチマネー口座円普通預金"));
				}
			}
			// 米ドル普通預金
			{
				var depositUSD = DepositUSD.getInstance(page);
//				logger.info("depositUSD  {}", depositUSD);
				var value = depositUSD.value;
				if (value.compareTo(BigDecimal.ZERO) != 0) {
					Currency currency = Currency.valueOf(depositUSD.currency);
					list.add(Asset.deposit(dateTime, Company.PRESTIA, currency, value, "米ドル普通預金"));
				}
			}
			// マルチマネー口座外貨普通預金
			{
				var depositMultiMoney = DepositMultiMoney.getInstance(page);
//				logger.info("depositMultiMoney  {}", depositMultiMoney.size());
				for(var e: depositMultiMoney) {
//					logger.info("depositMultiMoney  {}", e);
					var value = e.value;
					if (value.compareTo(BigDecimal.ZERO) != 0) {
						Currency currency = Currency.valueOf(e.currency);
						list.add(Asset.deposit(dateTime, Company.PRESTIA, currency, e.value, "マルチマネー口座外貨普通預金"));
					}
				}
			}
			// 外貨定期預金
			{
				var termDepositForeign = TermDepositForeign.getInstance(page);
//				logger.info("termDepositForeign  {}", termDepositForeign.size());
				for(var e: termDepositForeign) {
//					logger.info("termDepositForeign  {}", e);
					var value = e.value;
					if (value.compareTo(BigDecimal.ZERO) != 0) {
						Currency currency = Currency.valueOf(e.currency);
						list.add(Asset.termDeposit(dateTime, Company.PRESTIA, currency, e.value, "外貨定期預金"));
					}
				}
			}
		}
		{
			LocalDateTime dateTime;
			String        page;
			{
				var htmlFile = FILE_FUND_RETURNS;
				
				var instant = FileUtil.getLastModified(htmlFile);
				dateTime = LocalDateTime.ofInstant(instant, ZoneId.systemDefault()).truncatedTo(ChronoUnit.SECONDS);
				logger.info("dateTime  {}  {}", dateTime, htmlFile.getName());
				
				page = FileUtil.read().file(htmlFile);
			}
			// 投資信託　トータルリターン
			{
				var fundInfoMap   = StoragePrestia.FundInfoPrestia.getList().stream().collect(Collectors.toMap(o -> o.fundCode, Function.identity()));
				
				{
					var fundReturnsList = new ArrayList<FundReturns>();
					fundReturnsList.addAll(FundReturns.getInstanceUS(page));
					fundReturnsList.addAll(FundReturns.getInstanceJP(page));
					
					for(var e: fundReturnsList) {
//						logger.info("fundReturns  {}", e);
						var fundInfo = fundInfoMap.get(e.fundCode);
						if (fundInfo == null) {
							logger.error("Unexpected fundCode");
							logger.error("  fundReturns  {}", e.toString());
							throw new UnexpectedException("Unexpected fundCode");
						}
						
						var currency  = fundInfo.currency;
						var units     = e.units;
						var unitPrice = e.unitPrice;
						var value     = e.value;
						var cost      = e.buyTotal.subtract(e.soldTotal).stripTrailingZeros();
						var fundCode  = fundInfo.isinCode;
						var fundName  = e.fundName;
						list.add(Asset.fund(dateTime, Company.PRESTIA, currency, units.intValue(), unitPrice, value, cost, fundCode, fundName));
					}
				}
			}
		}
		
		for(var e: list) logger.info("list {}", e);
		
		logger.info("save  {}  {}", list.size(), file.getPath());
		save(list);
	}
	
	private static final UpdateAsset instance = new UpdateAssetPrestia();
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
