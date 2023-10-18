package yokwe.finance.provider.rakuten;

import java.io.File;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.stream.Collectors;

import yokwe.finance.fund.StorageFund;
import yokwe.finance.type.TradingFundType;
import yokwe.util.FileUtil;
import yokwe.util.UnexpectedException;
import yokwe.util.http.HttpUtil;
import yokwe.util.json.JSON;
import yokwe.util.json.JSON.Ignore;
import yokwe.util.json.JSON.Name;

public class UpdateTradingFundRakuten {
	private static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();
	
	private static final boolean DEBUG_USE_FILE  = false;

	private static final String  URL             = "https://www.rakuten-sec.co.jp/web/fund/scr/find/search/reloadscreener.asp";
	private static final String  CONTENT_TYPE    = "application/x-www-form-urlencoded;charset=UTF-8";

	private static String download(String url, String postBody, String filePath, boolean useFile) {
		final String page;
		{
			File file = new File(filePath);
			if (useFile && file.exists()) {
				page = FileUtil.read().file(file);
			} else {
				HttpUtil.Result result = HttpUtil.getInstance().withPost(postBody, CONTENT_TYPE).download(url);
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
		
		@Name("BunruiMapping")   @Ignore public BunruiMapping bunruiMapping;
		@Name("BunruiText")      @Ignore public String        bunruiText;
		@Name("Conditions")      @Ignore public String        conditions;
		@Name("Data")                    public String[][]    data;
		@Name("Headers")                 public String[]      headers;
		@Name("JSONBunruiCount") @Ignore public String        jsonBunruiCount;
		@Name("JSONCount")       @Ignore public String        jsonCount;
		@Name("PageInfo")                public PageInfo      pageInfo;
		@Name("ResultCounts")    @Ignore public ResultCounts  resultCounts;
		@Name("TotalCount")      @Ignore public String        totalCount;
		@Name("Warnings")                public String        warnings;
		
		@Override
		public String toString() {
			return String.format("{record %s - %s  %s / %s  %d}", pageInfo.startRecord, pageInfo.endRecord, pageInfo.pageSelected, pageInfo.pagesTotal, data.length);
		}
	}
	
	private static String getPostBody() {
		LinkedHashMap<String, String> map = new LinkedHashMap<>();
		map.put("result", "ファンド名称,actual_charge");
		map.put("pg", "0");
		map.put("count", "9999");
		map.put("sortnull", "コード=up");
		
		String string = map.entrySet().stream().map(o -> o.getKey() + "=" + o.getValue()).collect(Collectors.joining("&"));
		return String.format("query=%s", URLEncoder.encode(string, StandardCharsets.UTF_8));
	}

	private static void update() {
		List<TradingFundType> list = new ArrayList<>();
		{
			var isinCodeSet = StorageFund.FundInfo.getList().stream().map(o -> o.isinCode).collect(Collectors.toSet());
			
			String page;
			{
				String postBody = getPostBody();
				String filePath = StorageRakuten.getPath("reloadscreener.json");
				
				page = download(URL, postBody, filePath, DEBUG_USE_FILE);
			}
			
			PageData pageData = JSON.unmarshal(PageData.class, page);
			logger.info("page  {}", pageData.toString());
			for(int i = 0; i < pageData.data.length; i++) {
				String[] data = pageData.data[i];
				
				String isinCode      = data[0];
				String name          = data[1].replace("&amp;", "&");
//				String actual_charge = data[2];

				// sanity check
				if (isinCodeSet.contains(isinCode)) {
					list.add(new TradingFundType(isinCode, BigDecimal.ZERO));
				} else {
					logger.warn("Bogus isinCode  {}  {}", isinCode, name);
				}
			}
		}
		
		logger.info("save  {}  {}", list.size(), StorageRakuten.TradingFundRakuten.getPath());
		StorageRakuten.TradingFundRakuten.save(list);
	}
	
	
	public static void main(String[] args) {
		logger.info("START");
		
		update();
				
		logger.info("STOP");
	}
}
