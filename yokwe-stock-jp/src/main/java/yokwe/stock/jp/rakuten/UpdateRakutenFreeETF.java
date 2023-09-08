package yokwe.stock.jp.rakuten;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import yokwe.stock.jp.jpx.Stock;
import yokwe.util.ScrapeUtil;
import yokwe.util.UnexpectedException;
import yokwe.util.http.HttpUtil;

public class UpdateRakutenFreeETF {
	private static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();
	
	private static final String URL = "https://www.rakuten-sec.co.jp/web/domestic/etf-etn-reit/lineup/0-etf.html";

	// <th rowspan="2" class="ta-c va-m">1305</th>
	// <th rowspan="2" class="ta-c va-m">1540<br>◆</th>

	public static class ETFInfo {
		public static final Pattern PAT = Pattern.compile(
				"<th rowspan=\"2\" class=\"ta-c va-m\">(?<stockCode>.+?)(?:<br>◆)?</th>"
		);
		public static List<ETFInfo> getInstance(String page) {
			return ScrapeUtil.getList(ETFInfo.class, PAT, page);
		}
		
		public final String stockCode;
		
		public ETFInfo(String stockCode) {
			this.stockCode = Stock.toStockCode5(stockCode);
		}
		
		@Override
		public String toString() {
			return String.format("%s", stockCode);
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
			String stockCode = e.stockCode;
			logger.info("etf  {}", stockCode);
			if (stockMap.containsKey(stockCode)) {
				list.add(stockMap.get(stockCode));
			} else {
				logger.error("Unexpected stockCode  {}!", stockCode);
			}
		}

		return list;
	}


	public static void main(String[] args) {
		logger.info("START");
		
		List<Stock> list = getList();
		
		logger.info("save   {}  {}", list.size(), RakutenFreeETF.getPath());
		RakutenFreeETF.save(list);
		
		logger.info("STOP");
	}

}
