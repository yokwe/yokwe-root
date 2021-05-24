package yokwe.stock.jp.xbrl.tdnet.label;

import java.util.ArrayList;
import java.util.List;

import jakarta.xml.bind.Unmarshaller;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlEnum;
import jakarta.xml.bind.annotation.XmlEnumValue;
import jakarta.xml.bind.annotation.XmlType;

import org.slf4j.LoggerFactory;

import yokwe.stock.jp.xbrl.XML;
import yokwe.util.UnexpectedException;

public class LabelLink {
	private static final org.slf4j.Logger logger = LoggerFactory.getLogger(LabelLink.class);

	@XmlEnum
	@XmlType(name="LabelLink-Type")
	public enum Type {
	@XmlEnumValue("extended")
		EXTENDED,
	}

	@XmlEnum
	@XmlType(name="LabelLink-Role")
	public enum Role {
		@XmlEnumValue("http://www.xbrl.org/2003/role/link")
		LINK,
	}
	
	// Role can be "http://www.xbrl.org/2003/role/label
	@XmlAttribute(name = "type", namespace = XML.NS_XLINK, required = true)
	public Type type;
	
	// Role can be "http://www.xbrl.org/2003/role/label
	@XmlAttribute(name = "role", namespace = XML.NS_XLINK, required = true)
	public Role role;
	
	@XmlElement(name = "loc")
	public final List<Loc>      locList      = new ArrayList<>();
	@XmlElement(name = "label")
	public final List<Label>    labelList    = new ArrayList<>();
	@XmlElement(name = "labelArc")
	public final List<LabelArc> labelArcList = new ArrayList<>();
	
	@Override
	public String toString() {
		return String.format("{%s %s  loc %d  label %d  labelArc %d}", type, role, locList.size(), labelList.size(), labelArcList.size());
	}
	
	void afterUnmarshal(Unmarshaller u, Object parent) {
		// Sanity check
		if (type == null) {
			logger.error("type is null %s", this);
			throw new UnexpectedException("type is null");
		}
		if (role == null) {
			logger.error("role is null %s", this);
			throw new UnexpectedException("role is null");
		}
	}
}

//<linkbase xmlns="http://www.xbrl.org/2003/linkbase" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:xlink="http://www.w3.org/1999/xlink" xmlns:xbrli="http://www.xbrl.org/2003/instance">
//<labelLink xlink:type="extended" xlink:role="http://www.xbrl.org/2003/role/link">
//  <loc xlink:type="locator" xlink:href="tse-ed-t-2014-01-12.xsd#tse-ed-t_AmountChangeGrossOperatingRevenues" xlink:label="AmountChangeGrossOperatingRevenues"/>
//  <label xlink:type="resource" xlink:label="label_AmountChangeGrossOperatingRevenues" xlink:role="http://www.xbrl.org/2003/role/label" xml:lang="ja" id="label_AmountChangeGrossOperatingRevenues">増減額</label>
//  <labelArc xlink:type="arc" xlink:arcrole="http://www.xbrl.org/2003/arcrole/concept-label" xlink:from="AmountChangeGrossOperatingRevenues" xlink:to="label_AmountChangeGrossOperatingRevenues"/>
