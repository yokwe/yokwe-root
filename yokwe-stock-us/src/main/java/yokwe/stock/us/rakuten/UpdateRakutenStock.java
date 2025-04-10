package yokwe.stock.us.rakuten;

import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import yokwe.stock.us.Stock;
import yokwe.util.CSVUtil;
import yokwe.util.CSVUtil.ColumnName;
import yokwe.util.ToString;
import yokwe.util.UnexpectedException;
import yokwe.util.http.HttpUtil;

public class UpdateRakutenStock {
	private static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();

	public static final class STOCK {
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

		private static List<Stock> getList(Map<String, Stock> stockMap) {
			List<Stock> stockList = new ArrayList<>();
			
			logger.info("STOCK");
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
				String symbol = e.ticker;
				
				if (stockMap.containsKey(symbol)) {
					Stock stock = stockMap.get(symbol);
					stockList.add(stock);
				} else {
					logger.warn("Unpexpected symbol  {}  {}", e.ticker, e.name);
				}
			}
			return stockList;
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

		private static List<Stock> getList(Map<String, Stock> stockMap) {
			List<Stock> stockList = new ArrayList<>();
			
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
					logger.error("  data {}", ToString.withFieldName(e));
					throw new UnexpectedException("Unexpected");
				}

				String symbol = e.symbol;
				
				if (stockMap.containsKey(symbol)) {
					Stock stock = stockMap.get(symbol);
					stockList.add(stock);
				} else {
					logger.warn("Unpexpected symbol  {}  {}", e.symbol, e.name);
				}
			}
			return stockList;
		}
	}

	public static void main(String[] args) {
		logger.info("START");
		
		Map<String, Stock> stockMap = Stock.getMap();
		
		List<Stock> stockList = STOCK.getList(stockMap);
		List<Stock> etfList   = ETF.getList(stockMap);
		
		logger.info("stock  {}", stockList.size());
		logger.info("etf    {}", etfList.size());
		
		List<Stock> list = new ArrayList<>();
		Set<String> set = new TreeSet<>();
		for(var e: stockList) {
			String symbol = e.symbol;
			if (set.contains(symbol)) continue;
			set.add(symbol);
			list.add(e);
		}
		for(var e: etfList) {
			String symbol = e.symbol;
			if (set.contains(symbol)) continue;
			set.add(symbol);
			list.add(e);
		}
				
		logger.info("save   {}  {}", list.size(), RakutenStock.getPath());
		RakutenStock.save(list);
		
		logger.info("STOP");
	}
}
