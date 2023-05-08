package yokwe.stock.jp.nikkei;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import yokwe.stock.jp.Storage;
import yokwe.util.FileUtil;
import yokwe.util.http.HttpUtil;

public class DownloadFile {
	private static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();

	public interface DownloadInfo {
		String getName();
		String getURL(String code);
		String getPath(String code);
		boolean isValid(String code, String page);
	}
	
	private static final String URL_BASE = "https://www.nikkei.com/nkd/fund/%s?fcode=%s";
	private static String getURL(String category, String code) {
		return String.format(URL_BASE, category, code);
	}
	private static String getURL(String code) {
		return getURL("", code);
	}
	
	private static String getPath(String dir, String code) {
		return Storage.Nikkei.getPath(dir, code + ".html");
	}
	
	private static class DownloadFundInfo implements DownloadInfo {
		public String getName() {
			return "fund";
		}
		public String getURL(String code) {
			return DownloadFile.getURL(code);
		}
		public String getPath(String code) {
			return DownloadFile.getPath(getName(), code);
		}
		public boolean isValid(String code, String page) {
			// <!-- ▼ QP-COMMON01：共通（コード他） ▼ -->
			if (!page.contains("QP-COMMON01")) {
				logger.warn("{}  {} has no QP-COMMON01", getName(), code);
				return false;
			}
			// <!-- ▼ QP-COMMON02：共通（ファンド名称） ▼ -->
			if (!page.contains("QP-COMMON02")) {
				logger.warn("{}  {} has no QP-COMMON02", getName(), code);
				return false;
			}
			// <!-- ▼ QP-COMMON03：共通（価格） ▼ -->
			if (!page.contains("QP-COMMON03")) {
				logger.warn("{}  {} has no QP-COMMON03", getName(), code);
				return false;
			}
			// <!-- ▼ QP-COMMON04：共通（QUICK-FMスコア） ▼ -->
			if (!page.contains("QP-COMMON03")) {
				logger.warn("{}  {} has no QP-COMMON04", getName(), code);
				return false;
			}
			// <!-- ▼ QP-BASPR：ファンド基本情報 ▼ -->
			if (!page.contains("QP-BASPR")) {
				logger.warn("{}  {} has no QP-BASPR", getName(), code);
				return false;
			}
			// <!-- ▼ QP-FDINFO：ファンド概要 ▼ -->
			if (!page.contains("QP-FDINFO")) {
				logger.warn("{}  {} has no QP-FDINFO", getName(), code);
				return false;
			}
			// <!-- ▼ QP-INVPO：運用方針 ▼ -->
			if (!page.contains("QP-INVPO")) {
				logger.warn("{}  {} has no QP-INVPO", getName(), code);
				return false;
			}
			if (page.contains("<span class=\"m-companyCategory_text  a-baseLinkStyleType02\">-- : --</span>")) {
				logger.warn("{}  {} has no companyCategory_text", getName(), code);
				return false;
			}
			return true;
		}
	}
	public static final DownloadInfo downloadFundInfo = new DownloadFundInfo();
	
	private static class DownloadPerfInfo implements DownloadInfo {
		public String getName() {
			return "perf";
		}
		public String getURL(String code) {
			return DownloadFile.getURL("performance/", code);
		}
		public String getPath(String code) {
			return DownloadFile.getPath(getName(), code);
		}
		public boolean isValid(String code, String page) {
			// <!-- ▼ QP-COMMON01：共通（コード他） ▼ -->
			if (!page.contains("QP-COMMON01")) {
				logger.warn("{}  {} has no QP-COMMON01", getName(), code);
				return false;
			}
			// <!-- ▼ QP-COMMON02：共通（ファンド名称） ▼ -->
			if (!page.contains("QP-COMMON02")) {
				logger.warn("{}  {} has no QP-COMMON02", getName(), code);
				return false;
			}
			// <!-- ▼ QP-COMMON03：共通（価格） ▼ -->
			if (!page.contains("QP-COMMON03")) {
				logger.warn("{}  {} has no QP-COMMON03", getName(), code);
				return false;
			}
			// <!-- ▼ QP-RSRET：リスク・リターン ▼ -->
			if (!page.contains("QP-RSRET")) {
				logger.warn("{}  {} has no QP-RSRET", getName(), code);
				return false;
			}
			// <!-- ▼ QP-SCORE：QUICK投信モニタリングスコア ▼ -->
			if (!page.contains("QP-SCORE")) {
				logger.warn("{}  {} has no QP-SCORE", getName(), code);
				return false;
			}
			return true;
		}
	}
	public static final DownloadInfo downloadPerfInfo = new DownloadPerfInfo();

