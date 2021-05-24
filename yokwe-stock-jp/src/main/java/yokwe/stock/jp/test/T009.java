package yokwe.security.japan.test;

import java.io.IOException;
import java.io.StringReader;

import jakarta.xml.bind.JAXB;

import org.slf4j.LoggerFactory;

import yokwe.security.japan.xsd.Schema;
import yokwe.util.FileUtil;

public class T009 {
	static final org.slf4j.Logger logger = LoggerFactory.getLogger(T009.class);
	
	private static final String PATH_DATA = "tmp/data/61_taxonomy/tse-ed-2014-01-12/taxonomy/jp/tse/tdnet/ed/t/2014-01-12/tse-ed-t-2014-01-12.xsd";

	public static void main(String[] args) throws IOException {
		logger.info("START");
		
		String data = FileUtil.read().file(PATH_DATA);
		
		Schema schema = JAXB.unmarshal(new StringReader(data), Schema.class);
		logger.info("schema {}", schema);

		logger.info("STOP");
	}
}
