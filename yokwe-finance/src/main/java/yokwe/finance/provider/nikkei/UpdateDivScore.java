package yokwe.finance.provider.nikkei;

import java.io.File;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.Map;
import java.util.regex.Pattern;

import yokwe.finance.fund.StorageFund;
import yokwe.util.FileUtil;
import yokwe.util.ScrapeUtil;
import yokwe.util.StringUtil;
import yokwe.util.http.HttpUtil;

public class UpdateDivScore {
	private static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();
	
	private static final boolean DEBUG_USE_FILE = true;
	
	private static final String  CHARSET = "UTF-8";
	
	private static final long    SLEEP_IN_MILLI = 1000;
	
		
	private static String download(String url, String charset, String filePath, boolean useFile) {
		final String page;
		{
			File file = new File(filePath);
			if (useFile && file.exists()) {
				page = FileUtil.read().file(file);
			} else {
				HttpUtil.Result result = HttpUtil.getInstance().withCharset(charset).download(url);
				if (result == null) {
//					logger.warn("Unexpected  result is null  {}", url);
					return null;
				}
				if (result.result == null) {
					logger.warn("Unexpected  result.result is null  {}  {}  {}", result.code, result.reasonPhrase, result.url);
					return null;
				}
				page = result.result;
				// debug
//				if (DEBUG_USE_FILE) logger.info("save  {}  {}", page.length(), file.getPath());
				FileUtil.write().file(file, page);
			}
		}
		return page;
	}
	
	private static String getURL(String fundCode) {
		return String.format("https://www.nikkei.com/nkd/fund/dividend/?fcode=%s", fundCode);
	}
	private static String getFilePath(String fundCode) {
		return StorageNikkei.getPath("page", fundCode + ".html");
	}
	
	
	public static class DivScoreInfo {
		/*
		//<!-- ▼ QP-BUNPAISD：分配金健全度 ▼ -->
		//<div class="m-articleFrame a-w100p">
		//		    <div class="m-headline">
		//		        <h2 class="m-headline_text">分配金健全度<a href="//www.nikkei.com/help/contents/markets/fund/#qf13" target="_blank" class="m-iconQ">（解説）</a></h2>
		//		    </div>
		//		    <div class="m-tableType01 a-mb40">
		//		        <div class="m-tableType01_table">
		//		            <table class="w668">
		//		                <thead>
		//		                <tr>
		//		                    <th class="a-taC a-w25p">1年</th>
		//		                    <th class="a-taC a-w25p">3年</th>
		//		                    <th class="a-taC a-w25p">5年</th>
		//		                    <th class="a-taC a-w25p">10年</th>
		//		                </tr>
		//		                </thead>
		//		                <tbody>
		//		                <tr>
		//		                    <td class="a-taR">0.00%</td>
		//		                    <td class="a-taR">100.00%</td>
		//		                    <td class="a-taR">100.00%</td>
		//		                    <td class="a-taR">100.00%</td>
		//		                </tr>
		//		                </tbody>
		//		            </table>
		//		        </div>
		//		    </div>
		//</div>
		//<!-- ▲ QP-BUNPAISD：分配金健全度 ▲ -->
		*/
				
		public static final String HEADER = "<!-- ▼ QP-BUNPAISD：分配金健全度 ▼ -->";
		public static final Pattern PAT = Pattern.compile(
			HEADER + "\\s+" +
			"<div .+?>\\s+" +
			"<div .+?>\\s+" +
			"<h2 .+?>分配金健全度.+?</h2>\\s+" +
			"</div>\\s+" +
			"<div .+?>\\s+" +
			"<div .+?>\\s+" +
			"<table .+?>\\s+" +
			"<thead>\\s+" +
			"<tr>\\s+" +
			"<th .+?>1年</th>\\s+" +
			"<th .+?>3年</th>\\s+" +
			"<th .+?>5年</th>\\s+" +
			"<th .+?>10年</th>\\s+" +
			"</tr>\\s+" +
			"</thead>\\s+" +
			"<tbody>\\s+" +
			"<tr>\\s+" +
			"<td .+?>(?<score1Y>.+?)</td>\\s+" +
			"<td .+?>(?<score3Y>.+?)</td>\\s+" +
			"<td .+?>(?<score5Y>.+?)</td>\\s+" +
			"<td .+?>(?<score10Y>.+?)</td>\\s+" +
			"</tr>\\s+" +
			"</tbody>\\s+" +
			"</table>\\s+" +
			"</div>\\s+" +
			"</div>\\s+" +
			"</div>\\s+" +

			""
		);
		public static DivScoreInfo getInstance(String page) {
			return ScrapeUtil.get(DivScoreInfo.class, PAT, page);
		}

