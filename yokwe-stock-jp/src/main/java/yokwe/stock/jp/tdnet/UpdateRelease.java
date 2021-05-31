package yokwe.stock.jp.tdnet;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import yokwe.util.FileUtil;
import yokwe.util.ScrapeUtil;
import yokwe.util.StringUtil;
import yokwe.util.UnexpectedException;
import yokwe.util.http.HttpUtil;

public class UpdateRelease {
	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(UpdateRelease.class);
	
	private static final String URL_BASE = "https://www.release.tdnet.info/inbs";
	private static String getPageURL(LocalDate date, int page) {
		String dateString = String.format("%4d%02d%02d", date.getYear(), date.getMonthValue(), date.getDayOfMonth());
		return String.format("%s/I_list_%03d_%s.html", URL_BASE, page, dateString);
	}
	private static String getDataURL(String filename) {
		return String.format("%s/%s", URL_BASE, filename);
	}
	private static byte[] downloadData(String filename) {
		String url = getDataURL(filename);
		HttpUtil.Result result = HttpUtil.getInstance().withRawData(true).download(url);
		return result.rawData;
	}

	
	// <div id="kaiji-text-1">に開示された情報はありません。</div>
	private static final Pattern KAIJI_TEXT_PAT = Pattern.compile("<div id=\"kaiji-text-1\">(.+?)</div>");
	private static final String KAIJI_TEXT_NO_DATA = "に開示された情報はありません。";

	private static final Pattern NEXT_PAT = Pattern.compile("<div class=\"pager-R\" onClick=\"pager\\('(.*?)'\\)\">");

	public static class ReleaseInfo {
		private static final Pattern PAT = Pattern.compile(
			"<tr.*?>\\s+" +
			"<td class=\"(?:even|odd)new-L kjTime\".+?>(?<time>.+?)</td>\\s+" +
			"<td class=\"(?:even|odd)new-M kjCode\".+?>(?<code>.+?)</td>\\s+" +
			"<td class=\"(?:even|odd)new-M kjName\".+?>(?<name>.+?)\\s*</td>\\s+" +
			"<td class=\"(?:even|odd)new-M kjTitle\".+?><a href=\"(?<pdf>.+?)\" .+?>(?<title>.+?)</a>(?:<BR>.+?)?</td>\\s+" +
			"<td class=\"(?:even|odd)new-M kjXbrl\".+?>(?:<div class=\"xbrl-mask\">.+href=\"(?<xbrl>.+?)\".+?)? </td>\\s+" +
			"<td class=\"(?:even|odd)new-M kjPlace\".+?>(?<place>.+?)\\s*</td>\\s+" +
			"<td class=\"(?:even|odd)new-R kjHistroy\".+?>(?<history>.*?)(?:[　]+?)?</td>\\s+" +
			"</tr>"
			);
		
		public static List<ReleaseInfo> getInstance(String page) {
			return ScrapeUtil.getList(ReleaseInfo.class, PAT, page);
		}
		
		public String time;
		public String code;
		public String name;
		public String pdf;
		public String title;
		public String xbrl;
		public String place;
		public String history;
		
		public ReleaseInfo(String time, String code, String name, String pdf, String title, String xbrl, String place, String history) {
			this.time    = time;
			this.code    = code;
			this.name    = name;
			this.pdf     = pdf;
			this.title   = title;
			this.xbrl    = xbrl;
			this.place   = place;
			this.history = history;
		}

		@Override
		public String toString() {
			return StringUtil.toString(this);
		}
	}

	private static class Page {
		public LocalDate     date;
		public List<Release> entryList;
		
		private Page(LocalDate date) {
			this.date      = date;
			this.entryList = new ArrayList<>();
		}
		@Override
		public String toString() {
			return String.format("{%s %4d}", date, entryList.size());
		}
		
