package yokwe.stock.jp.xbrl.inline;

import java.util.Set;
import java.util.TreeSet;

import yokwe.stock.jp.xbrl.XBRL;
import yokwe.util.UnexpectedException;
import yokwe.util.xml.Attribute;
import yokwe.util.xml.Element;
import yokwe.util.xml.QValue;

public class BooleanElement extends BaseElement {
	public static Set<QValue> validAttributeSet = new TreeSet<>();
	static {
		validAttributeSet.add(new QValue("", "contextRef"));
		validAttributeSet.add(new QValue("", "name"));
		validAttributeSet.add(new QValue("", "format"));
		//
		validAttributeSet.add(new QValue("", "escape"));
	}
	public final String  escape;
	public final Boolean booleanValue;
	
	public BooleanElement(Element element) {
		super(Kind.BOOLEAN, element);
		
		this.escape = element.getAttributeOrNull("escape");
		
		if (isNull) {
			this.booleanValue = null;
		} else {
			if (this.qFormat.equals(XBRL.IXT_BOOLEAN_FALSE)) {
				this.booleanValue = false;
			} else if (qFormat.equals(XBRL.IXT_BOOLEAN_TRUE)) {
				this.booleanValue = true;
			} else {
				logger.error("Unexpected format");
				logger.error("  element  {}", element);
				logger.error("  qFormat {}", qFormat);
				throw new UnexpectedException("Unexpected format");
			}
		}
		
		// Sanity check
		for(Attribute attribute: element.attributeList) {
			QValue value = new QValue(attribute);
			if (validAttributeSet.contains(value)) continue;
			logger.error("Unexpected attribute {}!", value);
			logger.error("attribute {}!", attribute.name);
			logger.error("element {}!", element);
			throw new UnexpectedException("Unexpected attribute");
		}
	}
	@Override
	public String toString() {
		if (isNull) {
			return String.format("{BOOLEAN %s %s %s *NULL*}", name, contextSet, escape);
		} else {
			return String.format("{BOOLEAN %s %s %s %s}", name, contextSet, escape, booleanValue);
		}
	}
}