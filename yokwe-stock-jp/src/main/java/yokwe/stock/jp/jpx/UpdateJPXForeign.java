package yokwe.stock.jp.jpx;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import yokwe.stock.jp.Storage;
import yokwe.util.FileUtil;
import yokwe.util.ScrapeUtil;
import yokwe.util.ToString;
import yokwe.util.UnexpectedException;
import yokwe.util.http.HttpUtil;

public class UpdateJPXForeign {
	private static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();
	
	// Current
	// https://www.jpx.co.jp/equities/products/foreign/issues/index.html
	
	private static final String URL       = "https://www.jpx.co.jp/equities/products/foreign/issues/index.html";
	private static final String PAGE_FILE = "jpx-foreign-page.html";
	
	private static final boolean DEBUG_USE_FILE = false;
	
/*

<tr>
  
     <td width="22%" class="a-center a-middle ">ビート・ホールディングス・リミテッド</td>
     <td width="10%" class="a-center a-middle "><a href="https://www2.jpx.co.jp/tseHpFront/StockSearch.do?method=topsearch&topSearchStr=9399" target="_blank">9399</a></td>
     <td width="14%" class="a-center a-middle "><a href="https://beatholdings.com/for-investors-info/" target="_blank">日本語サイト</a></td>
     <td width="12%" class="a-center a-middle ">1</td>
     <td width="16%" class="a-center a-middle ">英領ケイマン諸島 </td>
     <td width="10%" class="a-center a-middle ">情報・通信</td>
     <td width="8%" class="a-center a-middle ">12月</td>
     <td width="8%" class="a-center a-middle ">-</td>
</tr>


*/

	public static class ForeignInfo {
		public static final Pattern PAT = Pattern.compile(
				"<tr.*?>\\s+" +
				"<td width=\"22%\" .+?>(?<name>.+?)</td>\\s+" +
				"<td width=\"10%\" .+?><a .+?>(?<stockCode>.+?)</a></td>\\s+" +
				""
		);
		public static List<ForeignInfo> getInstance(String page) {
			return ScrapeUtil.getList(ForeignInfo.class, PAT, page);
		}
		
		public String name;
		public String stockCode;
		
		public ForeignInfo(String name, String stockCode) {
			this.name      = name;
			this.stockCode = stockCode;
		}
		
		@Override
		public String toString() {
			return ToString.withFieldName(this);
		}
	}
	
	private static List<JPXForeign> getList(String url, String pageFile) {
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
		
		List<JPXForeign> list = new ArrayList<>();
		for(var e: ForeignInfo.getInstance(page)) {
			JPXForeign entry = new JPXForeign();
			entry.name      = e.name;
			entry.stockCode = Stock.toStockCode5(e.stockCode);

			list.add(entry);
		}
		
		return list;
	}
	
	public static void main(String[] args) {
		logger.info("START");
		
		List<JPXForeign> list = getList(URL, PAGE_FILE);
		logger.info("list  {}", list.size());
				
		logger.info("save   {}  {}", list.size(), JPXForeign.getPath());
		JPXForeign.save(list);
		
		logger.info("STOP");
	}
}
