package yokwe.finance.provider.sbi;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import yokwe.finance.Storage;
import yokwe.finance.type.TradingStockInfo;
import yokwe.util.FileUtil;
import yokwe.util.ScrapeUtil;
import yokwe.util.StringUtil;
import yokwe.util.StringUtil.MatcherFunction;
import yokwe.util.UnexpectedException;
import yokwe.util.http.HttpUtil;

public class UpdateTradingStock {
	private static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();
	
	private static final boolean DEBUG_USE_FILE = false;
	
	private static final class BuyFreeETF {
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
		
		private static Set<String> getSet() {
			Set<String> set = new HashSet<>();
			
			String string;
			{
				File file = new File(Storage.provider_sbi.getPath("lp_us_etf_selection_220331.html"));
				if (DEBUG_USE_FILE && file.exists()) {
					string = FileUtil.read().file(file);
				} else {
					HttpUtil.Result result = HttpUtil.getInstance().download(URL);
					if (result == null || result.result == null) {
						logger.error("Unexpected");
						logger.error("  result  {}", result);
						throw new UnexpectedException("Unexpected");
					}
					string = result.result;
					// debug
					if (DEBUG_USE_FILE) logger.info("save  {}  {}", file.getPath(), string.length());
					FileUtil.write().file(file, string);
				}
			}
			
			for(var etfInfo: ETFInfo.getInstance(string)) {
				set.add(etfInfo.symbol);
			}
			
			return set;
		}
	}
	
	
	private static final String URL       = "https://search.sbisec.co.jp/v2/popwin/info/stock/pop6040_usequity_list.html";
	private static final String ENCODING  = "SHIFT_JIS";
	
	public static class StockInfo {
		// STOCK
		//	<tr>
		//	<th class="vaM alC">A</th>
		//	<td>Agilent Technologies<br>アジレント テクノロジーズ</td>
		//	<td>環境、食品の品質・安全性等の化学分析を行うツールを開発、提供</td>
		//	<td class="vaM alC">NYSE</td>
		//	</tr>
		
		public static final Pattern PAT = Pattern.compile(
				"<tr>\\s+" +
				"<th class=\"vaM alC\">(?<symbol>.+?)</th>\\s+" +
				"<td>(?<nameEN>.+?)<br>(?<nameJP>.+?)</td>\\s+" +
				"<td>(?<note>.+?)\\s*</td>\\s+" +
				"<td class=\"vaM alC\">(?<exchange>.+?)</td>\\s+" +
				"</tr>"
		);
		public static List<StockInfo> getInstance(String page) {
			return ScrapeUtil.getList(StockInfo.class, PAT, page);
		}
		
		public final String symbol;
		public final String nameEN;
		public final String nameJP;
		public final String note;
		public final String exchange;
		
		public StockInfo(String symbol, String nameEN, String nameJP, String note, String exchange) {
			this.symbol   = symbol;
			this.nameEN   = nameEN;
			this.nameJP   = nameJP;
			this.note     = note;
			this.exchange = exchange;
		}
		
		@Override
		public String toString() {
			return String.format("%s %s %s %s %s", symbol, nameEN, nameJP, note, exchange);
		}
	}

	public static class ETFInfo {
		// ETF
		//	<tr>
		//	<th class="vaM alC">QQQ</th>
		//	<td>Invesco QQQ Trust,Series 1 ETF<br>インベスコ QQQ トラスト シリーズ1 ETF</td>
		//	<td class="vaM alC">NASDAQ</td>
		//	</tr>
		//
		//	<tr>
		//	<th class="vaM alC">FINX</th><td>Global X FinTech ETF<br>グローバルX フィンテック ETF</td>
		//	<td class="vaM alC">NASDAQ</td>
		//	</tr>
		//
		//	<tr>
		//	<th class="vaM alC">AIQ</th>
		//	<td>Global X Funds Global X Artificial Intelligence & Technology ETF<br>グローバルX AI＆ビッグデータETF
		//	</td>
		//	<td class="vaM alC">NASDAQ</td>
		//	</tr>    
		//
		//	<tr><th class="vaM alC">XLC</th>
		//	<td>Communication Service Select Sector SPDR Fund<br>コミュニケーション サービス セレクト セクターSPDRファンド</td>
		//	<td class="vaM alC">NYSE Arca</td>
		//	</tr>
		//	<tr>
		
		public static final Pattern PAT = Pattern.compile(
				"<tr>\\s*" +
				"<th class=\"vaM alC\">(?<symbol>.+?)</th>\\s*" +
				"<td>(?<nameEN>.+?)<br>(?<nameJP>.+?)(\\s+)?</td>\\s+" +
				"<td class=\"vaM alC\">(?<exchange>.+?)</td>\\s+" +
				"</tr>"
		);
		public static List<ETFInfo> getInstance(String page) {
			return ScrapeUtil.getList(ETFInfo.class, PAT, page);
		}
		
		public final String symbol;
		public final String nameEN;
		public final String nameJP;
		public final String exchange;
		
		public ETFInfo(String symbol, String nameEN, String nameJP, String exchange) {
			this.symbol   = symbol;
			this.nameEN   = nameEN;
			this.nameJP   = nameJP;
			this.exchange = exchange;
		}
		
		@Override
		public String toString() {
			return String.format("%s %s %s %s", symbol, nameEN, nameJP, exchange);
		}
	}
	
