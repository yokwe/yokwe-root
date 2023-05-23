package yokwe.stock.trade.monex;

import java.util.ArrayList;
import java.util.List;

import yokwe.stock.us.Stock;
import yokwe.util.UnexpectedException;
import yokwe.util.http.HttpUtil;
import yokwe.util.json.JSON;

public class UpdateMonexStock {
	private static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();
	
	private static final String DATA_URL     = "https://mxp1.monex.co.jp/mst/servlet/ITS/ucu/UsMeigaraJsonGST";
	private static final String DATA_CHARSET = "SHIFT_JIS";
	
	public static class Data {
		@JSON.Name("Ticker")
		public String ticker;
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
		
		public Data() {
			ticker    = "";
			name      = "";
			jname     = "";
			keyword   = "";
			etf       = "";
			shijo     = "";
			update    = "";
			gyoshu    = "";
			jigyo     = "";
			benchmark = "";
			shisan    = "";
			chiiki    = "";
			category  = "";
			keihi     = "";
			comp      = "";
			pdf       = "";
		}

		public boolean isETF() {
			return etf.equals("1");
		}
	}

	public static void main(String[] args) {
		logger.info("START");
		
		logger.info("url      {}", DATA_URL);
		HttpUtil.Result result = HttpUtil.getInstance().withCharset(DATA_CHARSET).download(DATA_URL);
		logger.info("response {}", result.response);
		logger.info("result   {}", result.result.length());
		// FileUtil.write().file(Storage.Monex.getPath("symbol-name"), result.result); // For debug
		String string = result.result;

		List<Data> dataList;
		{
			List<String> stringList = new ArrayList<>();
			for(String line: string.split("\n")) {
				line = line.trim();
				if (line.isEmpty()) continue;
				if (line.charAt(0) == '{') {
					if (line.charAt(line.length() - 1) == ',') {
						line = line.substring(0, line.length() - 1);
						// replace TAB with empty string for JSON compatibility
						line = line.replaceAll("\t", "");
						stringList.add(line);
					} else {
						logger.error("Unexpected line");
						logger.error("  line {}!", line);
						throw new UnexpectedException("Unpexted line");
					}
				}
			}
			logger.info("stringList {}", stringList.size());

			String jsonString = "[" + String.join(",\n", stringList) + "]";
			// FileUtil.write().file(Storage.Monex.getPath("symbol-name.json"), result.result); // For debug

			dataList = JSON.getList(Data.class, jsonString);
		}
		logger.info("dataList {}", dataList.size());

		List<Stock> list = new ArrayList<>();
		{
			var stockMap = Stock.getMap();
			for(var e: dataList) {
				if (e.name.isEmpty()) continue;
				
				String symbol = e.ticker;
				if (stockMap.containsKey(symbol)) {
					Stock stock = stockMap.get(symbol);
					list.add(stock);
				} else {
					logger.info("unexpeced symbol  {}  {}", e.ticker, e.name);
				}
			}
		}
		
		logger.info("save  {} {}", list.size(), MonexStock.getPath());
		MonexStock.save(list);
		
		logger.info("STOP");
	}
}