		public String score1Y;
		public String score3Y;
		public String score5Y;
		public String score10Y;

		public DivScoreInfo(
			String score1Y,
			String score3Y,
			String score5Y,
			String score10Y
		) {
			this.score1Y = score1Y;
			this.score3Y = score3Y;
			this.score5Y = score5Y;
			this.score10Y = score10Y;
		}
		@Override
		public String toString() {
			return StringUtil.toString(this);
		}
	}
	
	private static BigDecimal fromPercentString(String string) {
		String numericString = string.trim().replace("%", "");
		return numericString.compareTo("--") == 0 ? DivScoreType.NO_VALUE :  new BigDecimal(numericString).movePointLeft(2);
	}

	
	private static int updateMap(Map<String, DivScoreType> map) {
		logger.info("map     {}", map.size());

		var fundInfoList = StorageFund.FundInfo.getList();
		{
			logger.info("fund    {}", fundInfoList.size());
			// remove already processed item
			fundInfoList.removeIf(o -> map.containsKey(o.isinCode));
			logger.info("fund    {}", fundInfoList.size());
		}
		
		int count  = 0;
		int countA = 0;
		int countB = 0;
		int countC = 0;
		Collections.shuffle(fundInfoList);
		for(var fundInfo: fundInfoList) {
			var isinCode = fundInfo.isinCode;
			var fundCode = fundInfo.fundCode;
			
			if ((++count % 100) == 1) logger.info("{}  /  {}", count, fundInfoList.size());
			
			try {
				Thread.sleep(SLEEP_IN_MILLI);
			} catch (InterruptedException e) {
				//
			}
			
			String page;
			{
				var url      = getURL(fundCode);
				var filePath = getFilePath(fundCode);
				page = download(url, CHARSET, filePath, DEBUG_USE_FILE);
			}
			if (page == null) {
//				logger.info("skip  {}  page is null", isinCode);
				countA++;
				continue;
			}
			var divScoreInfo = DivScoreInfo.getInstance(page);
			if (divScoreInfo == null) {
				logger.info("skip  {}  divScoreInfo is null", isinCode);
				countB++;
				continue;
			}
			
			BigDecimal score1Y  = fromPercentString(divScoreInfo.score1Y);
			BigDecimal score3Y  = fromPercentString(divScoreInfo.score3Y);
			BigDecimal score5Y  = fromPercentString(divScoreInfo.score5Y);
			BigDecimal score10Y = fromPercentString(divScoreInfo.score10Y);
			
			map.put(isinCode, new DivScoreType(isinCode, score1Y, score3Y, score5Y, score10Y));
			countC++;
			
			if ((countC % 10) == 1) StorageNikkei.DivScore.save(map.values());
		}
		
		StorageNikkei.DivScore.save(map.values());

		logger.info("countA  {}", countA);
		logger.info("countB  {}", countB);
		logger.info("countC  {}", countC);
		return countC;
	}
	
	private static void update() {
		var map = StorageNikkei.DivScore.getMap();
		for(int countUpdate = 1; countUpdate < 10; countUpdate++) {
			logger.info("update  {}", countUpdate);
			logger.info("------");
			int countMod = updateMap(map);
			if (countMod == 0) break;
		}
	}
	
	
	public static void main(String[] args) {
		logger.info("START");
		
		update();
				
		logger.info("STOP");
	}
}
