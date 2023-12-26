package yokwe.finance.account.nikko;

import java.io.File;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.stream.Collectors;

import yokwe.finance.Storage;
import yokwe.finance.account.Asset;
import yokwe.finance.account.Asset.Company;
import yokwe.finance.account.AssetRisk;
import yokwe.finance.account.UpdateAsset;
import yokwe.finance.fund.StorageFund;
import yokwe.finance.stock.StorageStock;
import yokwe.finance.type.Currency;
import yokwe.util.FileUtil;
import yokwe.util.UnexpectedException;

public final class UpdateAssetNikko implements UpdateAsset {
	private static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();
	
	private static final Storage storage = Storage.account.nikko;
	
	private static final File FILE_TOP          = storage.getFile("top.html");
	private static final File FILE_BALANCE      = storage.getFile("balance.html");
	private static final File FILE_BALANCE_BANK = storage.getFile("balance-bank.html");
	
	private static final File[] FILES = {
		FILE_TOP,
		FILE_BALANCE,
		FILE_BALANCE_BANK,
	};
	
	
	@Override
	public Storage getStorage() {
		return storage;
	}
	
	@Override
	public void download() {
		deleteFile(FILES);
		
		try(var browser = new WebBrowserNikko()) {
			logger.info("login");
			browser.login();
			browser.savePage(FILE_TOP);
			
			logger.info("balance");
			browser.balance();
			browser.savePage(FILE_BALANCE);
			
			logger.info("balance bank");
			browser.balanceBank();
			browser.savePage(FILE_BALANCE_BANK);
			
			logger.info("logout");
			browser.logout();
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
//				logger.info("mrfInfo  {}", mrfInfo);
				list.add(Asset.mrf(dateTime, Company.NIKKO, Currency.JPY, BigDecimal.valueOf(mrfInfo.value), "MRF"));
			}
			
			
			var fundInfoList = BalancePage.FundInfo.getInstance(page);
			for(var e: fundInfoList) {
//				logger.info("fundInfo  {}", e);
				var value = new BigDecimal(e.value);
								
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
				var risk = AssetRisk.fundCode.getRisk(isinCode);
				list.add(Asset.fund(dateTime, Company.NIKKO, Currency.JPY, value, risk, isinCode, e.fundName));
			}
			
			var foreignStockInfoList = BalancePage.ForeignStockInfo.getInstance(page);
			var usStockMap = StorageStock.StockInfoUSTrading.getMap();
			for(var e: foreignStockInfoList) {
//				logger.info("foreginStock  {}", e);
				var currency = Currency.valueOf(e.currency);
				
				var units = new BigDecimal(e.units);
				var price = new BigDecimal(e.price);
				var value = price.multiply(units).setScale(2, RoundingMode.HALF_EVEN);
				var risk  = AssetRisk.stockUS.getRisk(e.stockCode);
				var code  = e.stockCode;
				var name  = e.stockName;
				if (usStockMap.containsKey(code)) {
					name = usStockMap.get(code).name;
				}
				list.add(Asset.stock(dateTime, Company.NIKKO, currency, value, risk, code, name));
			}
			
			var foreignMMFList = BalancePage.ForeignMMFInfo.getInstance(page);
			for(var e: foreignMMFList) {
//				logger.info("foreignMMF  {}", e);
				var currency = Currency.valueOf(e.currency);
				var value    = new BigDecimal(e.value);
				
				list.add(Asset.mmf(dateTime, Company.NIKKO, currency, value, e.name));
			}
			
			var foreignBondList = BalancePage.ForeignBondInfo.getInstance(page);
			for(var e: foreignBondList) {
//				logger.info("foreignBond  {}", e);
				var currency = Currency.valueOf(e.currency);
				var value    = new BigDecimal(e.units);
				list.add(Asset.bond(dateTime, Company.NIKKO, currency, value, e.code, e.name));
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
					list.add(Asset.depositTime(dateTime, Company.SMBC, Currency.JPY, BigDecimal.valueOf(termDeposit.value), "DEPOSIT_TERM"));
				}
			}
		}
		
		for(var e: list) logger.info("list {}", e);
		
		logger.info("save  {}  {}", list.size(), file.getPath());
		save(list);
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
