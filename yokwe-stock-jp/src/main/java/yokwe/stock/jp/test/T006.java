package yokwe.security.japan.test;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.TreeMap;
import java.util.stream.Stream;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.slf4j.LoggerFactory;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

import yokwe.util.UnexpectedException;

public class T006 {
	static final org.slf4j.Logger logger = LoggerFactory.getLogger(T006.class);
	
	public static final String NS_XHTML = "http://www.w3.org/1999/xhtml";
	public static final String NS_IX    = "http://www.xbrl.org/2008/inlineXBRL";
	public static final String NS_TSE   = "http://www.xbrl.tdnet.info/taxonomy/jp/tse/tdnet/ed/t/2014-01-12";
	
	static class XMLElement {
		public static XMLElement getInstance(String path, String uri, String localName, String qName, Attributes attributes) {
			return new XMLElement(path, uri, localName, qName, attributes);
		}
		
		public final String path;
		public final String uri;
		public final String localName;
		public final String qName;
		
		public final Map<String, XMLAttribute> attributeMap;
		
		public final StringBuilder content;
		
		private XMLElement(String path, String uri, String localName, String qName, Attributes attributes) {
			this.path          = path;
			this.uri           = uri;
			this.localName     = localName;
			this.qName         = qName;
			this.attributeMap  = new TreeMap<>();
			this.content       = new StringBuilder();
			
			List<XMLAttribute> attributeList = XMLAttribute.getInstance(attributes);
			for(XMLAttribute xmlAttribute: attributeList) {
				String name = xmlAttribute.qName;
				
				if (attributeMap.containsKey(name)) {
					logger.error("duplicate name {}", name);
					logger.error("old {}", attributeMap.get(name));
					logger.error("new {}", xmlAttribute);
					throw new UnexpectedException("duplicate name");
				} else {
					attributeMap.put(name, xmlAttribute);
				}
			}
		}
		
		public void characters (char ch[], int start, int length) {
			String chars = new String(ch);
			content.append(chars.substring(start, start + length));
		}
		
		@Override
		public String toString() {
//			return String.format("{%s %s %s}", uri, localName, attributeList);
			return String.format("{%s \"%s\" %s}", path, content.toString(), attributeMap);
		}
	}
	static class XMLAttribute {
		static List<XMLAttribute> getInstance(Attributes attributes) {
			int length = attributes.getLength();
			
			List<XMLAttribute> ret = new ArrayList<>(length);
			for(int i = 0; i < length; i++) {
				String localName = attributes.getLocalName(i);
				String qName     = attributes.getQName(i);
				String type      = attributes.getType(i);
				String uri       = attributes.getURI(i);
				String value     = attributes.getValue(i);
				XMLAttribute xmlAttribute = new XMLAttribute(localName, qName, type, uri, value);
				
				ret.add(xmlAttribute);
			}
			return ret;
		}
		final String localName;
		final String qName;
		final String type;
		final String uri;
		final String value;
		
		private XMLAttribute(String localName, String qName, String type, String uri, String value) {
			this.localName = localName;
			this.qName     = qName;
			this.type      = type;
			this.uri       = uri;
			this.value     = value;
		}
		
		@Override
		public String toString() {
//			return String.format("{%s = \"%s\"}", qName, value);
			return String.format("\"%s\"", value);
		}
	}
		
	private static class SAXHandler extends DefaultHandler {
		final Stream.Builder<XMLElement> builder;
		final Stack<XMLElement>          xmlElementStack;
		final Stack<String>              nameStack;
		
		SAXHandler(Stream.Builder<XMLElement> builder) {
			this.builder         = builder;
			this.xmlElementStack = new Stack<>();
			this.nameStack       = new Stack<>();
		}
		
		@Override
		public void startElement (String uri, String localName, String qName, Attributes attributes) {
			nameStack.push(qName);
			
			String path = String.join("/", nameStack);
			XMLElement xmlElement = XMLElement.getInstance(path, uri, localName, qName, attributes);
			
			xmlElementStack.push(xmlElement);
		}
		@Override
		public void endElement (String uri, String localName, String qName) {
			XMLElement xmlElement = xmlElementStack.pop();
			nameStack.pop();
			
//			logger.info("{}", xmlElement);
			builder.accept(xmlElement);
		}
		@Override
		public void characters (char ch[], int start, int length) {
			XMLElement xmlElement = xmlElementStack.peek();
			xmlElement.characters(ch, start, length);
		}
		@Override
		public void warning (SAXParseException e) {
			String exceptionName = e.getClass().getSimpleName();
			logger.error("{} {}", exceptionName, e);
			throw new UnexpectedException(exceptionName, e);
		}
		@Override
		public void error (SAXParseException e) {
			String exceptionName = e.getClass().getSimpleName();
			logger.error("{} {}", exceptionName, e);
			throw new UnexpectedException(exceptionName, e);
		}
		@Override
		public void fatalError (SAXParseException e) {
			String exceptionName = e.getClass().getSimpleName();
			logger.error("{} {}", exceptionName, e);
			throw new UnexpectedException(exceptionName, e);
		}
	}
	
	public static Stream<XMLElement> buildStream(InputStream in) {
		Stream.Builder<XMLElement> builder = Stream.builder();
		
		SAXParserFactory saxParserFactory = SAXParserFactory.newInstance();
		saxParserFactory.setNamespaceAware(true);
		
		SAXHandler handler = new SAXHandler(builder);

		try {
			SAXParser parser = saxParserFactory.newSAXParser();
			parser.parse(in, handler);
		} catch (ParserConfigurationException | SAXException | IOException e) {
			String exceptionName = e.getClass().getSimpleName();
			logger.error("{} {}", exceptionName, e);
			throw new UnexpectedException(exceptionName, e);
		}

		builder.accept(null);
		
		return builder.build();
	}


	
	public static void main(String[] args) throws IOException {
		logger.info("START");
		
		String path = "tmp/tse-qcedjpsm-42340-20191125430315-ixbrl.htm";
		
		File file = new File(path);
		InputStream is = new FileInputStream(file);

		buildStream(is).forEach(o -> logger.info("XX {}", o));
				
		logger.info("STOP");
	}
}
