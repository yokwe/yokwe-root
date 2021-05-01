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

	private static final String PREFIX = "download";
	
	public static String getPath(String path) {
		return Toushin.getPath(String.format("%s/%s", PREFIX, path));
	}
	
	private static final String USER_AGENT = "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit";
	
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
	
	
	public static final class Page {
		private static final String DIR = "page";
		private static final String URL = "https://toushin-lib.fwg.ne.jp/FdsWeb/FDST030000?isinCd=%s";
		
		public static String getPath(String isinCode) {
			return DownloadFile.getPath(String.format("%s/%s", DIR, isinCode));
		}
		public static String getPath() {
			return DownloadFile.getPath(DIR);
		}
		
		private static void download(Context context) {
			String dir = getPath();
			
			// delete foreign file
			deleteForeignFile(dir, context.set);
			// create list of not existing file
			List<String> list = notExistingList(dir, context.set);
			
			Collections.shuffle(list);
			
			context.download
				.clearHeader()
				.setUserAgent(USER_AGENT);
			
			logger.info("download page {}", list.size());
			for(var isinCode: list) {
				String uriString = String.format(URL, isinCode);
				File   file      = new File(getPath(isinCode));
				Task   task      = FileTask.get(uriString, file);
				context.download.addTask(task);
			}
			
			logger.info("BEFORE RUN");
			context.download.startAndWait();
			logger.info("AFTER  RUN");	
		}
	}
	
	
	public static final class Price {
		private static final String  DIR = "price";
		private static final String  URL = "https://toushin-lib.fwg.ne.jp/FdsWeb/FDST030000/csv-file-download?isinCd=%s&associFundCd=%s";
		private static final Charset DEFAULT_CHARSET = Charset.forName("Shift_JIS");

		public static String getPath(String isinCode) {
			return DownloadFile.getPath(String.format("%s/%s", DIR, isinCode));
		}
		public static String getPath() {
			return DownloadFile.getPath(DIR);
		}

		private static void download(Context context) {
			String dir = getPath();
			
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
			
			context.download
				.clearHeader()
				.setUserAgent(USER_AGENT);
						
			logger.info("download price {}", list.size());
			for(var isinCode: list) {
				String fundCode  = context.map.get(isinCode);
				String uriString = String.format(URL, isinCode, fundCode);
				File   file      = new File(getPath(isinCode));
				Task   task      = FileTask.get(uriString, file, DEFAULT_CHARSET);
				context.download.addTask(task);
			}
			
			logger.info("BEFORE RUN");
			context.download.startAndWait();
			logger.info("AFTER  RUN");
		}
	}
	
	
	public static final class Seller {
		private static final String DIR = "seller";
		private static final String URL = "https://toushin-lib.fwg.ne.jp/FdsWeb/FDST030000/company-search";
		
		public static String getPath(String isinCode) {
			return DownloadFile.getPath(String.format("%s/%s", DIR, isinCode));
		}
		public static String getPath() {
			return DownloadFile.getPath(DIR);
		}

		private static void download(Context context) {
			String dir = getPath();
			
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
			
			context.download
				.clearHeader()
				.setUserAgent(USER_AGENT)
				.addHeader("Accept",           "*/*")
				.addHeader("X-Requested-With", "XMLHttpRequest")
				.addHeader("Origin",           "toushin-lib.fwg.ne.jp")
				.addHeader("Accept-Encoding",  "gzip, deflate")
				.addHeader("Accept-Language",  "ja");

			logger.info("download seller {}", list.size());
			for(var isinCode: list) {
				String uriString = URL;
				String content   = String.format("isinCd=%s", isinCode);
				String path      = getPath(isinCode);
				Task   task      = FileTask.post(uriString, new File(path), content);
				context.download.addTask(task);
			}
			
			logger.info("BEFORE RUN");
			context.download.startAndWait();
			logger.info("AFTER  RUN");
		}
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
		
		Page.download(context);
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

		Price.download(context);
		Seller.download(context);
		
		logger.info("STOP");
	}

}
