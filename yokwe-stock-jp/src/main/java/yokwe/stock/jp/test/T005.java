package yokwe.security.japan.test;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.slf4j.LoggerFactory;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

import yokwe.util.UnexpectedException;

public class T005 {
	static final org.slf4j.Logger logger = LoggerFactory.getLogger(T005.class);
	
	public static final String NS_IX  = "http://www.xbrl.org/2008/inlineXBRL";
	public static final String NS_TSE = "http://www.xbrl.tdnet.info/taxonomy/jp/tse/tdnet/ed/t/2014-01-12";
	
	
	private static class MyHandler extends DefaultHandler {
		static String toString(Attributes attributes) {
			List<String> ret = new ArrayList<>();
			
			int length = attributes.getLength();
			for(int i = 0; i < length; i++) {
//				String localName = attributes.getLocalName(i);
				String qName     = attributes.getQName(i);
//				String type      = attributes.getType(i);
//				String uri       = attributes.getURI(i);
				String value     = attributes.getValue(i);
				
//				ret.add(String.format("{%s = %s = %s = %s = %s}", localName, qName, type, uri, value));
				ret.add(String.format("{%s = %s}", qName, value));
			}
			
			return ret.toString();
		}
		
		int level = 0;
		String spaces = "                                                                            ";
		String pad = "";
		private void nest() {
			level++;
			pad = spaces.substring(0,  level * 2);
		}
		private void unnest() {
			level--;
			pad = spaces.substring(0,  level * 2);
		}
		
		@Override
	    public void startDocument () {
			logger.info("{}startDocument", pad);
		}
		@Override
	    public void endDocument () {
			logger.info("{}endDocument", pad);
		}
		@Override
	    public void startPrefixMapping (String prefix, String uri) {
//			logger.info("startPrefixMapping  {} = {}", prefix, uri);
		}
		@Override
		public void endPrefixMapping (String prefix) {
//			logger.info("endPrefixMapping  {}", prefix);
		}
		@Override
		public void startElement (String uri, String localName, String qName, Attributes attributes) {
//			logger.info("{}startElement {} = {} = {} = {}", pad, uri, localName, qName, toString(attributes));
			logger.info("{}startElement {} = {}", pad, qName, toString(attributes));

			nest();
		}
		@Override
		public void endElement (String uri, String localName, String qName) {
			unnest();
			
//			logger.info("endElement {} = {} = {}", uri, localName, qName);
			logger.info("{}endElement", pad);
		}
		@Override
		public void characters (char ch[], int start, int length) {
			String chars = new String(ch);
			logger.info("{}characters \"{}\"", pad, chars.substring(start, start + length));
		}
		@Override
		public void ignorableWhitespace (char ch[], int start, int length) {
			//
		}
		@Override
		public void processingInstruction (String target, String data) {
//			logger.info("{}processingInstruction {} = {}", pad, target, data);
		}
		@Override
		public void skippedEntity (String name) {
//			logger.info("{}skippedEntity {}", pad, name);
		}
		@Override
		public void warning (SAXParseException e) {
			logger.info("warn {}", e.toString());
		}
		@Override
		public void error (SAXParseException e) {
			logger.info("errro {}", e.toString());
		}
		@Override
		public void fatalError (SAXParseException e) {
			logger.info("fatalError {}", e.toString());
		}
	}
	
	
	public static void main(String[] args) throws IOException {
		logger.info("START");
		
		String path = "tmp/tse-qcedjpsm-42340-20191125430315-ixbrl.htm";
		
		File file = new File(path);
		InputStream in = new FileInputStream(file);

		SAXParserFactory saxParserFactory = SAXParserFactory.newInstance();
		saxParserFactory.setNamespaceAware(true);
		
		MyHandler handler = new MyHandler();
		
		SAXParser parser;
		try {
			parser = saxParserFactory.newSAXParser();
			parser.parse(in, handler);
		} catch (ParserConfigurationException | SAXException e) {
			String exceptionName = e.getClass().getSimpleName();
			logger.error("{} {}", exceptionName, e);
			throw new UnexpectedException(exceptionName, e);
		}

		
		logger.info("STOP");
	}
}
