package yokwe.stock.jp.xbrl.edinet.manifest;

import java.util.List;

import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlElementWrapper;
import yokwe.util.StringUtil;

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
	@XmlElementWrapper(name="list")
	@XmlElement(name="instance")
	public List<Instance> list;
	
	@Override
	public String toString() {
		return StringUtil.toString(this);
	}
}
