package yokwe.stock.jp.toushin;

import java.io.File;
import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.hc.core5.http2.HttpVersionPolicy;
import org.slf4j.LoggerFactory;

import yokwe.util.FileUtil;
import yokwe.util.ScrapeUtil;
import yokwe.util.StringUtil;
import yokwe.util.http.Download;
import yokwe.util.http.DownloadSync;
import yokwe.util.http.FileTask;
import yokwe.util.http.RequesterBuilder;
import yokwe.util.http.Task;
import yokwe.util.json.JSON;

public final class DataFile {
	static final org.slf4j.Logger logger = LoggerFactory.getLogger(DataFile.class);

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
	
	
	public static final class PageFile {
		private static final String DIR = "page";
		private static final String URL = "https://toushin-lib.fwg.ne.jp/FdsWeb/FDST030000?isinCd=%s";
		
		public static String getPath(String isinCode) {
			return DataFile.getPath(String.format("%s/%s", DIR, isinCode));
		}
		public static String getPath() {
			return DataFile.getPath(DIR);
		}
		
		//
		// download
		//
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

		//
		// update
		//
		public static class PageInfo {
			private static final Pattern PAT = Pattern.compile(
				"<h3 .+?>(?<name>[^<+]+)</h3>\\s+<div [^>]+>\\s+愛称：.+?</div>" +
				".+?" +
				"<div .+?>\\s+運用会社名：(?<issuer>.+?)</div>" +
				".+?" +
				"<th>設定日<.+?<td .+?>(?<issueDate>.+?)</td>" +
				".+?" +
				"<th>償還日<.+?<td .+?>(?<redemptionDate>.+?)</td>" +
				".+?" +
				"<th>決算頻度<.+?<td .+?>(?<settlementFrequency>.+?)</td>" +
				".+?" +
				"<th>決算日<.+?<td .+?>(?<settlementDate>.+?)</td>" +
				".+?" +
				"<th>解約手数料<.+?<td .+?>(?<cancelationFee>.+?)</td>" +
				".+?" +
				"運用管理費用<br>（信託報酬）.+?<td .+?>(?<initialFeeLimit>.+?)</td>\\s+<td>(?<redemptionFee>.+?)</td>\\s+<td>(?<trustFee>.+?)</td>" +
				".+?" +
				"<tr>\\s+<th .+?>運用会社</th>\\s+<td>(?<trustFeeOperation>.+?)</td>\\s+</tr>" +
				".+?" +
				"<tr>\\s+<th .+?>販売会社</th>\\s+<td>(?<trustFeeSeller>.+?)</td>\\s+</tr>" +
				".+?" +
				"<tr>\\s+<th .+?>信託銀行</th>\\s+<td>(?<trustFeeBank>.+?)</td>\\s+</tr>" +
				".+?" +
				"<input type=\"hidden\"\\s+id=\"isinCd\"\\s+value=\"(?<isinCode>[0-9A-Z]+)\"" + 
				".+?" +
				"<input type=\"hidden\"\\s+id=\"associFundCd\"\\s+value=\"(?<fundCode>[0-9A-Z]+)\"" + 
				"",
				Pattern.DOTALL
				);
			
			public static PageInfo getInstance(String page) {
				return ScrapeUtil.get(PageInfo.class, PAT, page);
			}
			
			public String name;
			public String issuer;               // 運用会社名
			public String issueDate;            // 設定日
			public String redemptionDate;       // 償還日
			public String settlementFrequency;  // 決算頻度
			public String settlementDate;       // 決算日
			public String cancelationFee;       // 解約手数料
			public String initialFeeLimit;      // 購入時手数料 上限
			public String redemptionFee;        // 信託財産留保額
			public String trustFee;             // 信託報酬 
			public String trustFeeOperation ;   // 信託報酬 運用会社
			public String trustFeeSeller;       // 信託報酬 販売会社
			public String trustFeeBank;         // 信託報酬 信託銀行
			public String isinCode;
			public String fundCode;
			
			public PageInfo(String name, String issuer, String issueDate, String redemptionDate,
					String settlementFrequency, String settlementDate, String cancelationFee, String initialFeeLimit, String redemptionFee,
					String trustFee, String trustFeeOperation, String trustFeeSeller, String trustFeeBank,
					String isinCode, String fundCode) {
				this.name                = name.trim();
				this.issuer              = issuer.trim();
				this.issueDate           = issueDate.trim().replace("/", "-");
				this.redemptionDate      = redemptionDate.trim().replace("/", "-");
				if (this.redemptionDate.equals("無期限")) {
					this.redemptionDate = MutualFund.INDEFINITE;
				}
				
				this.settlementFrequency = settlementFrequency.trim();
				this.settlementDate      = settlementDate.trim();
				this.cancelationFee      = cancelationFee.trim();
				this.initialFeeLimit     = initialFeeLimit.trim();
				this.redemptionFee       = redemptionFee.trim();
				this.trustFee            = trustFee.trim();
				this.trustFeeOperation   = trustFeeOperation.trim();
				this.trustFeeSeller      = trustFeeSeller.trim();
				this.trustFeeBank        = trustFeeBank.trim();
				this.isinCode            = isinCode.trim();
				this.fundCode            = fundCode.trim();
			}

			@Override
			public String toString() {
				return StringUtil.toString(this);
			}
		}
		
