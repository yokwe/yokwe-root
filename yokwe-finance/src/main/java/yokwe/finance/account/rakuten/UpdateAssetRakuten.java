package yokwe.finance.account.rakuten;

import java.io.BufferedReader;
import java.io.File;
import java.io.StringReader;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.Charset;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriverException;

import yokwe.finance.Storage;
import yokwe.finance.account.Asset;
import yokwe.finance.account.Asset.Company;
import yokwe.finance.account.Secret;
import yokwe.finance.account.UpdateAsset;
import yokwe.finance.fund.StorageFund;
import yokwe.finance.stock.StorageStock;
import yokwe.finance.type.Currency;
import yokwe.finance.type.FundInfoJP;
import yokwe.finance.type.StockCodeJP;
import yokwe.finance.util.webbrowser.Target;
import yokwe.finance.util.webbrowser.WebBrowser;
import yokwe.util.CSVUtil;
import yokwe.util.FileUtil;
import yokwe.util.StringUtil;
import yokwe.util.UnexpectedException;

public final class UpdateAssetRakuten implements UpdateAsset {
	private static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();
	
	private static final Storage storage          = Storage.account.rakuten;
	
	private static final File    FILE_TOP         = storage.getFile("top.html");
	private static final File    FILE_BALANCE     = storage.getFile("balance.html");
	
	private static final File    DIR_DOWNLOAD     = storage.getFile("download");
	private static final File    FILE_BALANCE_ALL = storage.getFile("balance-all.csv");
	
	
	private static final Target LOGIN_A = new Target.Get("https://www.rakuten-sec.co.jp/ITS/V_ACT_Login.html", "総合口座ログイン | 楽天証券");
	private static final Target LOGIN_B = new Target.Click(By.id("login-btn"), "ホーム");
	private static final Target LOGOUT  = new Target.Javascript("logoutDialog()");
	
	private static final Target MY_MENU         = new Target.Click(By.xpath("//span[@class='pcm-gl-g-header-mymenu-btn']"));	
	private static final Target MY_MENU_BALANCE = new Target.Click(By.xpath("//a[text()='保有商品一覧']"),    "保有商品一覧-すべて");
	
	private static final Target SAVE_AS_CSV = new Target.Click(By.xpath("//img[@alt='CSVで保存']"));

	private static final Charset CHARSET_CSV = Charset.forName("Shift_JIS");
	
	public static void login(WebBrowser browser) {
		var secret = Secret.read().rakuten;
		login(browser, secret.account, secret.password);
	}
	public static void login(WebBrowser browser, String account, String password) {
		LOGIN_A.action(browser);
		
		browser.sendKey(By.name("loginid"), account);
		browser.sendKey(By.name("passwd"),  password);
		
		LOGIN_B.action(browser);
		browser.sleepRandom();
	}
	
	public void logout(WebBrowser browser) {
		LOGOUT.action(browser);
		browser.wait.untilAlertIsPresent().accept();
		browser.sleepRandom();
	}
	
	public void balance(WebBrowser browser) {
		MY_MENU.action(browser);
		MY_MENU_BALANCE.action(browser);
		// pause before return
		browser.sleepRandom(1500);
	}
	public void saveAsCSV(WebBrowser browser) {
		SAVE_AS_CSV.action(browser);
		browser.sleepRandom();
	}

	
	@Override
	public Storage getStorage() {
		return storage;
	}
	
	@Override
	public void download() {
		for(var e: DIR_DOWNLOAD.listFiles()) e.delete();
		
		try(var browser = new WebBrowser(DIR_DOWNLOAD)) {
			logger.info("login");
			login(browser);
			browser.savePage(FILE_TOP);
			
			logger.info("balance");
			balance(browser);
			browser.savePage(FILE_BALANCE);
			
			// download csv file
			{
				saveAsCSV(browser);
				browser.sleep(1000);
				
				File[] files = DIR_DOWNLOAD.listFiles(o -> o.getName().startsWith("assetbalance(all)_"));
				
				if (files.length == 1) {
					var file = files[0];
					browser.wait.untilDownloadFinish(file);
					
					String string = FileUtil.read().withCharset(CHARSET_CSV).file(file);
					FileUtil.write().file(FILE_BALANCE_ALL, string);
					logger.info("save  {}  {}", string.length(), FILE_BALANCE_ALL.getPath());
				} else {
					logger.error("Unexpected download file");
					logger.error("  files  {}", files.length);
					for(var i = 0; i < files.length; i++) {
						logger.error("  files  {}  {}", i, files[i].getPath());
					}
					throw new UnexpectedException("Unexpected download file");
				}
			}
			
			logger.info("logout");
			logout(browser);
		} catch (WebDriverException e){
			String exceptionName = e.getClass().getSimpleName();
			logger.warn("{} {}", exceptionName, e);
		}
	}
	
	private static final List<FundInfoJP> fundInfoList = StorageFund.FundInfo.getList();
	private static FundInfoJP getFundInfo(String name) {
		var alternateNameA = StringUtil.toFullWidth(name);
		var alternateNameB = name.contains("(") ? name.substring(0, name.indexOf("(")) : name;
		
		for(var e: fundInfoList) {
			if (e.name.equals(name)) return e;
			if (e.name.equals(alternateNameA)) return e;
			if (e.name.equals(alternateNameB)) return e;
		}
		
		logger.error("Unexpected name");
		logger.error("  name  {}!", name);
		logger.error("  altA  {}!", alternateNameA);
		logger.error("  altB  {}!", alternateNameB);
		throw new UnexpectedException("Unexpected name");
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
				var csvFile = FILE_BALANCE_ALL;
				
				var instant = FileUtil.getLastModified(csvFile);
				dateTime = LocalDateTime.ofInstant(instant, ZoneId.systemDefault()).truncatedTo(ChronoUnit.SECONDS);
				page     = FileUtil.read().file(csvFile);
				logger.info("  {}  {}  {}", dateTime, page.length(), csvFile.getPath());
			}
			
