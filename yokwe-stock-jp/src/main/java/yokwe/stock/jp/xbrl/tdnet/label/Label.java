package yokwe.stock.jp.xbrl.tdnet.label;

import jakarta.xml.bind.Unmarshaller;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlEnum;
import jakarta.xml.bind.annotation.XmlEnumValue;
import jakarta.xml.bind.annotation.XmlType;
import jakarta.xml.bind.annotation.XmlValue;
import yokwe.stock.jp.xbrl.XBRL;
import yokwe.stock.jp.xbrl.XML;
import yokwe.util.UnexpectedException;

// Documentation of XBRL Concepts MUST be contained in <label> element.
public class Label {
	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(Label.class);

	@XmlEnum
	@XmlType(name="Label-Type")
	public enum Type {
		@XmlEnumValue("resource")
		RESOURCE,
	}

	@XmlEnum
	@XmlType(name="Label-Role")
	public enum Role {
		@XmlEnumValue(XBRL.ROLE_LABLE)
		LABLE(XBRL.ROLE_LABLE),
		@XmlEnumValue(XBRL.ROLE_TERSE_LABEL)
		TERSE_LABEL(XBRL.ROLE_TERSE_LABEL),
		@XmlEnumValue(XBRL.ROLE_VERBOSE_LABEL)
		VERBOSE_LABEL(XBRL.ROLE_VERBOSE_LABEL),
		@XmlEnumValue(XBRL.ROLE_QUARTERLY_VERBOSE_LABEL)
		QUARTERLY_VERBOSE_LABEL(XBRL.ROLE_QUARTERLY_VERBOSE_LABEL),
		@XmlEnumValue(XBRL.ROLE_INTERIM_VERBOSE_LABEL)
		INTERIM_VERBOSE_LABEL(XBRL.ROLE_INTERIM_VERBOSE_LABEL),
		@XmlEnumValue(XBRL.ROLE_NON_CONSOLIDATED_LABEL)
		NON_CONSOLIDATED_LABEL(XBRL.ROLE_NON_CONSOLIDATED_LABEL),
		@XmlEnumValue(XBRL.ROLE_NON_CONSOLIDATED_VERBOSELABEL)
		NON_CONSOLIDATED_VERBOSELABEL(XBRL.ROLE_NON_CONSOLIDATED_VERBOSELABEL),
		@XmlEnumValue(XBRL.ROLE_QUARTERLYLABEL)
		QUARTERLYLABEL(XBRL.ROLE_QUARTERLYLABEL),
		@XmlEnumValue(XBRL.ROLE_INTERLIM_LABEL)
		INTERLIM_LABEL(XBRL.ROLE_INTERLIM_LABEL);
		
		public final String value;
		Role(String value) {
			this.value = value;
		}
	}
	
	@XmlEnum
	@XmlType(name="Label-Lang")
	public enum Lang {
		@XmlEnumValue(XBRL.LANG_EN)
		EN(XBRL.LANG_EN),
		@XmlEnumValue(XBRL.LANG_JA)
		JA(XBRL.LANG_JA);
		
		public final String value;
		Lang(String value) {
			this.value = value;
		}
	}
	
	// Role can be "http://www.xbrl.org/2003/role/label
	@XmlAttribute(name = "type", namespace = XML.NS_XLINK, required = true)
	public Type type;
	
	// Role can be "http://www.xbrl.org/2003/role/label
	@XmlAttribute(name = "role", namespace = XML.NS_XLINK, required = true)
	public Role role;
	
	// All <label> resources MUST have an @xml:lang attribute identifying the language used for the content of the label.
	@XmlAttribute(name = "lang", namespace = XML.NS_XML, required = true)
	public Lang lang;
	
	// The @xlink:label attribute on a resource identifies the resource so that Arcs in the same Extended Link can reference it.
	@XmlAttribute(name = "label", namespace = XML.NS_XLINK, required = true)
	public String label;
	
	
	@XmlValue
	public String value;
	
	@Override
	public String toString() {
		return String.format("{%s %s %s %s\"%s\"}", type, role, lang, label, value);
	}
	
	void afterUnmarshal(Unmarshaller u, Object parent) {
		// Sanity check
		if (type == null) {
			logger.error("type is null {}", this);
			throw new UnexpectedException("type is null");
		}
		if (role == null) {
			logger.error("role is null {}", this);
			throw new UnexpectedException("role is null");
		}
		if (lang == null) {
			logger.error("lang is null {}", this);
			throw new UnexpectedException("lang is null");
		}
	}
}

//<linkbase xmlns="http://www.xbrl.org/2003/linkbase" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:xlink="http://www.w3.org/1999/xlink" xmlns:xbrli="http://www.xbrl.org/2003/instance">
//<labelLink xlink:type="extended" xlink:role="http://www.xbrl.org/2003/role/link">
//  <loc xlink:type="locator" xlink:href="tse-ed-t-2014-01-12.xsd#tse-ed-t_AmountChangeGrossOperatingRevenues" xlink:label="AmountChangeGrossOperatingRevenues"/>
//  <label xlink:type="resource" xlink:label="label_AmountChangeGrossOperatingRevenues" xlink:role="http://www.xbrl.org/2003/role/label" xml:lang="ja" id="label_AmountChangeGrossOperatingRevenues">増減額</label>
//  <labelArc xlink:type="arc" xlink:arcrole="http://www.xbrl.org/2003/arcrole/concept-label" xlink:from="AmountChangeGrossOperatingRevenues" xlink:to="label_AmountChangeGrossOperatingRevenues"/>
