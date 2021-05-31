package yokwe.stock.jp.xbrl.tdnet.label;

import jakarta.xml.bind.Unmarshaller;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlEnum;
import jakarta.xml.bind.annotation.XmlEnumValue;
import jakarta.xml.bind.annotation.XmlType;
import yokwe.stock.jp.xbrl.XML;
import yokwe.util.UnexpectedException;

// Locators are child elements of an Extended Link that point to resources external to the extended link itself.
public class Loc {
	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(Loc.class);

	@XmlEnum
	@XmlType(name="Loc-Type")
	public enum Type {
		@XmlEnumValue("locator")
		LOCATOR,
	}

	// The @xlink:type attribute MUST occur on all Locators and MUST have the fixed content "locator".
	@XmlAttribute(name = "type", namespace = XML.NS_XLINK, required = true)
	public Type type;  // type must be "locator"
	
	// A Locator MUST have an @xlink:href attribute. The @xlink:href attribute MUST be a URI.
	// The URI MUST point to an XML document or to one or more XML fragments within an XML document.
	@XmlAttribute(name = "href", namespace = XML.NS_XLINK, required = true)
	public String href;
	
	// The @xlink:label attribute on a Locator identifies the locator so that Arcs in the same Extended Link can reference it.
	@XmlAttribute(name = "label", namespace = XML.NS_XLINK, required = true)
	public String label;
	
	@Override
	public String toString() {
		return String.format("{%s %s}", href, label);
	}
	
	void afterUnmarshal(Unmarshaller u, Object parent) {
		// Sanity check
		if (type == null) {
			logger.error("type is null {}", this);
			throw new UnexpectedException("type is null");
		}
	}
}

//<linkbase xmlns="http://www.xbrl.org/2003/linkbase" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:xlink="http://www.w3.org/1999/xlink" xmlns:xbrli="http://www.xbrl.org/2003/instance">
//<labelLink xlink:type="extended" xlink:role="http://www.xbrl.org/2003/role/link">
//<loc xlink:type="locator" xlink:href="tse-ed-t-2014-01-12.xsd#tse-ed-t_AmountChangeGrossOperatingRevenues" xlink:label="AmountChangeGrossOperatingRevenues"/>
//<label xlink:type="resource" xlink:label="label_AmountChangeGrossOperatingRevenues" xlink:role="http://www.xbrl.org/2003/role/label" xml:lang="ja" id="label_AmountChangeGrossOperatingRevenues">増減額</label>
//<labelArc xlink:type="arc" xlink:arcrole="http://www.xbrl.org/2003/arcrole/concept-label" xlink:from="AmountChangeGrossOperatingRevenues" xlink:to="label_AmountChangeGrossOperatingRevenues"/>
