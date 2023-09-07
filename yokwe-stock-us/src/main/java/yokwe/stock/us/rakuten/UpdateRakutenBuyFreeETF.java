package yokwe.stock.us.rakuten;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import yokwe.stock.us.Stock;
import yokwe.util.ScrapeUtil;
import yokwe.util.UnexpectedException;
import yokwe.util.http.HttpUtil;

public class UpdateRakutenBuyFreeETF {
	private static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();
	
	private static final String URL = "https://www.rakuten-sec.co.jp/web/foreign/etf/etf-etn-reit/lineup/0-etf.html";
	
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
		
		logger.info("save   {}  {}", list.size(), RakutenBuyFreeETF.getPath());
		RakutenBuyFreeETF.save(list);
		
		logger.info("STOP");
	}

}
