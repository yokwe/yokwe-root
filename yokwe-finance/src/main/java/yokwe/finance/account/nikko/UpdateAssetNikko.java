package yokwe.finance.account.nikko;

import java.io.File;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.Charset;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Set;
import java.util.stream.Collectors;

import org.openqa.selenium.By;

import yokwe.finance.Storage;
import yokwe.finance.account.Asset;
import yokwe.finance.account.Secret;
import yokwe.finance.account.Asset.Company;
import yokwe.finance.account.UpdateAsset;
import yokwe.finance.fund.StorageFund;
import yokwe.finance.type.Currency;
import yokwe.finance.util.webbrowser.Target;
import yokwe.finance.util.webbrowser.WebBrowser;
import yokwe.util.CSVUtil;
import yokwe.util.FileUtil;
import yokwe.util.UnexpectedException;

public final class UpdateAssetNikko implements UpdateAsset {
	private static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();
	
	private static final Storage storage = Storage.account.nikko;
	
	private static final File FILE_TOP           = storage.getFile("top.html");
	private static final File FILE_BALANCE       = storage.getFile("balance.html");
	private static final File FILE_BALANCE_BANK  = storage.getFile("balance-bank.html");
	
	private static final File FILE_TORIREKI      = storage.getFile("torireki.csv");
	private static final File FILE_TRADE_HISTORY = storage.getFile("trade-history.csv");
	
	private static final File DIR_DOWNLOAD       = storage.getFile("download");
	
	private static final Charset CHARSET_CSV = Charset.forName("Shift_JIS");
	
	private static final String URL_LOGIN = "https://trade.smbcnikko.co.jp/Login/0/login/ipan_web/hyoji/";
	
	private static final Target LOGIN_PAGE = new Target.Get(URL_LOGIN);
	
	private static final Target LOGIN_A = new Target.Get(URL_LOGIN, "ログイン");
	private static final Target LOGIN_B = new Target.Click(By.xpath("//button[@class='hyoji-submit__button__type']"), "トップ");

	private static final Target LOGOUT  = new Target.Click(By.name("btn_logout"), "ログアウト");
	
	private static final Target BALANCE      = new Target.Click(By.name("menu04"), "口座残高");
	private static final Target BALANCE_BANK = new Target.Click(By.linkText("銀行・証券残高一覧"), "銀行・証券残高一覧");
	
	private static final Target TRADE                   = new Target.Click(By.name("menu03"), "お取引");
	private static final Target TRADE_LIST_STOCK_US     = new Target.Click(By.linkText("米国株式"), "米国株式 - 取扱銘柄一覧");
	private static final Target TRADE_LIST_FOREIGN_BOND = new Target.Click(By.linkText("外国債券"), "外国債券 - 取扱銘柄一覧");
	
	private static final Target NEXT_30_ITEMS     = new Target.Click(By.linkText("次の30件"));
	
	private static final Target TRADE_HISTORY           = new Target.Click(By.linkText("お取引履歴"), "お取引履歴 - 検索");
	private static final Target TRADE_HISTORY_3_MONTH   = new Target.Click(By.xpath("//input[@id='term02']"));
//	private static final Target TRADE_HISTORY_1_YEAR    = new Target.Click(By.xpath("//input[@id='term03']"));
//	private static final Target TRADE_HISTORY_3_YEAR    = new Target.Click(By.xpath("//input[@id='term04']"));
	private static final Target TRADE_HISTORY_DOWNLOAD  = new Target.Click(By.xpath("//input[@id='dlBtn']"));
	
	public static boolean isSystemMaintenance(WebBrowser webBrowser) {
		LOGIN_PAGE.action(webBrowser);
		return webBrowser.getTitle().contains("システムメンテナンス");
	}
	