			var usStockMap = StorageStock.StockInfoUSTrading.getMap();
			
			var br = new BufferedReader(new StringReader(page));
			for(;;) {
				var stringArray = CSVUtil.parseLine(br, ',');
				if (stringArray == null) break;
				
				var length = stringArray.length;
				var kind   = stringArray[0];
				
				if (length == 12) {
					var kindExpect = "預り金";
					if (kind.equals(kindExpect)) {
						var value = new BigDecimal(stringArray[1].replace(",", ""));
						list.add(Asset.deposit(dateTime, Company.RAKUTEN, Currency.JPY, value, kindExpect));
					}
				} else if (length == 18) {
					if (kind.equals("種別")) continue;
					
					var code      = stringArray[1];
					var name      = stringArray[2];
					var units     = new BigDecimal(stringArray[4].replace(",", ""));
					var unitCost  = stringArray[6].equals("-") ? BigDecimal.ZERO : new BigDecimal(stringArray[6].replace(",", ""));
					var unitPrice = new BigDecimal(stringArray[8].replace(",", ""));
					var currency  = Currency.valueOf(stringArray[9].replace("円/USD", "USD").replace("円", "JPY").replace("％", "USD"));

					var valueJPY  = new BigDecimal(stringArray[14].replace(",", "")); // 時価評価額[円]
					var profitJPY = stringArray[16].equals("-") ? BigDecimal.ZERO : new BigDecimal(stringArray[16].replace(",", "")); // 評価損益[円]
					var costJPY   = valueJPY.subtract(profitJPY);                     // コスト = 時価評価額[円] - 評価損益[円]

					{
						var kindExpect = "国内株式";
						if (kind.equals(kindExpect)) {
							if (currency != Currency.JPY) {
								logger.error("Unexpected currency");
								logger.error("{}  {}  {}", kind, code, name);
								throw new UnexpectedException("Unexpected currency");
							}
							
							code = StockCodeJP.toStockCode5(code);
							list.add(Asset.stock(dateTime, Company.RAKUTEN, currency, units.intValue(), unitPrice, valueJPY, costJPY, code, name));
						}
					}
					{
						var kindExpect = "投資信託";
						if (kind.equals(kindExpect)) {
							if (currency != Currency.JPY) {
								logger.error("Unexpected currency");
								logger.error("{}  {}  {}", kind, code, name);
								throw new UnexpectedException("Unexpected currency");
							}
							var fund  = getFundInfo(stringArray[2]);
							name  = fund.name;
							code  = fund.isinCode;
							list.add(Asset.fund(dateTime, Company.RAKUTEN, currency, units.intValue(), unitPrice, valueJPY, costJPY, code, name));
						}
					}
					{
						var kindExpect = "外貨預り金";
						if (kind.equals(kindExpect)) {
							if (currency != Currency.USD) {
								logger.error("Unexpected currency");
								logger.error("{}  {}  {}", kind, code, name);
								throw new UnexpectedException("Unexpected currency");
							}

							var value = units.setScale(2);
							list.add(Asset.deposit(dateTime, Company.RAKUTEN, currency, value, kindExpect));
						}
					}
					{
						var kindExpect = "米国株式";
						if (kind.equals(kindExpect)) {
							if (usStockMap.containsKey(code)) {
								name = usStockMap.get(code).name;
							}
							var value     = units.multiply(unitPrice).setScale(2, RoundingMode.HALF_EVEN);
							var cost      = units.multiply(unitCost).setScale(2, RoundingMode.HALF_EVEN);
							list.add(Asset.stock(dateTime, Company.RAKUTEN, currency, units.intValue(), unitPrice, value, cost, code, name));
						}
					}
					{
						var kindExpect = "外貨建MMF";
						if (kind.equals(kindExpect)) {
							if (currency != Currency.USD) {
								logger.error("Unexpected currency");
								logger.error("{}  {}  {}", kind, code, name);
								logger.error("currency  {}", currency);
								throw new UnexpectedException("Unexpected currency");
							}
							BigDecimal value = units.movePointLeft(2).setScale(2, RoundingMode.HALF_EVEN);
							list.add(Asset.deposit(dateTime, Company.RAKUTEN, currency, value, name));
						}
					}
					{
						var kindExpect = "外国債券";
						if (kind.equals(kindExpect)) {
							if (currency != Currency.USD) {
								logger.error("Unexpected currency");
								logger.error("{}  {}  {}", kind, code, name);
								throw new UnexpectedException("Unexpected currency");
							}
							var value = units;
							var cost  = value; // FIXME get cost of foreign bond
							list.add(Asset.bond(dateTime, Company.RAKUTEN, currency, value, cost, code, name));
						}
					}
				}
			}
		}

		for(var e: list) logger.info("list {}", e);
		
		logger.info("save  {}  {}", list.size(), file.getPath());
		save(list);
	}
	
	private static final UpdateAsset instance = new UpdateAssetRakuten();
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
