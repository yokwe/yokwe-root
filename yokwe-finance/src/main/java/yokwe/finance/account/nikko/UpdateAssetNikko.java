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
import yokwe.finance.account.Asset.Currency;
import yokwe.finance.account.AssetRisk;
import yokwe.finance.fund.StorageFund;
import yokwe.util.FileUtil;
import yokwe.util.UnexpectedException;

public class UpdateAssetNikko {
	private static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();
	
	private static final Storage storage = Storage.account.nikko;
	
	private static final File FILE_TOP          = storage.getFile("top.html");
	private static final File FILE_BALANCE      = storage.getFile("balance.html");
	private static final File FILE_BALANCE_BANK = storage.getFile("balance-bank.html");
	
	private static void download() {		
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
			
//			logger.info("trade histtory download");
//			browser.trade();
//			browser.tradeHistory();
//			browser.tradeHistoryY1();
//			browser.tradeHistoryDownload();
			
			logger.info("logout");
			browser.logout();
		}
	}
		
	
	private static void update() {
		
		var list = new ArrayList<Asset>();
		
		// build assetList
		{
			var fundCodeMap = StorageFund.FundInfo.getList().stream().collect(Collectors.toMap(o -> o.fundCode, o -> o.isinCode));
			
			LocalDateTime dateTime;
			{
				var instant = FileUtil.getLastModified(FILE_BALANCE);
				dateTime = LocalDateTime.ofInstant(instant, ZoneId.systemDefault()).truncatedTo(ChronoUnit.SECONDS);
			}
			logger.info("dateTime  {}", dateTime);
			
			var page = FileUtil.read().file(FILE_BALANCE);
			
			{
				var mrfInfo = BalancePage.MRFInfo.getInstance(page);
//				logger.info("mrfInfo  {}", mrfInfo);
				list.add(Asset.mrf(dateTime, Company.NIKKO, Currency.JPY, BigDecimal.valueOf(mrfInfo.value)));
			}
			
			
			var fundInfoList = BalancePage.FundInfo.getInstance(page);
			for(var e: fundInfoList) {
//				logger.info("fundInfo  {}", e);
				var units = new BigDecimal(e.units);
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
				var status = AssetRisk.fund.getStatus(isinCode);
				
				list.add(Asset.fund(dateTime, Company.NIKKO, Currency.JPY, value, status, units.intValue(), isinCode, e.fundName));
			}
			
			var foreignStockInfoList = BalancePage.ForeignStockInfo.getInstance(page);
			for(var e: foreignStockInfoList) {
//				logger.info("foreginStock  {}", e);
				var currency = Currency.valueOf(e.currency);
				
				var units  = new BigDecimal(e.units);
				var price  = new BigDecimal(e.price);
				var value  = price.multiply(units).setScale(2, RoundingMode.HALF_EVEN);
				var code   = e.stockCode;
				var name   = e.stockName;
				var status = AssetRisk.stockUS.getStatus(e.stockCode);
				
				list.add(Asset.stock(dateTime, Company.NIKKO, currency, value, status, units.intValue(), code, name));
			}
			
			var foreignMMFList = BalancePage.ForeignMMFInfo.getInstance(page);
			for(var e: foreignMMFList) {
//				logger.info("foreignMMF  {}", e);
				var currency = Currency.valueOf(e.currency);
				var value = new BigDecimal(e.value);
				
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
		
		for(var e: list) {
			logger.info("list {}", e);
		}
		
		logger.info("save  {}  {}", list.size(), StorageNikko.Asset.getPath());
		StorageNikko.Asset.save(list);
	}
	
	public static void main(String[] args) {
		logger.info("START");
		
		download();
		update();
		
		logger.info("STOP");
	}
}
