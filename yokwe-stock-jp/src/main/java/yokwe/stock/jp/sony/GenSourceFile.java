package yokwe.stock.jp.sony;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import yokwe.stock.jp.smbctb.GenearateJSONStub;
import yokwe.util.UnexpectedException;
import yokwe.util.http.HttpUtil;

public class GenSourceFile {
	static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(GenSourceFile.class);

	public static void main(String[] args) {
    	logger.info("START");

//    	GenearateJSONStub.generate("yokwe.security.japan.sony.json", "Screener", new File("tmp/screener.json"));
//    	GenearateJSONStub.generate("yokwe.security.japan.sony.json", "Security", new File("tmp/F000005MIQ.json"));
//    	GenearateJSONStub.generate("yokwe.security.japan.sony.json", "Price",    new File("tmp/F000000MU9-price.json"));
//    	GenearateJSONStub.generate("yokwe.security.japan.sony.json", "Dividend", new File("tmp/F000000MU9-div.json"));
    	
    	{
    		String url = "https://moneykit.net/data/fund/SFBA1700F471.js";
    		HttpUtil.Result result = HttpUtil.getInstance().withCharset("MS932").download(url);
			logger.info("result {} {} {} {}", result.code, result.reasonPhrase, result.version, result.rawData.length);
//			FileUtil.write().file("tmp/a", result.result); // FIXME
			
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
//			FileUtil.write().file("tmp/b", jsonString); // FIXME

        	GenearateJSONStub.generate("yokwe.security.japan.sony.json", "FundData", jsonString);
    	}
 //   	{
//    		String url = "https://apl.morningstar.co.jp/xml/chart/funddata/JP90C000FZP8.xml";
//    		HttpUtil.Result result = HttpUtil.getInstance().withCharset("MS932").download(url);
//			logger.info("result {} {} {} {}", result.code, result.reasonPhrase, result.version, result.rawData.length);
//			FileUtil.write().file("tmp/a", result.result); // FIXME
//
//    		
//    	}
        
    	logger.info("STOP");
    }

}
