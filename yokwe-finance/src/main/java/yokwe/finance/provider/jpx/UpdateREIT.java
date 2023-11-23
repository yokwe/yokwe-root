package yokwe.finance.provider.jpx;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import yokwe.finance.type.StockInfoJPType;
import yokwe.util.FileUtil;
import yokwe.util.ScrapeUtil;
import yokwe.util.StringUtil;
import yokwe.util.UnexpectedException;
import yokwe.util.http.HttpUtil;

public class UpdateREIT {
	private static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();
	
	// Current
	// https://www.jpx.co.jp/equities/products/reits/issues/index.html

	private static final String URL       = "https://www.jpx.co.jp/equities/products/reits/issues/index.html";
	private static final String PAGE_FILE = "reit-page.html";
	
	private static final boolean DEBUG_USE_FILE = false;
	
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
	
	
	public static class REITInfo {
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
	
	private static List<StockNameType> getList(String url, String pageFile) {
		String charset  = "UTF-8";
		String filePath = StorageJPX.storage.getPath(pageFile);
		String page     = download(url, charset, filePath, DEBUG_USE_FILE);
		
		List<StockNameType> list = new ArrayList<>();
		for(var e: REITInfo.getInstance(page)) {
			String stockCode = StockInfoJPType.toStockCode5(e.stockCode);
			String name     = e.name;
			
			if (name.contains("注")) {
				logger.warn("{}  {}", stockCode, name);
			}

			list.add(new StockNameType(stockCode, name));
		}
		
		return list;
	}
	
	public static void main(String[] args) {
		logger.info("START");
		
		var list = getList(URL, PAGE_FILE);
		logger.info("list  {}", list.size());
				
		logger.info("save  {}  {}", list.size(), StorageJPX.REIT.getPath());
		StorageJPX.REIT.save(list);
		
		logger.info("STOP");
	}
}
