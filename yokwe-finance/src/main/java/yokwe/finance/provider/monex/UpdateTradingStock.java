package yokwe.finance.provider.monex;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import yokwe.finance.Storage;
import yokwe.finance.type.TradingStockInfo;
import yokwe.util.FileUtil;
import yokwe.util.UnexpectedException;
import yokwe.util.http.HttpUtil;
import yokwe.util.json.JSON;

public class UpdateTradingStock {
	private static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();
	
	private static final String URL     = "https://mxp1.monex.co.jp/mst/servlet/ITS/ucu/UsMeigaraJsonGST";
	private static final String CHARSET = "SHIFT_JIS";
	
	private static final boolean DEBUG_USE_FILE = false;
	
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
		final String page;
		{
			File file = new File(Storage.provider_monex.getPath("UsMeigaraJsonGST"));
			if (DEBUG_USE_FILE && file.exists()) {
				page = FileUtil.read().file(file);
			} else {
				HttpUtil.Result result = HttpUtil.getInstance().withCharset(CHARSET).download(URL);
				if (result == null || result.result == null) {
					logger.error("Unexpected");
					logger.error("  result  {}", result);
					throw new UnexpectedException("Unexpected");
				}
				page = result.result;
				// debug
				if (DEBUG_USE_FILE) logger.info("save  {}  {}", file.getPath(), page.length());
				FileUtil.write().file(file, page);
			}
		}
		
		List<TradingStockInfo> list = new ArrayList<>();
		{
			for(String line: page.split("\n")) {
				line = line.trim();
				if (line.startsWith("{\"Ticker\":") && line.endsWith("},")) {
					String string = line.substring(0, line.length() - 1);
					UsMeigara usMeigara = JSON.unmarshal(UsMeigara.class, string);
					if (usMeigara.name.isEmpty()) continue;
					
					TradingStockInfo tradingStock = new TradingStockInfo();
					tradingStock.stockCode = usMeigara.stockCode;
					tradingStock.feeType   = TradingStockInfo.FeeType.NOT_FREE;
					tradingStock.tradeType = TradingStockInfo.TradeType.BUY_SELL;
					
					list.add(tradingStock);
				}
			}
		}
		
		logger.info("save  {}  {}", list.size(), TradingStock.getPath());
		TradingStock.save(list);
	}
	
	public static void main(String[] args) {
		logger.info("START");
		
		update();
				
		logger.info("STOP");
	}
}
