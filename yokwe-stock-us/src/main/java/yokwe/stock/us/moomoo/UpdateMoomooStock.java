package yokwe.stock.us.moomoo;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import yokwe.stock.us.Stock;
import yokwe.stock.us.Storage;
import yokwe.util.FileUtil;
import yokwe.util.ScrapeUtil;
import yokwe.util.ToString;
import yokwe.util.UnexpectedException;
import yokwe.util.http.HttpUtil;

public class UpdateMoomooStock {
	private static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();
	
	private static final boolean DEBUG_USE_FILE = false;
	
	private static final String URL       = "https://www.moomoo.com/jp/support/topic7_134";
	private static final String CHARSET   = "UTF-8";
	private static final String FILE_PATH = Storage.Moomoo.getPath("topic7_134.html");
	
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

	public static class StockInfo {
		public static final Pattern PAT = Pattern.compile(
				"<tr>\\s+" +
				"<td>(?<stockCode>.+?)</td>\\s+" +
				"<td.*?>(?<name>.+?)</td>\\s+" +
				"<td>(?<industry>.+?)</td>\\s+" +
				"<td>(?<market>.+?)</td>\\s+" +
				"</tr>",
				Pattern.DOTALL
		);
		public static List<StockInfo> getInstance(String page) {
			return ScrapeUtil.getList(StockInfo.class, PAT, page);
		}
		
		public String stockCode;
		public String name;
		public String industry;
		public String market;
		
		public StockInfo(String stockCode, String name, String industry, String market) {
			this.stockCode = stockCode;
			this.name      = name;
			this.industry  = industry;
			this.market    = market;
		}
		
		@Override
		public String toString() {
			return ToString.withFieldName(this);
		}
	}
	
	private static void update() {
		final String page = download(URL, CHARSET, FILE_PATH, DEBUG_USE_FILE);
		
		List<Stock> stockList = new ArrayList<>();
		{
			var stockMap = Stock.getMap();
			
			for(var e: StockInfo.getInstance(page)) {
				String symbol = e.stockCode.replace("*", "");
				
				if (stockMap.containsKey(symbol)) {
					Stock stock = stockMap.get(symbol);
					stockList.add(stock);
				} else {
					logger.info("unexpeced symbol  {}  {}", symbol, e.name);
				}
			}
		}
		
		logger.info("save   {}  {}", stockList.size(), MoomooStock.getPath());
		MoomooStock.save(stockList);
	}
	
	public static void main(String[] args) {
		logger.info("START");
		
		update();
		
		logger.info("STOP");
	}
}