	public static void login(WebBrowser webBrowser) {
		var secret = Secret.read().nikko;
		login(webBrowser, secret.branch, secret.account, secret.password);
	}
	public static void login(WebBrowser webBrowser, String branch, String account, String password) {
		LOGIN_A.action(webBrowser);
		
		webBrowser.sendKey(By.name("koza1"),  branch);
		webBrowser.sendKey(By.name("koza2"),  account);
		webBrowser.sendKey(By.name("passwd"), password);
		
		LOGIN_B.action(webBrowser);
	}
	public static void logout(WebBrowser webBrowser) {
		LOGOUT.action(webBrowser);
	}
	public static void balance(WebBrowser webBrowser) {
		BALANCE.action(webBrowser);
	}
	public static void balanceBank(WebBrowser webBrowser) {
		BALANCE_BANK.action(webBrowser);
	}
	public static void trade(WebBrowser webBrowser) {
		TRADE.action(webBrowser);
	}
	public static void listStockUS(WebBrowser webBrowser) {
		TRADE_LIST_STOCK_US.action(webBrowser);
	}
	public static void listForeignBond(WebBrowser webBrowser) {
		TRADE_LIST_FOREIGN_BOND.action(webBrowser);
	}
	public static void next30Items(WebBrowser webBrowser) {
		NEXT_30_ITEMS.action(webBrowser);
	}
	public static void tradeHistory(WebBrowser webBrowser) {
		TRADE_HISTORY.action(webBrowser);
	}
	public static void tradeHistoryDownload(WebBrowser webBrowser) {
		TRADE_HISTORY_3_MONTH.action(webBrowser);
		TRADE_HISTORY_DOWNLOAD.action(webBrowser);
	}

	
	@Override
	public Storage getStorage() {
		return storage;
	}
	