		private static final String DOESNOT_EXIST = "該当ファンドは存在しない";
		public static void update() {
			logger.info("update");
			File dir = new File(DataFile.PageFile.getPath());
			if (!dir.exists()) {
				dir.mkdirs();
			}
			
			List<MutualFund> list = new ArrayList<>();
			
			for(var file: dir.listFiles()) {
				if (file.isDirectory()) continue;
			
				String string = FileUtil.read().file(file);
				if (string.contains(DOESNOT_EXIST)) continue;
				
				PageInfo page = PageInfo.getInstance(string);
				MutualFund mutualFund = new MutualFund(
					page.isinCode, page.fundCode,
					0, 0,
					page.name, page.issuer, page.issueDate, page.redemptionDate,
					page.settlementFrequency, page.settlementDate, 
					page.cancelationFee, page.initialFeeLimit, page.redemptionFee,
					page.trustFee, page.trustFeeOperation, page.trustFeeSeller, page.trustFeeBank);

				list.add(mutualFund);
			}
			logger.info("save {} {}", list.size(), MutualFund.getPath());
			MutualFund.save(list);
		}
	
	}
	
	
	public static final class PriceFile {
		private static final String  DIR = "price";
		private static final String  URL = "https://toushin-lib.fwg.ne.jp/FdsWeb/FDST030000/csv-file-download?isinCd=%s&associFundCd=%s";
		private static final Charset DEFAULT_CHARSET = Charset.forName("Shift_JIS");

		public static String getPath(String isinCode) {
			return DataFile.getPath(String.format("%s/%s", DIR, isinCode));
		}
		public static String getPath() {
			return DataFile.getPath(DIR);
		}

		//
		// download
		//
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

		//
		// update
		//
		private static final String  CSV_HEADER  = "年月日,基準価額(円),純資産総額（百万円）,分配金,決算期";
		
		public static void update() {
			logger.info("update");
			
			int count = 0;
			File dir = new File(DataFile.PriceFile.getPath());
			for(var file: dir.listFiles()) {
				if (file.isDirectory()) continue;
				String isinCode = file.getName();
				
				String string = FileUtil.read().file(file);
				
				String[] lines = string.split("[\\r\\n]+");
				if (lines[0].equals(CSV_HEADER)) {
					List<Price> priceList = new ArrayList<>();
					
					for(int i = 1; i < lines.length; i++) {
						String line = lines[i];
						String[] fields = line.split(",", -1);
						if (fields.length != 5) {
							logger.warn("Unexpected field");
							logger.warn("  {} - {}!", fields.length, line);
							System.exit(0); // FIXME
						} else {
							String date          = fields[0]; // 年月日
							String basePrice     = fields[1]; // 基準価額(円) = 純資産総額 / (総口数 * 10,000)
							String netAssetValue = fields[2]; // 純資産総額（百万円）
							String dividend      = fields[3]; // 分配金
							String period        = fields[4]; // 決算期
							
							date = date.replace("年", "-").replace("月", "-").replace("日", "");

							Price fundPrice = new Price(date, basePrice, netAssetValue, dividend, period);
							priceList.add(fundPrice);
						}
					}
					Price.save(isinCode, priceList);
					count++;
				} else {
					logger.warn("Unpexpected header");
					logger.warn("  {}!", lines[0]);
				}
			}
			
			logger.info("count {}", count);
		}
		
	}
	
	
	public static final class SellerFile {
		private static final String DIR = "seller";
		private static final String URL = "https://toushin-lib.fwg.ne.jp/FdsWeb/FDST030000/company-search";
		
		public static String getPath(String isinCode) {
			return DataFile.getPath(String.format("%s/%s", DIR, isinCode));
		}
		public static String getPath() {
			return DataFile.getPath(DIR);
		}

		//
		// download
		//
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

		public static class SellerInfo {
			public String     fdsInstCd;
			public BigDecimal salesFee;
			public String     salesInstDiv;
			public String     kanaName;
			public String     instName;
			public String     associFundCd;
			
			public SellerInfo() {
				fdsInstCd    = null;
				salesFee     = null;
				salesInstDiv = null;
				kanaName     = null;
				instName     = null;
				associFundCd = null;
			}
			
			@Override
			public String toString() {
				return StringUtil.toString(this);
			}
		}

		public static void update() {
			logger.info("update");

			int count = 0;
			File dir = new File(DataFile.SellerFile.getPath());
			for(var file: dir.listFiles()) {
				if (file.isDirectory()) continue;
				String isinCode = file.getName();
				
				String string = FileUtil.read().file(file);
				
				if (string.startsWith("[")) {
					List<Seller> list = new ArrayList<>();
					for(var e: JSON.getList(SellerInfo.class, string)) {
						if (e.salesFee == null) {
							e.salesFee = BigDecimal.ZERO;
						}
						
						list.add(new Seller(isinCode, e.fdsInstCd, e.salesFee, e.instName));
					}
					
					Seller.save(isinCode, list);
					count++;
				} else {
					logger.warn("string is not JSON");
					logger.warn("string - {}", string);
				}
			}
			
			logger.info("count {}", count);
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
		logger.info("jasdec {}", set.size());
		Map<String, String> map = new TreeMap<>();
		
		Context context = new Context(download, set, map);
		
		PageFile.download(context);
		PageFile.update();
		{
			List<MutualFund> list = MutualFund.load();
			if (list != null) {
				for(var e: list) {
					context.map.put(e.isinCode, e.fundCode);
				}
			}
			logger.info("context.map {}", context.map.size());
		}

		PriceFile.download(context);
		SellerFile.download(context);
		
		PriceFile.update();
		SellerFile.update();
		
		logger.info("update countPrice and countSeller");
		List<MutualFund> list = MutualFund.load();
		for(var e: list) {
			List<Price>	priceList = Price.load(e.isinCode);
			List<Seller> sellerList = Seller.load(e.isinCode);
			
			e.countPrice = priceList.size();
			e.countSeller = sellerList.size();
		}
		MutualFund.save(list);
		
		logger.info("STOP");
	}	
}
