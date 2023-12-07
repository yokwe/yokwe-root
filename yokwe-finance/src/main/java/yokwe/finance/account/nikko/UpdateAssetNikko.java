package yokwe.finance.account.nikko;

import java.io.File;
import java.util.List;
import java.util.regex.Pattern;

import yokwe.finance.Storage;
import yokwe.util.FileUtil;
import yokwe.util.ScrapeUtil;
import yokwe.util.StringUtil;

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
			
			logger.info("trade histtory download");
			browser.trade();
			browser.tradeHistory();
			browser.tradeHistoryY1();
			browser.tradeHistoryDownload();
			
			logger.info("logout");
			browser.logout();
		}
	}
	
	// MRF・お預り金予定残高
	public static class MRFInfo {
		public static final Pattern PAT = Pattern.compile(
			"<span .+?>※本日の残高は.+?<span .+?>\\s+" +
			"(?<value>[0-9,]+)</span>&nbsp;円&nbsp;です。\\s+" +
			"</span>"
		);
		public static MRFInfo getInstance(String page) {
			return ScrapeUtil.get(MRFInfo.class, PAT, page);
		}
		
		@ScrapeUtil.AsNumber
		public final int value;
		
		public MRFInfo(int value) {
			this.value = value;
		}
		
		@Override
		public String toString() {
			return String.format("%d", value);
		}
	}
	
	// 国内投資信託
	public static class FundInfo {
		public static final Pattern PAT = Pattern.compile(
			"<tr>\\s+" +
			"<td .+?><span .+?><a href=\"http://fund2\\.smbcnikko\\.co\\.jp.+?;KEY1=(?<fundCode>.+?)\".+?>(?<fundName>.+?)</a>（.+?）\\s+" +
			"<div .+?>.+?</td>\\s+" +
			"<td .+?>.+?</td>\\s+" +
			"<td .+?><span .+?>(?<accountType>.+?)</span></td>\\s+" +
			"<td .+?><span .+?>(?<units>.+?)</span></td>\\s+" +
			"<td .+?><span .+?>(?<unitPrice>.+?)<br>\\s+<a .+?>(?<unitCost>.+?)</a>\\s+</span></td>\\s+" +
			"<td .+?><span .+?>(?<value>.+?)<br><span .+?>(?<returns>.+?)</span></span></td>\\s+" +
			"</tr>\\s+" +
			"",
			Pattern.DOTALL
		);
		public static List<FundInfo> getInstance(String page) {
			return ScrapeUtil.getList(FundInfo.class, PAT, page);
		}
		
		public final String fundCode;
		public final String fundName;
		public final String accountType;
		@ScrapeUtil.AsNumber
		public final String units;
		@ScrapeUtil.AsNumber
		public final String unitPrice;
		@ScrapeUtil.AsNumber
		public final String unitCost;
		@ScrapeUtil.AsNumber
		public final String value;
		@ScrapeUtil.AsNumber
		public final String returns;
		
		public FundInfo(String fundCode, String fundName, String accountType, String units, String unitPrice, String unitCost, String value, String returns) {
			this.fundCode    = fundCode;
			this.fundName    = fundName;
			this.accountType = accountType;
			this.units       = units;
			this.unitPrice   = unitPrice;
			this.unitCost    = unitCost;
			this.value       = value;
			this.returns     = returns;
		}
		
		@Override
		public String toString() {
			return StringUtil.toString(this);
		}
	}
	
	
	// 外国株式
	public static class ForeignStockInfo {
		public static final Pattern PAT = Pattern.compile(
			"<tr>\\s+" +
			"<td .+?><span .+?>\\s+<a href=\"/InvestmentInformation/.+?>(?<stockName>.+?)</a>\\s+<br>\\s+(?<stockCode>.+?)\\(.+?</td>\\s+" +
			"<td .+?>.+?</td>\\s+" +
			"<td .+?><span .+?>(?<accountType>.+?)</span></td>\\s+" +
			"<td .+?><span .+?>(?<units>.+?)<br>(?<currency>.+?)</span></td>\\s+" +
			"",
			Pattern.DOTALL
		);
		public static List<ForeignStockInfo> getInstance(String page) {
			return ScrapeUtil.getList(ForeignStockInfo.class, PAT, page);
		}
		
		public final String stockName;
		public final String stockCode;
		public final String accountType;
		@ScrapeUtil.AsNumber
		public final String units;
		public final String currency;
		
		public ForeignStockInfo(String stockName, String stockCode, String accountType, String units, String currency) {
			this.stockName   = stockName;
			this.stockCode   = stockCode;
			this.accountType = accountType;
			this.units       = units;
			this.currency    = currency;
		}
		
		@Override
		public String toString() {
			return StringUtil.toString(this);
		}
	}
	
	
	private static void update() {
		{
			var page = FileUtil.read().file(FILE_BALANCE);

			var mrfInfo = MRFInfo.getInstance(page);
			logger.info("mrfInfo  {}", mrfInfo);
			
			var fundInfoList = FundInfo.getInstance(page);
			for(var e: fundInfoList) {
				logger.info("fundInfo  {}", e);
			}
			
			var foreignStockInfoList = ForeignStockInfo.getInstance(page);
			for(var e: foreignStockInfoList) {
				logger.info("foreginStock  {}", e);
			}

		}
		
	}
	
	public static void main(String[] args) {
		logger.info("START");
		
//		download();
		update();
		
		logger.info("STOP");
	}
}
