package yokwe.finance.provider.jpx;

import java.io.File;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Pattern;

import yokwe.finance.Storage;
import yokwe.finance.type.StockInfoJP;
import yokwe.util.FileUtil;
import yokwe.util.ScrapeUtil;
import yokwe.util.StringUtil;
import yokwe.util.UnexpectedException;
import yokwe.util.http.HttpUtil;

public class UpdateJPXETF {
	private static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();
	
	// Current
	// https://www.jpx.co.jp/equities/products/etfs/issues/01.html
	// https://www.jpx.co.jp/equities/products/etfs/leveraged-inverse/01.html
	
	// Delist
	// https://www.jpx.co.jp/equities/products/etfs/delisting/index.html
	
	// New
	// https://www.jpx.co.jp/equities/products/etfs/issues/index.html
	
	private static final String URL_A = "https://www.jpx.co.jp/equities/products/etfs/issues/01.html";
	private static final String URL_B = "https://www.jpx.co.jp/equities/products/etfs/leveraged-inverse/01.html";
	
	private static final String PAGE_FILE_A = "jpx-etf-page-A.html";
	private static final String PAGE_FILE_B = "jpx-etf-page-B.html";

	
	private static final boolean DEBUG_USE_FILE = false;
	
/*

	<tr>
	  
    <td class="tb-color001">TOPIX</td>
  
<td class="a-center tb-color001">
  <a href="https://quote.jpx.co.jp/jpx/template/quote.cgi?F=tmp/stock_detail&amp;MKTN=T&amp;QCODE=1305" rel="external">1305</a>
</td>
<td class="tb-color001">
iFreeETF TOPIX（年1回決算型）
    <div>
      <a href="http://tse.factsetdigitalsolutions.com/iopv/table?language=jp" rel="external" class="inav-btn">iNAV</a>
    </div>
    
</td>
<td class="tb-color001">
  <a href="https://www.daiwa-am.co.jp/" rel="external">大和アセットマネジメント(13054)</a>
</td>
<td class="a-right tb-color001 w-space">0.06%</td>
<td class="a-center tb-color001">●</td>
<td class="a-center tb-color001">
  ●
  
</td>
<td class="a-center tb-color001">
  
      <a href="./files/1305-j.pdf" rel="external"><img src="/common/images/icon/tvdivq000000019l-img/icon-pdf.png" alt="PDF" title="PDF" width="16" height="16" /></a>
    
</td>
<td class="a-center tb-color001">
  <a href="https://money-bu-jpx.com/search/1305/?utm_source=jpx.co.jp&utm_medium=referral&utm_campaign=etf-search&utm_content=etftable" rel="external"><img src="/common/images/icon/icon-money-bu.png" alt="銘柄詳細" title="銘柄詳細" width="16" height="16"></a>
      
</td>
</tr>

*/

	public static class ETFInfo {
		public static final Pattern PAT = Pattern.compile(
				"<tr>\\s+" +
				"<td class=\"tb-color00[12]\">(?<indexName>.+?)</td>\\s+" +
				"<td class=\"a-center tb-color00[12]\">\\s+<a .+?>(?<stockCode>.+?)</a>\\s+</td>\\s+" +
				"<td class=\"tb-color00[12]\">\\s*(?<name>.+?)\\s*(?:<div>.+?</div>)?\\s*</td>\\s+" +
				"<td class=\"tb-color00[12]\">.+?</td>\\s+" +
				"<td class=\"a-right tb-color00[12] w-space\">(?<expenseRatio>[0-9]+\\.[0-9]+).+?</td>\\s+" +
				"",
				Pattern.DOTALL
		);
		public static List<ETFInfo> getInstance(String page) {
			return ScrapeUtil.getList(ETFInfo.class, PAT, page);
		}
		
		public String indexName;
		public String stockCode;
		public String name;
		public String expenseRatio;
		
		public ETFInfo(String indexName, String stockCode, String name, String expenseRatio) {
			this.indexName    = indexName;
			this.stockCode    = stockCode;
			this.name         = name;
			this.expenseRatio = expenseRatio;
		}
		
		@Override
		public String toString() {
			return StringUtil.toString(this);
		}
	}
	
	private static List<JPXETF> getList(String url, String pageFile) {
		final String page;
		{
			File file = new File(Storage.Provider.JPX.getPath(pageFile));
			if (DEBUG_USE_FILE && file.exists()) {
				page = FileUtil.read().file(file);
			} else {
				HttpUtil.Result result = HttpUtil.getInstance().download(url);
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
		
		List<JPXETF> list = new ArrayList<>();
		for(var e: ETFInfo.getInstance(page)) {
			JPXETF entry = new JPXETF();
			entry.indexName    = e.indexName.replace("&amp;", "&");
			entry.stockCode    = StockInfoJP.toStockCode5(e.stockCode);
			entry.name         = e.name.replace("&amp;", "&").replace("(注2)", "").replace("(注5)", "").replace("(注6)", "");
			entry.expenseRatio = new BigDecimal(e.expenseRatio).movePointLeft(2);
			
			if (entry.name.contains("注")) {
				logger.warn("{}  {}", entry.stockCode, entry.name);
			}

			list.add(entry);
		}
		
		return list;
	}
	
	public static void main(String[] args) {
		logger.info("START");
		
		List<JPXETF> listA = getList(URL_A, PAGE_FILE_A);
		logger.info("listA  {}", listA.size());
		List<JPXETF> listB = getList(URL_B, PAGE_FILE_B);
		logger.info("listB  {}", listB.size());
		
		Map<String, JPXETF> map = new TreeMap<>();
		for(var e: listA) {
			map.put(e.stockCode, e);
		}
		for(var e: listB) {
			var old = map.put(e.stockCode, e);
			if (old != null) {
				logger.error("Unexpected stockCode");
				logger.error("  new  {}", e);
				logger.error("  old  {}", e);
				throw new UnexpectedException("Unexpected stockCode");
			}
		}
		
		logger.info("save   {}  {}", map.size(), JPXETF.getPath());
		JPXETF.save(map.values());
		
		logger.info("STOP");
	}
}
