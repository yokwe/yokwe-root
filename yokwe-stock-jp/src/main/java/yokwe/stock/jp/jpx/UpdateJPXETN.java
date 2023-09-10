package yokwe.stock.jp.jpx;

import java.io.File;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Pattern;

import yokwe.stock.jp.Storage;
import yokwe.util.FileUtil;
import yokwe.util.ScrapeUtil;
import yokwe.util.StringUtil;
import yokwe.util.UnexpectedException;
import yokwe.util.http.HttpUtil;

public class UpdateJPXETN {
	private static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();
	
	// Current
	// https://www.jpx.co.jp/equities/products/etns/issues/01.html
	// https://www.jpx.co.jp/equities/products/etns/leveraged-inverse/01.html

	private static final String URL_A = "https://www.jpx.co.jp/equities/products/etns/issues/01.html";
	private static final String URL_B = "https://www.jpx.co.jp/equities/products/etns/leveraged-inverse/01.html";
	
	private static final String PAGE_FILE_A = "jpx-etn-page-A.html";
	private static final String PAGE_FILE_B = "jpx-etn-page-B.html";

	private static final boolean DEBUG_USE_FILE = false;
	
/*

<tr>
  <td class="a-center tb-color001 w-space">2013/10/28</td>
  <td class="tb-color001">東証マザーズ指数</td>
  <td class="a-center tb-color001">
    <a href="https://quote.jpx.co.jp/jpx/template/quote.cgi?F=tmp/stock_detail&amp;MKTN=T&amp;QCODE=2042" rel="external">2042</a>
  </td>
  <td class="tb-color001">
NEXT NOTES 東証マザーズ ETN
      <div>
        <a href="http://tse.factsetdigitalsolutions.com/iopv/table?language=jp" rel="external" class="inav-btn">iNAV</a>
      </div>
      
  </td>
  <td class="tb-color001">
    <a href="http://nextnotes.com/" rel="external">ノムラ・ヨーロッパ・ファイナンス・エヌ・ブイ(20314)</a>
  </td>
  <td class="a-center tb-color001 w-space">2033/08/08</td>
  <td class="a-right tb-color001 w-space">0.50%</td>
  <td class="a-center tb-color001">
    
        <a href="./files/2042-j.pdf" rel="external"><img src="/common/images/icon/tvdivq000000019l-img/icon-pdf.png" alt="PDF" title="PDF" width="16" height="16" /></a>
      
  </td>
  <td class="a-center tb-color001">
    <a href="https://money-bu-jpx.com/search/2042/?utm_source=jpx.co.jp&utm_medium=referral&utm_campaign=etf-search&utm_content=etftable" rel="external"><img src="/common/images/icon/icon-money-bu.png" alt="銘柄詳細" title="銘柄詳細" width="16" height="16"></a>
        
  </td>
</tr>

*/

	public static class ETFInfo {
		public static final Pattern PAT = Pattern.compile(
				"<tr>\\s+" +
				"<td .+?</td>\\s+" +
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
	
	private static List<JPXETN> getList(String url, String pageFile) {
		final String page;
		{
			File file = new File(Storage.JPX.getPath(pageFile));
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
		
		List<JPXETN> list = new ArrayList<>();
		for(var e: ETFInfo.getInstance(page)) {
			JPXETN entry = new JPXETN();
			entry.indexName    = e.indexName.replace("&amp;", "&");
			entry.stockCode    = Stock.toStockCode5(e.stockCode);
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
		
		List<JPXETN> listA = getList(URL_A, PAGE_FILE_A);
		logger.info("listA  {}", listA.size());
		List<JPXETN> listB = getList(URL_B, PAGE_FILE_B);
		logger.info("listB  {}", listB.size());
		
		Map<String, JPXETN> map = new TreeMap<>();
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
		
		logger.info("save   {}  {}", map.size(), JPXETN.getPath());
		JPXETN.save(map.values());
		
		logger.info("STOP");
	}
}
