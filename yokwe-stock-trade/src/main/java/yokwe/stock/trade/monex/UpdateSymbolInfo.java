package yokwe.stock.trade.monex;

import java.util.ArrayList;
import java.util.List;

import yokwe.stock.trade.Storage;
import yokwe.stock.us.SymbolInfo;
import yokwe.stock.us.SymbolInfo.Type;
import yokwe.util.ClassUtil;
import yokwe.util.UnexpectedException;
import yokwe.util.http.HttpUtil;
import yokwe.util.json.JSON;

public class UpdateSymbolInfo {
	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(ClassUtil.getCallerClass());
	
	private static final String DATA_URL     = "https://mxp1.monex.co.jp/mst/servlet/ITS/ucu/UsMeigaraJsonGST";
	private static final String DATA_CHARSET = "SHIFT_JIS";
	
	private static final String PATH = Storage.Monex.getPath("symbol-info.csv");
	public static final String getPath() {
		return PATH;
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
	
	private static void updateSymbolInfo() {
		logger.info("updateSymbolInfo");
		
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
		
		List<SymbolInfo> list = new ArrayList<>();
		for(var e: dataList) {
			SymbolInfo symbolName = new SymbolInfo(e.ticker, e.isETF() ? Type.ETF : Type.STOCK, e.name);
			if (symbolName.name.isEmpty()) continue;
			list.add(symbolName);
		}
		
		logger.info("save  {} {}", list.size(), getPath());

		save(list);
	}
	public static void main(String[] args) {
		logger.info("START");
		
		updateSymbolInfo();
		
		logger.info("STOP");
	}

}
