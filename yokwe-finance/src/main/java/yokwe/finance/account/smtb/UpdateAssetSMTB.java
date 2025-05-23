package yokwe.finance.account.smtb;

import java.io.File;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.stream.Collectors;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriverException;

import yokwe.finance.Storage;
import yokwe.finance.account.Asset;
import yokwe.finance.account.Asset.Company;
import yokwe.finance.account.Secret;
import yokwe.finance.account.UpdateAsset;
import yokwe.finance.account.smtb.BalancePage.DepositJPY;
import yokwe.finance.account.smtb.BalancePage.Fund;
import yokwe.finance.account.smtb.BalancePage.TermDepositJPY;
import yokwe.finance.fund.StorageFund;
import yokwe.finance.type.Currency;
import yokwe.util.FileUtil;
import yokwe.util.UnexpectedException;
import yokwe.util.selenium.WebDriverWrapper;

public final class UpdateAssetSMTB implements UpdateAsset {
	private static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();
	
	private static final Storage storage          = Storage.account.smtb;
	
	private static final File    FILE_LOGIN       = storage.getFile("login.html");
	private static final File    FILE_TOP         = storage.getFile("top.html");
	private static final File    FILE_BALANCE     = storage.getFile("balance.html");
	private static final File    FILE_FUND        = storage.getFile("fund.html");
	private static final File    FILE_LOGOUT      = storage.getFile("logout.html");

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
				driver.getAndWait("https://direct.smtb.jp/ap1/ib/login.do");
				driver.savePage(FILE_LOGIN);
				// sanity check
				driver.check.titleContains("ログイン");
				
				var secret = Secret.read().smtb;
				driver.sendKey(By.name("kaiinNo"),    secret.account);
				driver.sendKey(By.name("ibpassword"), secret.password);
				driver.clickAndWait(By.xpath("//input[contains(@value, 'ログイン')]"));
				driver.savePage(FILE_TOP);
				// sanity check
				driver.check.titleContains("トップページ");
			}
			
			// balance
			{
				logger.info("balance");
				driver.clickAndWait(By.xpath("//img[@alt='お取引き・残高照会']"));
				driver.savePage(FILE_BALANCE);
				// sanity check
				driver.check.titleContains("お取引・残高照会");
			}
			// fund
			{
				logger.info("fund");
				driver.clickAndWait(By.xpath("//input[contains(@value, '残高明細・売却')]"));
				driver.savePage(FILE_FUND);
				// sanity check
				driver.check.titleContains("投資信託売却｜保管残高明細");
			}
			
			// logout
			{
				logger.info("logout");
				driver.clickAndWait(By.xpath("//img[@alt='ログアウト']"));
				driver.savePage(FILE_LOGOUT);
				// sanity check
				driver.check.titleContains("ログアウト");
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
				page     = FileUtil.read().file(htmlFile);
				logger.info("  {}  {}  {}", dateTime, page.length(), htmlFile.getPath());
			}
			
			{
				var depositJPY = DepositJPY.getInstance(page);
//				logger.info("depositJPY  {}", depositJPY);
				list.add(Asset.deposit(dateTime, Company.SMTB, Currency.JPY, depositJPY.value, "円普通預金"));
			}
			{
				var termDepositJPY = TermDepositJPY.getInstance(page);
//				logger.info("depositJPY  {}", depositJPY);
				list.add(Asset.termDeposit(dateTime, Company.SMTB, Currency.JPY, termDepositJPY.value, "円定期預金"));
			}
		}
		{
			LocalDateTime dateTime;
			String        page;
			{
				var htmlFile = FILE_FUND;
				
				var instant = FileUtil.getLastModified(htmlFile);
				dateTime = LocalDateTime.ofInstant(instant, ZoneId.systemDefault()).truncatedTo(ChronoUnit.SECONDS);
				page     = FileUtil.read().file(htmlFile);
				logger.info("  {}  {}  {}", dateTime, page.length(), htmlFile.getPath());
			}
			
			{
				var fundInfoMap = StorageFund.FundInfo.getList().stream().collect(Collectors.toMap(o -> o.fundCode, o -> o));
				var fundList    = Fund.getInstance(page);
				for(var fund: fundList) {
//					logger.info("fund  {}", fund);
					var fundInfo  = fundInfoMap.get(fund.fundCode);
					if (fundInfo == null) {
						logger.error("Unexpecetd fundCode");
						logger.error("  fund  {}", fund.toString());
						throw new UnexpectedException("Unexpecetd fundCode");
					}
					
					var code      = fundInfo.isinCode;
					var units     = fund.units;
					var unitPrice = fund.unitPrice;
					var value     = fund.value;
					var cost      = fund.cost;
					var name      = fundInfo.name;
//					logger.info("fund  {}  {}  {}  {}  {}", code, units, unitPrice, value, cost);
					list.add(Asset.fund(dateTime, Company.SMTB, Currency.JPY, units.intValue(), unitPrice, value, cost, code, name));
				}
			}
		}
		
		for(var e: list) logger.info("list {}", e);
		
		logger.info("save  {}  {}", list.size(), file.getPath());
		save(list);
	}
	
	private static final UpdateAsset instance = new UpdateAssetSMTB();
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
