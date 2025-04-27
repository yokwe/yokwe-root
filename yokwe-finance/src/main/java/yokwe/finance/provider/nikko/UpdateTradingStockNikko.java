package yokwe.finance.provider.nikko;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriverException;

import yokwe.finance.Storage;
import yokwe.finance.account.Secret;
import yokwe.finance.stock.StorageStock;
import yokwe.finance.type.TradingStockType;
import yokwe.finance.type.TradingStockType.FeeType;
import yokwe.finance.type.TradingStockType.TradeType;
import yokwe.util.FileUtil;
import yokwe.util.ScrapeUtil;
import yokwe.util.UnexpectedException;
import yokwe.util.selenium.ChromeWebDriver;

public class UpdateTradingStockNikko {
	private static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();
	
	public static class PageNoInfo {
		// <input name="pageNo" type="hidden" value="2">
		public static final Pattern PAT = Pattern.compile(
			"<input name=\"pageNo\" type=\"hidden\" value=\"(?<pageNo>\\d+)\">" +
			""
		);
		public static PageNoInfo getInstance(String page) {
			return ScrapeUtil.get(PageNoInfo.class, PAT, page);
		}
		
		public int pageNo;
		
		public PageNoInfo(int pageNo) {
			this.pageNo = pageNo;
		}
		
		@Override
		public String toString() {
			return String.format("{%d}", pageNo);
		}
	}
	
	public static class ShowPageInfo {
		// <A href="javascript:showPage(0)">1</A>
		public static final Pattern PAT = Pattern.compile(
				"<a href=\"javascript:showPage\\((?<pageNo>\\d+)\\)\">\\d+</a>" +
				""
		);
		public static List<ShowPageInfo> getInstance(String page) {
			return ScrapeUtil.getList(ShowPageInfo.class, PAT, page);
		}
		
		public int pageNo;
		
		public ShowPageInfo(int pageNo) {
			this.pageNo = pageNo;
		}
		
		@Override
		public String toString() {
			return String.format("{%d}", pageNo);
		}
	}
	
	public static class StockInfo implements Comparable<StockInfo> {
		public static final Pattern PAT_A = Pattern.compile(
				"<tr>\\s+" +
			    // stockCode
			    "<td .+?>\\s+" +
				"<span .+?>\\s+" +
//			    "(?:<a .+?>)?(?<stockCode>[A-Z/]+)(?:</a>)?\\s+" +
			    "<a .+?>(?<stockCode>[A-Z/]+)</a>\\s+" +
//			    "(?<stockCode>[A-Z]+?)\\s+" +
			    "</span>\\s+" +
				"</td>\\s+" +
				// name
				"<td .+?>.+?<a .+?>(?<name>.+?)</a>.+?</td>\\s+" +
				// exchange
				"<td .+?>.+?</td>\\s+" +
				// industry
				"<td .+?>.+?</td>\\s+" +
				// restriction
				"<td .+?>.+?</td>\\s+" +
				// buy sell
				"<td .+?>(?<buySell>.+?)</td>\\s+" +
				// report
				"<td .+?>.+?</td>\\s+" +
				"</tr>" +
			
				"", Pattern.DOTALL);
		public static final Pattern PAT_B = Pattern.compile(
				"<tr>\\s+" +
			    // stockCode
			    "<td .+?>\\s+" +
				"<span .+?>\\s+" +
//			    "(?:<a .+?>)?(?<stockCode>[A-Z/]+)(?:</a>)?\\s+" +
//			    "<a .+?>(?<stockCode>[A-Z/]+)</a>\\s+" +
			    "(?<stockCode>[A-Z]+?)\\s+" +
			    "</span>\\s+" +
				"</td>\\s+" +
				// name
				"<td .+?>.+?<a .+?>(?<name>.+?)</a>.+?</td>\\s+" +
				// exchange
				"<td .+?>.+?</td>\\s+" +
				// industry
				"<td .+?>.+?</td>\\s+" +
				// restriction
				"<td .+?>.+?</td>\\s+" +
				// buy sell
				"<td .+?>(?<buySell>.+?)</td>\\s+" +
				// report
				"<td .+?>.+?</td>\\s+" +
				"</tr>" +
			
				"", Pattern.DOTALL);
		
		public static List<StockInfo> getInstance(String page) {
			var list_A = ScrapeUtil.getList(StockInfo.class, PAT_A, page);
			var list_B = ScrapeUtil.getList(StockInfo.class, PAT_B, page);
			
			list_A.addAll(list_B);
			Collections.sort(list_A);
			return list_A;
		}
		
		public String stockCode;
		public String name;
		public String buySell;
		
		public StockInfo(String stockCode, String name, String buySell) {
			this.stockCode = stockCode;
			this.name      = name;
			this.buySell   = buySell;
		}
		
		@Override
		public String toString() {
			return String.format("{%s  %s  %s}", stockCode, name, buySell);
		}

		@Override
		public int compareTo(StockInfo that) {
			return this.stockCode.compareTo(that.stockCode);
		}
	}
	
	private static String getPath(int pageNo) {
		return StorageNikko.storage.getPath("stock", String.format("%03d.html", pageNo));
	}
	
