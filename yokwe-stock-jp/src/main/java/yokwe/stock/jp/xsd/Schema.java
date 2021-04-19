package yokwe.stock.jp.xsd;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

@XmlRootElement(name = "schema")
public class Schema {
	@XmlAttribute
	public String targetNamespace;
	
	@XmlElement(name = "element")
	public final List<Element> elementList = new ArrayList<>();	
	
	@XmlTransient
	public final Map<String, Element> elementMap = new TreeMap<>();
	
	@Override
	public String toString() {
		return String.format("{%s %d / %d}", targetNamespace, elementMap.size(), elementList.size());
	}
	
	// Unmarshal Event Callbacks
	public void afterUnmarshal(Unmarshaller unmarshaller, Object parent) {
		elementMap.clear();
		for(Element element: elementList) {
			elementMap.put(element.name, element);
		}
	}
}

//  <schema targetNamespace="http://www.xbrl.tdnet.info/taxonomy/jp/tse/tdnet/ed/t/2014-01-12" attributeFormDefault="unqualified" elementFormDefault="qualified" xmlns="http://www.w3.org/2001/XMLSchema" xmlns:tse-ed-t="http://www.xbrl.tdnet.info/taxonomy/jp/tse/tdnet/ed/t/2014-01-12" xmlns:link="http://www.xbrl.org/2003/linkbase" xmlns:num="http://www.xbrl.org/dtr/type/numeric" xmlns:nonnum="http://www.xbrl.org/dtr/type/non-numeric" xmlns:xbrldt="http://xbrl.org/2005/xbrldt" xmlns:deprecated="http://www.xbrl.org/2009/role/deprecated" xmlns:xl="http://www.xbrl.org/2003/XLink" xmlns:xbrli="http://www.xbrl.org/2003/instance" xmlns:tse-ed-types="http://www.xbrl.tdnet.info/taxonomy/jp/tse/tdnet/ed/o/types/2014-01-12" xmlns:xlink="http://www.w3.org/1999/xlink">
//    <element name="FilingDate" id="tse-ed-t_FilingDate" type="xbrli:dateItemType" substitutionGroup="xbrli:item" abstract="false" nillable="true" xbrli:periodType="instant"/>
