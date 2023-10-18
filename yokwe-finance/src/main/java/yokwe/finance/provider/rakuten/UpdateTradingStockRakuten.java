package yokwe.finance.provider.rakuten;

import java.io.File;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import yokwe.finance.stock.StorageStock;
import yokwe.finance.type.TradingStockType;
import yokwe.util.CSVUtil;
import yokwe.util.CSVUtil.ColumnName;
import yokwe.util.FileUtil;
import yokwe.util.ScrapeUtil;
import yokwe.util.StringUtil;
import yokwe.util.UnexpectedException;
import yokwe.util.http.HttpUtil;

public class UpdateTradingStockRakuten {
	private static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();
	
	private static final boolean DEBUG_USE_FILE = false;
	
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
		private static final String URL       = "https://www.rakuten-sec.co.jp/web/foreign/etf/etf-etn-reit/lineup/0-etf.html";
		private static final String CHARSET   = "UTF-8";
		private static final String FILE_PATH = StorageRakuten.getPath("0-etf.html");
		
		//<tr>
		//  <td class="ta-c va-m" rowspan="2"><a href="https://www.rakuten-sec.co.jp/web/market/search/us_search/quote.html?ric=AGG.P">AGG</a><br><strong>【NEW】</strong></td>
		//  <td><a href="https://www.rakuten-sec.co.jp/web/market/search/us_search/quote.html?ric=AGG.P">iシェアーズ コア 米国総合債券市場 ETF</a></td>
		//  <td class="ta-c">NYSE Arca</td>
		//  <td class="ta-c">0.03%</td>
		//</tr>
		public static class ETFInfo {
			public static final Pattern PAT = Pattern.compile(
					"<tr>\\s+" +
					"<td class=\"ta-c va-m\" rowspan=\"2\"><a .+?>(?<symbol>.+?)</a>.*?</td>\\s+" +
					"<td>.+?</td>\\s+" +
					"<td class=\"ta-c\">(?<exchange>.+?)</td>\\s+" +
					"<td class=\"ta-c\">(?<expenseRatio>.+?)</td>\\s+" +
					"</tr>"
			);
			public static List<ETFInfo> getInstance(String page) {
				return ScrapeUtil.getList(ETFInfo.class, PAT, page);
			}
			
			public final String symbol;
			public final String exchange;
			public final String expenseRatio;
			
			public ETFInfo(String symbol, String exchange, String expenseRatio) {
				this.symbol       = symbol;
				this.exchange     = exchange;
				this.expenseRatio = expenseRatio;
			}
			
