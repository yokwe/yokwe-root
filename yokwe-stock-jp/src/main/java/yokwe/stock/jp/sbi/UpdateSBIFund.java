package yokwe.stock.jp.sbi;

import java.io.File;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.stream.Collectors;

import yokwe.stock.jp.Storage;
import yokwe.stock.jp.toushin.Fund;
import yokwe.util.FileUtil;
import yokwe.util.UnexpectedException;
import yokwe.util.http.HttpUtil;
import yokwe.util.json.JSON;

public final class UpdateSBIFund {
	private static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();
	
	private static final boolean DEBUG_USE_FILE  = false;
	
	private static final String  URL             = "https://site0.sbisec.co.jp/marble/fund/powersearch/fundpsearch/search.do";
	private static final String  REFERER         = "https://site0.sbisec.co.jp/marble/fund/powersearch/fundpsearch.do";
	private static final String  CONTENT_TYPE    = "application/x-www-form-urlencoded; charset=UTF-8";
	
	private static final Map<String, String> fundCodeMap = new TreeMap<>();
	//                       old     new
	static {
		//JP90C0006MC9,7231109B,ＮＮインドネシア株式ファンド
		//JP90C000AHA9,72311146,ＮＮ欧州リート・ファンド（毎月決算コース／為替ヘッジなし）
		//JP90C000AHB7,72312146,ＮＮ欧州リート・ファンド（資産形成コース／為替ヘッジなし）
		//JP90C000B8E4,72312151,ＮＮ欧州リート・ファンド（資産形成コース／為替ヘッジあり）
		//JP90C000B8F1,72311151,ＮＮ欧州リート・ファンド（毎月決算コース／為替ヘッジあり）
		fundCodeMap.put("3531209B", "7231109B");  // 3531209B  ＧＳ－ＮＮインドネシア株式ファンド
		fundCodeMap.put("35313151", "72312151");  // 35313151  ＧＳ－ＮＮ欧州リート・ファンド（資産形成コース／為替ヘッジあり）
		fundCodeMap.put("35313146", "72312146");  // 35313146  ＧＳ－ＮＮ欧州リート・ファンド（資産形成コース／為替ヘッジなし）
		fundCodeMap.put("35312151", "72311151");  // 35312151  ＧＳ－ＮＮ欧州リート・ファンド（毎月決算コース／為替ヘッジあり）
		fundCodeMap.put("35312146", "72311146");  // 35312146  ＧＳ－ＮＮ欧州リート・ファンド（毎月決算コース／為替ヘッジなし）
	}

	private static final Map<String, Fund> fundMap = Fund.getList().stream().collect(Collectors.toMap(o -> o.fundCode, Function.identity()));
	//                       fundCode
	
	public static String getPostBody(int pageNo) {
		LinkedHashMap<String, String> map = new LinkedHashMap<>();
		map.put("pageNo",          String.valueOf(pageNo));
		map.put("fundName",        "");
		map.put("pageRows",        "100");
		map.put("tabName",         "base");
		map.put("sortColumn",      "090");
		map.put("sortOrder",       "1");
		map.put("unyouColumnName", "totalReturnColumns");
		map.put("hitLimit",        "0");
		map.put("searchWordsMode", "1");
		map.put("commission",      "X");
		map.put("trustCharge",     "X");
		map.put("yield",           "X");
		map.put("sharpRatio",      "X");
		map.put("sigma",           "X");
		map.put("flow",            "X");
		map.put("asset",           "X");
		map.put("standardPrice",   "X");
		map.put("redemption",      "X");
		map.put("period",          "X");
		map.put("company",         "--");
		map.put("budget",          "1");
		
		String string = map.entrySet().stream().map(o -> o.getKey() + "=" + o.getValue()).collect(Collectors.joining("&"));
		return string;
	}
	
