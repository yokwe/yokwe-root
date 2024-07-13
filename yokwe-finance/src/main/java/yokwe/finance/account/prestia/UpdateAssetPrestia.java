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

import yokwe.finance.Storage;
import yokwe.finance.account.Asset;
import yokwe.finance.account.Secret;
import yokwe.finance.account.Asset.Company;
import yokwe.finance.account.UpdateAsset;
import yokwe.finance.account.prestia.BalancePage.DepositJPY;
import yokwe.finance.account.prestia.BalancePage.DepositMultiMoney;
import yokwe.finance.account.prestia.BalancePage.DepositMultiMoneyJPY;
import yokwe.finance.account.prestia.BalancePage.DepositUSD;
import yokwe.finance.account.prestia.BalancePage.TermDepositForeign;
import yokwe.finance.account.prestia.FundPage.FundReturns;
import yokwe.finance.provider.prestia.StoragePrestia;
import yokwe.finance.type.Currency;
import yokwe.finance.util.webbrowser.Target;
import yokwe.finance.util.webbrowser.WebBrowser;
import yokwe.util.FileUtil;
import yokwe.util.UnexpectedException;

public final class UpdateAssetPrestia implements UpdateAsset {
	private static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();
	
	private static final Storage storage = Storage.account.prestia;
	
	private static final File FILE_BALANCE         = storage.getFile("balance.html");
	private static final File FILE_FUND_RETURNS    = storage.getFile("fund-returns.html");
	
	private static final Target LOGIN_A = new Target.Get("https://login.smbctb.co.jp/ib/portal/POSNIN1prestiatop.prst", "プレスティア オンライン");
	private static final Target LOGIN_B = new Target.Click(By.linkText("サインオン"));
	private static final Target LOGOUT  = new Target.Click(By.linkText("サインオフ"));
	
	private static final Target BALANCE_A = new Target.Click(By.id("header-nav-label-0"));
	private static final Target BALANCE_B = new Target.Click(By.linkText("口座残高"));
	
	private static final Target FUND_ENTER_A = new Target.Click(By.id("header-nav-label-3"));
	private static final Target FUND_ENTER_B = new Target.Click(By.linkText("投資信託サービス"), "インターネットバンキング投資信託");
	private static final Target FUND_RETURNS = new Target.Click(By.xpath("//*[@id=\"navi02_03\"]/li[4]/a")); // トータルリターン
	private static final Target FUND_EXIT    = new Target.Click(By.xpath("//*[@id=\"header\"]/img[1]"), "プレスティア オンライン");
	
	
	private static void login(WebBrowser webBrowser) {
		var secret = Secret.read().prestia;
		login(webBrowser, secret.account, secret.password);
	}
	private static void login(WebBrowser webBrowser, String account, String password) {
		LOGIN_A.action(webBrowser);
		webBrowser.savePage(storage.getFile("login-a.html"));
		
		// To prevent pop up dialog for new login, use 0 for sleep
		webBrowser.sendKey(By.id("dispuserId"),   account, 0);
		webBrowser.sendKey(By.id("disppassword"), password, 0);
		
		LOGIN_B.action(webBrowser);
		webBrowser.wait.untilPageContains("代表口座");
		webBrowser.savePage(storage.getFile("login-b.html"));
	}
	private static void logout(WebBrowser webBrowser) {
		LOGOUT.action(webBrowser);
		webBrowser.wait.untilPageContains("サインオフが完了しました");
	}
	private static void balance(WebBrowser webBrowser) {
		BALANCE_A.action(webBrowser);
		BALANCE_B.action(webBrowser);
	}
	private static void fundEnter(WebBrowser webBrowser) {
		FUND_ENTER_A.action(webBrowser);
		FUND_ENTER_B.action(webBrowser);
	}
	private static void fundReturns(WebBrowser webBrowser) {
		// hover mouse to navi02_03_active
		webBrowser.moveMouse(By.id("navi02_03_active"));
		
		FUND_RETURNS.action(webBrowser);
	}
	private static void fundExit(WebBrowser webBrowser) {
		FUND_EXIT.action(webBrowser);
	}
	
	private static void savePage(WebBrowser webBrowser, File file) {
		webBrowser.savePage(file);
	}

	
	@Override
	public Storage getStorage() {
		return storage;
	}
	
	@Override
	public void download() {
		try(var browser = new WebBrowser()) {
			logger.info("login");
			login(browser);
			
			logger.info("balance");
			balance(browser);
			savePage(browser, FILE_BALANCE);
			
			logger.info("fund");
			fundEnter(browser);
			fundReturns(browser);
			savePage(browser, FILE_FUND_RETURNS);
			fundExit(browser);
			
			logger.info("logout");
			logout(browser);
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
