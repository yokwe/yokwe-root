package yokwe.stock.trade.rakuten;

import java.io.Reader;
import java.io.StringReader;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import yokwe.stock.trade.Storage;
import yokwe.stock.us.SymbolInfo;
import yokwe.util.CSVUtil;
import yokwe.util.CSVUtil.ColumnName;
import yokwe.util.StringUtil;
import yokwe.util.UnexpectedException;
import yokwe.util.http.HttpUtil;

public final class UpdateSymbolInfo {
	private static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();

	private static final String PATH = Storage.Rakuten.getPath("symbol-info.csv");
	public static final String getPath() {
		return PATH;
	}
	private static final String PATH_ETF   = Storage.Rakuten.getPath("symbol-info-etf.csv");
	private static final String PATH_STOCK = Storage.Rakuten.getPath("symbol-info-stock.csv");

	public static void save(Collection<SymbolInfo> collection) {
		SymbolInfo.save(collection, getPath());
	}
	public static void save(List<SymbolInfo> list) {
		SymbolInfo.save(list, getPath());
	}
	public static List<SymbolInfo> load() {
		return SymbolInfo.load(getPath());
	}
	public static List<SymbolInfo> getList() {
		return SymbolInfo.getList(getPath());
	}

	public static final class Stock {
		private static final String URL = "https://www.trkd-asia.com/rakutensec/exportcsvus?all=on&vall=on&r1=on&forwarding=na&target=0&theme=na&returns=na&head_office=na&name=&sector=na&pageNo=&c=us&p=result";

		// 現地コード,銘柄名(English),銘柄名,市場,業種,取扱
		public static class Data {
			public static final String TRADEABLE_YES = "○";
			
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

		private static void update(Map<String, SymbolInfo> map) {
			logger.info("Stock");
			List<Data> list;
			{
				String string = HttpUtil.getInstance().download(URL).result;
				logger.info("string {}", string.length());
				
				Reader reader = new StringReader(string);
				
				list = CSVUtil.read(Data.class).file(reader);
			}
			logger.info("list   {}", list.size());
			
			//  symbol
			for(var e: list) {
				String     symbol      = e.ticker;
				SymbolInfo symbolName = new SymbolInfo(symbol, SymbolInfo.Type.STOCK, e.name);
				
				if (map.containsKey(symbol)) {
					var old = map.get(symbol);
					logger.error("Duplicate symbol");
					logger.error("  old {}", old.toString());
					logger.error("  new {}", symbol.toString());
					throw new UnexpectedException("Duplicate symbol");
				} else {
					map.put(symbol, symbolName);
				}
			}
		}
	}
	
	public static final class ETF {
		public static final String URL = "https://www.rakuten-sec.co.jp/web/market/search/etf_search/ETFD.csv";

		public static class Data {
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

		private static void update(Map<String, SymbolInfo> map) {
			logger.info("ETF");
			List<Data> list;
			{
				String string = HttpUtil.getInstance().download(URL).result;
				logger.info("string {}", string.length());
				
				Reader reader = new StringReader(string);
				
				// csv file contains no header
				list = CSVUtil.read(Data.class).withHeader(false).file(reader);
			}
			logger.info("list   {}", list.size());
			
			//  symbol
			for(var e: list) {
				// sanity check
				if (e.symbol.isEmpty()) continue;
				switch(e.exchangeJP) {
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
					logger.warn("  data {}", e.toString());
					continue;
				default:
					logger.error("Unpexpected exchangeJP");
					logger.error("  data {}", StringUtil.toString(e));
					throw new UnexpectedException("Unexpected");
				}

				String     symbol     = e.symbol;
				String     name       = e.name.replaceAll("&amp;", "&");
				SymbolInfo symbolInfo = new SymbolInfo(symbol, SymbolInfo.Type.ETF, name);
				
				if (map.containsKey(symbol)) {
					var old = map.get(symbol);
					logger.error("Duplicate symbol");
					logger.error("  old {}", old.toString());
					logger.error("  new {}", symbol.toString());
					throw new UnexpectedException("Duplicate symbol");
				} else {
					map.put(symbol, symbolInfo);
				}
			}
		}
	}

	public static void main(String[] args) {
		logger.info("START");
		
		// Stock
		Map<String, SymbolInfo> stockMap = new TreeMap<>();
		Stock.update(stockMap);
		logger.info("save {} {}", stockMap.size(), PATH_STOCK);
		SymbolInfo.save(stockMap.values(), PATH_STOCK);
		
		// ETF
		Map<String, SymbolInfo> etfMap = new TreeMap<>();
		ETF.update(etfMap);
		logger.info("save {} {}", etfMap.size(), PATH_ETF);
		SymbolInfo.save(etfMap.values(), PATH_ETF);

		// merge stockMap and etfMap
		int countA = 0;
		int countB = 0;
		Map<String, SymbolInfo> map = new TreeMap<>(stockMap);
		for(var etf: etfMap.values()) {
			String symbol = etf.symbol;
			if (map.containsKey(symbol)) {
				var entry = map.get(symbol);
				entry.name = etf.name;
				countA++;
			} else {
				map.put(symbol, etf);
				countB++;
				// logger.info("etf {}", etf);
			}
		}
		logger.info("countA {}", countA);
		logger.info("countB {}", countB);
		
		logger.info("save  {} {}", map.size(), getPath());
		SymbolInfo.save(map.values(), getPath());

		logger.info("STOP");
	}
}
