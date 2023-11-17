package yokwe.finance.provider.nikko;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import yokwe.finance.Storage;
import yokwe.finance.account.nikko.WebBrowserNikko;
import yokwe.finance.stock.StorageStock;
import yokwe.finance.type.TradingStockType;
import yokwe.finance.type.TradingStockType.FeeType;
import yokwe.finance.type.TradingStockType.TradeType;
import yokwe.util.FileUtil;
import yokwe.util.ScrapeUtil;
import yokwe.util.UnexpectedException;

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
		return StorageNikko.getPath("stock", String.format("%03d.html", pageNo));
	}
	
	private static int download() {
		logger.info("download");
		try(var browser = new WebBrowserNikko()) {
			logger.info("login");
			browser.login();
			
			logger.info("trade");
			browser.trade();
			
			logger.info("listStockUS");
			browser.listStockUS();
			
			int pageNoMax = ShowPageInfo.getInstance(browser.getPage()).stream().mapToInt(o -> o.pageNo).max().getAsInt();
			logger.info("pageNoMax  {}", pageNoMax);
						
			for(;;) {
				browser.sleepRandom(1000);
				
				var page = browser.getPage();				
				var pageNo = PageNoInfo.getInstance(page).pageNo;
				logger.info("pageNo     {}", pageNo);
				
				FileUtil.write().file(getPath(pageNo), page);
				
				if (pageNo == pageNoMax) break;

				{
					var pageNoExpect = pageNo + 1;
					var script = String.format("showPage(%d);", pageNoExpect);
					browser.javaScript(script);
					
					for(int i = 0; i < 100; i++) {
						if (i == 10) {
							logger.error("Unexpected");
							throw new UnexpectedException("Unexpected");
						}

						var pageNoActual = PageNoInfo.getInstance(browser.getPage()).pageNo;
						if (pageNoActual == pageNoExpect) break;
						logger.info("same page  {}  {}  {}", i, pageNoExpect, pageNoActual);
						browser.sleepRandom();
					}
				}
			}
			
			logger.info("logout");
			browser.logout();
			
			logger.info("close");
			browser.close();
			
			return pageNoMax;
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
