package yokwe.stock.jp.sony;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import yokwe.util.UnexpectedException;
import yokwe.util.http.HttpUtil;

public class GenSourceFile {
	private static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();

	public static void main(String[] args) {
    	logger.info("START");

//    	GenearateJSONStub.generate("yokwe.security.japan.sony.json", "Screener", new File("tmp/screener.json"));
//    	GenearateJSONStub.generate("yokwe.security.japan.sony.json", "Security", new File("tmp/F000005MIQ.json"));
//    	GenearateJSONStub.generate("yokwe.security.japan.sony.json", "Price",    new File("tmp/F000000MU9-price.json"));
//    	GenearateJSONStub.generate("yokwe.security.japan.sony.json", "Dividend", new File("tmp/F000000MU9-div.json"));
    	
    	{
    		HttpUtil.Result result = HttpUtil.getInstance().withCharset("MS932").download(UpdateFund.URL_FUND_LIST);
			logger.info("result {} {} {} {}", result.code, result.reasonPhrase, result.version, result.rawData.length);
			
    		String jsonString;
    		{
    			Pattern p = Pattern.compile(";FundData= (?<json>\\{.+\\});");
    			Matcher m = p.matcher(result.result);
    			if (m.find()) {
    				jsonString = m.group("json");
    			} else {
    				throw new UnexpectedException("no match PAT_FUNDDATA");
    			}
    		}

        	GenearateJSONStub.generate("yokwe.stock.jp.sony.json", "FundData", jsonString);
    	}
        
    	logger.info("STOP");
    }

}
