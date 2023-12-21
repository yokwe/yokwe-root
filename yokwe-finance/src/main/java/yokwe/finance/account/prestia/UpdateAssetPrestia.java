package yokwe.finance.account.prestia;

import java.io.File;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;

import yokwe.finance.Storage;
import yokwe.finance.account.Asset;
import yokwe.finance.account.Asset.Company;
import yokwe.finance.account.AssetRisk;
import yokwe.finance.account.UpdateAsset;
import yokwe.finance.account.prestia.BalancePage.DepositJPY;
import yokwe.finance.account.prestia.BalancePage.DepositMultiMoney;
import yokwe.finance.account.prestia.BalancePage.DepositMultiMoneyJPY;
import yokwe.finance.account.prestia.BalancePage.DepositUSD;
import yokwe.finance.account.prestia.BalancePage.TermDepositForeign;
import yokwe.finance.account.prestia.FundPage.FundInfo;
import yokwe.finance.type.Currency;
import yokwe.util.FileUtil;

public class UpdateAssetPrestia implements UpdateAsset {
	private static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();
	
	private static final Storage storage = Storage.account.prestia;
	
	private static final File FILE_TOP     = storage.getFile("top.html");
	private static final File FILE_BALANCE = storage.getFile("balance.html");
	private static final File FILE_FUND    = storage.getFile("fund.html");
	
	@Override
	public Storage getStorage() {
		return storage;
	}
	
	@Override
	public void download() {
		try(var browser = new WebBrowserPrestia()) {
			logger.info("login");
			browser.login();
			
			browser.savePage(FILE_TOP);
			
			logger.info("balance");
			browser.balance();
			browser.savePage(FILE_BALANCE);
			
			logger.info("fund");
			browser.fundEnter();
			browser.fundBalance();
			browser.savePage(FILE_FUND);
			browser.fundExit();
			
			logger.info("logout");
			browser.logout();
		}
	}
	
	@Override
	public void update() {
		var list = new ArrayList<Asset>();
		
		// build assetList
		{
			LocalDateTime dateTime;
			String        page;
			{
				var file = FILE_BALANCE;
				
				var instant = FileUtil.getLastModified(file);
				dateTime = LocalDateTime.ofInstant(instant, ZoneId.systemDefault()).truncatedTo(ChronoUnit.SECONDS);
				logger.info("dateTime  {}  {}", dateTime, file.getName());
				
				page = FileUtil.read().file(file);
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
						list.add(Asset.depositTime(dateTime, Company.PRESTIA, currency, e.value, "外貨定期預金"));
					}
				}
			}
		}
		{
			LocalDateTime dateTime;
			String        page;
			{
				var file = FILE_FUND;
				
				var instant = FileUtil.getLastModified(file);
				dateTime = LocalDateTime.ofInstant(instant, ZoneId.systemDefault()).truncatedTo(ChronoUnit.SECONDS);
				logger.info("dateTime  {}  {}", dateTime, file.getName());
				
				page = FileUtil.read().file(file);
			}
			// 投資信託
			{
				var fundInfo = FundInfo.getInstance(page);
//				logger.info("fundInfo  {}", fundInfo.size());
				for(var e: fundInfo) {
//					logger.info("fundInfo  {}", e);
					Currency currency = Currency.valueOf(e.currency);
					var risk = AssetRisk.fundPrestia.getRisk(e.fundCode);
					list.add(Asset.fund(dateTime, Company.PRESTIA, currency, e.value, risk, e.fundCode, e.fundName));
				}
			}
		}
		
		for(var e: list) logger.info("list {}", e);
		
		logger.info("save  {}  {}", list.size(), getFile().getPath());
		save(list);
	}
	
	public static final UpdateAsset instance = new UpdateAssetPrestia();

	public static void main(String[] args) {
		logger.info("START");
				
		instance.download();
		instance.update();
		
		logger.info("STOP");
	}
}