	public static String getPage(int pageNo) {
		final File file;
		{
			String name = String.format("search-%d.json", pageNo);
			String path = Storage.SBI.getPath("page", name);
			file = new File(path);
		}
		
		if (DEBUG_USE_FILE) {
			if (file.exists()) return FileUtil.read().file(file);
		}

		String postBody = getPostBody(pageNo);
		String url      = URL;
		HttpUtil.Result result = HttpUtil.getInstance().withReferer(REFERER).withPost(postBody, CONTENT_TYPE).download(url);
		
		if (result == null) {
			logger.error("result == null");
			logger.error("  url {}!", url);
			throw new UnexpectedException("result == null");
		}
		if (result.result == null) {
			logger.error("result.result == null");
			logger.error("  url       {}!", url);
			logger.error("  response  {}  {}", result.code, result.reasonPhrase);
			throw new UnexpectedException("result.result == null");
		}
		
		String page = result.result;
		
		// for debug
		FileUtil.write().file(file, page);

		return page;
	}
	
	private static class SearchResult {
		public static class Filler {
			//
		}
		
		public static class Pager {			
			@JSON.Name("dataList")    @JSON.Ignore public String[] dataList;
			@JSON.Name("endNumber")   @JSON.Ignore public int      endNumber;
			@JSON.Name("linkParam")   @JSON.Ignore public String   linkParam;
			@JSON.Name("linkUrl")     @JSON.Ignore public String   linkUrl;
			@JSON.Name("pageInfo")    @JSON.Ignore public Filler   pageInfo;
			@JSON.Name("startNumber") @JSON.Ignore public int      startNumber;
			@JSON.Name("totalCount")               public int      totalCount;
			@JSON.Name("totalPage")                public int      totalPage;
			
