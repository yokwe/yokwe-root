package yokwe.stock.jp.toushin;

import java.io.File;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.apache.hc.core5.http2.HttpVersionPolicy;
import org.slf4j.LoggerFactory;

import yokwe.util.http.Download;
import yokwe.util.http.DownloadSync;
import yokwe.util.http.FileTask;
import yokwe.util.http.RequesterBuilder;
import yokwe.util.http.Task;

public final class DownloadFile {
	static final org.slf4j.Logger logger = LoggerFactory.getLogger(DownloadFile.class);

	public static final String PREFIX = "download";
	
	public static final String DIR_PAGE   = "page";
	public static final String DIR_PRICE  = "price";
	public static final String DIR_SELLER = "seller";
	
	public static final String getPathPage(String isinCode) {
		return Toushin.getPath(String.format("%s/%s/%s", PREFIX, DIR_PAGE, isinCode));
	}
	public static final String getPathPrice(String isinCode) {
		return Toushin.getPath(String.format("%s/%s/%s", PREFIX, DIR_PRICE, isinCode));
	}
	public static final String getPathSeller(String isinCode) {
		return Toushin.getPath(String.format("%s/%s/%s", PREFIX, DIR_SELLER, isinCode));
	}
	
	private static final String URL_PAGE = "https://toushin-lib.fwg.ne.jp/FdsWeb/FDST030000?isinCd=%s";
	
	private static final String USER_AGENT = "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit";
	
	private static final String URL_PRICE = "https://toushin-lib.fwg.ne.jp/FdsWeb/FDST030000/csv-file-download?isinCd=%s&associFundCd=%s";
	
	private static class Context {
		public Download            download;
		public Set<String>         set; // isinCode list
		public Map<String, String> map; // map isinCode -> fundCode
		
		public Context(Download download, Set<String> set, Map<String, String> map) {
			this.download = download;
			this.set      = set;
			this.map      = map;
		}
	}

	private static void deleteForeignFile(String path, Set<String> set) {
		File dir = new File(path);
		if (!dir.exists()) {
			dir.mkdirs();
		}
		for(var e: dir.listFiles()) {
			if (e.isDirectory()) continue;
			if (e.length() == 0) {
				e.delete();
				continue;
			}
			if (set.contains(e.getName())) continue;
			
			e.delete();
		}
	}
	private static List<String> notExistingList(String path, Set<String> set) {
		File dir = new File(path);
		if (!dir.exists()) {
			dir.mkdirs();
		}
		
		List<String> ret = new ArrayList<>();
		for(var e: set) {
			File file = new File(dir, e);
			if (file.exists()) continue;
			ret.add(e);
		}
		return ret;
	}
	
	private static void downloadPage(Context context) {
		String dir = getPathPage("");
		
		// delete foreign file
		deleteForeignFile(dir, context.set);
		// create list of not existing file
		List<String> list = notExistingList(dir, context.set);
		
		Collections.shuffle(list);
		
		context.download.clearHeader();
		context.download.setUserAgent(USER_AGENT);
		
		logger.info("download page {}", list.size());
		for(var isinCode: list) {
			String uriString = String.format(URL_PAGE, isinCode);
			File   file      = new File(getPathPage(isinCode));
			Task   task      = FileTask.text(uriString, file);
			context.download.addTask(task);
		}
		
		logger.info("BEFORE RUN");
		context.download.startAndWait();
		logger.info("AFTER  RUN");
	}
	
	private static void downloadPrice(Context context) {
		String dir = getPathPrice("");
		
		// delete foreign file
		deleteForeignFile(dir, context.map.keySet());
		// create list of not existing file
		List<String> list = new ArrayList<>();
		{
			for(var e: notExistingList(dir, context.set)) {
				if (context.map.containsKey(e)) {
					list.add(e);
				}
			}
		}
		
		Collections.shuffle(list);
		
		context.download.clearHeader();
		context.download.setUserAgent(USER_AGENT);
		
		Charset defaultCharset = Charset.forName("Shift_JIS");
		
		logger.info("download price {}", list.size());
		for(var e: list) {
			String fundCode  = context.map.get(e);
			String uriString = String.format(URL_PRICE, e, fundCode);
			File   file      = new File(getPathPrice(e));
			Task   task      = FileTask.text(uriString, file, defaultCharset);
			context.download.addTask(task);
		}
		
		logger.info("BEFORE RUN");
		context.download.startAndWait();
		logger.info("AFTER  RUN");
	}
	
	private static void downloadSeller(Context context) {
		String dir = getPathSeller("");
		
		// delete foreign file
		deleteForeignFile(dir, context.set);
		// create list of not existing file
		List<String> list = new ArrayList<>();
		{
			for(var e: notExistingList(dir, context.set)) {
				if (context.map.containsKey(e)) {
					list.add(e);
				}
			}
		}
				
		Collections.shuffle(list);
		
		context.download.clearHeader();
		context.download.setUserAgent(USER_AGENT);
		context.download.addHeader("Accept",           "*/*");
		context.download.addHeader("X-Requested-With", "XMLHttpRequest");
		context.download.addHeader("Origin",           "toushin-lib.fwg.ne.jp");
		context.download.addHeader("Accept-Encoding",  "gzip, deflate");
		context.download.addHeader("Accept-Language",  "ja");

		logger.info("download seller {}", list.size());
		for(var isinCode: list) {
			String uriString = "https://toushin-lib.fwg.ne.jp/FdsWeb/FDST030000/company-search";
			String content   = String.format("isinCd=%s", isinCode);
			String path      = getPathSeller(isinCode);
			Task   task      = FileTask.text(uriString, new File(path), content);
			context.download.addTask(task);
		}
		
		logger.info("BEFORE RUN");
		context.download.startAndWait();
		logger.info("AFTER  RUN");
	}
	
	public static void main(String[] args) {
		logger.info("START");
		
		Download download = new DownloadSync();
		{
			int threadCount = 5; // 50 is too high for this site
			int maxPerRoute = 50;
			int maxTotal    = 100;
			int soTimeout   = 30;
			logger.info("threadCount {}", threadCount);
			logger.info("maxPerRoute {}", maxPerRoute);
			logger.info("maxTotal    {}", maxTotal);
			logger.info("soTimeout   {}", soTimeout);
			
			RequesterBuilder requesterBuilder = RequesterBuilder.custom()
					.setVersionPolicy(HttpVersionPolicy.NEGOTIATE)
					.setSoTimeout(soTimeout)
					.setMaxTotal(maxTotal)
					.setDefaultMaxPerRoute(maxPerRoute);

			download.setRequesterBuilder(requesterBuilder);
			
			// Configure thread count
			download.setThreadCount(threadCount);
		}
		Set<String> set = new TreeSet<>(yokwe.stock.jp.jasdec.Fund.load().stream().map(o -> o.isinCode).collect(Collectors.toList()));
		logger.info("set {}", set.size());
		Map<String, String> map = new TreeMap<>();
		
		Context context = new Context(download, set, map);
		
		downloadPage(context);
		UpdatePage.update();
		{
			List<MutualFund> list = MutualFund.load();
			if (list != null) {
				for(var e: list) {
					context.map.put(e.isinCode, e.fundCode);
				}
			}
			logger.info("context.map {}", context.map.size());
		}

		downloadPrice(context);
		downloadSeller(context);
		
		logger.info("STOP");
	}

}
