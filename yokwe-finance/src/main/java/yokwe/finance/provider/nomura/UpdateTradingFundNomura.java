package yokwe.finance.provider.nomura;

import java.io.File;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import yokwe.finance.fund.StorageFund;
import yokwe.finance.type.TradingFundType;
import yokwe.util.FileUtil;
import yokwe.util.ScrapeUtil;
import yokwe.util.StringUtil;
import yokwe.util.UnexpectedException;
import yokwe.util.http.HttpUtil;

public class UpdateTradingFundNomura {
	private static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();
	
	private static final boolean DEBUG_USE_FILE  = false;
	
	private static final String CHARSET     = "UTF-8";
	
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
				page = result.result.replace('\r', '\n');
				// debug
				if (DEBUG_USE_FILE) logger.info("save  {}  {}", page.length(), file.getPath());
				FileUtil.write().file(file, page);
			}
		}
		return page;
	}
	
	private static final int MAXDISP = 50;
	private static String downloadPage_ALL(int pageNo) {
		String url      = String.format("https://advance.quote.nomura.co.jp/meigara/nomura2/qsearch.exe?F=users/nomura/list2&MAXDISP=%d&GO_BEFORE=&BEFORE=%d", MAXDISP, MAXDISP * (pageNo - 1));
		String filePath = StorageNomura.storage.getPath("page", "all-" + pageNo + ".html");
		return download(url, CHARSET, filePath, DEBUG_USE_FILE);
	}
	private static String downloadPage_NOLOAD(int pageNo) {
		String url      = String.format("https://advance.quote.nomura.co.jp/meigara/nomura2/qsearch.exe?F=users/nomura/list2&MAXDISP=%d&KEY37=1&GO_BEFORE=&BEFORE=%d", MAXDISP, MAXDISP * (pageNo - 1));
		String filePath = StorageNomura.storage.getPath("page", "noload-" + pageNo + ".html");
		return download(url, CHARSET, filePath, DEBUG_USE_FILE);
	}
	
	// <a href="./qsearch.exe?F=users/nomura/detail2&KEY1=03311187" class="link -forward">ｅＭＡＸＩＳ Ｓｌｉｍ 米国株式（Ｓ＆Ｐ５００）</a>
	public static class FundLink {
		public static final Pattern PAT = Pattern.compile(
				"<a href=\"\\./qsearch\\.exe\\?F=users/nomura/detail2&KEY1=(?<fundCode>.{8})\" class=.+?>(?<fundName>.+?)</a>"
		);
		public static List<FundLink> getInstance(String page) {
			return ScrapeUtil.getList(FundLink.class, PAT, page);
		}
		
		public String fundCode;
		public String fundName;
		
		public FundLink(String fundCode, String fundName) {
			this.fundCode = fundCode;
			this.fundName = fundName;
		}
		
		@Override
		public String toString() {
			return StringUtil.toString(this);
		}
	}

	private static final Pattern PAT = Pattern.compile("showHit\\(\"(?<value>[0-9]+)\"\\);");
	private static int getHitCount(String page) {
		var m = PAT.matcher(page);
		if (m.find()) {
			return Integer.valueOf(m.group("value"));
		} else {
			throw new UnexpectedException("Unexpected");
		}
	}
	
	static final Map<String, String> fundCodeMap = StorageFund.FundInfo.getList().stream().collect(Collectors.toMap(o -> o.fundCode, o -> o.isinCode));
	//               fundCode isinCode
	
	private static void updateSet(Set<String> set, String page) {
		//                            isinCode
		var list = FundLink.getInstance(page);
		for(var e: list) {
			if (fundCodeMap.containsKey(e.fundCode)) {
				set.add(fundCodeMap.get(e.fundCode));
			} else {
				logger.error("bogus fundCode  {}  {}", e.fundCode, e.fundName);
			}
		}
	}
	
	private static void update() {
		// map fundCode to isinCode
		
		var noloadSet = new HashSet<String>();
		// isinCode
		{
			int hitCount;
			{
				var page = downloadPage_NOLOAD(1);
				updateSet(noloadSet, page);
				hitCount = getHitCount(page);
			}
			int maxPageNo = (hitCount / MAXDISP) + 1; // inclusive
			
			for(int pageNo = 2; pageNo <= maxPageNo; pageNo++) {
				var page = downloadPage_NOLOAD(pageNo);
				updateSet(noloadSet, page);
			}
		}
		logger.info("noloadSet  {}", noloadSet.size());
		
		var allSet = new HashSet<String>();
		//  isinCode
		{
			int hitCount;
			{
				var page = downloadPage_ALL(1);
				updateSet(allSet, page);
				hitCount = getHitCount(page);
			}
			int maxPageNo = (hitCount / MAXDISP) + 1; // inclusive
			
			for(int pageNo = 2; pageNo <= maxPageNo; pageNo++) {
				var page = downloadPage_ALL(pageNo);
				updateSet(allSet, page);
			}
		}
		logger.info("allSet     {}", allSet.size());

		var list = new ArrayList<TradingFundType>();
		{
			for(var isinCode: allSet) {
				list.add(new TradingFundType(isinCode, noloadSet.contains(isinCode) ? BigDecimal.ZERO : TradingFundType.SALES_FEE_UNKNOWN));
			}
		}
		logger.info("save  {}  {}", list.size(), StorageNomura.TradingFundNomura.getPath());
		StorageNomura.TradingFundNomura.save(list);
	}
	
	public static void main(String[] args) {
		logger.info("START");
		
		update();
				
		logger.info("STOP");
	}
}
