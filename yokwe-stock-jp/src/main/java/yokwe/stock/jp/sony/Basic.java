package yokwe.stock.jp.sony;

import java.io.ByteArrayInputStream;

import javax.xml.bind.JAXB;

import org.slf4j.LoggerFactory;

import yokwe.util.StringUtil;
import yokwe.util.http.HttpUtil;

public class Basic {
	private static final org.slf4j.Logger logger = LoggerFactory.getLogger(Basic.class);

	public static void main(String[] args) {
		logger.info("START");
		
		String url = "https://apl.morningstar.co.jp/webasp/funddataxml/basic/basic_201306280D.xml";
		HttpUtil.Result result = HttpUtil.getInstance().withRawData(true).download(url);
		logger.info("result {} {} {} {}", result.code, result.reasonPhrase, result.version, result.rawData.length);

		byte[] byetArray = result.rawData;
		logger.info("byetArray {}!", byetArray.length);
		
		yokwe.stock.jp.sony.xml.Basic basic = JAXB.unmarshal(new ByteArrayInputStream(byetArray), yokwe.stock.jp.sony.xml.Basic.class);
		logger.info("basic {}", StringUtil.toString(basic));
		
		logger.info("STOP");
	}
}
