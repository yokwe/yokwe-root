package yokwe.security.japan.test;

import java.io.File;
import java.io.IOException;

import org.slf4j.LoggerFactory;

import yokwe.security.japan.xbrl.inline.InlineXBRL;
import yokwe.util.xml.XMLStream;

public class T008 {
	static final org.slf4j.Logger logger = LoggerFactory.getLogger(T008.class);
	
	public static void main(String[] args) throws IOException {
		logger.info("START");
		
		File dir = new File("tmp/TD2019111900049/XBRLData/Summary");
//		File dir = new File("tmp/TD2019111900049/XBRLData/Attachment");
		
		for(File file: dir.listFiles(o -> o.getName().endsWith("htm"))) {
			logger.info("==== {}", file.getName());
//			XMLUtil.buildStream(file).filter(o -> o.qName.startsWith("ix:header")).forEach(o -> logger.info("{}", o));
//			XMLUtil.buildStream(file).filter(o -> o.qName.startsWith("ix:")).filter(o -> !o.qName.equals("ix:header")).filter(o -> !o.qName.equals("ix:resources")).filter(o -> !o.qName.equals("ix:hidden")).filter(o -> !o.qName.equals("ix:references")).forEach(o -> logger.info("{}", Value.getInstance(o)));
			
			XMLStream.buildStream(file).filter(o -> InlineXBRL.canGetInstance(o)).forEach(o -> logger.info("{}", InlineXBRL.getInstance(o)));
		}
				
		logger.info("STOP");
	}
}
