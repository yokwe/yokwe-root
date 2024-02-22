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

import yokwe.finance.Storage;
import yokwe.finance.account.Asset;
import yokwe.finance.account.Asset.Company;
import yokwe.finance.account.AssetInfo;
import yokwe.finance.account.UpdateAsset;
import yokwe.finance.fund.StorageFund;
import yokwe.finance.type.Currency;
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
	
	private static final File[] FILES = {
		FILE_TOP,
		FILE_BALANCE,
		FILE_BALANCE_BANK,
		FILE_TORIREKI,
	};
	
	private static final Charset CHARSET_CSV = Charset.forName("Shift_JIS");
	
	@Override
	public Storage getStorage() {
		return storage;
	}
	
	@Override
	public void download() {
		deleteFile(FILES);
		deleteFile(DIR_DOWNLOAD.listFiles());
		
		try(var browser = new WebBrowserNikko(DIR_DOWNLOAD)) {
			logger.info("login");
			browser.login();
			browser.savePage(FILE_TOP);
			
			logger.info("balance");
			browser.balance();
			browser.savePage(FILE_BALANCE);
			
			logger.info("balance bank");
			browser.balanceBank();
			browser.savePage(FILE_BALANCE_BANK);
						
			// download csv file
			{
				logger.info("trade history");
				browser.trade();
				browser.tradeHistory();
				browser.tradeHistoryDownload();
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
				var assetInfo = AssetInfo.fundCode.getAssetInfo(isinCode);
				list.add(Asset.fund(dateTime, Company.NIKKO, Currency.JPY, value, assetInfo, cost, isinCode, e.fundName));
			}
			
			var foreignStockInfoList = BalancePage.ForeignStockInfo.getInstance(page);
			for(var e: foreignStockInfoList) {
//				logger.info("foreginStock  {}", e);
				var currency = Currency.valueOf(e.currency);
				
				var fxRate    = new BigDecimal(e.fxRate);
				var valueJPY  = new BigDecimal(e.valueJPY);
				var costJPY   = new BigDecimal(e.costJPY);
				
				var value     = valueJPY.divide(fxRate, 2, RoundingMode.HALF_EVEN);
				var cost      = costJPY.divide(fxRate, 2, RoundingMode.HALF_EVEN);
				var code      = e.stockCode;
				var assetInfo = AssetInfo.stockUS.getAssetInfo(code);
				var name      = assetInfo.name;
				
				list.add(Asset.stock(dateTime, Company.NIKKO, currency, value, assetInfo, cost, code, name));
			}
			
			var foreignMMFList = BalancePage.ForeignMMFInfo.getInstance(page);
			for(var e: foreignMMFList) {
//				logger.info("foreignMMF  {}", e);
				var currency = Currency.valueOf(e.currency);
				var value    = new BigDecimal(e.value);
				
				list.add(Asset.deposit(dateTime, Company.NIKKO, currency, value, e.name));
			}
			
			var foreignBondList = BalancePage.ForeignBondInfo.getInstance(page);
			for(var e: foreignBondList) {
//				logger.info("foreignBond  {}", e);
				var currency = Currency.valueOf(e.currency);
				var value    = new BigDecimal(e.units);
				var cost     = value;  // FIXME get cost of foreign bond
				list.add(Asset.bond(dateTime, Company.NIKKO, currency, value, cost, e.code, e.name));
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
					list.add(Asset.termDeposit(dateTime, Company.SMBC, Currency.JPY, BigDecimal.valueOf(termDeposit.value), "DEPOSIT_TERM"));
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
