package yokwe.finance.provider.jpx;

import java.io.File;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import yokwe.finance.Storage;
import yokwe.finance.type.StockInfoJPType;
import yokwe.util.CSVUtil;
import yokwe.util.FileUtil;
import yokwe.util.ScrapeUtil;
import yokwe.util.StringUtil;
import yokwe.util.UnexpectedException;
import yokwe.util.http.HttpUtil;

public class UpdateStockSplit {
	private static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();
	
	private static final Storage storage = StorageJPX.storage;
	
	private static final String URL_HOST = "https://www.jpx.co.jp";
	
	private static final String URL_PREFIX = URL_HOST + "/markets/equities/rights/";
	private static final String getURL(String name) {
		return URL_PREFIX + name;
	}
	
	private static final String FILE_PREFIX = "split";
	private static File getFile(String name) {
		return storage.getFile(FILE_PREFIX, name);
	}
	
	public static class CSVData {
		public String name;
		public String stockCode;
		public String units;
		public String ratio; // n:m
		public String ratioNumber;
		public String ratioPrice;
		
		@Override
		public String toString() {
			return StringUtil.toString(this);
		}
	}
	
	private static final boolean DEBUG_USE_FILE = false;
	
	private static String download(String url, File file, boolean useFile) {
		final String page;
		{
			if (useFile && file.exists()) {
				page = FileUtil.read().file(file);
			} else {
				HttpUtil.Result result = HttpUtil.getInstance().download(url);
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
	
	// <option value="/markets/equities/rights/index.html" selected="selected">2024年2月</option>
	// <option value="/markets/equities/rights/archives-01.html">2024年1月</option>
	
	public static class OptionValue {
		public static final Pattern PAT = Pattern.compile(
			"<option value=\"/markets/equities/rights/(?<name>.+?)\"" +
			""
		);
		public static List<OptionValue> getInstance(String page) {
			return ScrapeUtil.getList(OptionValue.class, PAT, page);
		}

		public final String name;
		
		public OptionValue(String name) {
			this.name = name;
		}
		
		@Override
		public String toString() {
			return name;
		}
	}
	
	public static class TitleID {
		// <span id="title_skc8fn0000002br4">
		// <span id="title_skc8fn0000002br4">2024年2月 末日割当銘柄(</span>

		public static final Pattern PAT = Pattern.compile(
			"<span id=\"title_(?<id>.+?)\">(?<dateString>.+?)割当銘柄\\(" +
			""
		);
		public static List<TitleID> getInstance(String page) {
			return ScrapeUtil.getList(TitleID.class, PAT, page);
		}

		public final String id;
		public final String dateString;
		
		public TitleID(String id, String dateString) {
			this.id         = id;
			this.dateString = dateString;
		}
		
		@Override
		public String toString() {
			return String.format("{%s  %s}", id, dateString);
		}
	}
	// url: '/markets/equities/rights/skc8fn0000002br4-att/skc8fn0000002bsa.csv',
	public static class CSVURL {
		public static final Pattern PAT = Pattern.compile(
			"url: '/markets/equities/rights/(?<name>.+?csv)'," +
			""
		);
		public static List<CSVURL> getInstance(String page) {
			return ScrapeUtil.getList(CSVURL.class, PAT, page);
		}

		public final String name;
		
		public CSVURL(String name) {
			this.name = name;
		}
		
		@Override
		public String toString() {
			return name;
		}
	}

	public static void update() {
		List<String> nameList = new ArrayList<>();
		{
			var name = "index.html";
			var file = getFile(name);
			var page = download(getURL(name), file, DEBUG_USE_FILE);
//			logger.info("page  {}  {}", page.length(), name);
			
			var list = OptionValue.getInstance(page);
//			logger.info("list  {}", list.size());
			for(var e: list) {
				nameList.add(e.name);
			}
		}
		logger.info("nameList  {}", nameList.size());
		
		// 2023年3月 末日
		// 2023年3月21日
		var datePat  = Pattern.compile("(?<yyyy>[0-9]+)年(?<mm>[0-9]+)月(?<dd>[0-9]+| 末)日");
		var ratioPat = Pattern.compile("(?<before>[0-9]+?):(?<after>[0-9]+?)");
		
		var map = StorageJPX.StockSplit.getMap();
		logger.info("map       {}", map.size());

		var dateSet = new TreeSet<LocalDate>();
		{
			var dateList = map.values().stream().map(o -> o.date).distinct().sorted().collect(Collectors.toList());
			logger.info("dateList  {}  {}  {}", dateList.size(), dateList.get(0), dateList.get(dateList.size() - 1));
			dateSet.addAll(dateList);
		}

		var today = LocalDate.now();
		int countMod = 0;
		for(var name: nameList) {
			var file = getFile(name);
			var page = download(getURL(name), file, DEBUG_USE_FILE);
//			logger.info("page  {}  {}", page.length(), name);
			
			var csvURLList = CSVURL.getInstance(page);
			logger.info("csvURL    {}  {}", csvURLList.size(), name);
			var csvMap = csvURLList.stream().collect(Collectors.toMap(o -> o.name.substring(0, o.name.indexOf("-")), Function.identity()));

			var titleIDList = TitleID.getInstance(page);
			for(var id: titleIDList) {
				LocalDate date;
				{
					var dateString = id.dateString;
					var m = datePat.matcher(dateString);
					if (m.matches()) {
						var yyyy = Integer.valueOf(m.group("yyyy"));
						var mm   = Integer.valueOf(m.group("mm"));
						var dd   = m.group("dd");
						
						if (dd.endsWith("末")) {
							date = LocalDate.parse(String.format("%d-%02d-01", yyyy, mm)).with(TemporalAdjusters.lastDayOfMonth());
						} else {
							date = LocalDate.parse(String.format("%d-%02d-%02d", yyyy, mm, Integer.valueOf(dd)));
						}
					} else {
						logger.error("Unexpected dateString");
						logger.error("  dateString  {}", dateString);
						throw new UnexpectedException("Unexpected dateString");
					}
				}
				if (date.isBefore(today) && dateSet.contains(date)) {
					// already processed
					//logger.info("skip  {}", date);
					continue;
				}

				var csv     = csvMap.get(id.id);				
				var csvURL  = getURL(csv.name);
				var csvFile = getFile(csv.name.replace("/", "-"));
				download(csvURL, csvFile, DEBUG_USE_FILE);				
				var csvDataList = CSVUtil.read(CSVData.class).withHeader(false).file(csvFile);
//				logger.info("csv  {}  {}", date, csvDataList.size());

				for(var csvData: csvDataList) {
//					logger.info("csvData  {}", csvData);
					var m = ratioPat.matcher(csvData.ratio);
					if (m.matches()) {
						var stockCode = StockInfoJPType.toStockCode5(csvData.stockCode);
						var before = Integer.valueOf(m.group("before"));
						var after  = Integer.valueOf(m.group("after"));
						var split = new StockSplitType(date, stockCode, before, after, csvData.name);
						var key = split.getKey();
						if (map.containsKey(key)) {
							var old = map.get(key);
							if (old.equals(split)) {
								// OK
							} else {
								logger.error("Unexpected split");
								logger.error("  old  {}", old);
								logger.error("  new  {}", split);
								throw new UnexpectedException("Unexpected split");
							}
						} else {
							map.put(key, split);
							countMod++;
						}
					} else {
						logger.error("Unexpected ratio");
						logger.error("  csvData  {}", csvData);
						throw new UnexpectedException("Unexpected ratio");
					}
				}
			}
		}
		logger.info("countMod  {}", countMod);
		if (countMod != 0) {
			logger.info("save      {}  {}", map.size(), StorageJPX.StockSplit.getPath());
			StorageJPX.StockSplit.save(map.values());
		}
	}
	
	public static void main(String[] args) {
		logger.info("START");
		
		update();
		
		logger.info("STOP");
	}
}