	@Override
	public void download() {
		for(var e: DIR_DOWNLOAD.listFiles()) e.delete();
		
process:
		try(var browser = new WebBrowser(DIR_DOWNLOAD)) {
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
			
			logger.info("balance bank");
			balanceBank(browser);
			browser.savePage(FILE_BALANCE_BANK);
						
			// download csv file
			{
				logger.info("trade history");
				trade(browser);
				tradeHistory(browser);
				tradeHistoryDownload(browser);
				browser.sleep(1000);
				
				File[] files = DIR_DOWNLOAD.listFiles(o -> o.getName().startsWith("Torireki"));
				if (files.length == 1) {
					var file = files[0];
					browser.wait.untilDownloadFinish(file);
					
					String string = FileUtil.read().withCharset(CHARSET_CSV).file(file);
					FileUtil.write().file(FILE_TORIREKI, string);
					logger.info("save  {}  {}", string.length(), FILE_TORIREKI.getPath());
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
		}
	}
		
	@Override
	public void update() {
		File file = getFile();
		file.delete();
		
		var list = new ArrayList<Asset>();
		// build list
		{
			var fundCodeMap = StorageFund.FundInfo.getList().stream().collect(Collectors.toMap(o -> o.fundCode, o -> o.isinCode));
			
			LocalDateTime dateTime;
			String        page;
			{
				var htmlFile = FILE_BALANCE;
				
				var instant = FileUtil.getLastModified(htmlFile);
				dateTime = LocalDateTime.ofInstant(instant, ZoneId.systemDefault()).truncatedTo(ChronoUnit.SECONDS);
				logger.info("dateTime  {}  {}", dateTime, htmlFile.getName());
				
				page = FileUtil.read().file(htmlFile);
			}
			
			{
				var mrfInfo = BalancePage.MRFInfo.getInstance(page);
				list.add(Asset.deposit(dateTime, Company.NIKKO, Currency.JPY, BigDecimal.valueOf(mrfInfo.value), "MRF"));
			}
			
			
			var fundInfoList = BalancePage.FundInfo.getInstance(page);
			for(var e: fundInfoList) {
//				logger.info("fundInfo  {}", e);
				var units     = new BigDecimal(e.units);
				var unitPrice = new BigDecimal(e.unitPrice);
				var unitCost  = new BigDecimal(e.unitCost);
				
				// assume currency is JPY
				var value = units.multiply(unitPrice).setScale(0, RoundingMode.HALF_EVEN);
				var cost  = units.multiply(unitCost).setScale(0, RoundingMode.HALF_EVEN);
				
				String isinCode;
				{
					var fundCode = e.fundCode;
					if (fundCodeMap.containsKey(fundCode)) {
						isinCode = fundCodeMap.get(fundCode);
					} else {
						logger.error("Unpexpeced fundCode");
						logger.error("  fundCoce {}  {}", fundCode, e.fundName);
						throw new UnexpectedException("Unpexpeced fundCode");
					}
				}
				var name = e.fundName;
				list.add(Asset.fund(dateTime, Company.NIKKO, Currency.JPY, value, cost, isinCode, name));
			}
			
			var foreignStockInfoList = BalancePage.ForeignStockInfo.getInstance(page);
			for(var e: foreignStockInfoList) {
//				logger.info("foreginStock  {}", e);
				var currency = Currency.valueOf(e.currency);
				
				var fxRate    = new BigDecimal(e.fxRate);
				var valueJPY  = new BigDecimal(e.valueJPY);
				var costJPY   = new BigDecimal(e.costJPY);
				
				var value     = valueJPY.divide(fxRate, 2, RoundingMode.HALF_EVEN);
				var units     = Integer.valueOf(e.units);
				var cost      = costJPY.divide(fxRate, 2, RoundingMode.HALF_EVEN);
				var code      = e.stockCode;
				var name      = e.stockName;
				
				list.add(Asset.stock(dateTime, Company.NIKKO, currency, value, units, cost, code, name));
			}
			
			var foreignMMFList = BalancePage.ForeignMMFInfo.getInstance(page);
			for(var e: foreignMMFList) {
//				logger.info("foreignMMF  {}", e);
				var currency = Currency.valueOf(e.currency);
				var value    = new BigDecimal(e.value);
				var name     = e.name;
				
				list.add(Asset.deposit(dateTime, Company.NIKKO, currency, value, name));
			}
			
			var foreignBondList = BalancePage.ForeignBondInfo.getInstance(page);
			for(var e: foreignBondList) {
//				logger.info("foreignBond  {}", e);
				var currency = Currency.valueOf(e.currency);
				var value    = new BigDecimal(e.units);
				var cost     = value;  // FIXME get cost of foreign bond
				var code     = e.code;
				var name     = e.name;
				list.add(Asset.bond(dateTime, Company.NIKKO, currency, value, cost, code, name));
			}
		}
		
		{
			LocalDateTime dateTime;
			String        page;
			{
				var htmlFile = FILE_BALANCE_BANK;

				var instant = FileUtil.getLastModified(htmlFile);
				dateTime = LocalDateTime.ofInstant(instant, ZoneId.systemDefault()).truncatedTo(ChronoUnit.SECONDS);
				logger.info("dateTime  {}  {}", dateTime, htmlFile.getName());
				
				page = FileUtil.read().file(htmlFile);
			}
			
			if (page.contains("サービス時間外です")) {
				// https://www.smbc.co.jp/kojin/banktrade/channel.html
				// 毎週日曜日21:00～翌月曜日7:00は、ご利用いただけません
				logger.warn("bank & trade is out of service from Sunday 2100 to Monday 0700.");
			} else {
				var deposit = BalanceBankPage.DepositInfo.getInstance(page);
//				logger.info("deposit  {}", deposit);
				if (deposit.value != 0) {
					list.add(Asset.deposit(dateTime, Company.SMBC, Currency.JPY, BigDecimal.valueOf(deposit.value), "DEPOSIT"));
				}
				
				var termDeposit = BalanceBankPage.TermDepositInfo.getInstance(page);
//				logger.info("termDeposit  {}", termDeposit);
				if (termDeposit.value != 0) {
					list.add(Asset.termDeposit(dateTime, Company.SMBC, Currency.JPY, BigDecimal.valueOf(termDeposit.value), "TERM_DEPOSIT"));
				}
			}
		}
		
		for(var e: list) logger.info("list {}", e);
		
		logger.info("save  {}  {}", list.size(), file.getPath());
		save(list);
		
		// update trade history
		{
			var tradeHistory = CSVUtil.read(TradeHistory.class).file(FILE_TRADE_HISTORY);
			var torireki     = CSVUtil.read(Torireki.class).file(FILE_TORIREKI);
			
			if (tradeHistory == null) tradeHistory = new ArrayList<TradeHistory>();
			if (torireki     == null) torireki     = new ArrayList<Torireki>();
			
			var candidateList = torireki.stream().map(o -> o.toTradeHistory()).collect(Collectors.toList());
			{
				Set<LocalDate> dateSet = tradeHistory.stream().map(o -> o.settlementDate).collect(Collectors.toSet());
				dateSet.add(LocalDate.now());
				candidateList.removeIf(o -> dateSet.contains(o.settlementDate));
			}
			if (!candidateList.isEmpty()) {
				logger.info("candidateList  {}", candidateList.size());
			}
			
			var tradeHistoryNew = new ArrayList<TradeHistory>(candidateList.size() + tradeHistory.size());
			tradeHistoryNew.addAll(candidateList);
			tradeHistoryNew.addAll(tradeHistory);
			logger.info("save  {}  {}", tradeHistoryNew.size(), FILE_TRADE_HISTORY.getPath());
			CSVUtil.write(TradeHistory.class).file(FILE_TRADE_HISTORY, tradeHistoryNew);
		}
	}
	
	private static final UpdateAsset instance = new UpdateAssetNikko();
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