		public static Page getInstance(LocalDate date) {
			Page ret = new Page(date);
			
			for(int page = 1;; page++) {
				String url = getPageURL(date, page);
//				logger.info("url {}", url);
				
				HttpUtil.Result result = HttpUtil.getInstance().download(url);
//				logger.info("result {}", result.response);
				
				if (result == null) {
					// Can be HttpStatus.SC_NOT_FOUND 404 or HttpStatus.SC_BAD_REQUEST 400
					if (page == 1) return null;
					else break;
				}
				
				final String string = result.result;
//				logger.info("string {}", result.result.length());

				{
					String kaijiText = StringUtil.getGroupOne(KAIJI_TEXT_PAT, string);
//						logger.info("kaijiText {}", kaijiText);
					if (kaijiText.equals(KAIJI_TEXT_NO_DATA)) break;
				}

				
				List<ReleaseInfo> list = ReleaseInfo.getInstance(string);
				for(var e: list) {
					LocalDateTime dateTime = LocalDateTime.of(date, LocalTime.parse(e.time));
					Release release = new Release(dateTime, e.code, e.name, e.pdf, e.title, e.xbrl, e.place, e.history);
					ret.entryList.add(release);
				}
				
				{
					String next = StringUtil.getGroupOne(NEXT_PAT, string);
					if (next.isEmpty()) break;
				}
			}
			return ret;
		}
	}
	
	public static void main(String[] args) {
		logger.info("START");
		
		int days = Integer.getInteger("days", 10);
		logger.info("days {}", days);
		
		LocalDate date  = LocalDate.now();
		LocalDate lastDate = date.minusDays(days);
		logger.info("date {} - {}", lastDate, date);

		{
			// fill map with existing release info from tmp/data/releas.csv
			Map<String, Release> map = Release.getMap();
			logger.info("release map {}", map.size());

			int       count = 0;
			
			for(;;) {
				Page page = Page.getInstance(date);
				if (page == null) break;
				
				logger.info("page {}", page);
				for(Release e: page.entryList) {
//					logger.info("  {}", e);
					
//					{
//						String filename = String.format("%s.pdf", e.id);
//						File file = Release.getDataFile(date, filename);
//
//						if (!file.exists()) {
//							logger.info("file {}", file.getPath());
//							byte[] content  = Release.downloadData(e.pdf);
//							FileUtil.rawWrite().file(file, content);
//						}
//					}
					{
						if (!e.xbrl.isEmpty()) {
							String filename = String.format("%s.zip", e.id);
							File file = Release.getReleaseFile(date, filename);

							if (!file.exists()) {
								logger.info("file {}", file.getPath());
								byte[] content  = downloadData(e.xbrl);
								FileUtil.rawWrite().file(file, content);
							}
						}
					}

					map.put(e.id, e);
					count++;
				}
				
				date = date.minusDays(1);
				if (date.equals(lastDate)) break;
			}
			
			{
				logger.info("update count {}", count);
				if (0 < count) {
					List<Release> list = new ArrayList<>(map.values());
					logger.info("save {} {}", list.size(), Release.getPath());
					Release.save(list);
				}
			}
		}
		
		// Save summary XBRL file in zip file
		{
			Map<SummaryFilename, File> map = TDNET.getSummaryFileMap();
			logger.info("tdnet map {}", map.size());
			
			int countSave = 0;
			for(File releaseFile: Release.getReleaseFileList()) {
				if (releaseFile.getName().endsWith(".zip")) {
					try (ZipFile zipFile = new ZipFile(releaseFile)) {
						Enumeration<? extends ZipEntry> entries = zipFile.entries();

					    while(entries.hasMoreElements()){
					        ZipEntry entry = entries.nextElement();
					        SummaryFilename filename = SummaryFilename.getInstance(entry.getName());
					        if (filename == null) continue;
					        
					        if (map.containsKey(filename)) {
					        	// No need to save, because the file is already saved
					        } else {
					        	countSave++;
					        	String path = TDNET.getSummaryFilePath(filename);
					        	logger.info("save {}", path);
					        	//
					        	File file = new File(path);
						        try (InputStream is = zipFile.getInputStream(entry)) {
						        	FileUtil.rawWrite().file(file, is);
						        }
					        }
					    }
					} catch (IOException e) {
						String exceptionName = e.getClass().getSimpleName();
						logger.error("{} {}", exceptionName, e);
						throw new UnexpectedException(exceptionName, e);
					}
				}
			}
			logger.info("save count {}", countSave);
			if (0 < countSave) {
				TDNET.touch();
			}
		}
		
		logger.info("STOP");
	}
}
