package yokwe.finance.provider.monex;

import java.io.File;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import yokwe.finance.Storage;
import yokwe.finance.fund.FundInfo;
import yokwe.finance.type.TradingFundType;
import yokwe.util.FileUtil;
import yokwe.util.ScrapeUtil;
import yokwe.util.UnexpectedException;
import yokwe.util.http.HttpUtil;

public class UpdateTradingFundMonex {
	private static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();
	
	// NOTE Manex use obsolete fundCode and altered fund name, So some fund cannot find in master fund list.
	
	private static final boolean DEBUG_USE_FILE = false;
	
	private static final String CHARSET = "UTF-8";
	
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
	
	
	private static String getURL(int pageNo) {
		return "https://fund.monex.co.jp/search?page=" + pageNo + "&pagesize=100";
	}
	private static String getPath(int pageNo) {
		return Storage.provider_monex.getPath("page", pageNo + ".html");
	}
	
	
	public static class FundCodeInfo {
		// <td class="fund-name left"><a href="./detail/7531103B">トヨタ自動車／トヨタグループ株式ファンド<br/>
		public static final Pattern PAT = Pattern.compile(
				"<td class=\"fund-name left\"><a href=\"\\./detail/(?<fundCode>.+?)\">(?<fundName>.+?)<br/>" +
				""
		);
		public static List<FundCodeInfo> getInstance(String page) {
			return ScrapeUtil.getList(FundCodeInfo.class, PAT, page);
		}
		
		public final String fundCode;
		public final String fundName;
		
		public FundCodeInfo(String fundCode, String fundName) {
			this.fundCode = fundCode;
			this.fundName = fundName;
		}
		
		@Override
		public String toString() {
			return String.format("{%s  %s}", fundCode, fundName);
		}
	}
	
	private static void update() {
		var list = new ArrayList<TradingFundType>();
		{
			var fundCodeMap = FundInfo.getList().stream().collect(Collectors.toMap(o -> o.fundCode, o -> o.isinCode));
			Map<String, String> fundNameMap;
			{
				var fundInfoList = FundInfo.getList();
				
				var dupSet = new HashSet<String>();
				var nameSet = new HashSet<String>();
				for(var fundInfo: fundInfoList) {
					var name = fundInfo.name.replace("　", "");
					if (nameSet.contains(name)) dupSet.add(name);
					nameSet.add(name);
				}
				nameSet.removeIf(o -> dupSet.contains(o));
				fundNameMap = fundInfoList.stream().filter(o -> !dupSet.contains(o.name)).collect(Collectors.toMap(o -> o.name.replace("　", ""), o -> o.isinCode));
				logger.info("fundList     {}", fundInfoList.size());
				logger.info("dupSet       {}", dupSet.size());
				logger.info("nameSet      {}", nameSet.size());
				logger.info("fundNameMap  {}", fundNameMap.size());
			}
			
			int countA = 0;
			int countB = 0;
			int countSkip = 0;
			for(int i = 1; i < 99; i++) {
				logger.info("page  {}", i);
				
				String url  = getURL(i);
				String path = getPath(i);
				
				final String page = download(url, CHARSET, path, DEBUG_USE_FILE);
				
				var fundCodeInfoList = FundCodeInfo.getInstance(page);
				if (fundCodeInfoList.isEmpty()) break;
				
				for(var fundCodeInfo: fundCodeInfoList) {
					var fundCode = fundCodeInfo.fundCode;
					var fundName = fundCodeInfo.fundName.replace("　", "");
					
					if (fundCodeMap.containsKey(fundCode)) {
						var isinCode = fundCodeMap.get(fundCode);
						list.add(new TradingFundType(isinCode, BigDecimal.ZERO));
						countA++;
					} else if (fundNameMap.containsKey(fundName)) {
						var isinCode = fundNameMap.get(fundName);
						list.add(new TradingFundType(isinCode, BigDecimal.ZERO));
						countB++;
					} else {
						logger.error("Unexpected fundCode");
						logger.error("  fundCode  {}  {}", fundCodeInfo.fundCode, fundCodeInfo.fundName);
						countSkip++;
					}
				}
			}
			
			logger.info("countA  {}", countA);
			logger.info("countB  {}", countB);
			logger.info("skiep   {}", countSkip);
		}
		
		logger.info("save  {}  {}", list.size(), TradingFundMonex.getPath());
		TradingFundMonex.save(list);
	}
	
	
	public static void main(String[] args) {
		logger.info("START");
		
		update();
				
		logger.info("STOP");
	}
}
