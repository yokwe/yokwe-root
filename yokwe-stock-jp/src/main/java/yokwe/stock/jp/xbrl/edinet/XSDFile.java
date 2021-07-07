package yokwe.stock.jp.xbrl.edinet;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import jakarta.xml.bind.Unmarshaller;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import yokwe.stock.jp.xbrl.XML;
import yokwe.util.EnumUtil;
import yokwe.util.FileUtil;
import yokwe.util.StringUtil;
import yokwe.util.UnexpectedException;
import yokwe.util.xml.JAXB;

public class XSDFile {
	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(XSDFile.class);

	@XmlRootElement(name = "schema", namespace = XML.NS_XSD)
	public static class Schema {
		@XmlAttribute(name = "targetNamespace", required = true)
		public String targetNamespace;
		
		@XmlElement(name = "element", namespace = XML.NS_XSD)
		public List<Element> elementList = new ArrayList<>();

		@XmlElement(name = "import", namespace = XML.NS_XSD)
		public List<Import> importList = new ArrayList<>();

		@Override
		public String toString() {
			return String.format("{%s %d}", this.targetNamespace, this.idMap.size());
		}
		
		private Map<String, Element> idMap   = new TreeMap<>();
		private Map<String, Element> nameMap = new TreeMap<>();
		
		public Element getElementByID(String id) {
			if (idMap.containsKey(id)) {
				return idMap.get(id);
			} else {
				logger.error("Unpexpected value");
				logger.error("  id {}", id);
				throw new UnexpectedException("Unpexpected value");
			}
		}
		public Element getElementByName(String name) {
			if (nameMap.containsKey(name)) {
				return nameMap.get(name);
			} else {
				logger.error("Unpexpected value");
				logger.error("  name {}", name);
				throw new UnexpectedException("Unpexpected value");
			}
		}
		
		void afterUnmarshal(Unmarshaller u, Object parent) {
			// build idMap and nameMap
			for(var e: elementList) {
				String id   = e.id;
				String name = e.name;
				
				if (idMap.containsKey(id)) {
					logger.error("Unpexpected value");
					logger.error("  e {}", e);
					throw new UnexpectedException("Unpexpected value");
				} else {
					idMap.put(id, e);
				}

				if (nameMap.containsKey(name)) {
					logger.error("Unpexpected value");
					logger.error("  e {}", e);
					throw new UnexpectedException("Unpexpected value");
				} else {
					nameMap.put(name, e);
				}
			}
		}
	}
	
	public static class Element {
//	    <xsd:element
//        name="CabinetOfficeOrdinanceOnDisclosureOfCorporateInformationEtcFormNo2SecuritiesRegistrationStatementHeading"
//        id="jpcrp_cor_CabinetOfficeOrdinanceOnDisclosureOfCorporateInformationEtcFormNo2SecuritiesRegistrationStatementHeading"
//        type="xbrli:stringItemType"
//        substitutionGroup="iod:identifierItem" abstract="true"
//        nillable="true" xbrli:periodType="duration" />
		
		public enum Type {
			DOMAIN              ("nonnum:domainItemType"),
			TEXT_BLOCK          ("nonnum:textBlockItemType"),
			
			PER_SHARE           ("num:perShareItemType"),
			PERCENT             ("num:percentItemType"),
			
			DATE                ("xbrli:dateItemType"),
			DECIMAL             ("xbrli:decimalItemType"),
			MONETARY            ("xbrli:monetaryItemType"),
			NON_NEGATIVE_INTEGER("xbrli:nonNegativeIntegerItemType"),
			SHARES              ("xbrli:sharesItemType"),
			STRING              ("xbrli:stringItemType");

			public final String value;
			Type(String value) {
				this.value = value;
			}
			
			@Override
			public String toString() {
				return value;
			}
		}
		
		public static class TypeAdapter extends EnumUtil.GenericXmlAdapter {
			TypeAdapter() {
				super(Type.class);
			}
		}
		
		@XmlAttribute(name = "name", required = true)
		public String name;

		@XmlAttribute(name = "id", required = true)
		public String id;

		@XmlAttribute(name = "type", required = true)
		@XmlJavaTypeAdapter(TypeAdapter.class)
		public Type type;

//		@XmlAttribute(name = "substitutionGroup", required = true)
//		public String substitutionGroup;

//		@XmlAttribute(name = "abstract", required = true)
//		public Boolean isAbstract;

//		@XmlAttribute(name = "nillable", required = true)
//		public Boolean nillable;

		@Override
		public String toString() {
			return StringUtil.toString(this);
		}
	}
	
	public static class Import {
//		<xsd:import
//	        namespace="http://www.xbrl.org/dtr/type/numeric"
//	        schemaLocation="http://www.xbrl.org/dtr/type/numeric-2009-12-16.xsd" />
			
		@XmlAttribute(name = "namespace", required = true)
		public String namespace;
		
		@XmlAttribute(name = "schemaLocation", required = true)
		public String schemaLocation;
			
		@Override
		public String toString() {
			return StringUtil.toString(this);
		}
	}
		

	
	public static XSDFile.Schema getInstance(String path) {
		String data = FileUtil.read().file(path);
		Schema schema = JAXB.unmarshal(new StringReader(data), Schema.class);
		return schema;
	}
	
	public static void main(String[] args) {
		logger.info("START");
		
		String path = Taxonomy.getPath("jpcrp/2020-11-01/jpcrp_cor_2020-11-01.xsd");
		logger.info("path {}", path);
		String data = FileUtil.read().file(path);
		logger.info("data {}", data.length());
		
		Schema schema = JAXB.unmarshal(new StringReader(data), Schema.class);
		logger.info("schema {}", schema);
		
		logger.info("element {}", schema.elementList.get(0));
		logger.info("import {}", schema.importList.get(0));
				
		logger.info("END");
	}
	
}
