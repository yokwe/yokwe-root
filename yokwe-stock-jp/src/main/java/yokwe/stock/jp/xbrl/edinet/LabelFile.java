package yokwe.stock.jp.xbrl.edinet;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlValue;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import yokwe.stock.jp.xbrl.XBRL;
import yokwe.stock.jp.xbrl.XML;
import yokwe.util.EnumUtil;
import yokwe.util.FileUtil;
import yokwe.util.JAXBUtil;
import yokwe.util.ToString;

public class LabelFile {
	private static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();

	@XmlRootElement(name = "linkbase", namespace = XBRL.NS_LINKBASE)
	public static class LinkBase {
		
		@XmlElement(name = "roleRef", namespace = XBRL.NS_LINKBASE)
		public List<RoleRef> roleRefList = new ArrayList<>();
		
		@XmlElement(name = "labelLink", namespace = XBRL.NS_LINKBASE)
		public LabelLink labelLink;
		
		@Override
		public String toString() {
			return String.format("{%s}", labelLink.toString());
		}
	}
	
	public static class LabelLink {
		@XmlElement(name = "label", namespace = XBRL.NS_LINKBASE)
		public List<Label> labelList = new ArrayList<>();
		
		@XmlElement(name = "labelArc", namespace = XBRL.NS_LINKBASE)
		public List<LabelArc> labelArcList = new ArrayList<>();
		
		@XmlElement(name = "loc", namespace = XBRL.NS_LINKBASE)
		public List<Loc> locList = new ArrayList<>();
		
		@Override
		public String toString() {
			return String.format("{label %d  labelArc %d  loc %d}", labelList.size(), labelArcList.size(), locList.size());
		}
	}
	
	public static class Label {
//        <link:label
//		      xlink:type="resource"
//            xlink:label="label_DividendsOfSurplusTable"
//            xlink:role="http://www.xbrl.org/2003/role/label"
//            xml:lang="ja"
//			  id="label_DividendsOfSurplusTable">剰余金の配当</link:label>
		
		public enum Lang {
			EN(XBRL.LANG_EN),
			JA(XBRL.LANG_JA);
			
			public final String value;
			Lang(String value) {
				this.value = value;
			}
			
			@Override
			public String toString() {
				return value;
			}
		}
		
		public static class LangAdapter extends EnumUtil.GenericXmlAdapter {
			LangAdapter() {
				super(Lang.class);
			}
		}

		@XmlAttribute(name = "type", namespace = XML.NS_XLINK, required = true)
		public String type;
		@XmlAttribute(name = "label", namespace = XML.NS_XLINK, required = true)
		public String label;
		@XmlAttribute(name = "id", required = true)
		public String id;
		@XmlAttribute(name = "lang", namespace = XML.NS_XML, required = true)
		@XmlJavaTypeAdapter(LangAdapter.class)
		public Lang lang;
		
		@XmlValue
		public String value;

		@Override
		public String toString() {
			return ToString.withFieldName(this);
		}
	}
	
//  <link:labelArc xlink:type="arc"
//      xlink:arcrole="http://www.xbrl.org/2003/arcrole/concept-label"
//      xlink:from="ParticularsOfPublicOfferingHeading"
//      xlink:to="label_ParticularsOfPublicOfferingHeading" />

	public static class LabelArc {
		@XmlAttribute(name = "type", namespace = XML.NS_XLINK, required = true)
		public String type;
		@XmlAttribute(name = "from", namespace = XML.NS_XLINK, required = true)
		public String from;
		@XmlAttribute(name = "to", namespace = XML.NS_XLINK, required = true)
		public String to;

		@Override
		public String toString() {
			return ToString.withFieldName(this);
		}
	}
	
//  <link:loc
//	    xlink:type="locator"
//	    xlink:href="../jpcrp_cor_2020-11-01.xsd#jpcrp_cor_CabinetOfficeOrdinanceOnDisclosureOfCorporateInformationEtcFormNo23SecuritiesRegistrationStatementHeading"
//	    xlink:label="CabinetOfficeOrdinanceOnDisclosureOfCorporateInformationEtcFormNo23SecuritiesRegistrationStatementHeading"/>

	public static class Loc {
//		@XmlAttribute(name = "type", namespace = XML.NS_XLINK, required = true)
//		public String type;
		@XmlAttribute(name = "href", namespace = XML.NS_XLINK, required = true)
		public String href;
		@XmlAttribute(name = "label", namespace = XML.NS_XLINK, required = true)
		public String labels;

		@Override
		public String toString() {
			return ToString.withFieldName(this);
		}
	}
	
//<link:roleRef
//  roleURI="http://disclosure.edinet-fsa.go.jp/jpcrp/std/alt/Consolidated/role/label"
//  xlink:type="simple"
//  xlink:href="../jpcrp_rt_2020-11-01.xsd#rol_std_altConsolidatedLabel" />
	
	public static class RoleRef {
		@XmlAttribute(name = "roleURI", required = true)
		public String roleURI;
		@XmlAttribute(name = "type", namespace = XML.NS_XLINK, required = true)
		public String type;
		@XmlAttribute(name = "href", namespace = XML.NS_XLINK, required = true)
		public String href;

		@Override
		public String toString() {
			return ToString.withFieldName(this);
		}
	}
	
	public static void main(String[] args) {
		logger.info("START");
		
		String path = Taxonomy.getPath("jpcrp/2020-11-01/label/jpcrp_2020-11-01_lab.xml");
		logger.info("path {}", path);
		String data = FileUtil.read().file(path);
		logger.info("data {}", data.length());
		
		LinkBase linkBase = JAXBUtil.unmarshal(new StringReader(data), LinkBase.class);
		logger.info("linkBase {}", linkBase);
		logger.info("roleRef  {}", linkBase.roleRefList.size());
		logger.info("label    {}", linkBase.labelLink.labelList.size());
		logger.info("labelArc {}", linkBase.labelLink.labelArcList.size());
		logger.info("loc      {}", linkBase.labelLink.locList.size());
		
		logger.info("roleRef  {}", linkBase.roleRefList.get(0));
		logger.info("label    {}", linkBase.labelLink.labelList.get(0));
		logger.info("labelArc {}", linkBase.labelLink.labelArcList.get(0));
		logger.info("loc      {}", linkBase.labelLink.locList.get(0));
		
		logger.info("END");
	}

}
