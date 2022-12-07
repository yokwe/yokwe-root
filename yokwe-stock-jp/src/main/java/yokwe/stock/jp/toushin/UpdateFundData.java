package yokwe.stock.jp.toushin;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.StringJoiner;

import yokwe.util.UnexpectedException;
import yokwe.util.http.HttpUtil;
import yokwe.util.json.JSON;

public class UpdateFundData {
	private static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();
	
	private static FundDataSearch getFundDataSearch(int startNo, int draw) {
		String url = "https://toushin-lib.fwg.ne.jp/FdsWeb/FDST999900/fundDataSearch";

		Map<String, String> bodyMap = new LinkedHashMap<>();
		
		bodyMap.put("t_keyword",                "");
		bodyMap.put("t_kensakuKbn",             "1");
		bodyMap.put("t_fundCategory",           "");
		bodyMap.put("s_keyword",                "");
		bodyMap.put("s_kensakuKbn",             "1");
		bodyMap.put("s_supplementKindCd",       "1");
		bodyMap.put("s_standardPriceCond1",     "0");
		bodyMap.put("s_standardPriceCond2",     "0");
		bodyMap.put("s_riskCond1",              "0");
		bodyMap.put("s_riskCond2",              "0");
		bodyMap.put("s_sharpCond1",             "0");
		bodyMap.put("s_sharpCond2",             "0");
		bodyMap.put("s_buyFee",                 "1");
		bodyMap.put("s_trustReward",            "1");
		bodyMap.put("s_monthlyCancelCreateVal", "1");
		bodyMap.put("s_instCd",                 "");
		bodyMap.put("salesInstDiv",             "");
		bodyMap.put("s_fdsInstCd",              "");
		bodyMap.put("startNo",                  Integer.toString(startNo));
		bodyMap.put("draw",                     Integer.toString(draw));
		bodyMap.put("searchBtnClickFlg",        "false");
		
		final String body;
		{
			StringJoiner sj = new StringJoiner("&");		
			for(var entry: bodyMap.entrySet()) {
				sj.add(entry.getKey() + "=" + entry.getValue());
			}
			body = sj.toString();
		}

		String contentType = "application/x-www-form-urlencoded;charset=UTF-8";

		HttpUtil.Result result = HttpUtil.getInstance().withPost(body, contentType).download(url);
		if (result.result == null) {
			logger.error("Dowload failed");
			logger.error("  url    {}", url);
			logger.error("  result {} {}", result.code, result.reasonPhrase);
			throw new UnexpectedException("Dowload failed");
		}
		return JSON.unmarshal(FundDataSearch.class, result.result);
	}
	
	public static void update() {
		var list = new ArrayList<FundData>();
		
		final int allPageNo;
		final int pageSize;
		int startNo = 0;
		int draw    = 1;
		{
			var fundData = getFundDataSearch(startNo, draw);
			logger.info("allPageNo       {}", fundData.allPageNo);
			logger.info("recordsTotal    {}", fundData.recordsTotal);
			allPageNo = fundData.allPageNo;
			pageSize = fundData.pageSize;
			
			for(var e: fundData.resultInfoArray) {
				String isinCode = e.isinCd;
				String name     = e.fundNm;
				
				list.add(new FundData(isinCode, name));
			}
		}
		
		for(int i = 1; i < allPageNo; i++) {
			if ((i % 10) == 0) logger.info("download {}", String.format("%4d / %4d", i, allPageNo));
			startNo += pageSize;
			draw++;
			var fundData = getFundDataSearch(startNo, draw);			
			for(var e: fundData.resultInfoArray) {
				String isinCode = e.isinCd;
				String name     = e.fundNm;
				
				list.add(new FundData(isinCode, name));
			}
		}
		
		logger.info("save {} {}", list.size(), FundData.getPath());
		FundData.save(list);
	}
	
	public static void main(String[] args) {
		logger.info("START");
		
		update();
		
		logger.info("STOP");
	}
}
