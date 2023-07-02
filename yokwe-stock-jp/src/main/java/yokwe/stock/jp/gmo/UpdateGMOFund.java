package yokwe.stock.jp.gmo;

import java.io.File;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import yokwe.stock.jp.Storage;
import yokwe.stock.jp.toushin.Fund;
import yokwe.util.FileUtil;
import yokwe.util.UnexpectedException;
import yokwe.util.http.HttpUtil;
import yokwe.util.json.JSON;

public class UpdateGMOFund {
	private static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();
	
	private static final boolean DEBUG_USE_FILE = false;
	
	private static final Charset UTF_8 = StandardCharsets.UTF_8;
	
	private static final Map<String, Fund> fundMap = Fund.getMap();
	//                       isinCode
	
	public static class CallFunds {
		public static class Fund {
			String isinCode;    // isinCode: JP90C000GKC6
			String fundname;    // fundname: ｅＭＡＸＩＳ　Ｓｌｉｍ　米国株式（Ｓ＆Ｐ５００）
			String area;        // area: 北米
			String assets;      // assets: 株式
			String kagaku;      // kagaku: 22664
			String width;       // width: 202
			String ratio;       // ratio: 0.90
			String netassets;   // netassets: 2355746
			String return1m;    // return1m: 6.13
			String return3m;    // return3m: 8.65
			String return6m;    // return6m: 7.71
			String return1y;    // return1y: 11.82
			String return3y;    // return3y: 87.56
			String return5y;    // return5y: --
			String netassetsio; // netassetsio: 321.58
			String commission;  // commission: 0.00000000
			String mgtfee;      // mgtfee: 0.09372000
		}
		
		public String state;
		public String hitCount;
		public Fund[] fundList;
	}
	
	
	
	public static String getURL(int before) {
		LinkedHashMap<String, String> map = new LinkedHashMap<>();
		map.put("callback", "callFunds");
		map.put("F", "fund/fund_list");
		map.put("KEY1", "");
		map.put("KEY21", "JPN,FRN,EMR");
		map.put("KEY3", "株式,債券,資産複合,不動産投信,その他資産");
		map.put("KEY5", "");
		map.put("KEY6", "");
		map.put("KEY7", "");
		map.put("KEY8", "");
		map.put("KEY9", "");
		map.put("KEY10", "");
		map.put("KEY11", "");
		map.put("KEY12", "");
		map.put("KEY13", "");
		map.put("KEY14", "");
		map.put("KEY15", "");
		map.put("KEY16", "");
		map.put("KEY17", "");
		map.put("KEY18", "");
		map.put("KEY19", "");
		map.put("KEY20", "");
		map.put("KEY20", "");
		map.put("KEY20", "");
		map.put("BEFORE", String.valueOf(before));
		map.put("GO_BEFORE", "");
		map.put("REFINDEX", "-AF純資産総額");
		map.put("MAXDISP", "10000");
		
		String string = map.entrySet().stream().map(o -> o.getKey() + "=" + URLEncoder.encode(o.getValue(), UTF_8)).collect(Collectors.joining("&"));
		
		return String.format("https://ot36.qhit.net/gmo-clsec/qsearch.exe?%s", string);
	}
	
	private static String getPage(int before) {
		final File file;
		{
			String name = String.format("callFunds-%d.json", before);
			String path = Storage.GMO.getPath("page", name);
			file = new File(path);
		}
		
		if (DEBUG_USE_FILE) {
			if (file.exists()) return FileUtil.read().file(file);
		}
		
		String url = getURL(before);
		HttpUtil.Result result = HttpUtil.getInstance().download(url);
		
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
		
		String page = result.result.replace("\r", "\n");

		// for debug
		FileUtil.write().file(file, page);

		return page;
	}
	
	private static void addFund(List<GMOFund> fundList, CallFunds callFunds) {
		for(var e: callFunds.fundList) {
			String     isinCode       = e.isinCode;
			BigDecimal salesFeeRatio = new BigDecimal(e.commission).movePointLeft(2).stripTrailingZeros();
			BigDecimal expenseRatio  = new BigDecimal(e.mgtfee).movePointLeft(2).stripTrailingZeros();
			
			{
				BigDecimal comsumptionTaxRatio = new BigDecimal("1.1");
				if (!salesFeeRatio.equals(BigDecimal.ZERO)) {
					salesFeeRatio = salesFeeRatio.divide(comsumptionTaxRatio, 8, RoundingMode.HALF_EVEN).stripTrailingZeros();
				}
				if (!expenseRatio.equals(BigDecimal.ZERO)) {
					expenseRatio = expenseRatio.divide(comsumptionTaxRatio, 8, RoundingMode.HALF_EVEN).stripTrailingZeros();
				}
			}
			
			if (fundMap.containsKey(isinCode)) {
				Fund fund = fundMap.get(isinCode);
				
				String fundCode = fund.fundCode;
				String name     = fund.name;
								
				GMOFund gmoFund = new GMOFund(isinCode, fundCode, salesFeeRatio, expenseRatio, name);
				fundList.add(gmoFund);
			} else {
				logger.warn("Unexpecetd fundCode");
				logger.warn("  fund  {}", e);
				continue;
			}
		}
	}

	public static void main(String[] args) {
		logger.info("START");
		
		List<GMOFund> fundList = new ArrayList<>();
		
		int before = 0;
		int count = 0;
		for(;;) {
			count++;
			String    page      = getPage(before).replace("callFunds(", "").replace(");", "").replace("\n\n,\n", "\n\n\n");		
			CallFunds callFunds = JSON.unmarshal(CallFunds.class, page);
			logger.info("callFunds  {} fundList  {}  {}  {}", before, callFunds.state, callFunds.hitCount, callFunds.fundList.length);
			int hitCount = Integer.valueOf(callFunds.hitCount);
			
			addFund(fundList, callFunds);
			
			if (fundList.size() == hitCount) break;
			if (count == 2) {
				logger.error("Unexpected count");
				logger.error("  count     {}", count);
				logger.error("  hitCount  {}", hitCount);
				logger.error("  fundList  {}", fundList.size());
				throw new UnexpectedException("Unexpected count");
			}
			before = fundList.size();
		}
		
		logger.info("save  {}  {}", fundList.size(), GMOFund.getPath());
		GMOFund.save(fundList);
		
		logger.info("STOP");
	}

}
