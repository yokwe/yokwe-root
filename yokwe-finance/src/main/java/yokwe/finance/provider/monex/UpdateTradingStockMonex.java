package yokwe.finance.provider.monex;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import yokwe.finance.Storage;
import yokwe.finance.stock.us.StockInfo;
import yokwe.finance.type.TradingStockInfo;
import yokwe.util.FileUtil;
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
		final String page = download(URL, CHARSET, FILE_PATH, DEBUG_USE_FILE);
		
		List<TradingStockInfo> list = new ArrayList<>();
		{
			for(String line: page.split("\n")) {
				line = line.trim();
				if (line.startsWith("{\"Ticker\":") && line.endsWith("},")) {
					String string = line.substring(0, line.length() - 1);
					UsMeigara usMeigara = JSON.unmarshal(UsMeigara.class, string);
					if (usMeigara.name.isEmpty()) continue;
					
					var stockCode = usMeigara.stockCode;
					var feeType   = TradingStockInfo.FeeType.PAID;
					var tradeType = TradingStockInfo.TradeType.BUY_SELL;
					
					list.add(new TradingStockInfo(stockCode, feeType, tradeType));
				}
			}
		}
		logger.info("list       {}", list.size());

		var stockCodeSet = StockInfo.getList().stream().map(o -> o.stockCode).collect(Collectors.toSet());
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
