package yokwe.finance.provider.nikko;

import java.io.File;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import yokwe.finance.Storage;
import yokwe.finance.fund.StorageFund;
import yokwe.finance.provider.nikko.FundPage.FundInfo;
import yokwe.util.FileUtil;
import yokwe.util.ScrapeUtil;
import yokwe.util.UnexpectedException;
import yokwe.util.http.HttpUtil;

public class UpdateFundInfoNikko {
	private static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();
	
	private static final Storage storage = Storage.provider.nikko;
	
	private static final File PAGE_DIR = storage.getFile("fund");
	
	private static final String URL_FORMAT = "https://fund2.smbcnikko.co.jp/smbc_nikko_fund/qsearch.exe?F=list_kokunai&GO_BEFORE=&BEFORE=%d&MAXDISP=%d";
		
	private static final int MAXDISP = 50;
	private static final Charset CHARSET = Charset.forName("Shift_JIS");

	private static String getURL(int pageNo) {
		var before = pageNo * MAXDISP;
		return String.format(URL_FORMAT, before, MAXDISP);
	}
	private static File getFile(int pageNo) {
		var child = String.format("%03d.html", pageNo);
		return new File(PAGE_DIR, child);
	}
	
	// <input type="hidden" name="HIT_COUNT" id="HIT_COUNT" value="1056" />
	public static class HitCountInfo {
		public static final Pattern PAT = Pattern.compile(
			"<input type=\"hidden\" name=\"HIT_COUNT\" id=\"HIT_COUNT\" value=\"(?<hitCount>.+?)\" />"
		);
		public static HitCountInfo getInstance(String page) {
			return ScrapeUtil.get(HitCountInfo.class, PAT, page);
		}
		
		public final int hitCount;
		
		public HitCountInfo(int hitCount) {
			this.hitCount = hitCount;
		}
		
		@Override
		public String toString() {
			return String.format("%d", hitCount);
		}
	}
	
	private static final boolean DEBUG_USE_FILE = false;
	private static String download(String url, Charset charset, File file, boolean useFile) {
		final String page;
		{
			if (useFile && file.exists()) {
				page = FileUtil.read().file(file);
			} else {
				HttpUtil.Result result = HttpUtil.getInstance().withCharset(charset).download(url);
				if (result == null || result.result == null) {
					logger.error("Unexpected");
					logger.error("  result  {}", result);
					throw new UnexpectedException("Unexpected");
				}
				page = result.result.replace("\r","\n").replace("shift_jis", "utf-8"); // cr -> nl  shift_jis -> utf-8
				
				// debug
				if (DEBUG_USE_FILE) logger.info("save  {}  {}", page.length(), file.getPath());
				FileUtil.write().file(file, page);
			}
		}
		return page;
	}
	
	private static void deleteAllFiles(File dir) {
		File[] files = dir.listFiles();
		if (files != null) {
			for(var file: files) {
				if (file.isFile()) file.delete();
			}
		}
	}
	
	private static void download() {
		deleteAllFiles(PAGE_DIR);
		
		int maxPageNo = 999;
		for(int pageNo = 0; pageNo <= maxPageNo; pageNo++) {
			var url  = getURL(pageNo);
			var file = getFile(pageNo);
			
			var page = download(url, CHARSET, file, DEBUG_USE_FILE);
			FileUtil.write().file(file, page);
			logger.info("page  {}  /  {}   {}", pageNo, maxPageNo, page.length());
			
			if (pageNo == 0) {
				var hitCountInfo = HitCountInfo.getInstance(page);
				maxPageNo = hitCountInfo.hitCount / MAXDISP;
				logger.info("hitCount   {}", hitCountInfo.hitCount);
				logger.info("maxPageNo  {}", maxPageNo);
			}
			
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				//
			}
		}
	}
	
	private static void update() {
		var fundCodeMap = StorageFund.FundInfo.getList().stream().collect(Collectors.toMap(o -> o.fundCode, o -> o.isinCode));

		File[] files = PAGE_DIR.listFiles();
		Arrays.sort(files);
		var fileLast = files[files.length - 1];
		
		var list = new ArrayList<FundInfoNikko>();
		
		for(var file: files) {
			var page = FileUtil.read().file(file);
			page = page.replaceAll("<!--[\\s\\S]*?-->", "");
			
			var fundInfoList = FundInfo.getInstance(page);
			logger.info("fundInfoList  {}  {}", fundInfoList.size(), file.getName());
			
			int countFund = 0;
			for (var fundInfo: fundInfoList) {
//				logger.info("fundInfo  {}", fundInfo);
				
				var isinCode   = fundCodeMap.get(fundInfo.fundCode);
				if (isinCode == null) {
					// In case fund is redemption, it disappeared from fundList
					logger.warn("Unpexpected fundCode  {}", fundInfo);
					continue;
				}
				int prospectus = fundInfo.prospectus ? 1 : 0;
				int sougou     = fundInfo.sougou     ? 1 : 0;
				int direct     = fundInfo.direct     ? 1 : 0;
				var name       = fundInfo.fundName;
				
				list.add(new FundInfoNikko(isinCode, prospectus, sougou, direct, name));
				
				countFund++;
			}
//			logger.info("countFund  {}", countFund);
			
			if (countFund != MAXDISP && !file.equals(fileLast)) {
				// somethign goes wrong
				logger.warn("Uexpected countFund  {}", countFund);
			}
		}
		
		logger.info("save  {}  {}", list.size(), StorageNikko.FundInfoNikko.getPath());
		StorageNikko.FundInfoNikko.save(list);
	}
	
	public static void main(String[] args) {
		logger.info("START");
		
		download();
		update();
		
		logger.info("STOP");
	}
}