			@Override
			public String toString() {
				return String.format("{%d  %d}", totalCount, totalPage);
			}
			
		}
		public static class Record {
			@JSON.Name("FDDividendsSchedule")     @JSON.Ignore public String     fdDividendsSchedule;
			@JSON.Name("FDPolicy")                @JSON.Ignore public String     fdPolicy;
			@JSON.Name("FDReservedAssetNum")      @JSON.Ignore public int        fdReservedAssetNum;
			@JSON.Name("FDReservedAssetNumFlg")   @JSON.Ignore public String     fdReservedAssetNumFlg;
			@JSON.Name("FDTrustChargeNum")                     public BigDecimal fdTrustChargeNum;
			@JSON.Name("FDTrustChargeNumComment") @JSON.Ignore public String     fdTrustChargeNumComment;
			@JSON.Name("FDTrustChargeNumFlg")     @JSON.Ignore public String     fdTrustChargeNumFlg;
			@JSON.Name("MFFundAisyo")             @JSON.Ignore public String     mfFundAisyo;
			@JSON.Name("MFName")                               public String     mfName;
			@JSON.Name("baseFundType")            @JSON.Ignore public String     baseFundType;
			@JSON.Name("budget")                  @JSON.Ignore public String     budget;
			@JSON.Name("commissionAll")           @JSON.Ignore public BigDecimal commissionAll;
			@JSON.Name("commissionKingak")        @JSON.Ignore public BigDecimal commissionKingak;
			@JSON.Name("commissionKuchisu")       @JSON.Ignore public BigDecimal commissionKuchisu;
			@JSON.Name("commissionNisa")          @JSON.Ignore public BigDecimal commissionNisa;
			@JSON.Name("comparable")              @JSON.Ignore public boolean    comparable;
			@JSON.Name("dividendsYieldDisp")      @JSON.Ignore public String     dividendsYieldDisp;
			@JSON.Name("fphAsset")                @JSON.Ignore public BigDecimal fphAsset;
			@JSON.Name("fphComp")                 @JSON.Ignore public BigDecimal fphComp;
			@JSON.Name("fphPrice")                @JSON.Ignore public BigDecimal fphPrice;
			@JSON.Name("fundCode")                             public String     fundCode;
			@JSON.Name("fundDividend")            @JSON.Ignore public BigDecimal fundDividend;
			@JSON.Name("kingakuCommission1")      @JSON.Ignore public BigDecimal kingakuCommission1;
			@JSON.Name("kingakuCommission2")      @JSON.Ignore public BigDecimal kingakuCommission2;
			@JSON.Name("kingakuCommission3")      @JSON.Ignore public BigDecimal kingakuCommission3;
			@JSON.Name("kingakuCommission4")      @JSON.Ignore public BigDecimal kingakuCommission4;
			@JSON.Name("kingakuCommission5")      @JSON.Ignore public BigDecimal kingakuCommission5;
			@JSON.Name("kutiCommission1")         @JSON.Ignore public BigDecimal kutiCommission1;
			@JSON.Name("kutiCommission2")         @JSON.Ignore public BigDecimal kutiCommission2;
			@JSON.Name("kutiCommission3")         @JSON.Ignore public BigDecimal kutiCommission3;
			@JSON.Name("kutiCommission4")         @JSON.Ignore public BigDecimal kutiCommission4;
			@JSON.Name("kutiCommission5")         @JSON.Ignore public BigDecimal kutiCommission5;
			@JSON.Name("lr1year")                 @JSON.Ignore public BigDecimal lr1year;
			@JSON.Name("lr3years")                @JSON.Ignore public BigDecimal lr3years;
			@JSON.Name("lr6months")               @JSON.Ignore public BigDecimal lr6months;
			@JSON.Name("msCategoryName")          @JSON.Ignore public String     msCategoryName;
			@JSON.Name("msRatingAllName")         @JSON.Ignore public String     msRatingAllName;
			@JSON.Name("nextAccountingDate")      @JSON.Ignore public String     nextAccountingDate;
			@JSON.Name("nisaCommission")          @JSON.Ignore public BigDecimal nisaCommission;
			@JSON.Name("nisaFlg")                 @JSON.Ignore public String     nisaFlg;
			@JSON.Name("regionName")              @JSON.Ignore public String     regionName;
			@JSON.Name("riskMajor3years")         @JSON.Ignore public String     riskMajor3years;
			@JSON.Name("salseAmountArrowKbn")     @JSON.Ignore public String     salseAmountArrowKbn;
			@JSON.Name("salseAmountRankDisp")     @JSON.Ignore public int        salseAmountRankDisp;
			@JSON.Name("sbiRecommendedFlg")       @JSON.Ignore public String     sbiRecommendedFlg;
			@JSON.Name("sdSigma1year")            @JSON.Ignore public BigDecimal sdSigma1year;
			@JSON.Name("searchCommission1")       @JSON.Ignore public BigDecimal searchCommission1;
			@JSON.Name("searchCommission2")       @JSON.Ignore public BigDecimal searchCommission2;
			@JSON.Name("searchCommission3")       @JSON.Ignore public BigDecimal searchCommission3;
			@JSON.Name("searchCommission4")       @JSON.Ignore public BigDecimal searchCommission4;
			@JSON.Name("searchCommission5")       @JSON.Ignore public BigDecimal searchCommission5;
			@JSON.Name("sharpRatio1year")         @JSON.Ignore public BigDecimal sharpRatio1year;
			@JSON.Name("totalreturn1year")        @JSON.Ignore public BigDecimal totalreturn1year;
			@JSON.Name("totalreturn3years")       @JSON.Ignore public BigDecimal totalreturn3years;
			@JSON.Name("totalreturn6months")      @JSON.Ignore public BigDecimal totalreturn6months;
			@JSON.Name("tumitateButtonKbn")       @JSON.Ignore public String     tumitateButtonKbn;
			@JSON.Name("yearDividends")           @JSON.Ignore public BigDecimal yearDividends;
			
			@Override
			public String toString() {
				return String.format("{%s  %s  %s}", fundCode, fdTrustChargeNum.toPlainString(), mfName);
			}
		}
		
