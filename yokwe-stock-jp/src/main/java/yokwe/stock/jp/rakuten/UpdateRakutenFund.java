package yokwe.stock.jp.rakuten;

import java.io.File;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import yokwe.stock.jp.Storage;
import yokwe.stock.jp.toushin.Fund;
import yokwe.util.FileUtil;
import yokwe.util.UnexpectedException;
import yokwe.util.http.HttpUtil;
import yokwe.util.json.JSON;
import yokwe.util.json.JSON.Ignore;
import yokwe.util.json.JSON.Name;


public class UpdateRakutenFund {
	private static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();
	
	private static final boolean DEBUG_USE_FILE  = false;
	
	private static final Charset UTF_8           = StandardCharsets.UTF_8;
	private static final String  URL             = "https://www.rakuten-sec.co.jp/web/fund/scr/find/search/reloadscreener.asp";
	private static final String  CONTENT_TYPE    = "application/x-www-form-urlencoded;charset=UTF-8";

	private static final Map<String, Fund> fundMap = Fund.getList().stream().collect(Collectors.toMap(o -> o.isinCode, Function.identity()));
	//                       isinCode
	
	public static String getPostBody() {
		LinkedHashMap<String, String> map = new LinkedHashMap<>();
		map.put("result", "ファンド名称,actual_charge");
		map.put("pg", "0");
		map.put("count", "9999");
		map.put("sortnull", "コード=up");
		
		String string = map.entrySet().stream().map(o -> o.getKey() + "=" + o.getValue()).collect(Collectors.joining("&"));
		return String.format("query=%s", URLEncoder.encode(string, UTF_8));
	}
	
	public static String getPage() {
		final File file;
		{
			String name = "reloadscreener.json";
			String path = Storage.Rakuten.getPath(name);
			file = new File(path);
		}
		
		if (DEBUG_USE_FILE) {
			if (file.exists()) return FileUtil.read().file(file);
		}

		String postBody = getPostBody();
		String url      = URL;
		HttpUtil.Result result = HttpUtil.getInstance().withPost(postBody, CONTENT_TYPE).download(url);
		
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
	
	public static List<RakutenFund> getFundList() {
		List<RakutenFund> fundList = new ArrayList<>();
		
		{
			String page = getPage();
			PageData pageData = JSON.unmarshal(PageData.class, page);
			logger.info("page  {}", pageData.toString());
			for(int i = 0; i < pageData.data.length; i++) {
				String[] data = pageData.data[i];
				String isinCode      = data[0];
				String name          = data[1];
				String actual_charge = data[2];
				
				BigDecimal salesFee     = BigDecimal.ZERO;
				BigDecimal expenseRatio = new BigDecimal(actual_charge).movePointLeft(2);
				
				if (fundMap.containsKey(isinCode)) {
					Fund fund = fundMap.get(isinCode);
					fundList.add(new RakutenFund(fund.isinCode, fund.fundCode, salesFee, expenseRatio, fund.name));
				} else {
					logger.warn("Bogus isinCode  {}  {}", isinCode, name);
				}				
			}
		}
		return fundList;
	}
	
	private static class PageData {
		public static class BunruiMapping {
			//
		}
		public static class PageInfo {
			@Name("StartRecord")     public String        startRecord;
			@Name("EndRecord")       public String        endRecord;
			
			@Name("NbrPagesTotal")   public String        pagesTotal;
			@Name("NbrRecsSelected") public String        recordsSelected;
			@Name("NbrRecsTotal")    public String        recordsTotal;
			@Name("PageSelected")    public String        pageSelected;
			@Name("RecordsPerPage")  public String        recordsPerPage;
		}
		public static class ResultCounts {
			//
		}
		
		@Ignore
		@Name("BunruiMapping")   public BunruiMapping bunruiMapping;
		@Ignore
		@Name("BunruiText")      public String        bunruiText;
		@Ignore
		@Name("Conditions")      public String        conditions;
		@Name("Data")            public String[][]    data;
		@Name("Headers")         public String[]      headers;
		@Ignore
		@Name("JSONBunruiCount") public String        jsonBunruiCount;
		@Ignore
		@Name("JSONCount")       public String        jsonCount;
		@Name("PageInfo")        public PageInfo      pageInfo;
		@Ignore
		@Name("ResultCounts")    public ResultCounts  resultCounts;
		@Ignore
		@Name("TotalCount")      public String        totalCount;
		@Name("Warnings")        public String        warnings;
		
		@Override
		public String toString() {
			return String.format("{record %s - %s  %s / %s  %d", pageInfo.startRecord, pageInfo.endRecord, pageInfo.pageSelected, pageInfo.pagesTotal, data.length);
		}
	}
	
	// https://www.rakuten-sec.co.jp/web/fund/detail/?ID=JP90C0000W25
	public static void main(String[] args) {
		logger.info("START");
		
		List<RakutenFund> fundList = getFundList();		
		logger.info("save  {}  {}", fundList.size(), RakutenFund.getPath());
		RakutenFund.save(fundList);
		
		logger.info("STOP");
	}

}
