package yokwe.finance.provider.smtb;

import java.io.File;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import yokwe.finance.fund.StorageFund;
import yokwe.finance.provider.smtb.WebPage.PageInfo;
import yokwe.finance.type.FundInfoJP;
import yokwe.finance.type.TradingFundType;
import yokwe.util.FileUtil;
import yokwe.util.UnexpectedException;
import yokwe.util.http.HttpUtil;

public class UpdateTradingFundSMTB {
	private static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();
	
	private static final boolean DEBUG_USE_FILE  = false;
	
	private static final int MAXDISP = 100;
	private static String getURL(int pageNo) { // pageNo starts from zero
		// https://fund.smtb.jp/smtbhp/qsearch.exe?F=openlist&type=result&MAXDISP=100&TAB=t&BEFORE=0&GO_BEFORE=
		// https://fund.smtb.jp/smtbhp/qsearch.exe?F=openlist&type=result&MAXDISP=100&TAB=t&BEFORE=100&GO_BEFORE=
		int before = MAXDISP * pageNo;
		return String.format("https://fund.smtb.jp/smtbhp/qsearch.exe?F=openlist&type=result&MAXDISP=%d&TAB=t&BEFORE=%d&GO_BEFORE=", MAXDISP, before);
	}
	private static String getPath(int pageNo) {
		var fileName = String.format("fund-smtb-%d.html", pageNo);
		return StorageSMTB.storage.getPath(fileName);
	}
	
	private static String download(String url, String filePath, boolean useFile) {
		final String page;
		{
			File file = new File(filePath);
			if (useFile && file.exists()) {
				page = FileUtil.read().file(file);
			} else {
				HttpUtil.Result result = HttpUtil.getInstance().download(url);
				if (result == null || result.result == null) {
					logger.error("Unexpected");
					logger.error("  result  {}", result);
					throw new UnexpectedException("Unexpected");
				}
				page = result.result.replace("\r\n", "\n").replace("\r", "\n");
				// debug
				if (DEBUG_USE_FILE) logger.info("save  {}  {}", page.length(), file.getPath());
				FileUtil.write().file(file, page);
			}
		}
		return page;
	}
	
	private static final Map<Character, String> NORMALIZE_CHAR_MAP;
	static {
		NORMALIZE_CHAR_MAP = new HashMap<>();
		NORMALIZE_CHAR_MAP.put(Character.valueOf('-'),   "ー");
		NORMALIZE_CHAR_MAP.put(Character.valueOf(' '),   "");
		NORMALIZE_CHAR_MAP.put(Character.valueOf('　'),  "");
		
		{
			char h = 'A';
			char f = 'Ａ';
			for(int i = 0; i < 26; i++) {
				NORMALIZE_CHAR_MAP.put(Character.valueOf(h++), Character.toString(f++));
			}
		}
		{
			char h = '0';
			char f = '０';
			for(int i = 0; i < 10; i++) {
				NORMALIZE_CHAR_MAP.put(Character.valueOf(h++), Character.toString(f++));
			}
		}
	}
	
	private static String normalizeString(String string) {
		int length = string.length();
		var ret = new StringBuilder(length);
		for(int i = 0; i < length; i++) {
			char c = string.charAt(i);
			var nomalized = NORMALIZE_CHAR_MAP.get(c);
			if (nomalized == null) {
				ret.append(c);
			} else {
				ret.append(nomalized);
			}
		}
		return ret.toString();
	}
	private static List<FundInfoJP>fundInfoList;
	static {
		fundInfoList = StorageFund.FundInfo.getList();
		for(var e: fundInfoList) {
			e.name = normalizeString(e.name);
		}
	}
	private static String getISINCode(String fundCode, String fundName) {
		var name = normalizeString(fundName);
		
		for(var fundInfo: fundInfoList) {
			if (fundInfo.fundCode.equals(fundCode)) return fundInfo.isinCode;
			if (fundInfo.name.equals(name)) return fundInfo.isinCode;
		}
		logger.error("Unexpected fundCode fundName");
		logger.error("  fundCode  {}", fundCode);
		logger.error("  fundName  {}", fundName);
		throw new UnexpectedException("Unexpected fundCode fundName");
	}
	
	private static void processPage(String page, List<TradingFundType> list) {
//		logger.info("page  {}", page.length());
		var fundList = WebPage.FundInfo.getInstance(page);
		logger.info("fundList   {}", fundList.size());
		
		int skipCount = 0;
		for(var e: fundList) {
			var fundCode = e.fundCode;
			var fundName = e.fundName;
			if (e.initialFee.isEmpty()) {
//				logger.info("skip  {}  {}", fundCode, fundName);
				skipCount++;
				continue;
			}
			var salesFee = new BigDecimal(e.initialFee);
			
//			logger.info("fund  {}  {}", fundCode, fundName);
			
			var isinCode = getISINCode(fundCode, fundName);
			list.add(new TradingFundType(isinCode, salesFee));
		}
		logger.info("skipCount  {}", skipCount);
	}
	
	private static String downloadPage(int pageNo) {
		String path = getPath(pageNo);
		String url  = getURL(pageNo);
		return download(url, path, DEBUG_USE_FILE);
	}
	private static void update() {
		var list = new ArrayList<TradingFundType>();
		
		int pageNoMax;
		{
			logger.info("pageNo  {}", 0);
			String page = downloadPage(0);
			processPage(page, list);
						
			var pageInfo = PageInfo.getInstance(page);
			logger.info("pageInfo  {}", pageInfo);
			
			pageNoMax = pageInfo.hitcount / pageInfo.maxdisp;
			logger.info("pageNoMax  {}", pageNoMax);
		}
		for(int pageNo = 1; pageNo <= pageNoMax; pageNo++) {
			logger.info("pageNo  {}", pageNo);
			String page = downloadPage(pageNo);
			processPage(page, list);
		}
		
		logger.info("save  {}  {}", list.size(), StorageSMTB.TradingFundSMTB.getPath());
		StorageSMTB.TradingFundSMTB.save(list);
	}
	public static void main(String[] args) {
		logger.info("START");
		
		update();
				
		logger.info("STOP");
	}
}
