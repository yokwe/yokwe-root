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
import yokwe.util.ClassUtil;
import yokwe.util.StringUtil;
import yokwe.util.UnexpectedException;
import yokwe.util.http.HttpUtil;

public final class UpdateSymbolInfo {
	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(ClassUtil.getCallerClass());

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
//			"QQQ.O"
			public String tickerInternal;
//			"QQQ"
			public String ticker;
//			"Invesco QQQ Trust Series 1"
			public String name;
//			"ﾅｽﾀﾞｯｸ"
			public String exchangeJP;
//			"NASDAQ 100 TR"
			public String indexName;
//			"0.20"
			public String expenseRatio;
//			"株式"
			public String assetClass;
//			"アメリカ"
			public String assetRegion;
//			"252.07"
			public String pctChangeStart;
//			"2.85"
			public String pctChangeYTD;
//			"-8.01"
			public String pctChane1M;
//			"-11.55"
			public String pctChane3M;
//			"-5.09"
			public String pctChane6M;
//			"-2.82"
			public String pctChane9M;
//			"3.17"
			public String pctChaneY1;
//			"36.69"
			public String pctChaneY2;
//			"43.23"
			public String pctChaneY3;
//			"101.22"
			public String pctChaneY5;
//			"米ドル"
			public String currency;
//			"0.83"
			public String pctChange1D;
//			"159.22"
			public String referencePrice;
//			"2018/11/20"
			public String referencePriceDate;
//			"パワーシェアーズ QQQ 信託シリーズ1"
			public String nameJP;
//			"パワーシェアーズ QQQ 信託シリーズ1(PowerShares QQQ Trust Series 1)はパワーシェアーズ・キューキューキュー指数連動株式と呼ばれる証券を発行するユニット型投資信託。同信託はナスダック100指数(Nasdaq-100 Index)(同指数)の構成証券の全てを保有する。同信託の投資目的は同指数の価格・利回り実績に連動する投資成果を提供すること。同信託のスポンサーはInvesco PowerShares Capital Management, LLCで、受託銀行はThe Bank of New York Mellonである。"
			public String description;
//			"67716.70"
			public String aum;
//			"百万米ドル"
			public String aumUnit;
//			"2018/10/31"
			public String aumDate;
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
				if (e.ticker.isEmpty()) continue;
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

				String     symbol     = e.ticker;
				String     name       = e.name.replaceAll("&amp;", "&");
				SymbolInfo symbolName = new SymbolInfo(symbol, SymbolInfo.Type.ETF, name);
				
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