		@JSON.Name("blockage")        @JSON.Ignore public boolean  blockage;
		@JSON.Name("blockageMessage") @JSON.Ignore public String   blockageMessage;
		@JSON.Name("compareTarget")   @JSON.Ignore public Filler[] compareTarget;
		@JSON.Name("counts")          @JSON.Ignore public Filler   counts;
		@JSON.Name("loggedIn")        @JSON.Ignore public boolean  loggedIn;
		@JSON.Name("pager")                        public Pager    pager;
		@JSON.Name("records")                      public Record[] records;
		@JSON.Name("selects")         @JSON.Ignore public Filler   selects;
		@JSON.Name("feature")         @JSON.Ignore public Filler[] fature;
		@JSON.Name("flow")            @JSON.Ignore public String   flow;
		@JSON.Name("fundName")        @JSON.Ignore public String   fundName;
		@JSON.Name("hitLimit")        @JSON.Ignore public String   hitLimit;
		@JSON.Name("msCategory")      @JSON.Ignore public Filler[] msCategory;
		@JSON.Name("msRanking")       @JSON.Ignore public Filler[] msRanking;
		@JSON.Name("other")           @JSON.Ignore public Filler[] other;
		@JSON.Name("period")          @JSON.Ignore public String   period;
		@JSON.Name("possession")      @JSON.Ignore public Filler[] possession;
		@JSON.Name("redemption")      @JSON.Ignore public String   redemption;
		@JSON.Name("region")          @JSON.Ignore public Filler[] region;
		@JSON.Name("riskMajor")       @JSON.Ignore public Filler[] riskMajor;
		@JSON.Name("searchWordsMode") @JSON.Ignore public String   searchWordsMode;
		@JSON.Name("sharpRatio")      @JSON.Ignore public String   sharpRatio;
		@JSON.Name("sigma")           @JSON.Ignore public String   sigma;
		@JSON.Name("standardPrice")   @JSON.Ignore public String   standardPrice;
		@JSON.Name("totalReturn")     @JSON.Ignore public Filler[] totalReturn;
		@JSON.Name("sortColumn")      @JSON.Ignore public String   sortColumn;
		@JSON.Name("sortOrder")       @JSON.Ignore public String   sortOrder;
		@JSON.Name("tabName")         @JSON.Ignore public String   tabName;
		@JSON.Name("unyouColumnName") @JSON.Ignore public String   unyouColumnName;

		@Override
		public String toString() {
			return String.format("{%s  %d}", pager, records.length);
		}
	}
	
	public static void addFund(List<SBIFund> fundList, SearchResult.Record[] records) {
		for(var e: records) {
			String     fundCode     = e.fundCode;
			BigDecimal salesFee     = BigDecimal.ZERO;
			BigDecimal expenseRatio = e.fdTrustChargeNum;
			
			if (fundCodeMap.containsKey(fundCode)) {
				fundCode = fundCodeMap.get(fundCode);
				
				if (fundMap.containsKey(fundCode)) {
					Fund fund = fundMap.get(fundCode);
					logger.info("fundCodeMap  old  {}  {}", e.fundCode, e.mfName);
					logger.info("             new  {}  {}", fund.fundCode, fund.name);
				} else {
					logger.warn("Unexpected fundCode  {}  {}  {}", fundCode, e.fundCode,  e.mfName);
					continue;
				}
			}
			
			if (fundMap.containsKey(fundCode)) {
				Fund fund = fundMap.get(fundCode);
				fundList.add(new SBIFund(fund.isinCode, fundCode, salesFee, expenseRatio, fund.name));
			} else {
				logger.warn("Unexpected fundCode  {}  {}", e.fundCode,  e.mfName);
			}
		}
	}
	
	public static List<SBIFund> getFundList() {
		List<SBIFund> fundList = new ArrayList<>();
		
		final int totalCount;
		final int totalPage;
		{
			int pageNo = 0;
			
			String       page         = getPage(pageNo);
			SearchResult searchResult = JSON.unmarshal(SearchResult.class, page);
			logger.info("page  {}  {}", pageNo, searchResult.records.length);

			totalCount = searchResult.pager.totalCount;
			totalPage  = searchResult.pager.totalPage;
			
			addFund(fundList, searchResult.records);
		}
		logger.info("totalCount  {}", totalCount);
		logger.info("totalPage   {}", totalPage);

		{
			for(int pageNo = 1; pageNo <= totalPage; pageNo++) {
				String       page         = getPage(pageNo);
				SearchResult searchResult = JSON.unmarshal(SearchResult.class, page);
				logger.info("page  {}  {}", pageNo, searchResult.records.length);

				addFund(fundList, searchResult.records);
			}
		}
		return fundList;
	}
	
	public static void main(String[] args) {
		logger.info("START");
		
		List<SBIFund> fundList = getFundList();		
		logger.info("save  {}  {}", fundList.size(), SBIFund.getPath());
		SBIFund.save(fundList);
		
		logger.info("STOP");
	}

}
