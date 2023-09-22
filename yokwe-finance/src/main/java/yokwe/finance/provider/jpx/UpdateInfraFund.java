package yokwe.finance.provider.jpx;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import yokwe.finance.Storage;
import yokwe.finance.type.StockInfoJP;
import yokwe.util.FileUtil;
import yokwe.util.ScrapeUtil;
import yokwe.util.StringUtil;
import yokwe.util.UnexpectedException;
import yokwe.util.http.HttpUtil;

public class UpdateInfraFund {
	private static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();
	
	// Current
	// https://www.jpx.co.jp/equities/products/infrastructure/issues/index.html

	private static final String URL       = "https://www.jpx.co.jp/equities/products/infrastructure/issues/index.html";
	private static final String PAGE_FILE = "infra-fund-page.html";
	
	private static final boolean DEBUG_USE_FILE = false;
	
/*

                  <tr>
                    <td class="a-center w-space 
  tb-color001
" rowspan="2">2021/06/22</td>
                    <td class="a-left 
  tb-color001
"><a href="https://www.tokaido-reit.co.jp/" rel="external">東海道リート投資法人　投資証券</a>
                    </td>
                    <td class="a-center w-space 
  tb-color001
" rowspan="2">
                      <a href="https://quote.jpx.co.jp/jpx/template/quote.cgi?F=tmp/stock_detail&MKTN=T&QCODE=2989" rel="external">2989<br />（JP3049110004）</a></td>
                    <td class="a-center w-space 
  tb-color001
" rowspan="2"><div><p class="component-text">1月末<br/>
7月末</p></div></td>
                    <td class="a-center 
  tb-color001
">-</td>
                    <td class="a-center 
  tb-color001
" rowspan="2">-</td>


*/

	public static class REITInfo {
		public static final Pattern PAT = Pattern.compile(
				"<tr>\\s+" +
				"<td class=\"a-center w-space.+?</td>\\s+" +
				"<td class=\"a-left.+?<a .+?>(?<name>.+?)</a>\\s+</td>\\s+" +
				"<td class=\"a-center.+?<a .+?>(?<stockCode>.+?)<br.+?</a></td>\\s+" +
				"",
				Pattern.DOTALL
		);
		public static List<REITInfo> getInstance(String page) {
			return ScrapeUtil.getList(REITInfo.class, PAT, page);
		}
		
		public String stockCode;
		public String name;
		
		public REITInfo(String stockCode, String name) {
			this.stockCode = stockCode;
			this.name      = name;
		}
		
		@Override
		public String toString() {
			return StringUtil.toString(this);
		}
	}
	
	private static List<InfraFund> getList(String url, String pageFile) {
		final String page;
		{
			File file = new File(Storage.provider_jpx.getPath(pageFile));
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
		
		List<InfraFund> list = new ArrayList<>();
		for(var e: REITInfo.getInstance(page)) {
			InfraFund entry = new InfraFund();
			entry.stockCode = StockInfoJP.toStockCode5(e.stockCode);
			entry.name      = e.name;
			
			if (entry.name.contains("注")) {
				logger.warn("{}  {}", entry.stockCode, entry.name);
			}

			list.add(entry);
		}
		
		return list;
	}
	
	public static void main(String[] args) {
		logger.info("START");
		
		List<InfraFund> list = getList(URL, PAGE_FILE);
		logger.info("list  {}", list.size());
				
		logger.info("save   {}  {}", list.size(), InfraFund.getPath());
		InfraFund.save(list);
		
		logger.info("STOP");
	}
}
