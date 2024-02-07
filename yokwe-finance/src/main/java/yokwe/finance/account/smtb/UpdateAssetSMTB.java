package yokwe.finance.account.smtb;

import java.io.File;
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
import yokwe.finance.account.smtb.BalancePage.DepositJPY;
import yokwe.finance.account.smtb.BalancePage.Fund;
import yokwe.finance.account.smtb.BalancePage.TermDepositJPY;
import yokwe.finance.fund.StorageFund;
import yokwe.finance.type.Currency;
import yokwe.util.FileUtil;

public final class UpdateAssetSMTB implements UpdateAsset {
	private static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();
	
	private static final Storage storage          = Storage.account.smtb;
	
	private static final File    FILE_TOP         = storage.getFile("top.html");
	private static final File    FILE_BALANCE     = storage.getFile("balance.html");
	private static final File    FILE_FUND        = storage.getFile("fund.html");
	
	private static final File[] FILES = {
		FILE_TOP,
		FILE_BALANCE,
	};
	
	@Override
	public Storage getStorage() {
		return storage;
	}
	
	@Override
	public void download() {
		deleteFile(FILES);
		
		try(var browser = new WebBrowserSMTB()) {
			logger.info("login");
			browser.login();
			browser.savePage(FILE_TOP);
			
			logger.info("balance");
			browser.balance();
			browser.savePage(FILE_BALANCE);
			
			logger.info("fund");
			browser.fund();
			browser.savePage(FILE_FUND);
			
			logger.info("logout");
			browser.logout();
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
				list.add(Asset.depositTime(dateTime, Company.SMTB, Currency.JPY, termDepositJPY.value, "円定期預金"));
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
					var fundInfo = fundInfoMap.get(fund.fundCode);
					var isinCode = fundInfo.isinCode;
					var risk     = AssetRisk.fundCode.getRisk(isinCode);
					var name     = fundInfo.name;
//					logger.info("fund  {}  {}  {}", isinCode, risk, fundInfo.name);
					list.add(Asset.fund(dateTime, Company.SMTB, Currency.JPY, fund.value, risk, isinCode, name));
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
