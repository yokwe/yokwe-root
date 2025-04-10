package yokwe.stock.jp.sony;

import java.io.ByteArrayInputStream;

import jakarta.xml.bind.JAXB;
import yokwe.util.ToString;
import yokwe.util.http.HttpUtil;

public class Basic {
	private static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();

	public static void main(String[] args) {
		logger.info("START");
		
		String url = "https://apl.morningstar.co.jp/webasp/funddataxml/basic/basic_201306280D.xml";
		HttpUtil.Result result = HttpUtil.getInstance().withRawData(true).download(url);
		logger.info("result {} {} {} {}", result.code, result.reasonPhrase, result.version, result.rawData.length);

		byte[] byetArray = result.rawData;
		logger.info("byetArray {}!", byetArray.length);
		
		yokwe.stock.jp.sony.xml.Basic basic = JAXB.unmarshal(new ByteArrayInputStream(byetArray), yokwe.stock.jp.sony.xml.Basic.class);
		logger.info("basic {}", ToString.withFieldName(basic));
		
		logger.info("STOP");
	}
}
