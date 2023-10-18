package yokwe.finance.provider.click;

import java.io.File;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import yokwe.finance.fund.StorageFund;
import yokwe.finance.type.TradingFundType;
import yokwe.util.FileUtil;
import yokwe.util.UnexpectedException;
import yokwe.util.http.HttpUtil;
import yokwe.util.json.JSON;

public class UpdateTradingFundClick {
	private static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();
	
	private static final boolean DEBUG_USE_FILE = false;
	
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
				page = result.result.replace("\r", "\n"); // fix end of line  -- server is windows
				// debug
				if (DEBUG_USE_FILE) logger.info("save  {}  {}", page.length(), file.getPath());
				FileUtil.write().file(file, page);
			}
		}
		return page;
	}

	
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
	
	
	private static String getURL(int before) {
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
		
		String string = map.entrySet().stream().map(o -> o.getKey() + "=" + URLEncoder.encode(o.getValue(), StandardCharsets.UTF_8)).collect(Collectors.joining("&"));
		
		return String.format("https://ot36.qhit.net/gmo-clsec/qsearch.exe?%s", string);
	}
	
	private static Set<String> isinCodeSet = StorageFund.FundInfo.getList().stream().map(o -> o.isinCode).collect(Collectors.toSet());
	
	private static void updateList(List<TradingFundType> list) {
		int before = 0;
		for(;;) {
			String page;
			{
				String url      = getURL(before);
				String filePath = StorageClick.getPath("page-" + before + ".html");
				page = download(url, CHARSET, filePath, DEBUG_USE_FILE).replace("callFunds(", "").replace(");", "").replace("\n\n,\n", "\n\n\n");
			}

			CallFunds callFunds = JSON.unmarshal(CallFunds.class, page);
			logger.info("callFunds  {} fundList  {}  {}  {}", before, callFunds.state, callFunds.hitCount, callFunds.fundList.length);
			for(var e: callFunds.fundList) {
				String     isinCode      = e.isinCode;
				BigDecimal salesFee      = new BigDecimal(e.commission).movePointLeft(2).stripTrailingZeros();
				
				// sanity check
				if (!isinCodeSet.contains(isinCode)) {
					logger.error("Unpexpeced isinCode");
					logger.error("  {}  {}", e.isinCode, e.fundname);
					throw new UnexpectedException("Unpexpeced isinCode");
				}
				
				list.add(new TradingFundType(isinCode, salesFee));
			}
			
			// update before
			before += callFunds.fundList.length;
			// check exit condition
			int hitCount = Integer.valueOf(callFunds.hitCount);
			if (before == hitCount) break;
		}
	}
	
	private static void update() {
		var list = new ArrayList<TradingFundType>();

		updateList(list);
		
		logger.info("save  {}  {}", list.size(), StorageClick.TradingFundClick.getPath());
		StorageClick.TradingFundClick.save(list);
	}
	
	
	public static void main(String[] args) {
		logger.info("START");
		
		update();
				
		logger.info("STOP");
	}
}
