package yokwe.finance.provider.monex;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import yokwe.finance.Storage;
import yokwe.finance.stock.StockInfoUS;
import yokwe.finance.type.TradingStockType;
import yokwe.util.FileUtil;
import yokwe.util.ScrapeUtil;
import yokwe.util.UnexpectedException;
import yokwe.util.http.HttpUtil;
import yokwe.util.json.JSON;

public class UpdateTradingStockMonex {
	private static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();
	
	private static final boolean DEBUG_USE_FILE = false;
	
	private static final String  URL     = "https://mxp1.monex.co.jp/mst/servlet/ITS/ucu/UsMeigaraJsonGST";
	private static final String  CHARSET = "SHIFT_JIS";
	private static final String  FILE_PATH = Storage.provider_monex.getPath("UsMeigaraJsonGST");
	
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
		private static final String FILE_PATH = Storage.provider_monex.getPath("etf_usa_program.html");
		
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
	
	
	static class UsMeigara {
		@JSON.Name("Ticker")
		public String stockCode;
		public String name;
		public String jname;
		public String keyword;
		public String etf;
		public String shijo;
		public String update;
		public String gyoshu;
		public String jigyo;
		public String benchmark;
		public String shisan;
		public String chiiki;
		public String category;
		public String keihi;
		public String comp;
		public String pdf;
	}
	
	
	private static void update() {
		var buyFreeSet = BuyFreeETF.getSet();
		logger.info("buyFree    {}", buyFreeSet.size());
		
		final String page = download(URL, CHARSET, FILE_PATH, DEBUG_USE_FILE);
		
		List<TradingStockType> list = new ArrayList<>();
		{
			for(String line: page.split("\n")) {
				line = line.trim();
				if (line.startsWith("{\"Ticker\":") && line.endsWith("},")) {
					String string = line.substring(0, line.length() - 1);
					UsMeigara usMeigara = JSON.unmarshal(UsMeigara.class, string);
					if (usMeigara.name.isEmpty()) continue;
					
					var stockCode = usMeigara.stockCode;
					var feeType   = buyFreeSet.contains(stockCode) ? TradingStockType.FeeType.BUY_FREE : TradingStockType.FeeType.PAID;
					var tradeType =TradingStockType.TradeType.BUY_SELL;
					
					list.add(new TradingStockType(stockCode, feeType, tradeType));
				}
			}
		}
		logger.info("list       {}", list.size());

		var stockCodeSet = StockInfoUS.getList().stream().map(o -> o.stockCode).collect(Collectors.toSet());
		logger.info("stockCode  {}", stockCodeSet.size());

		var list2   = list.stream().filter(o -> stockCodeSet.contains(o.stockCode)).collect(Collectors.toList());
		logger.info("list2      {}", list2.size());
		
		logger.info("save  {}  {}", list2.size(), TradingStockMonex.getPath());
		TradingStockMonex.save(list2);
	}
	
	public static void main(String[] args) {
		logger.info("START");
		
		update();
				
		logger.info("STOP");
	}
}
