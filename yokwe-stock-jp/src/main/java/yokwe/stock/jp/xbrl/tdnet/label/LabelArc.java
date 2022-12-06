package yokwe.stock.jp.xbrl.tdnet.label;

import jakarta.xml.bind.Unmarshaller;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlEnum;
import jakarta.xml.bind.annotation.XmlEnumValue;
import jakarta.xml.bind.annotation.XmlType;
import yokwe.stock.jp.xbrl.XML;
import yokwe.util.UnexpectedException;

// This arc role value is for use on a <labelArc> from a concept Locator (<loc> element) to a <label> element
// and it indicates that the label conveys human-readable information about the Concept.
public class LabelArc {
	private static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();

	@XmlEnum
	@XmlType(name="LabelArc-Type")
	public enum Type {
		@XmlEnumValue("arc")
		ARC,
	}

	@XmlEnum
	@XmlType(name="LabelArc-ArcRole")
	public enum ArcRole {
		@XmlEnumValue("http://www.xbrl.org/2003/arcrole/concept-label")
		CONCETPT_LABEL,
	}

	// xlink:arcrole must be "http://www.xbrl.org/2003/arcrole/concept-label"
	@XmlAttribute(name = "type", namespace = XML.NS_XLINK, required = true)
	public Type type;
	
	// xlink:arcrole must be "http://www.xbrl.org/2003/arcrole/concept-label"
	@XmlAttribute(name = "arcrole", namespace = XML.NS_XLINK, required = true)
	public ArcRole arcRole;
	
	// The @xlink:from attribute on an Arc MUST be equal to the value of an @xlink:label attribute of Locator.
	@XmlAttribute(name = "from", namespace = XML.NS_XLINK, required = true)
	public String from;
	
	// The @xlink:to attribute on an Arc MUST be equal to the value of an @xlink:label attribute of Label.
	@XmlAttribute(name = "to", namespace = XML.NS_XLINK, required = true)
	public String to;
	
	@Override
	public String toString() {
		return String.format("{%s %s %s %s}", type, arcRole, from, to);
	}
	
	void afterUnmarshal(Unmarshaller u, Object parent) {
		// Sanity check
		if (type == null) {
			logger.error("type is null {}", this);
			throw new UnexpectedException("type is null");
		}
		if (arcRole == null) {
			logger.error("arcRole is null {}", this);
			throw new UnexpectedException("arcRole is null");
		}
	}
}

//<linkbase xmlns="http://www.xbrl.org/2003/linkbase" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:xlink="http://www.w3.org/1999/xlink" xmlns:xbrli="http://www.xbrl.org/2003/instance">
//<labelLink xlink:type="extended" xlink:role="http://www.xbrl.org/2003/role/link">
//  <loc xlink:type="locator" xlink:href="tse-ed-t-2014-01-12.xsd#tse-ed-t_AmountChangeGrossOperatingRevenues" xlink:label="AmountChangeGrossOperatingRevenues"/>
//  <label xlink:type="resource" xlink:label="label_AmountChangeGrossOperatingRevenues" xlink:role="http://www.xbrl.org/2003/role/label" xml:lang="ja" id="label_AmountChangeGrossOperatingRevenues">増減額</label>
//  <labelArc xlink:type="arc" xlink:arcrole="http://www.xbrl.org/2003/arcrole/concept-label" xlink:from="AmountChangeGrossOperatingRevenues" xlink:to="label_AmountChangeGrossOperatingRevenues"/>
