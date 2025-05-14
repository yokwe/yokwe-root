package yokwe.finance.account.nikko;

import java.io.File;
import java.io.FileFilter;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Set;
import java.util.stream.Collectors;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriverException;

import yokwe.finance.Storage;
import yokwe.finance.account.Asset;
import yokwe.finance.account.Asset.Company;
import yokwe.finance.account.Secret;
import yokwe.finance.account.UpdateAsset;
import yokwe.finance.fund.StorageFund;
import yokwe.finance.type.Currency;
import yokwe.util.CSVUtil;
import yokwe.util.FileUtil;
import yokwe.util.UnexpectedException;
import yokwe.util.selenium.WebDriverWrapper;

public final class UpdateAssetNikko implements UpdateAsset {
	private static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();
	
	private static final Storage storage = Storage.account.nikko;
	
	private static final File FILE_LOGIN         = storage.getFile("login.html");
	private static final File FILE_TOP           = storage.getFile("top.html");
	private static final File FILE_BALANCE       = storage.getFile("balance.html");
	private static final File FILE_BALANCE_BANK  = storage.getFile("balance-bank.html");
	
	private static final File FILE_TORIREKI      = storage.getFile("torireki.csv");
	private static final File FILE_TRADE_HISTORY = storage.getFile("trade-history.csv");
	
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
				driver.getAndWait("https://trade.smbcnikko.co.jp/Login/0/login/ipan_web/hyoji/");
				driver.savePage(FILE_LOGIN);
				
				if (driver.getTitle().contains("システムメンテナンス")) {
					logger.info("skip system maintenance");
					return;
				}
				
				// sanity check
				driver.check.titleContains("ログイン");
				
				var secret = Secret.read().nikko;
				driver.sendKey(By.name("koza1"),  secret.branch);
				driver.sendKey(By.name("koza2"),  secret.account);
				driver.sendKey(By.name("passwd"), secret.password);
				driver.clickAndWait(By.xpath("//button[@class='hyoji-submit__button__type']"));
				driver.savePage(FILE_TOP);
			}
			
			// balance
			{
				logger.info("balance");
				driver.clickAndWait(By.name("menu04"));
				driver.savePage(FILE_BALANCE);
				
				// sanity check
				driver.check.titleContains("口座残高");
			}
			
			// balance bank
			{
				logger.info("balance bank");
				driver.clickAndWait(By.linkText("銀行・証券残高一覧"));
				driver.savePage(FILE_BALANCE_BANK);
				
				// sanity check
				driver.check.titleContains("銀行・証券残高一覧");
			}
			
			// trade history
			{
				logger.info("trade history");
				driver.clickAndWait(By.name("menu03"));
				// sanity check
				driver.check.titleContains("お取引");

				driver.clickAndWait(By.linkText("お取引履歴"));
				// sanity check
				driver.check.titleContains("お取引履歴 - 検索");
				
				// click 3 month
				driver.click(By.xpath("//input[@id='term02']"));
				
				// download
				logger.info("download");
				{
					File       dir        = Path.of(System.getProperty("user.home"), "Downloads").toFile();
					FileFilter fileFilter = o -> {var name = o.getName(); return name.startsWith("Torireki") && name.endsWith(".csv");};
					Runnable   download   = () -> driver.click(By.xpath("//input[@id='dlBtn']"));

					var file = driver.downloadFile(dir, fileFilter, download);
					logger.info("file  {}", file.getPath());
					
					var string = FileUtil.read().withCharset(Charset.forName("Shift_JIS")).file(file);
					FileUtil.write().file(FILE_TORIREKI, string);
					logger.info("save  {}  {}", string.length(), FILE_TORIREKI.getPath());
					
					// delete download file
					file.delete();
				}
			}
			
			// logout
			{
				logger.info("logout");
				driver.clickAndWait(By.name("btn_logout"));
				// sanity check
				driver.check.titleContains("ログアウト");
			}
		} catch (WebDriverException e) {
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
				list.add(Asset.fund(dateTime, Company.NIKKO, Currency.JPY, units.intValue(), unitPrice, value, cost, isinCode, name));
			}
			
			var foreignStockInfoList = BalancePage.ForeignStockInfo.getInstance(page);
			for(var e: foreignStockInfoList) {
//				logger.info("foreginStock  {}", e);
				var currency = Currency.valueOf(e.currency);
				
				var fxRate    = new BigDecimal(e.fxRate);
				var costJPY   = new BigDecimal(e.costJPY);
				var units     = new BigDecimal(e.units);
				var unitPrice = new BigDecimal(e.price);
				
				var value     = unitPrice.multiply(units).setScale(2, RoundingMode.HALF_EVEN);
				var cost      = costJPY.divide(fxRate, 2, RoundingMode.HALF_EVEN);
				var code      = e.stockCode;
				var name      = e.stockName;
				
				list.add(Asset.stock(dateTime, Company.NIKKO, currency, units.intValue(), unitPrice, value, cost, code, name));
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
