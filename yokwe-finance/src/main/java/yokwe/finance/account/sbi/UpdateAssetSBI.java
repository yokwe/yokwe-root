package yokwe.finance.account.sbi;

import java.io.File;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;

import yokwe.finance.Storage;
import yokwe.finance.account.Asset;
import yokwe.finance.account.AssetRisk;
import yokwe.finance.account.Asset.Company;
import yokwe.finance.account.sbi.BalancePage.BondForeign;
import yokwe.finance.account.sbi.BalancePage.DepositForeign;
import yokwe.finance.account.sbi.BalancePage.DepositJPY;
import yokwe.finance.account.sbi.BalancePage.MMFInfo;
import yokwe.finance.account.sbi.BalancePage.StockUS;
import yokwe.finance.stock.StorageStock;
import yokwe.finance.type.Currency;
import yokwe.util.FileUtil;
import yokwe.util.UnexpectedException;

public class UpdateAssetSBI {
	private static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();
	
	private static final Storage storage                      = Storage.account.sbi;
	private static final File    FILE_TOP                     = storage.getFile("top.html");
	private static final File    FILE_BALANCE                 = storage.getFile("balance.html");
	private static final File    FILE_BALANCE_FOREIGN         = storage.getFile("balance-foreign.html");
	private static final File    FILE_BALANCE_ASSET_FOREIGN   = storage.getFile("balance-asset-foreign.html");

	public static void download() {
		try(var browser = new WebBrowserSBI()) {
			logger.info("login");
			browser.login();
			browser.savePage(FILE_TOP);
			
			logger.info("balance");
			browser.balanceJPY();
			browser.savePage(FILE_BALANCE);
			
			logger.info("balance-foreign");
			browser.balanceForeign();
			browser.savePage(FILE_BALANCE_FOREIGN);
			browser.balanceAsset();
			browser.savePage(FILE_BALANCE_ASSET_FOREIGN);
			
			logger.info("logout");
			browser.logout();
		}
	}
	
	public static void update() {
		var list = new ArrayList<Asset>();
		
		// build assetList
		{
			LocalDateTime dateTime;
			String        page;
			{
				var file = FILE_BALANCE;
				
				var instant = FileUtil.getLastModified(file);
				dateTime = LocalDateTime.ofInstant(instant, ZoneId.systemDefault()).truncatedTo(ChronoUnit.SECONDS);
				page     = FileUtil.read().file(file);
				logger.info("  {}  {}  {}", dateTime, page.length(), file.getPath());
			}
			{
				var depositJPY = DepositJPY.getInstance(page);
//				logger.info("depositJPY  {}", depositJPY);
				list.add(Asset.deposit(dateTime, Company.SBI, Currency.JPY, depositJPY.value, "現金残高等"));
			}
		}
		{
			LocalDateTime dateTime;
			String        page;
			{
				var file = FILE_BALANCE_FOREIGN;
				
				var instant = FileUtil.getLastModified(file);
				dateTime = LocalDateTime.ofInstant(instant, ZoneId.systemDefault()).truncatedTo(ChronoUnit.SECONDS);
				page     = FileUtil.read().file(file);
				logger.info("  {}  {}  {}", dateTime, page.length(), file.getPath());
			}
			{
				var depositForeign = DepositForeign.getInstance(page);
				for(var e: depositForeign) {
//					logger.info("depositForeign  {}", e);
					var currency = Currency.valueOf(e.currency);
					list.add(Asset.deposit(dateTime, Company.SBI, currency, e.value, "現金"));
				}
			}
		}
		{
			LocalDateTime dateTime;
			String        page;
			{
				var file = FILE_BALANCE_ASSET_FOREIGN;
				
				var instant = FileUtil.getLastModified(file);
				dateTime = LocalDateTime.ofInstant(instant, ZoneId.systemDefault()).truncatedTo(ChronoUnit.SECONDS);
				page     = FileUtil.read().file(file);
				logger.info("  {}  {}  {}", dateTime, page.length(), file.getPath());
			}
			{
				var usStockMap = StorageStock.StockInfoUSTrading.getMap();

				var stockUSList = StockUS.getInstance(page);
				for(var e: stockUSList) {
//					logger.info("stockUSList  {}", e);
					var currency = Currency.USD;
					
					var code = e.code;
					String name;
					if (usStockMap.containsKey(code)) {
						name = usStockMap.get(code).name;
					} else {
						logger.error("unexpected code");
						logger.error("  code  {}!", code);
						throw new UnexpectedException("unexpected code");
					}
					var status = AssetRisk.stockUS.getStatus(code);

					list.add(Asset.stock(dateTime, Company.SBI, currency, e.value, status, code, name));
				}
			}
			{
				var bondForeignList = BondForeign.getInstance(page);
				for(var e: bondForeignList) {
//					logger.info("bondForeignList  {}", e);
					var currency = Currency.USD;
					list.add(Asset.bond(dateTime, Company.SBI, currency, e.value, e.code, e.name));
				}
			}
			{
				var mmfInfoList = MMFInfo.getInstance(page);
				for(var e: mmfInfoList) {
//					logger.info("mmfInfoList  {}", e);
					var currency = Currency.USD;
					list.add(Asset.mmf(dateTime, Company.SBI, currency, e.value, e.name));
				}
			}
		}
		
		for(var e: list) logger.info("list {}", e);
		
		logger.info("save  {}  {}", list.size(), StorageSBI.Asset.getPath());
		StorageSBI.Asset.save(list);
	}
	
	public static void main(String[] args) {
		logger.info("START");
				
		download();
		update();
		
		logger.info("STOP");
	}
}
