package yokwe.stock.jp.xbrl.edinet.manifest;

import java.util.List;

import jakarta.xml.bind.Unmarshaller;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlElementWrapper;
import yokwe.stock.jp.xbrl.XBRL;
import yokwe.util.StringUtil;
import yokwe.util.UnexpectedException;

//<?xml version="1.0" encoding="utf-8"?>
//<manifest xmlns="http://disclosure.edinet-fsa.go.jp/2013/manifest">
//  <tocComposition>
//    <title xml:lang="ja">提出本文書</title>
//    <title xml:lang="en">Main Document</title>
//    <item ref="jpcrp040300" extrole="http://disclosure.edinet-fsa.go.jp/role/jpcrp/rol_CabinetOfficeOrdinanceOnDisclosureOfCorporateInformationEtcFormNo43QuarterlySecuritiesReport" in="presentation" />
//  </tocComposition>
//  <list>
//    <instance id="jpcrp040300" type="PublicDoc" preferredFilename="jpcrp040300-q1r-001_E02882-000_2021-04-20_01_2021-06-04.xbrl">
//      <ixbrl>0000000_header_jpcrp040300-q1r-001_E02882-000_2021-04-20_01_2021-06-04_ixbrl.htm</ixbrl>
//      <ixbrl>0101010_honbun_jpcrp040300-q1r-001_E02882-000_2021-04-20_01_2021-06-04_ixbrl.htm</ixbrl>
//      <ixbrl>0102010_honbun_jpcrp040300-q1r-001_E02882-000_2021-04-20_01_2021-06-04_ixbrl.htm</ixbrl>
//      <ixbrl>0103010_honbun_jpcrp040300-q1r-001_E02882-000_2021-04-20_01_2021-06-04_ixbrl.htm</ixbrl>
//      <ixbrl>0104000_honbun_jpcrp040300-q1r-001_E02882-000_2021-04-20_01_2021-06-04_ixbrl.htm</ixbrl>
//      <ixbrl>0104310_honbun_jpcrp040300-q1r-001_E02882-000_2021-04-20_01_2021-06-04_ixbrl.htm</ixbrl>
//      <ixbrl>0104320_honbun_jpcrp040300-q1r-001_E02882-000_2021-04-20_01_2021-06-04_ixbrl.htm</ixbrl>
//      <ixbrl>0104400_honbun_jpcrp040300-q1r-001_E02882-000_2021-04-20_01_2021-06-04_ixbrl.htm</ixbrl>
//      <ixbrl>0201010_honbun_jpcrp040300-q1r-001_E02882-000_2021-04-20_01_2021-06-04_ixbrl.htm</ixbrl>
//    </instance>
//  </list>
//</manifest>

public class Manifest {
	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(Manifest.class);

	public static class Instance {
		@XmlAttribute(name="id")
		public String id;
		@XmlAttribute(name="type")
		public String type;
		@XmlAttribute(name="preferredFilename")
		public String preferredFilename;
		
		@XmlElement(namespace=XBRL.NS_EDINET_MANIFEST, name = "ixbrl")
		public List<String> ixbr;
		
		@Override
		public String toString() {
			return StringUtil.toString(this);
		}
		
		// Called from JAXB.unmarshal
		void afterUnmarshal(Unmarshaller u, Object parent) {
			// Sanity check
			if (ixbr.isEmpty()) {
				logger.error("ixbr is emtpy");
				logger.error("  {}", this);
				throw new UnexpectedException("ixbr is empty");
			}
		}
	}
	
	@XmlElementWrapper(namespace = XBRL.NS_EDINET_MANIFEST, name="list")
	@XmlElement(namespace = XBRL.NS_EDINET_MANIFEST, name="instance")
	public List<Instance> list;
	
	@Override
	public String toString() {
		return StringUtil.toString(this);
	}
	
	// Called from JAXB.unmarshal
	void afterUnmarshal(Unmarshaller u, Object parent) {
		// Sanity check
		if (list == null) {
			logger.error("list is emtpy");
			logger.error("  {}", this);
			throw new UnexpectedException("list is empty");
		}
	}

}
