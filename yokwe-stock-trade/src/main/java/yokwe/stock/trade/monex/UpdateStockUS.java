package yokwe.stock.trade.monex;

import java.util.ArrayList;
import java.util.List;

import yokwe.util.FileUtil;
import yokwe.util.UnexpectedException;
import yokwe.util.http.HttpUtil;
import yokwe.util.json.JSON;

public class UpdateStockUS {
	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(UpdateStockUS.class);

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
		FileUtil.write().file("tmp/monex-stock-us", result.result); // FIXME
		String string = result.result;
		
//		String string = FileUtil.read().file("tmp/monex-stock-us"); // FIXME
		
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
			FileUtil.write().file("tmp/monex-stock-us.json", jsonString); // FIXME
			dataList = JSON.getList(Data.class, jsonString);
		}
		logger.info("dataList {}", dataList.size());
		
		List<StockUS> list = new ArrayList<>();
		for(var e: dataList) {
			StockUS stockUS = new StockUS(e.ticker, e.name);
			if (stockUS.name.isEmpty()) continue;
			list.add(stockUS);
		}
		
		logger.info("save {} {}", StockUS.getPath(), list.size());
		StockUS.save(list);
		
		logger.info("STOP");
	}

}
