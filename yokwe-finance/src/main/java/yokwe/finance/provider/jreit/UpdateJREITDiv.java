package yokwe.finance.provider.jreit;

import java.io.File;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import yokwe.finance.type.DailyValue;
import yokwe.finance.type.StockCodeJP;
import yokwe.util.FileUtil;
import yokwe.util.ScrapeUtil;
import yokwe.util.ScrapeUtil.AsNumber;
import yokwe.util.ToString;
import yokwe.util.UnexpectedException;
import yokwe.util.http.HttpUtil;
import yokwe.util.json.JSON;

public class UpdateJREITDiv {
	private static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();
	
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
	
	// 分配金の推移
	public static class Dividend {
		// 分配金の推移
		//	<tr>
		//	<td>16</td>
		//	<td>2023-11-30</td>
		//	<td></td>
		//	<td>0</td>
		//	<tr>

		public static final Pattern PAT = Pattern.compile(
			"<tr>\\s+" +
			"<td>(?<term>[0-9]+)</td>\\s+" +
			"<td>(?<yyyy>20[0-9][0-9])-(?<mm>[01]?[0-9])-(?<dd>[0123]?[0-9])</td>\\s+" +
			"<td>(?<result>[0-9,]*)</td>\\s+" +
			"<td>(?<estimate>[0-9,]*)</td>\\s+" +
			"</tr>"
		);
		public static List<Dividend> getInstance(String page) {
			return ScrapeUtil.getList(Dividend.class, PAT, page);
		}
		
		public final int    term;
		public final int    yyyy;
		public final int    mm;
		public final int    dd;
		@AsNumber
		public final String result;
		@AsNumber
		public final String estimate;
		
		public Dividend(int term, int yyyy, int mm, int dd, String result, String estimate) {
			this.term     = term;
			this.yyyy     = yyyy;
			this.mm       = mm;
			this.dd       = dd;
			this.result   = result.trim();
			this.estimate = estimate.trim();
		}
		
		public LocalDate getDate() {
			return LocalDate.of(yyyy, mm, dd);
		}
		
		@Override
		public String toString() {
			return ToString.withFieldName(this);
		}
	}

	
	public static class Bunpai {
		//	    "date" : "2023-02-28",
		//	    "estimate" : 3900,
		//	    "result" : null,
		//	    "term" : 42
		public String  date;
		public Integer estimate;
		public Integer result;
		public int     term;
		
		public Bunpai(String date, Integer estimage, Integer result, int term) {
			this.date     = date;
			this.estimate = estimage;
			this.result   = result;
			this.term     = term;
		}
		public Bunpai() {
			this(null, null, null, 0);
		}
				
		@Override
		public String toString() {
			return ToString.withFieldName(this);
		}
	}

	private static void updateInfra(JREITInfoType reit) {
		String stockCode = reit.stockCode;
		
		String url      = String.format("https://www.japan-reit.com/infra/%s/dividend/", StockCodeJP.toStockCode4(stockCode));			
		String charset  = "UTF-8";
		String filePath = StorageJREIT.storage.getPath("page-div", stockCode + ".html");
		
		String page = download(url, charset, filePath, DEBUG_USE_FILE);
		
		var list = new ArrayList<DailyValue>();
		
		for(var e: Dividend.getInstance(page)) {
		    LocalDate  date  = e.getDate();
		    // sanity check
			if (e.result.isEmpty()) continue; // skip if no result
		    
			BigDecimal value = new BigDecimal(e.result);
			
			list.add(new DailyValue(date, value));
		}
		
//		logger.info("save  {}  {}", list.size(), REITDiv.getPath(stockCode));
		StorageJREIT.JREITDiv.save(stockCode, list);
	}
	private static void updateREIT(JREITInfoType reit) {
		String stockCode = reit.stockCode;

		String url      = String.format("https://www.japan-reit.com/meigara/%s/bunpai.json", StockCodeJP.toStockCode4(stockCode));
		String charset  = "UTF-8";
		String filePath = StorageJREIT.storage.getPath("page-div", stockCode + ".json");
		
		String page = download(url, charset, filePath, DEBUG_USE_FILE);
		
		var bunpaiList = JSON.getList(Bunpai.class, page);
		if (bunpaiList == null) {
			logger.warn("failed to getList {}", stockCode);
			return;
		}

		var list = new ArrayList<DailyValue>();

		for(var e: bunpaiList) {
		    LocalDate  date  =LocalDate.parse(e.date);
		    // sanity check
			if (e.result == null) continue; // skip if no result
			
			BigDecimal value = BigDecimal.valueOf(e.result.intValue());
			
			list.add(new DailyValue(date, value));
		}
		
//		logger.info("save  {}  {}", list.size(), REITDiv.getPath(stockCode));
		StorageJREIT.JREITDiv.save(stockCode, list);
	}
	
	
	private static void update() {
		var reitList = StorageJREIT.JREITInfo.getList();
		int count = 0;
		
		for(var reit: reitList) {
			if ((++count % 10) == 1) logger.info("{}  /  {}  {}", count, reitList.size(), reit.stockCode);
			
			if (reit.category.equals(JREITInfoType.CATEGORY_INFRA_FUND)) {
				updateInfra(reit);
			} else {
				updateREIT(reit);
			}
		}
	}
	
	
	public static void main(String[] args) {
		logger.info("START");
		
		update();
				
		logger.info("STOP");
	}
}
