package yokwe.stock.jp.jpx;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import yokwe.stock.jp.Storage;
import yokwe.util.FileUtil;
import yokwe.util.ScrapeUtil;
import yokwe.util.StringUtil;
import yokwe.util.UnexpectedException;
import yokwe.util.http.HttpUtil;

public class UpdateJPXPreferred {
	private static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();
	
	// Current
	// https://www.jpx.co.jp/equities/products/preferred-stocks/issues/index.html
	
	private static final String URL       = "https://www.jpx.co.jp/equities/products/preferred-stocks/issues/index.html";
	private static final String PAGE_FILE = "jpx-preferred-page.html";
	
	private static final boolean DEBUG_USE_FILE = false;
	
/*

<tr class="end">
     <td class="a-center a-middle ">2007/09/03</td>
     <td class="a-center a-middle ">25935</td>
     <td class="a-left a-middle ">（株）伊藤園<br/>
第1種優先株式</td>
     <td class="a-center a-middle ">プライム</td>
     <td class="a-center a-middle "><a href="/equities/products/preferred-stocks/issues/tvdivq0000007usm-att/25935g.pdf" rel="external"><img src="/common/images/icon/tvdivq000000019l-img/icon-pdf.png" alt="PDF" title="PDF" width="16" height="16" /></a></td>
     <td class="a-center a-middle "><a href="/equities/products/preferred-stocks/issues/tvdivq0000007usm-att/25935y.pdf" rel="external"><img src="/common/images/icon/tvdivq000000019l-img/icon-pdf.png" alt="PDF" title="PDF" width="16" height="16" /></a></td>
     <td class="a-center a-middle ">-</td>
     <td class="a-center a-middle ">-</td>
     <td class="a-center a-middle ">-</td>
     <td class="a-center a-middle ">100</td>
</tr>

*/

	public static class ForeignInfo {
		public static final Pattern PAT = Pattern.compile(
				"<tr.*?>\\s+" +
				"<td class=\"a-center a-middle.+?</td>\\s+" +
				"<td class=\"a-center a-middle.+?>(?<stockCode>.+?)</td>\\s+" +
				"<td class=.+?>\\s*(?<nameA>.+?)\\s*<br/>\\s*(?<nameB>.+?)\\s*</td>\\s+" +
				""
		);
		public static List<ForeignInfo> getInstance(String page) {
			return ScrapeUtil.getList(ForeignInfo.class, PAT, page);
		}
		
		public String stockCode;
		public String nameA;
		public String nameB;
		
		public ForeignInfo(String stockCode, String nameA, String nameB) {
			this.stockCode = stockCode;
			this.nameA     = nameA;
			this.nameB     = nameB;
		}
		
		@Override
		public String toString() {
			return StringUtil.toString(this);
		}
	}
	
	private static List<JPXPreferred> getList(String url, String pageFile) {
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
		
		List<JPXPreferred> list = new ArrayList<>();
		for(var e: ForeignInfo.getInstance(page)) {
			JPXPreferred entry = new JPXPreferred();
			entry.stockCode = Stock.toStockCode5(e.stockCode);
			entry.name      = e.nameA + " " + e.nameB;

			list.add(entry);
		}
		
		return list;
	}
	
	public static void main(String[] args) {
		logger.info("START");
		
		List<JPXPreferred> list = getList(URL, PAGE_FILE);
		logger.info("list  {}", list.size());
				
		logger.info("save   {}  {}", list.size(), JPXPreferred.getPath());
		JPXPreferred.save(list);
		
		logger.info("STOP");
	}

}