			@Override
			public String toString() {
				return String.format("%s %s %s", symbol, exchange, expenseRatio);
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
	
	private static final class STOCK {
		private static final String URL       = "https://www.trkd-asia.com/rakutensec/exportcsvus?all=on&vall=on&r1=on&forwarding=na&target=0&theme=na&returns=na&head_office=na&name=&sector=na&pageNo=&c=us&p=result";
		private static final String CHARSET   = "UTF-8";
		private static final String FILE_PATH = StorageRakuten.getPath("exportcsvus.csv");

		public static final String TRADEABLE_YES = "○";
		
		// 現地コード,銘柄名(English),銘柄名,市場,業種,取扱
		public static class Data {
			@ColumnName("現地コード")
			public String ticker;
			@ColumnName("銘柄名(English)")
			public String name;
			@ColumnName("銘柄名")
			public String nameJP;
			@ColumnName("市場")
			public String exchange;
			@ColumnName("業種")
			public String industry;
			@ColumnName("取扱")
			public String tradeable;
		}

		private static List<TradingStockType> getList(Set<String> buyFreeSet) {
			final String page = download(URL, CHARSET, FILE_PATH, DEBUG_USE_FILE);
			
			List<Data> dataList = CSVUtil.read(Data.class).file(new StringReader(page));
			
			//  trading symbol
			Set<String> set = new HashSet<>();
			List<TradingStockType> list = new ArrayList<>();
			for(var data: dataList) {
				String stockCode = data.ticker;
				
				// sanity check
				if (set.contains(stockCode)) {
					logger.info("sock duplicate  {}", stockCode);
					continue;
				}
				if (!data.tradeable.equals(TRADEABLE_YES)) {
					// skip not tradeable
					continue;
				}
				set.add(stockCode);
				
				var feeType   = buyFreeSet.contains(stockCode) ? TradingStockType.FeeType.BUY_FREE : TradingStockType.FeeType.PAID;
				var tradeType = TradingStockType.TradeType.BUY_SELL;
				
				list.add(new TradingStockType(stockCode, feeType, tradeType));
			}
			return list;
		}
	}
	
	public static final class ETF {
		private static final String URL       = "https://www.rakuten-sec.co.jp/web/market/search/etf_search/ETFD.csv";
		private static final String CHARSET   = "UTF-8";
		private static final String FILE_PATH = StorageRakuten.getPath("etfd.csv");

		static class Data {
			public String f01;
			public String symbol;
			public String name;
			public String exchangeJP;
			public String f05;
			public String f06;
			public String f07;
			public String f08;
			public String f09;
			
			public String f10;
			public String f11;
			public String f12;
			public String f13;
			public String f14;
			public String f15;
			public String f16;
			public String f17;
			public String f18;
			public String f19;
			
			public String f20;
			public String f21;
			public String f22;
			public String f23;
			public String f24;
			public String f25;
			public String f26;
			public String f27;
			public String f28;
			public String f29;
			
			public String f30;
			public String f31;
			public String f32;
			public String f33;
			public String f34;
			public String f35;
			public String f36;
			public String f37;
			public String f38;
			public String f39;
			
			public String f40;
			public String f41;
			public String f42;
			public String f43;
			public String f44;
			public String f45;
			public String f46;
			public String f47;
			public String f48;
			public String f49;
			
			public String f50;
			public String f51;
			public String f52;
			public String f53;
			public String f54;
			public String f55;
			public String f56;
			public String f57;
			public String f58;			
		}

		private static List<TradingStockType> getList(Set<String> buyFreeSet) {
			final String page = download(URL, CHARSET, FILE_PATH, DEBUG_USE_FILE);
			
			List<Data> dataList = CSVUtil.read(Data.class).withHeader(false).file(new StringReader(page));
			
			//  symbol
			Set<String> set = new HashSet<>();
			List<TradingStockType> list = new ArrayList<>();
			for(var data: dataList) {
				String stockCode = data.symbol;
				
				// sanity check
				if (stockCode.isEmpty()) continue;
				if (set.contains(stockCode)) {
					logger.info("etf duplicate  {}", stockCode);
					continue;
				}
				set.add(stockCode);
				
				switch(data.exchangeJP) {
				case "香港":
				case "名証ETF":
				case "東証ETF":
				case "ｼﾝｶﾞﾎﾟｰﾙ":
					continue;
				case "ﾅｽﾀﾞｯｸ":
				case "NYSE ARCA":
					break;
				case "":
					logger.warn("No exchangeJP");
					logger.warn("  data {}", data.toString());
					continue;
				default:
					logger.error("Unpexpected exchangeJP");
					logger.error("  data {}", StringUtil.toString(data));
					throw new UnexpectedException("Unexpected");
				}
				
				var feeType   = buyFreeSet.contains(stockCode) ? TradingStockType.FeeType.BUY_FREE : TradingStockType.FeeType.PAID;
				var tradeType = TradingStockType.TradeType.BUY_SELL;
				
				list.add(new TradingStockType(stockCode, feeType, tradeType));
			}
			return list;
		}
	}


	private static void update() {
		var buyFreeSet = BuyFreeETF.getSet();
		logger.info("buyFree    {}", buyFreeSet.size());
		
		List<TradingStockType> list;
		{
			var stockList = STOCK.getList(buyFreeSet);
			var etfList   = ETF.getList(buyFreeSet);
			
			Map<String, TradingStockType> map = new HashMap<>();
			for(var e: stockList) {
				if (!map.containsKey(e.stockCode)) map.put(e.stockCode, e);
			}
			for(var e: etfList) {
				if (!map.containsKey(e.stockCode)) map.put(e.stockCode, e);
			}
			
			logger.info("stock      {}", stockList.size());
			logger.info("etf        {}", etfList.size());
			logger.info("map        {}", map.size());
			
			list = map.values().stream().collect(Collectors.toList());
		}
		logger.info("list       {}", list.size());

		var stockCodeSet = StorageStock.StockInfoUS.getList().stream().map(o -> o.stockCode).collect(Collectors.toSet());
		logger.info("stockCode  {}", stockCodeSet.size());

		var list2   = list.stream().filter(o -> stockCodeSet.contains(o.stockCode)).collect(Collectors.toList());
		logger.info("list2      {}", list2.size());
		
		logger.info("save  {}  {}", list2.size(), StorageRakuten.TradingStockRakuten.getPath());
		StorageRakuten.TradingStockRakuten.save(list2);
	}
	
	public static void main(String[] args) {
		logger.info("START");
		
		update();
				
		logger.info("STOP");
	}
}
