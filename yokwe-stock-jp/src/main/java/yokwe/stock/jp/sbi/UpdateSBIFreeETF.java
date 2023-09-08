package yokwe.stock.jp.sbi;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import yokwe.stock.jp.jpx.Stock;
import yokwe.util.ScrapeUtil;
import yokwe.util.UnexpectedException;
import yokwe.util.http.HttpUtil;

public class UpdateSBIFreeETF {
	private static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();
	
	private static final String URL = "https://search.sbisec.co.jp/v2/popwin/info/dstock/pop_home_info191206_01.html";

	// <td class="vaM alC"><p>2561</p></td>
	
	//	<tr>
	//	<td class="vaM alC"><p>1546</p></td>
	//	<td class="vaM"><p>NEXT FUNDS ダウ・ジョーンズ工業株30種平均株価連動型上場投信</p></td>
	//	</tr>
	
	// <tr><td class="vaM alC"><p>2623</p></td><td class="vaM"><p>ｉシェアーズ　ユーロ建て投資適格社債　ＥＴＦ（為替ヘッジあり）</p></td></tr>

	public static class ETFInfo {
		public static final Pattern PAT = Pattern.compile(
				"<tr>\\s*" +
				"<td class=\"vaM alC\"><p>(?<stockCode>.+?)</p></td>\\s*" +
				"<td class=\"vaM\"><p>(?<name>.+?)</p></td>\\s*" +
				"</tr>"
		);
		public static List<ETFInfo> getInstance(String page) {
			return ScrapeUtil.getList(ETFInfo.class, PAT, page);
		}
		
		public final String stockCode;
		public final String name;
		
		public ETFInfo(String stockCode, String name) {
			this.stockCode = Stock.toStockCode5(stockCode);
			this.name      = name;
		}
		
		@Override
		public String toString() {
			return String.format("{%s  %s}", stockCode, name);
		}
	}

	
	private static List<Stock> getList() {
		Map<String, Stock> stockMap = Stock.getMap();
		
		List<Stock> list = new ArrayList<>();
		
		HttpUtil.Result result = HttpUtil.getInstance().withCharset("SJIS").download(URL);
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
				logger.error("Unexpected etf  {}!", e);
			}
		}

		return list;
	}


	public static void main(String[] args) {
		logger.info("START");
		
		List<Stock> list = getList();
		
		logger.info("save   {}  {}", list.size(), SBIFreeETF.getPath());
		SBIFreeETF.save(list);
		
		logger.info("STOP");
	}

}
