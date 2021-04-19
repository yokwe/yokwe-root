package yokwe.security.japan.test;

import java.io.IOException;
import java.util.Map;

import org.slf4j.LoggerFactory;

import yokwe.util.http.HttpUtil;

public class T001 {
	static final org.slf4j.Logger logger = LoggerFactory.getLogger(T001.class);
	
	public static void main(String[] args) throws IOException {
		logger.info("START");
		
		HttpUtil httpUtil = HttpUtil.getInstance().withCharset("SHIFT_JIS");

		{
			String url = "http://www.morningstar.co.jp/FundData/DownloadStdYmd.do?fnc=2010122702&selectStdDayFrom=8&selectStdDayTo=8&selectStdMonthFrom=11&selectStdMonthTo=11&selectStdYearFrom=2018&selectStdYearTo=2019";
			HttpUtil.Result result = httpUtil.download(url);
			for(Map.Entry<String, String> entry: result.headerMap.entrySet()) {
				logger.info("header {} = {}", entry.getKey(), entry.getValue());
			}
			logger.info("data = {}", result.result);
		}
		
		logger.info("STOP");
	}
}