	private static class DownloadDivInfo implements DownloadInfo {
		public String getName() {
			return "div";
		}
		public String getURL(String code) {
			return DownloadFile.getURL("dividend/", code);
		}
		public String getPath(String code) {
			return DownloadFile.getPath(getName(), code);
		}
		public boolean isValid(String code, String page) {
			// <!-- ▼ QP-COMMON01：共通（コード他） ▼ -->
			if (!page.contains("QP-COMMON01")) {
				logger.warn("{}  {} has no QP-COMMON01", getName(), code);
				return false;
			}
			// <!-- ▼ QP-COMMON02：共通（ファンド名称） ▼ -->
			if (!page.contains("QP-COMMON02")) {
				logger.warn("{}  {} has no QP-COMMON02", getName(), code);
				return false;
			}
			// <!-- ▼ QP-COMMON03：共通（価格） ▼ -->
			if (!page.contains("QP-COMMON03")) {
				logger.warn("{}  {} has no QP-COMMON03", getName(), code);
				return false;
			}
			// <!-- ▼ QP-BUNPAI：分配金実績 ▼ -->
			if (!page.contains("QP-BUNPAI")) {
				logger.warn("{}  {} has no QP-BUNPAI", getName(), code);
				return false;
			}
			//  <!-- ▼ QP-BUNPAISD：分配金健全度 ▼ -->
			if (!page.contains("QP-BUNPAISD")) {
				logger.warn("{}  {} has no QP-BUNPAISD", getName(), code);
				return false;
			}
			// <!-- ▼ QP-YIELD：分配金利回り ▼ -->
			if (!page.contains("QP-YIELD")) {
				logger.warn("{}  {} has no QP-YIELD", getName(), code);
				return false;
			}
			return true;
		}
	}
	public static final DownloadInfo downloadDivInfo = new DownloadDivInfo();

	private static void download(List<String> codeList, DownloadInfo download) {
		logger.info("download {}", download.getName());
		
		List<String> list = new ArrayList<>();
		for(var code: codeList) {
			File file = new File(download.getPath(code));
			if (file.exists()) {
				String page = FileUtil.read().file(file);
				if (!download.isValid(code, page)) {
					// delete bad file
					FileUtil.delete(file);
					// need to download code
					list.add(code);
				}
				continue;
			} else {
				// need to download code
				list.add(code);
			}
		}
		Collections.shuffle(list);
		
		logger.info("{}  download  {} / {}", download.getName(), list.size(), codeList.size());
		List<String> badList = new ArrayList<>();
		
		{
			int count = 0;
			for(var code: list) {
				count++;
				if ((count % 10) == 1) logger.info("{}  {}", download.getName(), String.format("%4d / %4d  %s", count, list.size(), code));
				
				String path = download.getPath(code);
				String url  = download.getURL(code);
				
				HttpUtil.Result result = HttpUtil.getInstance().download(url);
				if (result == null) {
					logger.warn("{}  {}  result is null", download.getName(), code);
					continue;
				}
				if (result.result == null) {
					logger.warn("{}  {}  result.result is null  {}  {}", download.getName(), code, result.code, result.reasonPhrase);
					continue;
				}
				if (download.isValid(code, result.result)) {
					FileUtil.write().file(path, result.result);
				} else {
					badList.add(code);
				}
				
				try {
					Thread.sleep(500);
				} catch (InterruptedException e) {
					logger.warn("Interruptted");
				}
			}
		}
		for(int retry = 0; retry < 5; retry++) {
			if (badList.isEmpty()) break;
			
			logger.info("{}  retry {}   badList {}", download.getName(), retry, badList.size());

			List<String> nextBadList = new ArrayList<>();

			int count = 0;
			for(var code: badList) {
				// if ((count % 10) == 1) logger.info("{}  {}", download.getName(), String.format("%4d / %4d  %s", count, list.size(), code));
				logger.info("{}  {}", download.getName(), String.format("%4d / %4d  %s", count, badList.size(), code));

				String path = download.getPath(code);
				String url  = download.getURL(code);
				
				HttpUtil.Result result = HttpUtil.getInstance().download(url);
				if (result == null) {
					logger.warn("result is null  {}", code);
					continue;
				}
				if (result.result == null) {
					logger.warn("result.result is null  {}  {}  {}", code, result.code, result.reasonPhrase);
					continue;
				}
				if (download.isValid(code, result.result)) {
					FileUtil.write().file(path, result.result);
				} else {
					nextBadList.add(code);
				}
				
				try {
					Thread.sleep(500);
				} catch (InterruptedException e) {
					logger.warn("Interruptted");
				}
			}
			badList = nextBadList;
		}
	}
	
	public static List<String> getCodeListFromToushin() {
		var list = yokwe.stock.jp.toushin.Fund.getList().stream().map(o -> o.fundCode).collect(Collectors.toList());
		Collections.sort(list);
		return list;
	}
	public static List<String> getCodeListFromFund() {
		var list = FileUtil.listFile(Storage.Nikkei.getPath("fund")).stream().filter(o -> o.getName().endsWith(".html")).map(o -> o.getName().replace(".html", "")).collect(Collectors.toList());
		Collections.sort(list);
		return list;
	}
	
	
	public static void main(String[] args) {
		logger.info("START");
		
		var toushinList = getCodeListFromToushin();
		
		logger.info("toushin   {}", toushinList.size());
		logger.info("fund      {}", getCodeListFromFund().size());

		// download using getCodeListFromToushin
		// download(toushinList, downloadFundInfo);
		// update
		download(getCodeListFromFund(), downloadFundInfo);
		
		var fundList = getCodeListFromFund();
		logger.info("fund      {}", fundList.size());

		download(fundList, downloadPerfInfo);
		download(fundList, downloadDivInfo);
		
		logger.info("STOP");
	}
	
}
