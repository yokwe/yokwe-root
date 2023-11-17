package yokwe.finance.provider.moomoo;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import yokwe.finance.stock.StorageStock;
import yokwe.finance.type.TradingStockType;
import yokwe.util.FileUtil;
import yokwe.util.ScrapeUtil;
import yokwe.util.StringUtil;
import yokwe.util.UnexpectedException;
import yokwe.util.http.HttpUtil;

public class UpdateTradingStockMoomoo {
	private static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();
	
	private static final boolean DEBUG_USE_FILE = false;
	
	private static final String URL       = "https://www.moomoo.com/jp/support/topic7_134";
	private static final String CHARSET   = "UTF-8";
	private static final String FILE_PATH = StorageMoomoo.getPath("topic7_134.html");
	
	private static String download(String url, String charset, String filePath, boolean useFile) {
		final String page;
		{
			File file = new File(filePath);
			if (useFile && file.exists()) {
				page = FileUtil.read().file(file);
			} else {
				HttpUtil.Result result = HttpUtil.getInstance().withCharset(charset).download(url);
				if (result == null || result.result == null) {
					logger.error("Unexpected");
					logger.error("  result  {}", result);
					throw new UnexpectedException("Unexpected");
				}
				page = result.result;
				// debug
				if (DEBUG_USE_FILE) logger.info("save  {}  {}", page.length(), file.getPath());
				FileUtil.write().file(file, page);
			}
		}
		return page;
	}

	//<tr>
	//<td>A</td>
	//<td>アジレント・テクノロジー</td>
	//<td>製薬-診断と研究</td>
	//<td>NYSE</td>
	//</tr>

	public static class StockInfoX {
		public static final Pattern PAT = Pattern.compile(
				"<tr>\\s+" +
				"<td>(?<stockCode>.+?)</td>\\s+" +
				"<td.*?>(?<name>.+?)</td>\\s+" +
				"<td>(?<industry>.+?)</td>\\s+" +
				"<td>(?<market>.+?)</td>\\s+" +
				"</tr>",
				Pattern.DOTALL
		);
		public static List<StockInfoX> getInstance(String page) {
			return ScrapeUtil.getList(StockInfoX.class, PAT, page);
		}
		
		public String stockCode;
		public String name;
		public String industry;
		public String market;
		
		public StockInfoX(String stockCode, String name, String industry, String market) {
			this.stockCode = stockCode;
			this.name      = name;
			this.industry  = industry;
			this.market    = market;
		}
		
		@Override
		public String toString() {
			return StringUtil.toString(this);
		}
	}

	private static void update() {
		final String page = download(URL, CHARSET, FILE_PATH, DEBUG_USE_FILE);
		
		List<TradingStockType> list = new ArrayList<>();
		{
			for(var e: StockInfoX.getInstance(page)) {
				// skip prohibited stock
				if (e.stockCode.contains("*")) continue;
				
				var stockCode = e.stockCode;
				var feeType   = TradingStockType.FeeType.PAID;
				var tradeType = TradingStockType.TradeType.BUY_SELL;
				
				list.add(new TradingStockType(stockCode, feeType, tradeType));
			}
		}
		logger.info("list       {}", list.size());
		
		// remove duplicate
		{
			var set = list.stream().collect(Collectors.toSet());
			list = set.stream().collect(Collectors.toList());
			logger.info("list       {}", list.size());
		}

		var stockCodeSet = StorageStock.StockInfoUSAll.getList().stream().map(o -> o.stockCode).collect(Collectors.toSet());
		logger.info("stockCode  {}", stockCodeSet.size());
		
		list.removeIf(o -> !stockCodeSet.contains(o.stockCode));
		logger.info("list       {}", list.size());
		
		logger.info("save  {}  {}", list.size(), StorageMoomoo.TradingStockMoomoo.getPath());
		StorageMoomoo.TradingStockMoomoo.save(list);
	}
	
	
	public static void main(String[] args) {
		logger.info("START");
		
		update();
		
		logger.info("STOP");
	}
}
