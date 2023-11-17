package yokwe.finance.provider.monex;

import java.io.File;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import yokwe.finance.stock.StorageStock;
import yokwe.finance.type.TradingStockType;
import yokwe.util.CSVUtil;
import yokwe.util.FileUtil;
import yokwe.util.ScrapeUtil;
import yokwe.util.UnexpectedException;
import yokwe.util.http.HttpUtil;

public class UpdateTradingStockMonex {
	private static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();
	
	private static final boolean DEBUG_USE_FILE = false;
	
	private static final String  URL       = "https://mst.monex.co.jp/pc/pdfroot/public/50/99/Monex_US_LIST.csv";
	private static final String  CHARSET   = "SHIFT_JIS";
	private static final String  FILE_PATH = StorageMonex.getPath("Monex_US_LIST.csv");
	
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
	
	
	static final class BuyFreeETF {
		private static final String URL       = "https://info.monex.co.jp/us-stock/etf_usa_program.html";
		private static final String CHARSET   = "UTF-8";
		private static final String FILE_PATH = StorageMonex.getPath("etf_usa_program.html");
		
		public static class ETFInfo {
			//  <tr>
			//    <th class="txt-al-c s-fw-b" rowspan="2"><a href="https://mst.monex.co.jp/pc/ITS/login/LoginIDPassword.jsp?transKbn=2&url1=/servlet/ITS/info/TransitionSsoForInsight&page=searchChart&dscr=VTI" class="link-cmn ico-cmn-blank" target="_blank">VTI</a></th>
			//    <td>バンガード・トータル・ストック・マーケットETF</td>
			//    <td rowspan="2">0.03%</td>
			//    <td rowspan="2">バンガード</td>
			//  </tr>
			
			public static final Pattern PAT = Pattern.compile(
					"<tr>\\s+" +
					"<th .+?><a .+?>(?:<span .+?>.+?</span></br>)?(?<symbol>.+?)(?:<span .+?>.+?</span>)?</a></th>\\s+" +
					"<td>(?<name>.+?)</td>\\s+" +
					"<td .+?>(?<expenseRatio>.+?)</td>\\s+" +
					"<td .+?>(?<company>.+?)</td>\\s+" +
					"</tr>" +
					""
			);
			public static List<ETFInfo> getInstance(String page) {
				return ScrapeUtil.getList(ETFInfo.class, PAT, page);
			}
			
			public final String symbol;
			public final String name;
			public final String expenseRatio;
			public final String company;
			
			
			public ETFInfo(String symbol, String name, String expenseRatio, String company) {
				this.symbol       = symbol;
				this.name         = name;
				this.expenseRatio = expenseRatio;
				this.company      = company;
			}
			
			@Override
			public String toString() {
				return String.format("%s", symbol);
			}
		}
		
		private static Set<String> getSet() {
			final String page = download(URL, CHARSET, FILE_PATH, DEBUG_USE_FILE);

			Set<String> set = new HashSet<>();
			for(var etfInfo: ETFInfo.getInstance(page)) {
				set.add(etfInfo.symbol);
			}
			
			return set;
		}
	}
	
	
	static class CSVData {
		//2023年10月20日
		//A,Agilent Technologies Inc,アジレント･テクノロジーズ,NYSE,Common Stock
		
		public String stockCode;
		public String name;
		public String nameJ;
		public String exch;
		public String type;
	}
	
	private static void update() {
		var buyFreeSet = BuyFreeETF.getSet();
		logger.info("buyFree    {}", buyFreeSet.size());
		
		final String page = download(URL, CHARSET, FILE_PATH, DEBUG_USE_FILE);
		
		List<TradingStockType> list = new ArrayList<>();
		{
			String csvString;
			{
				var array = page.split("[\r\n]+");
				csvString = String.join("\n", Arrays.copyOfRange(array, 1, array.length));
			}
			
			for(var csvData: CSVUtil.read(CSVData.class).withHeader(false).file(new StringReader(csvString))) {
				var stockCode = csvData.stockCode;
				var feeType   = buyFreeSet.contains(stockCode) ? TradingStockType.FeeType.BUY_FREE : TradingStockType.FeeType.PAID;
				var tradeType =TradingStockType.TradeType.BUY_SELL;
				
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
		
		logger.info("save  {}  {}", list.size(), StorageMonex.TradingStockMonex.getPath());
		StorageMonex.TradingStockMonex.save(list);
	}
	
	public static void main(String[] args) {
		logger.info("START");
		
		update();
				
		logger.info("STOP");
	}
}