	private static int download() {
		logger.info("download");
		var builder = ChromeWebDriver.builder();
//		builder.withArguments("--headless");
		var driver = builder.build();
		
		try {
			// login
			{
				logger.info("login");
				driver.get("https://trade.smbcnikko.co.jp/Login/0/login/ipan_web/hyoji/");
				driver.wait.untilPageTansitionFinish();
				
				if (driver.getTitle().contains("システムメンテナンス")) {
					logger.error("system maintenance");
					throw new UnexpectedException("system maintenance");
				}
				
				// sanity check
				driver.check.titleContains("ログイン");
				
				var secret = Secret.read().nikko;
				driver.wait.untilPresenceOfElement(By.name("koza1")).sendKeys(secret.branch);
				driver.wait.untilPresenceOfElement(By.name("koza2")).sendKeys(secret.account);
				driver.wait.untilPresenceOfElement(By.name("passwd")).sendKeys(secret.password);
				
				driver.wait.untilPresenceOfElement(By.xpath("//button[@class='hyoji-submit__button__type']")).click();
				driver.wait.untilPageTansitionFinish();
			}
			
			// trade
			{
				logger.info("trade");
				driver.wait.untilPresenceOfElement(By.name("menu03")).click();
				driver.wait.untilPageTansitionFinish();
				// sanity check
				driver.check.titleContains("お取引");
			}
			
			// listStockUS
			{
				logger.info("listStockUS");
				driver.wait.untilPresenceOfElement(By.linkText("米国株式")).click();
				driver.wait.untilPageTansitionFinish();
				// sanity check
				driver.check.titleContains("米国株式 - 取扱銘柄一覧");
			}
			
			int pageNoMax = ShowPageInfo.getInstance(driver.getPageSource()).stream().mapToInt(o -> o.pageNo).max().getAsInt();
			logger.info("pageNoMax  {}", pageNoMax);
			
			int pageNo = 0;
			for(;;) {
				logger.info("pageNo     {}  /  {}", pageNo, pageNoMax);
				var page = driver.getPageSource();				
				FileUtil.write().file(getPath(pageNo), page);				
				
				if (page.contains("次の30件")) {
					{
						driver.wait.untilPresenceOfElement(By.linkText("次の30件")).click();
						driver.wait.untilPageTansitionFinish();
					}
					
					// sanity check
					{
						var pageNoExpect = pageNo + 1;
						var pageNoActual = PageNoInfo.getInstance(driver.getPageSource()).pageNo;
						if (pageNoActual != pageNoExpect) {
							logger.error("Unexpected");
							logger.error("  pageNoExpect  {}", pageNoExpect);
							logger.error("  pageNoActual  {}", pageNoActual);
							throw new UnexpectedException("Unexpected");
						}
					}
					
					pageNo++;
					continue;
				}
				break;
			}
			
			// logout
			{
				logger.info("logout");
				driver.wait.untilPresenceOfElement(By.name("btn_logout")).click();
				driver.wait.untilPageTansitionFinish();
				
				// sanity check
				if (!driver.getTitle().contains("ログアウト")) {
					logger.error("Unexpected window title");
					logger.error("  {}!", driver.getTitle());
					throw new UnexpectedException("Unexpected window title");
				}
			}
			return pageNoMax;
		} catch (WebDriverException e) {
			String exceptionName = e.getClass().getSimpleName();
			logger.error("{} {}", exceptionName, e);
			throw new UnexpectedException(exceptionName, e);
		} finally {
			driver.quit();
		}
	}
	
	private static void update() {
		Storage.initialize();
		
		var pageNoMax = download();
//		var pageNoMax = 82;
		logger.info("pageNoMax  {}", pageNoMax);
		
		List<TradingStockType> list = new ArrayList<>();
		
		// no of stock in nikko as of 2023-11-11
		//   2466  all
		//    278  sell
		//   2188  buy sell
		
		for(var pageNo = 0; pageNo <= pageNoMax; pageNo++) {
			var path = getPath(pageNo);
			var page = FileUtil.read().file(path);
			
			{
				var stockInfoList = StockInfo.getInstance(page);
								
				var mark = (stockInfoList.size() != 30 && pageNo != pageNoMax) ? "*" : " ";				
				logger.info("list  {}  {}  {}", pageNo, mark, stockInfoList.size());
				
				for(var e: stockInfoList) {
					String stockCode = e.stockCode.replace("/", ".");
					
					TradeType tradeType;
					{
						boolean canBuy  = e.buySell.contains("買い注文");
						boolean canSell = e.buySell.contains("売り注文");
						if (canBuy && canSell) {
							tradeType = TradeType.BUY_SELL;
						} else if (!canBuy && canSell) {
							tradeType = TradeType.SELL;
						} else if (!canBuy && !canSell) {
							logger.info("no trade  {}  {}", stockCode, e.name);
							continue;
						} else {
							logger.error("Unexpected");
							logger.error("  {}  {}", stockCode, e.name);
							logger.error("  canBuy  {}", canBuy);
							logger.error("  canSell {}", canSell);
							throw new UnexpectedException("Unexpected");
						}
					}
					
					list.add(new TradingStockType(stockCode, FeeType.PAID, tradeType));
				}
			}
		}
		logger.info("list  {}", list.size());
		
		// remove duplicate
		{
			var set = list.stream().collect(Collectors.toSet());
			list = set.stream().collect(Collectors.toList());
			logger.info("list  {}", list.size());
		}
		
		{
			var stockMap = StorageStock.StockInfoUSAll.getMap();
			list.removeIf(o -> !stockMap.containsKey(o.stockCode));
		}
		logger.info("list  {}", list.size());
		
		logger.info("save  {}  {}", list.size(), StorageNikko.TradingStockNikko.getPath());
		StorageNikko.TradingStockNikko.save(list);
	}
	
	public static void main(String[] args) {
		logger.info("START");
				
		update();
		
		logger.info("STOP");
	}
}
