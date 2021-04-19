package yokwe.security.japan.test;

import java.io.IOException;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.slf4j.LoggerFactory;

import yokwe.util.StringUtil;
import yokwe.util.http.HttpUtil;

public class T002 {
	static final org.slf4j.Logger logger = LoggerFactory.getLogger(T002.class);
	
	// |2019110603/
	// /2002032802|
	//  1234567890
	private static final Pattern PAT = Pattern.compile("(\\||\\/)(?<fnc>[12][0-9]{9})(\\||\\/)");
	private static final StringUtil.MatcherFunction<String> OP = (m -> new String(m.group("fnc")));

	public static void main(String[] args) throws IOException {
		logger.info("START");
		
		HttpUtil httpUtil = HttpUtil.getInstance().withCharset("SHIFT_JIS");
		
		{
			String url = "http://www.morningstar.co.jp/common/js2016/fund_code.js";
			HttpUtil.Result result = httpUtil.download(url);
			
			List<String> fncList = StringUtil.find(result.result, PAT, OP).collect(Collectors.toList());
			logger.info("fncList {}", fncList.size());
		}
		
		logger.info("STOP");
	}
}
