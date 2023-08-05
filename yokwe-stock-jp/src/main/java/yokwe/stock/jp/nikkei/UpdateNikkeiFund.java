package yokwe.stock.jp.nikkei;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import yokwe.stock.jp.toushin.Fund;
import yokwe.util.http.HttpUtil;

public class UpdateNikkeiFund {
	private static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();
	
	private static String getURL(String fundCode) {
		return String.format("https://www.nikkei.com/nkd/fund/dividend/?fcode=%s", fundCode);
	}
	
	private static void sleep(long millis) {
		try {
			Thread.sleep(millis);
		} catch (InterruptedException e) {
			//
		}
	}
	
	private static String fromPercentString(String string) {
		String numericString = string.trim().replace("%", "");
		return numericString.compareTo("--") == 0 ? "" : new BigDecimal(numericString).movePointLeft(2).toPlainString();
	}
	
	private static final Map<String, Fund> fundMap = Fund.getList().stream().collect(Collectors.toMap(o -> o.fundCode, o -> o));
	//                       fundCode
	

	private static void process(List<String> processList, List<String> retryList, List<String> skipList, List<NikkeiFund> nikkeiFundList) {
		logger.info("process  {}  {}  {}", fundMap.size(), retryList.size(), nikkeiFundList.size(), processList.size());
		
		Set<String> nikkeiSet = nikkeiFundList.stream().map(o -> o.fundCode).collect(Collectors.toSet());
		Set<String> skipSet = skipList.stream().collect(Collectors.toSet());
		
		int count = 0;
		int processed = 0;
		for(var fundCode: processList) {
			count++;
			if ((count % 100) == 1) logger.info("{}", String.format("%4d / %4d", count, processList.size()));
			
			// already processed
			if (nikkeiSet.contains(fundCode)) continue;
			// skip
			if (skipSet.contains(fundCode)) continue;
			
			Fund fund = fundMap.get(fundCode);
			
			NikkeiFund nikkeiFund = new NikkeiFund();
			nikkeiFund.isinCode    = fund.isinCode;
			nikkeiFund.fundCode    = fund.fundCode;
			nikkeiFund.name        = fund.name;
			nikkeiFund.divScore1Y  = "";
			nikkeiFund.divScore3Y  = "";
			nikkeiFund.divScore5Y  = "";
			nikkeiFund.divScore10Y = "";

			{
				String url = getURL(fundCode);
				HttpUtil.Result result = HttpUtil.getInstance().download(url);
				if (result == null) {
					logger.warn("{}  {}  result is null", fundCode, count);
				} else if (result.result == null) {
					logger.warn("{}  {}  result.result is null  {}  {}", fundCode, count, result.code, result.reasonPhrase);
					retryList.add(fundCode);
					continue;
				} else {
					final String page = result.result;
					
					var divScore = DivScore.getInstance(page);
					if (divScore == null) {
						logger.warn("{}  divScore is null", fundCode);
						retryList.add(fundCode);
						continue;
					}
					
					nikkeiFund.divScore1Y  = fromPercentString(divScore.score1Y);
					nikkeiFund.divScore3Y  = fromPercentString(divScore.score3Y);
					nikkeiFund.divScore5Y  = fromPercentString(divScore.score5Y);
					nikkeiFund.divScore10Y = fromPercentString(divScore.score10Y);
				}
			}
			
			nikkeiFundList.add(nikkeiFund);
			processed++;
			if ((processed % 100) == 0) {
				logger.info("save  {}  {}", nikkeiFundList.size(), NikkeiFund.getPath());
				NikkeiFund.save(nikkeiFundList);
			}
		}
	}
	
	
	public static void main(String[] args) {
		logger.info("START");
		
		List<String>     processList  = new ArrayList<>(fundMap.keySet());
		List<String>     retryList = new ArrayList<>();
		List<String>     skipList  = new ArrayList<>();
		List<NikkeiFund> nikkeiFundList  = NikkeiFund.getList();
		logger.info("nikkeiFundList  {}", nikkeiFundList.size());
		
		
		for(;;) {
			Collections.shuffle(processList); // shuffle processList
			process(processList, retryList, skipList, nikkeiFundList);
			if (retryList.isEmpty()) break;
			
			logger.info("retryList  {}", retryList.size());
			logger.info("skipList   {}", skipList.size());
			
			processList.clear();
			processList.addAll(retryList);
			retryList.clear();
			sleep(1000);
		}
		
		logger.info("save  {}  {}", nikkeiFundList.size(), NikkeiFund.getPath());
		NikkeiFund.save(nikkeiFundList);
		
		logger.info("STOP");
	}

}
