package yokwe.stock.jp.xsd;

import jakarta.xml.bind.annotation.XmlAttribute;

public class Element {
	@XmlAttribute
	String name;
	
	@XmlAttribute
	String id;
	
	@XmlAttribute
	String type;
	
	@Override
	public String toString() {
		return String.format("{%s %s %s}", name, id, type);
	}
}

//<element name="FilingDate" id="tse-ed-t_FilingDate" type="xbrli:dateItemType" substitutionGroup="xbrli:item" abstract="false" nillable="true" xbrli:periodType="instant"/>
