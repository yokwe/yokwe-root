package yokwe.stock.jp.xbrl.tdnet.inline;

import java.util.Set;
import java.util.TreeSet;

import yokwe.stock.jp.xbrl.XML;
import yokwe.util.UnexpectedException;
import yokwe.util.xml.Attribute;
import yokwe.util.xml.Element;
import yokwe.util.xml.QValue;

public class StringValue extends InlineXBRL {
	public static Set<QValue> validAttributeSet = new TreeSet<>();
	static {
		validAttributeSet.add(new QValue("", "contextRef"));
		validAttributeSet.add(new QValue("", "name"));
		validAttributeSet.add(new QValue("", "format"));
		validAttributeSet.add(XML.XSI_NIL);
		//
		validAttributeSet.add(new QValue("", "escape"));
	}
	
	public final String escape;
	public final String stringValue;
	
	public StringValue(Element element) {
		super(Kind.STRING, element);
		this.escape = element.getAttributeOrNull("escape");
		
		if (isNull) {
			this.stringValue = null;
		} else {
			if (qFormat == null) {
				this.stringValue = element.content;
			} else {
				logger.error("Unexpected format", value);
				logger.error("  format  {}", format);
				logger.error("  qFormat {}", qFormat);
				throw new UnexpectedException("Unexpected format");
			}
		}
		
		// Sanity check
		for(Attribute attribute: element.attributeList) {
			QValue value = new QValue(attribute);
			if (validAttributeSet.contains(value)) continue;
			logger.error("Unexpected attribute {}", attribute.name);
			logger.error("element {}!", element);
			throw new UnexpectedException("Unexpected attribute");
		}
	}
	
	@Override
	public String toString() {
		if (isNull) {
			if (format == null) {
				return String.format("{STRING %s %s *NULL*}", name, contextSet);
			} else {
				return String.format("{STRING %s %s %s *NULL*}", name, contextSet, format);
			}
		} else {
			if (format == null) {
				return String.format("{STRING %s %s \"%s\"}", name, contextSet, stringValue);
			} else {
				return String.format("{STRING %s %s %s \"%s\"", name, contextSet, format, stringValue);
			}
		}
	}
}