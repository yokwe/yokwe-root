package yokwe.finance.account.rakuten;

import java.io.BufferedReader;
import java.io.File;
import java.io.StringReader;
import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import yokwe.finance.Storage;
import yokwe.finance.account.Asset;
import yokwe.finance.account.Asset.Company;
import yokwe.finance.account.AssetRisk;
import yokwe.finance.account.UpdateAsset;
import yokwe.finance.fund.StorageFund;
import yokwe.finance.stock.StorageStock;
import yokwe.finance.type.Currency;
import yokwe.finance.type.FundInfoJP;
import yokwe.util.CSVUtil;
import yokwe.util.FileUtil;
import yokwe.util.UnexpectedException;

public class UpdateAssetRakuten implements UpdateAsset {
	private static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();
	
	private static final Storage storage          = Storage.account.rakuten;
	
	private static final File    FILE_TOP         = storage.getFile("top.html");
	private static final File    FILE_BALANCE     = storage.getFile("balance.html");
	
	private static final File    DIR_DOWNLOAD     = storage.getFile("download");
	private static final File    FILE_BALANCE_ALL = storage.getFile("balance-all.csv");
	
	private static final Charset CHARSET_CSV = Charset.forName("Shift_JIS");
	
	@Override
	public Storage getStorage() {
		return storage;
	}
	
	@Override
	public void download() {
		// empty DIR_DOWNLOAD
		{
			File[] files = DIR_DOWNLOAD.listFiles();
			for(var e: files) e.delete();
		}

		try(var browser = new WebBrowserRakuten(DIR_DOWNLOAD)) {
			logger.info("login");
			browser.login();
			browser.savePage(FILE_TOP);
			
			logger.info("balance");
			browser.balance();
			browser.savePage(FILE_BALANCE);
			
			// download csv file
			{
				browser.saveAsCSV();
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
			browser.logout();
		}
	}
	
	private static final List<FundInfoJP> fundInfoList = StorageFund.FundInfo.getList();
	private static FundInfoJP getFundInfo(String name) {
		String isinCode = null;
		for(var e: fundInfoList) {
			if (e.name.equals(name)) return e;
		}
		if (isinCode == null) {
			if (name.contains("(")) {
				String nameB = name.substring(0, name.indexOf("("));
				for(var e: fundInfoList) {
					if (e.name.equals(nameB)) return e;
				}
			}
		}
		
		logger.error("Unexpected name");
		logger.error("  name  {}!", name);
		throw new UnexpectedException("Unexpected name");
	}
	
	
	@Override
	public void update() {
		var list = new ArrayList<Asset>();
		
		// build assetList
		{
			LocalDateTime dateTime;
			String        page;
			{
				var file = FILE_BALANCE_ALL;
				
				var instant = FileUtil.getLastModified(file);
				dateTime = LocalDateTime.ofInstant(instant, ZoneId.systemDefault()).truncatedTo(ChronoUnit.SECONDS);
				page     = FileUtil.read().file(file);
				logger.info("  {}  {}  {}", dateTime, page.length(), file.getPath());
			}
			
			var usStockMap = StorageStock.StockInfoUSTrading.getMap();
			
			var patValueCurrency = Pattern.compile("(?<value>[0-9,\\.]+) (?<currency>...)");
			
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
					{
						var kindExpect = "国内株式";
						if (kind.equals(kindExpect)) {
							Currency currency = Currency.JPY;
							String code;
							{
								var string = stringArray[1];
								code = (string.length() == 4) ? string + "0" : string;
							}
							
							var name   = stringArray[2];
//							var accountType = stringArray[3];
							var value  = new BigDecimal(stringArray[14].replace(",", ""));
							var risk = AssetRisk.stockJP.getRisk(code);
							list.add(Asset.stock(dateTime, Company.RAKUTEN, currency, value, risk, code, name));
						}
					}
					{
						var kindExpect = "投資信託";
						if (kind.equals(kindExpect)) {
							Currency currency;
							{
								var string = stringArray[7];
								if (string.equals("円")) {
									currency = Currency.JPY;
								} else {
									currency = Currency.valueOf(string);
								}
							}
							var fund   = getFundInfo(stringArray[2]);
							var name   = fund.name;
							var code   = fund.isinCode;
//							var accountType = stringArray[3];
							var value  = new BigDecimal(stringArray[14].replace(",", ""));
							var risk = AssetRisk.fundCode.getRisk(code); 
							list.add(Asset.fund(dateTime, Company.RAKUTEN, currency, value, risk, code, name));
						}
					}
					{
						var kindExpect = "外貨預り金";
						if (kind.equals(kindExpect)) {
							var value    = new BigDecimal(stringArray[4].replace(",", "")).setScale(2);
							var currency = Currency.valueOf(stringArray[5]);
							list.add(Asset.deposit(dateTime, Company.RAKUTEN, currency, value, kindExpect));
						}
					}
					{
						var kindExpect = "米国株式";
						if (kind.equals(kindExpect)) {
							var code = stringArray[1];
							var name = stringArray[2];
							if (usStockMap.containsKey(code)) {
								name = usStockMap.get(code).name;
							}
//							var accountType = stringArray[3];
							BigDecimal value;
							Currency   currency;
							{
								var string = stringArray[15];
								var m = patValueCurrency.matcher(string);
								if (m.matches()) {
									value = new BigDecimal(m.group("value").replace(",", ""));
									currency =Currency.valueOf(m.group("currency"));
								} else {
									logger.error("Unexpected string");
									logger.error("  string {}!", string);
									throw new UnexpectedException("Unexpected string");
								}
							}
							var risk = AssetRisk.stockUS.getRisk(code);
							list.add(Asset.stock(dateTime, Company.RAKUTEN, currency, value, risk, code, name));
						}
					}
					{
						var kindExpect = "外貨建MMF";
						if (kind.equals(kindExpect)) {
							var name = stringArray[2];
//							var accountType = stringArray[3];
							BigDecimal value;
							Currency   currency;
							{
								var string = stringArray[15];
								var m = patValueCurrency.matcher(string);
								if (m.matches()) {
									value = new BigDecimal(m.group("value").replace(",", ""));
									currency =Currency.valueOf(m.group("currency"));
								} else {
									logger.error("Unexpected string");
									logger.error("  string {}!", string);
									throw new UnexpectedException("Unexpected string");
								}
							}
							list.add(Asset.mmf(dateTime, Company.RAKUTEN, currency, value, name));
						}
					}
					{
						var kindExpect = "外国債券";
						if (kind.equals(kindExpect)) {
							Currency currency = Currency.USD; // FIXME
							var code  = "";
							var name  = stringArray[2];
//							var accountType = stringArray[3];
							var value = new BigDecimal(stringArray[4].replace(",", ""));
							list.add(Asset.bond(dateTime, Company.RAKUTEN, currency, value, code, name));
						}
					}
				}
			}
		}

		for(var e: list) logger.info("list {}", e);
		
		logger.info("save  {}  {}", list.size(), getFile().getPath());
		save(list);
	}
	
	public static final UpdateAsset instance = new UpdateAssetRakuten();
	
	public static void main(String[] args) {
		logger.info("START");
				
		instance.download();
		instance.update();
		
		logger.info("STOP");
	}
}
