package yokwe.stock.us.sbi;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import yokwe.stock.us.Stock;
import yokwe.util.ScrapeUtil;
import yokwe.util.UnexpectedException;
import yokwe.util.http.HttpUtil;

public class UpdateSBIBuyFreeETF {
	private static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();
	
	private static final String URL = "https://go.sbisec.co.jp/lp/lp_us_etf_selection_220331.html";
	
	// <p class="small">ティッカー：GLDM</p>
	public static class ETFInfo {
		public static final Pattern PAT = Pattern.compile(
				"<p class=\"small\">ティッカー：(?<symbol>.+?)</p>"
		);
		public static List<ETFInfo> getInstance(String page) {
			return ScrapeUtil.getList(ETFInfo.class, PAT, page);
		}
		
		public final String symbol;
		
		public ETFInfo(String symbol) {
			this.symbol       = symbol;
		}
		
		@Override
		public String toString() {
			return String.format("%s", symbol);
		}
	}

	
	private static List<Stock> getList() {
		Map<String, Stock> stockMap = Stock.getMap();
		
		List<Stock> list = new ArrayList<>();
		
		HttpUtil.Result result = HttpUtil.getInstance().download(URL);
		if (result == null || result.result == null) {
			logger.error("Unexpected");
			logger.error("  result  {}", result);
			throw new UnexpectedException("Unexpected");
		}
		logger.info("result  {}", result.result.length());
		
		for(var e: ETFInfo.getInstance(result.result)) {
			String symbol = e.symbol;
			logger.info("etf  {}", symbol);
			if (stockMap.containsKey(symbol)) {
				list.add(stockMap.get(symbol));
			} else {
				logger.error("Unexpected symbol  {}", symbol);
			}
		}

		return list;
	}


	public static void main(String[] args) {
		logger.info("START");
		
		List<Stock> list = getList();
		
		logger.info("save   {}  {}", list.size(), SBIBuyFreeETF.getPath());
		SBIBuyFreeETF.save(list);
		
		logger.info("STOP");
	}

}
