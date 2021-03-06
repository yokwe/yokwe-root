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
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.hc.core5.http2.HttpVersionPolicy;

import yokwe.stock.jp.toushin.Fund.Offer;
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
	static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(DataFile.class);

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
				"<h3 .+?>(?<name>[^<+]+)</h3>\\s+<div [^>]+>\\s+?????????.+?</div>" +
				".+?" +
				"<div .+?>\\s+??????????????????(?<issuer>.+?)</div>" +
				".+?" +
				"<div .+?>\\s+????????????.+?</div>\\s+<div .+?>\\s+" +
					"<span.+?>(?<cat1>.+?)</span>\\s+" +
					"<span.+?</span>\\s+" +
					"<span.+?>(?<cat2>.+?)</span>\\s+" +
					"<span.+?</span>\\s+" +
					"<span.+?>(?<cat3>.+?)</span>\\s+" +
					"</div>" +
				".+?" +
				"<th>?????????????????????<.+?<td .+?>(?<cat4>.+?)</td>" +
				".+?" +
				"<th>?????????<.+?<td .+?>(?<issueDate>.+?)</td>" +
				".+?" +
				"<th>?????????<.+?<td .+?>(?<redemptionDate>.+?)</td>" +
				".+?" +
				"<th>????????????<.+?<td .+?>(?<settlementFrequency>.+?)</td>" +
				".+?" +
				"<th>?????????<.+?<td .+?>(?<settlementDate>.+?)</td>" +
				".+?" +
				"<th>???????????????<.+?<td .+?>(?<cancelationFee>.+?)</td>" +
				".+?" +
				"??????????????????<br>??????????????????.+?<td .+?>(?<initialFeeLimit>.+?)</td>\\s+<td>(?<redemptionFee>.+?)</td>\\s+<td>(?<trustFee>.+?)</td>" +
				".+?" +
				"<tr>\\s+<th .+?>????????????</th>\\s+<td>(?<trustFeeOperation>.+?)</td>\\s+</tr>" +
				".+?" +
				"<tr>\\s+<th .+?>????????????</th>\\s+<td>(?<trustFeeSeller>.+?)</td>\\s+</tr>" +
				".+?" +
				"<tr>\\s+<th .+?>????????????</th>\\s+<td>(?<trustFeeBank>.+?)</td>\\s+</tr>" +
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
			public String issuer;               // ???????????????
			public String cat1;					// ?????????
			public String cat2;					// ??????
			public String cat3;					// ??????
			public String cat4;					// ?????????????????????
			public String issueDate;            // ?????????
			public String redemptionDate;       // ?????????
			public String settlementFrequency;  // ????????????
			public String settlementDate;       // ?????????
			public String cancelationFee;       // ???????????????
			public String initialFeeLimit;      // ?????????????????? ??????
			public String redemptionFee;        // ?????????????????????
			public String trustFee;             // ???????????? 
			public String trustFeeOperation ;   // ???????????? ????????????
			public String trustFeeSeller;       // ???????????? ????????????
			public String trustFeeBank;         // ???????????? ????????????
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
		
		private static final String DOESNOT_EXIST = "????????????????????????????????????";
		
		private static Pattern PAT_PERCENT_NUMBER = Pattern.compile("(?<number>[0-9]\\.[0-9]{5})%");
		private static BigDecimal getPercent(String string) {
			Matcher m = PAT_PERCENT_NUMBER.matcher(string);
			if (m.matches()) {
				String percentString = m.group(1);
				return normalize(new BigDecimal(percentString).movePointLeft(2)); // percent to decimal number
			} else {
				logger.error("Unpexpected percent number");
				logger.error("  {}!", string);
				throw new UnexpectedException("Unpexpected percent number");
			}
		}
		private static Pattern PAT_YEARLY_COUNT = Pattern.compile("???(?<number>[0-9]{1,2})???");
		private static int getYearlyCount(String string) {
			if (string.equals("??????")) return 365;
			
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

		
		public static List<Fund> update() {
			logger.info("update page");
			File dir = new File(DataFile.PageFile.getPath());
			dir.mkdirs();
			
			List<Fund> ret = new ArrayList<>();
			
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
				String settlementDate = page.settlementDate.equals("??????") ? Fund.SETTLEMENT_DATE_EVERYDAY : page.settlementDate;
				
				LocalDate issueDate = LocalDate.parse(page.issueDate);
				
				LocalDate redemptionDate = page.redemptionDate.equals("?????????") ? Fund.INDEFINITE : LocalDate.parse(page.redemptionDate);
				
				Fund fund = new Fund(
					page.isinCode, page.fundCode,
					0, 0, 0,
					BigDecimal.ZERO, BigDecimal.ZERO,
					null, null,
					page.cat1, page.cat2, page.cat3, page.cat4,
					page.name, page.issuer, issueDate, redemptionDate,
					settlementFrequency, settlementDate, 
					page.cancelationFee, page.redemptionFee,
					trustFee, trustFeeOperation, trustFeeSeller, trustFeeBank);

				ret.add(fund);
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
		private static void download(Download download, Set<String> set, Map<String, Fund> map) {
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
				String fundCode  = map.get(isinCode).fundCode;
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
		private static final String  CSV_HEADER  = "?????????,????????????(???),??????????????????????????????,?????????,?????????";
		
		public static void update(Map<String, Fund> fundMap) {
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
				Fund fund = fundMap.get(isinCode);
				
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
							String dateString          = fields[0]; // ?????????
							String basePriceString     = fields[1]; // ????????????(???) = ??????????????? / (????????? * 10,000) 1???????????????
							String netAssetValueString = fields[2]; // ??????????????????????????????
							String dividendString      = fields[3]; // ????????? 1???????????????
//							String periodString        = fields[4]; // ?????????
							
							dateString = dateString.replace("???", "-").replace("???", "-").replace("???", "");
							LocalDate date = LocalDate.parse(dateString);
							
							BigDecimal basePrice     = new BigDecimal(basePriceString);
							BigDecimal netAssetValue = new BigDecimal(netAssetValueString);
							BigDecimal totalUnits    = netAssetValue.movePointRight(6).divideToIntegralValue(basePrice); // convert million to number in NAVs

							priceList.add(new Price(date, isinCode, basePrice, netAssetValue, totalUnits));
							
							if (!dividendString.isEmpty()) {
								BigDecimal dividend = new BigDecimal(dividendString);
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

		public static void update(Map<String, Fund> fundMap) {
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
				Fund fund = fundMap.get(isinCode);
				
				String string = FileUtil.read().file(file);
				
				if (string.startsWith("[")) {
					BigDecimal initialFeeMin = null;
					BigDecimal initialFeeMax = null;
					
					List<Seller> sellerList = new ArrayList<>();
					for(var e: JSON.getList(SellerInfo.class, string)) {
						if (e.salesFee == null) {
							e.salesFee = MINUS_ONE;
						} else {
							e.salesFee = e.salesFee.movePointLeft(2); // percent to decimal number
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
	
	private static final Map<String, Offer> offerMap;
	static {
		offerMap = new TreeMap<>();
		offerMap.put("??????????????????",              Offer.PUBLIC);
		offerMap.put("?????????????????????????????????",     Offer.PRIVATE_GENERAL);
		offerMap.put("???????????????????????????????????????", Offer.PRIVATE_INSTITUTIONAL);
	}
	
	private static class FundInfo {
		public final String     isinCode;
		public final Offer      offer;
		public final BigDecimal unitPrice;
		
		public FundInfo(yokwe.stock.jp.jasdec.Fund fund) {
			if (!offerMap.containsKey(fund.offerCategory)) {
				logger.error("Unexpectec offerCategory");
				logger.error("  {} {}!", fund.isinCode, fund.offerCategory);
				throw new UnexpectedException("Unexpectec offerCategory");
			}

			this.isinCode  = fund.isinCode;
			this.offer     = offerMap.get(fund.offerCategory);
			this.unitPrice = fund.unitPrice;
		}
		
		public String isinCode() {
			return this.isinCode;
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
		
		// build fundMap
		Map<String, Fund> fundMap;
		{
			Map<String, FundInfo> fundInfoMap = yokwe.stock.jp.jasdec.Fund.load().stream().map(e -> new FundInfo(e)).collect(Collectors.toMap(FundInfo::isinCode, e -> e));
			logger.info("jasdec {}", fundInfoMap.size());
			
			PageFile.download(download, fundInfoMap.keySet());
			List<Fund> fundList = PageFile.update();
			int size = fundList.size();
			logger.info("fundList {}", size);
			
			// update fundList
			for(int i = 0; i < size; i++) {
				Fund fund = fundList.get(i);
				
				FundInfo fundInfo = fundInfoMap.get(fund.isinCode);
				
				fund.unitPrice = fundInfo.unitPrice;
				fund.offer     = fundInfo.offer;
			}
			// build fundMap
			fundMap = fundList.stream().collect(Collectors.toMap(Fund::isinCode, e -> e));
		}
		
		Set<String> isinSet = fundMap.keySet();

		PriceFile.download(download, isinSet, fundMap);
		SellerFile.download(download, isinSet);
		
		PriceFile.update(fundMap);
		SellerFile.update(fundMap);
		
		{
			List<Fund> list = fundMap.values().stream().collect(Collectors.toList());
			Fund.save(list);
			logger.info("save {} {}", list.size(), Fund.getPath());
		}
		
		logger.info("STOP");
	}	
}