	public static class ADRInfo {
		// ADR
		//	<tr>
		//	<th><div class="thM alC"><p class="fm01">BMA</p></div></th>
		//	<td><div class="tdM"><p class="fm01">Banco Macro SA<br>バンコ マクロ</p></div></td>
		//	<td><div class="tdM"><p class="fm01">アルゼンチンの金融機関</p></div></td>
		//	<td><div class="tdM alC"><p class="fm01">NYSE</p></div></td>
		//	</tr>
		
		//	<tr>
		//	<th><div class="thM alC"><p class="fm01">PKX</p></div></th>
		//	<td><div class="tdM"><p class="fm01">POSCO<BR>ポスコ</p></div></td>
		//	<td><div class="tdM"><p class="fm01">韓国の鉄鋼メーカー</p></div></td>
		//	<td><div class="tdM alC"><p class="fm01">NYSE</p></div></td>
		//	</tr>

		//  <tr>
		//  <th class="vaM alC"><p class="fm01">QFIN</p></th>
		//  <td><p class="fm01">Qifu Technology Inc ADR<br>キフ テクノロジー ADR</p></td>
		//  <td><p class="fm01">サービスが十分に提供されない借り手に合わせて調整されたオンライン消費者金融商品を提供する、デジタル消費者金融プラットフォームを運営。</p></td>
		//  <td class="vaM alC"><p class="fm01">NASDAQ</p></td>
		//  </tr>

		public static final Pattern PAT = Pattern.compile(
				"<tr>\\s*" +
				"<th.+?<p class=\"fm01\">(?<symbol>.+?)</p>.*?</th>\\s+" +
				"<td>.*?<p class=\"fm01\">(?<nameEN>.+?)<(br|BR)>(?<nameJP>.+?)</p>.*?</td>\\s+" +
				"<td>.*?<p class=\"fm01\">(?<note>.+?)\\s*</p>.*?</td>\\s+" +
				"<td.+?<p class=\"fm01\">(?<exchange>.+?)</p>.*?</td>\\s+" +
				"</tr>"
		);
		public static List<ADRInfo> getInstance(String page) {
			return ScrapeUtil.getList(ADRInfo.class, PAT, page);
		}
		
		public final String symbol;
		public final String nameEN;
		public final String nameJP;
		public final String note;
		public final String exchange;
		
		public ADRInfo(String symbol, String nameEN, String nameJP, String note, String exchange) {
			this.symbol   = symbol;
			this.nameEN   = nameEN;
			this.nameJP   = nameJP;
			this.note     = note;
			this.exchange = exchange;
		}
		
		@Override
		public String toString() {
			return String.format("%s %s %s %s %s", symbol, nameEN, nameJP, note, exchange);
		}
	}
	
	
	private static void update() {
		var buyFreeSet = BuyFreeETF.getSet();
		logger.info("buyFree  {}", buyFreeSet.size());
		
		String string;
		{
			File file = new File(Storage.provider_sbi.getPath("pop6040_usequity_list.html"));
			if (DEBUG_USE_FILE && file.exists()) {
				string = FileUtil.read().file(file);
			} else {
				HttpUtil.Result result = HttpUtil.getInstance().withCharset(ENCODING).download(URL);
				if (result == null || result.result == null) {
					logger.error("Unexpected");
					logger.error("  result  {}", result);
					throw new UnexpectedException("Unexpected");
				}
				string = result.result;
				// debug
				if (DEBUG_USE_FILE) logger.info("save  {}  {}", file.getPath(), string.length());
				FileUtil.write().file(file, string);
			}
		}
		
		// remove XML comment
		Pattern PAT_COMMENT = Pattern.compile("<!--.*?-->", Pattern.MULTILINE | Pattern.DOTALL);
		MatcherFunction<String> OP_REMOVE_COMMENT = (m) -> "";
		String page = StringUtil.replace(string, PAT_COMMENT, OP_REMOVE_COMMENT);
//		logger.info("string {}", string.length());
//		logger.info("page   {}", page.length());
		
		List<String> stockCodeList;
		{
			List<String> stockList = StockInfo.getInstance(page).stream().map(o -> o.symbol).toList();
			List<String> adrList   = ADRInfo.getInstance(page).stream().map(o -> o.symbol).toList();
			List<String> etfList   = ETFInfo.getInstance(page).stream().map(o -> o.symbol).toList();
			
			Set<String> set = new HashSet<>();
			set.addAll(stockList);
			set.addAll(adrList);
			set.addAll(etfList);
			
			stockCodeList = set.stream().toList();
			
			logger.info("stock    {}", stockList.size());
			logger.info("adr      {}", adrList.size());
			logger.info("etf      {}", etfList.size());
			logger.info("set      {}", set.size());
		}
		
		var list = new ArrayList<TradingStockInfo>();
		{
			for(var stockCode: stockCodeList) {
				if (stockCode.equals("ティッカー")) continue;
				if (10 <= stockCode.length()) continue;
				
				TradingStockInfo tradingStock = new TradingStockInfo();
				tradingStock.stockCode = stockCode;
				tradingStock.feeType   = buyFreeSet.contains(stockCode) ? TradingStockInfo.FeeType.BUY_FREE : TradingStockInfo.FeeType.PAID;
				tradingStock.tradeType = TradingStockInfo.TradeType.BUY_SELL;
				
				list.add(tradingStock);
			}
		}
		logger.info("list     {}", list.size());
		
		logger.info("save  {}  {}", list.size(), TradingStock.getPath());
		TradingStock.save(list);
	}
	
	public static void main(String[] args) {
		logger.info("START");
		
		update();
				
		logger.info("STOP");
	}
}
