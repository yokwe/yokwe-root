package yokwe.stock.jp.toushin;

import java.io.File;
import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.hc.core5.http2.HttpVersionPolicy;
import org.slf4j.LoggerFactory;

import yokwe.util.FileUtil;
import yokwe.util.ScrapeUtil;
import yokwe.util.StringUtil;
import yokwe.util.UnexpectedException;
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
	
	private static final BigDecimal MINUS_ONE = BigDecimal.ONE.negate();
	
	private static void deleteForeignFile(String path, Set<String> set) {
		File dir = new File(path);
		dir.mkdirs();
		
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
		dir.mkdirs();
		
		List<String> ret = new ArrayList<>();
		for(var e: set) {
			File file = new File(dir, e);
			if (file.exists()) continue;
			ret.add(e);
		}
		return ret;
	}
	private static BigDecimal normalize(BigDecimal value) {
		if (value.compareTo(BigDecimal.ZERO) == 0) {
			return BigDecimal.ZERO;
		}
		return value;
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
		private static void download(Download download, Set<String> isinCodeSet) {
			String dir = getPath();
			
			// delete foreign file
			deleteForeignFile(dir, isinCodeSet);
			// create list of not existing file
			List<String> list = notExistingList(dir, isinCodeSet);
			
			Collections.shuffle(list);
			
			download
				.clearHeader()
				.setUserAgent(USER_AGENT);
			
			logger.info("download page {}", list.size());
			for(var isinCode: list) {
				String uriString = String.format(URL, isinCode);
				File   file      = new File(getPath(isinCode));
				Task   task      = FileTask.get(uriString, file);
				download.addTask(task);
			}
			
			logger.info("BEFORE RUN");
			download.startAndWait();
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
				"<div .+?>\\s+商品分類.+?</div>\\s+<div .+?>\\s+" +
					"<span.+?>(?<cat1>.+?)</span>\\s+" +
					"<span.+?</span>\\s+" +
					"<span.+?>(?<cat2>.+?)</span>\\s+" +
					"<span.+?</span>\\s+" +
					"<span.+?>(?<cat3>.+?)</span>\\s+" +
					"</div>" +
				".+?" +
				"<th>インデックス型<.+?<td .+?>(?<cat4>.+?)</td>" +
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
			public String cat1;					// 追加型
			public String cat2;					// 海外
			public String cat3;					// 株式
			public String cat4;					// インデックス型
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
			
			public PageInfo(String name, String issuer,
					String cat1, String cat2, String cat3, String cat4,
					String issueDate, String redemptionDate,
					String settlementFrequency, String settlementDate, String cancelationFee, String initialFeeLimit, String redemptionFee,
					String trustFee, String trustFeeOperation, String trustFeeSeller, String trustFeeBank,
					String isinCode, String fundCode) {
				this.name                = name.trim();
				this.issuer              = issuer.trim();
				
				this.cat1				 = cat1.trim();
				this.cat2				 = cat2.trim();
				this.cat3				 = cat3.trim();
				this.cat4				 = cat4.trim();
				
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
		
		private static Pattern PAT_PERCENT_NUMBER = Pattern.compile("(?<number>[0-9]\\.[0-9]{5})%");
		private static BigDecimal getPercent(String string) {
			Matcher m = PAT_PERCENT_NUMBER.matcher(string);
			if (m.matches()) {
				String percentString = m.group(1);
				return normalize(new BigDecimal(percentString).movePointLeft(2));
			} else {
				logger.error("Unpexpected percent number");
				logger.error("  {}!", string);
				throw new UnexpectedException("Unpexpected percent number");
			}
		}
		private static Pattern PAT_YEARLY_COUNT = Pattern.compile("年(?<number>[0-9]{1,2})回");
		private static int getYearlyCount(String string) {
			if (string.equals("日々")) return 365;
			
			Matcher m = PAT_YEARLY_COUNT.matcher(string);
			if (m.matches()) {
				String countString = m.group(1);
				return Integer.parseInt(countString);
			} else {
				logger.error("Unpexpected yearly count number");
				logger.error("  {}!", string);
				throw new UnexpectedException("Unpexpected yearly count number");
			}
		}

		
		public static List<MutualFund> update() {
			logger.info("update page");
			File dir = new File(DataFile.PageFile.getPath());
			dir.mkdirs();
			
			List<MutualFund> ret = new ArrayList<>();
			
			for(var file: dir.listFiles()) {
				if (file.isDirectory()) continue;
			
				String string = FileUtil.read().file(file);
				if (string.contains(DOESNOT_EXIST)) continue;
				
				PageInfo page = PageInfo.getInstance(string);
				
				BigDecimal trustFee          = getPercent(page.trustFee);
				BigDecimal trustFeeOperation = getPercent(page.trustFeeOperation);
				BigDecimal trustFeeSeller    = getPercent(page.trustFeeSeller);
				BigDecimal trustFeeBank      = getPercent(page.trustFeeBank);
				
				int settlementFrequency = getYearlyCount(page.settlementFrequency);
				String settlementDate = page.settlementDate.equals("日々") ? "EVERYDAY" : page.settlementDate;
				
				MutualFund mutualFund = new MutualFund(
					page.isinCode, page.fundCode,
					0, 0, 0,
					BigDecimal.ZERO, BigDecimal.ZERO,
					page.cat1, page.cat2, page.cat3, page.cat4,
					page.name, page.issuer, page.issueDate, page.redemptionDate,
					settlementFrequency, settlementDate, 
					page.cancelationFee, page.redemptionFee,
					trustFee, trustFeeOperation, trustFeeSeller, trustFeeBank);

				ret.add(mutualFund);
			}
			logger.info("count {}", ret.size());
			return ret;
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
		private static void download(Download download, Set<String> set, Map<String, String> map) {
			String dir = getPath();
			
			// delete foreign file
			deleteForeignFile(dir, set);
			// create list of not existing file
			List<String> list = notExistingList(dir, set);
			
			Collections.shuffle(list);
			
			download
				.clearHeader()
				.setUserAgent(USER_AGENT);
						
			logger.info("download price {}", list.size());
			for(var isinCode: list) {
				String fundCode  = map.get(isinCode);
				String uriString = String.format(URL, isinCode, fundCode);
				File   file      = new File(getPath(isinCode));
				Task   task      = FileTask.get(uriString, file, DEFAULT_CHARSET);
				download.addTask(task);
			}
			
			logger.info("BEFORE RUN");
			download.startAndWait();
			logger.info("AFTER  RUN");
		}

		//
		// update
		//
		private static final String  CSV_HEADER  = "年月日,基準価額(円),純資産総額（百万円）,分配金,決算期";
		
		public static void update(Map<String, MutualFund> fundMap) {
			logger.info("update price");
			
			int count = 0;
			File dir = new File(DataFile.PriceFile.getPath());
			for(var file: dir.listFiles()) {
				if (file.isDirectory()) continue;
				String isinCode = file.getName();
				if (!fundMap.containsKey(isinCode)) {
					logger.error("Unexpected isinCode");
					logger.error("  {}!", isinCode);
					throw new UnexpectedException("Unexpected isinCode");
				}
				MutualFund fund = fundMap.get(isinCode);
				
				String string = FileUtil.read().file(file);
				
				String[] lines = string.split("[\\r\\n]+");
				if (lines[0].equals(CSV_HEADER)) {
					List<Price>    priceList = new ArrayList<>();
					List<Dividend> divList   = new ArrayList<>();
					
					for(int i = 1; i < lines.length; i++) {
						String line = lines[i];
						String[] fields = line.split(",", -1);
						if (fields.length != 5) {
							logger.warn("Unexpected field");
							logger.warn("  {} - {}!", fields.length, line);
							throw new UnexpectedException("Unexpected field");
						} else {
							String dateString          = fields[0]; // 年月日
							String basePriceString     = fields[1]; // 基準価額(円) = 純資産総額 / (総口数 * 10,000) 1万口あたり
							String netAssetValueString = fields[2]; // 純資産総額（百万円）
							String dividendString      = fields[3]; // 分配金 1万口あたり
//							String periodString        = fields[4]; // 決算期
							
							dateString = dateString.replace("年", "-").replace("月", "-").replace("日", "");
							LocalDate date = LocalDate.parse(dateString);
							
							BigDecimal basePrice     = new BigDecimal(basePriceString).movePointLeft(4);
							BigDecimal netAssetValue = new BigDecimal(netAssetValueString).movePointRight(6);
							BigDecimal totalUnits    = netAssetValue.divideToIntegralValue(basePrice);

							priceList.add(new Price(date, isinCode, basePrice, netAssetValue, totalUnits));
							
							if (!dividendString.isEmpty()) {
								BigDecimal dividend = new BigDecimal(dividendString).movePointLeft(4);
								divList.add(new Dividend(date, isinCode, normalize(dividend)));
							}
						}
					}
					Price.save(isinCode, priceList);
					Dividend.save(isinCode, divList);
					fund.countPrice    = priceList.size();
					fund.countDividend = divList.size();
					
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
		private static void download(Download download, Set<String> set) {
			String dir = getPath();
			
			// delete foreign file
			deleteForeignFile(dir, set);
			// create list of not existing file
			List<String> list = notExistingList(dir, set);
					
			Collections.shuffle(list);
			
			download
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
				download.addTask(task);
			}
			
			logger.info("BEFORE RUN");
			download.startAndWait();
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

		public static void update(Map<String, MutualFund> fundMap) {
			logger.info("update seller");

			int count = 0;
			File dir = new File(DataFile.SellerFile.getPath());
			for(var file: dir.listFiles()) {
				if (file.isDirectory()) continue;
				String isinCode = file.getName();
				if (!fundMap.containsKey(isinCode)) {
					logger.error("Unexpected isinCode");
					logger.error("  {}!", isinCode);
					throw new UnexpectedException("Unexpected isinCode");
				}
				MutualFund fund = fundMap.get(isinCode);
				
				String string = FileUtil.read().file(file);
				
				if (string.startsWith("[")) {
					BigDecimal initialFeeMin = null;
					BigDecimal initialFeeMax = null;
					
					List<Seller> sellerList = new ArrayList<>();
					for(var e: JSON.getList(SellerInfo.class, string)) {
						if (e.salesFee == null) {
							e.salesFee = MINUS_ONE;
						} else {
							e.salesFee = e.salesFee.movePointLeft(2);
						}
						if (initialFeeMin == null) {
							initialFeeMin = e.salesFee;
						} else {
							initialFeeMin = initialFeeMin.min(e.salesFee);
						}
						if (initialFeeMax == null) {
							initialFeeMax = e.salesFee;
						} else {
							initialFeeMax = initialFeeMax.min(e.salesFee);
						}
						
						sellerList.add(new Seller(isinCode, e.salesFee, e.fdsInstCd, e.instName));
					}
					
					Seller.save(isinCode, sellerList);
					fund.countSeller   = sellerList.size();
					fund.initialFeeMin = initialFeeMin == null ? MINUS_ONE : normalize(initialFeeMin);
					fund.initialFeeMax = initialFeeMax == null ? MINUS_ONE : normalize(initialFeeMax);
					
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
		Set<String> isinCodeSet = new TreeSet<>(yokwe.stock.jp.jasdec.Fund.load().stream().map(o -> o.isinCode).collect(Collectors.toList()));
		logger.info("jasdec {}", isinCodeSet.size());
		
		PageFile.download(download, isinCodeSet);
		List<MutualFund> fundList = PageFile.update();
		
		// build set and map
		Set<String>             set     = new TreeSet<>();
		Map<String, String>     map     = new TreeMap<>();
		Map<String, MutualFund> fundMap = new TreeMap<>();
		
		for(var e: fundList) {
			set.add(e.isinCode);
			map.put(e.isinCode, e.fundCode);
			fundMap.put(e.isinCode, e);
		}
		

		PriceFile.download(download, set, map);
		SellerFile.download(download, set);
		
		PriceFile.update(fundMap);
		SellerFile.update(fundMap);
		
		{
			List<MutualFund> list = fundMap.values().stream().collect(Collectors.toList());
			MutualFund.save(list);
			logger.info("mutual-fund {} {}", list.size(), MutualFund.getPath());
		}
		
		logger.info("STOP");
	}	
}
