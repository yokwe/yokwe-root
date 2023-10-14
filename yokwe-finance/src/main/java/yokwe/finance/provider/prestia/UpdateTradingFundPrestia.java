package yokwe.finance.provider.prestia;

import java.io.File;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.stream.Collectors;

import yokwe.finance.Storage;
import yokwe.finance.fund.FundInfo;
import yokwe.finance.type.TradingFundType;
import yokwe.util.FileUtil;
import yokwe.util.StringUtil;
import yokwe.util.UnexpectedException;
import yokwe.util.http.HttpUtil;
import yokwe.util.json.JSON;

public class UpdateTradingFundPrestia {
	private static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();
	
	private static final boolean DEBUG_USE_FILE  = true;
	
	private static final String URL     = "https://lt.morningstar.com/api/rest.svc/smbctbfund/security/screener";
	private static final String CHARSET = "UTF-8";
	
	
	private static String download(String url, String charset, String filePath, boolean useFile) {
		final String page;
		{
			File file = new File(filePath);
			if (useFile && file.exists()) {
				page = FileUtil.read().file(file);
			} else {
				HttpUtil.Result result = HttpUtil.getInstance().withCharset(charset).download(url);
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
	
	
	public static class Screener {
	    public static final class Rows {
	        @JSON.Name("isin")           @JSON.Optional public String isinCode; // STRING STRING
	        @JSON.Name("secId")                         public String secId;    // STRING STRING
	        @JSON.Name("customFundName")                public String fundName; // STRING STRING

	        @Override
	        public String toString() {
	            return StringUtil.toString(this);
	        }
	    }

	    @JSON.Name("page")     public BigDecimal page;     // NUMBER INT
	    @JSON.Name("pageSize") public BigDecimal pageSize; // NUMBER INT
	    @JSON.Name("rows")     public Rows[]     rows;     // ARRAY 168
	    @JSON.Name("total")    public BigDecimal total;    // NUMBER INT

	    @Override
	    public String toString() {
	        return StringUtil.toString(this);
	    }
	}

	
	private static void update() {
		//
		String page;
		{
			// page=1&pageSize=1000&outputType=json&languageId=ja-JP&securityDataPoints=isin|secId|customFundName";
			LinkedHashMap<String, String> map = new LinkedHashMap<>();
			map.put("page",  "1");
			map.put("pageSize", "1000");
			map.put("outputType", "json");
			map.put("languageId",  "ja-JP");
			map.put("securityDataPoints", "isin|secId|customFundName");
			String queryString = map.entrySet().stream().map(o -> o.getKey() + "=" + URLEncoder.encode(o.getValue(), StandardCharsets.UTF_8)).collect(Collectors.joining("&"));

			String url      = String.format("%s?%s", URL, queryString);
			String filePath = Storage.provider_prestia.getPath("screener.json");
			page = download(url, CHARSET, filePath, DEBUG_USE_FILE);
			
			logger.info("page  {}", page.length());
		}
		
		var screener = JSON.unmarshal(Screener.class, page);
		logger.info("screener  {}  {}  {}  {}", screener.page, screener.pageSize, screener.rows.length, screener.total);
		
		var list = new ArrayList<TradingFundType>();
		{
			var isinCodeSet = FundInfo.getList().stream().map(o -> o.isinCode).collect(Collectors.toSet());
			int countA = 0;
			int countB = 0;
			int countC = 0;
			int countD = 0;

			for(var row: screener.rows) {
				String isinCode = row.isinCode;
				String fundName = row.fundName;
				
				if (isinCodeSet.contains(isinCode)) {
					// FIXME find out buy fee of fund using link below
					//       Use Custom.CustomBuyFeeNote
					//       https://gllt.morningstar.com/api/rest.svc/smbctbfund/security_details/F00000XM7M?viewId=MFsnapshot&idtype=msid&responseViewFormat=json
					list.add(new TradingFundType(isinCode, TradingFundType.SALES_FEE_UNKNOWN));
					countA++;
				} else {
					if (isinCode.startsWith("JP")) {
						logger.warn("Unexpected isinCode  {}  {}", isinCode, fundName);
						countB++;
					} else if (isinCode.isEmpty()) {
						// no isinCode
						logger.warn("Ignore no isinCode   {}", fundName);
						countC++;
					} else {
						// ignore foreign registered fund
//						logger.warn("Ignore foreign fund  {}  {}", isinCode, fundName);
						countD++;
					}
				}
			}
			logger.info("countA    {}", countA);
			logger.info("countB    {}", countB);
			logger.info("countC    {}", countC);
			logger.info("countD    {}", countD);
		}
		logger.info("save  {}  {}", list.size(), TradingFundPrestia.getPath());
		TradingFundPrestia.save(list);
	}
	
	public static void main(String[] args) {
		logger.info("START");
		
		update();
				
		logger.info("STOP");
	}
}
