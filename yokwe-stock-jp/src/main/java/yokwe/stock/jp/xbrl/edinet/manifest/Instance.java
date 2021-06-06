package yokwe.stock.jp.xbrl.edinet.manifest;

import java.util.List;

import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import yokwe.util.StringUtil;

//<instance id="jpcrp040300" type="PublicDoc" preferredFilename="jpcrp040300-q1r-001_E02882-000_2021-04-20_01_2021-06-04.xbrl">
//  <ixbrl>0000000_header_jpcrp040300-q1r-001_E02882-000_2021-04-20_01_2021-06-04_ixbrl.htm</ixbrl>
//  <ixbrl>0101010_honbun_jpcrp040300-q1r-001_E02882-000_2021-04-20_01_2021-06-04_ixbrl.htm</ixbrl>
//  <ixbrl>0102010_honbun_jpcrp040300-q1r-001_E02882-000_2021-04-20_01_2021-06-04_ixbrl.htm</ixbrl>
//  <ixbrl>0103010_honbun_jpcrp040300-q1r-001_E02882-000_2021-04-20_01_2021-06-04_ixbrl.htm</ixbrl>
//  <ixbrl>0104000_honbun_jpcrp040300-q1r-001_E02882-000_2021-04-20_01_2021-06-04_ixbrl.htm</ixbrl>
//  <ixbrl>0104310_honbun_jpcrp040300-q1r-001_E02882-000_2021-04-20_01_2021-06-04_ixbrl.htm</ixbrl>
//  <ixbrl>0104320_honbun_jpcrp040300-q1r-001_E02882-000_2021-04-20_01_2021-06-04_ixbrl.htm</ixbrl>
//  <ixbrl>0104400_honbun_jpcrp040300-q1r-001_E02882-000_2021-04-20_01_2021-06-04_ixbrl.htm</ixbrl>
//  <ixbrl>0201010_honbun_jpcrp040300-q1r-001_E02882-000_2021-04-20_01_2021-06-04_ixbrl.htm</ixbrl>
//</instance>

public class Instance {
	@XmlAttribute(name="id")                public String id;
	@XmlAttribute(name="type")              public String type;
	@XmlAttribute(name="preferredFilename") public String preferredFilename;
	
	@XmlElement(name = "ixbrl")             public List<String> list;
	
	@Override
	public String toString() {
		return StringUtil.toString(this);
	}
}