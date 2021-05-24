package yokwe.stock.jp.xbrl.tdnet;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.slf4j.LoggerFactory;

import yokwe.util.UnexpectedException;
import yokwe.stock.jp.xbrl.XBRL;
import yokwe.util.AutoIndentPrintWriter;
import yokwe.util.StringUtil;

public class GenerateTaxonomyLabelClass {
	private static final org.slf4j.Logger logger = LoggerFactory.getLogger(GenerateTaxonomyLabelClass.class);
	
	private static final String PATH_JAVA_SOURCE_TAXONOMY_DIR = "src/main/java/yokwe/stock/jp/xbrl/tdnet/taxonomy";
	
	private static Map<String, String> classNameMap = new TreeMap<>();
	//                 namespace
	//                         class name
	static {
		classNameMap.put(XBRL.NS_TSE_ED_T, "TSE_ED_T");
//		classNameMap.put(XBRL.NS_TSE_AT_T, "TSE_AT_T");
		classNameMap.put(XBRL.NS_TSE_RE_T, "TSE_RE_T");
		
		classNameMap.put(XBRL.NS_TSE_T_CG, "TSE_T_CG");
	}
	
	private static void generateClass(String namespace, String className, Map<String, Entry> entryMap) {
		if (entryMap.isEmpty()) {
			logger.warn("entryMap is empty  {}", className);
			return;
		}
		String path = String.format("%s/%s.java", PATH_JAVA_SOURCE_TAXONOMY_DIR, className);
		logger.info("generate {} {}", entryMap.size(), path);
		try (AutoIndentPrintWriter out = new AutoIndentPrintWriter(new PrintWriter(path))) {
			out.println("package yokwe.stock.jp.xbrl.tdnet.taxonomy;");
			out.println();
//			out.println("import java.util.Map;");
//			out.println("import java.util.TreeMap;");
//			out.println();
//			out.println("import yokwe.util.UnexpectedException;");
			out.println("import yokwe.util.xml.QValue;");
			out.println();
			
			out.println("public enum %s {", className);
			
			List<Entry> entryList = new ArrayList<>(entryMap.values());
			int entryListSize = entryList.size();
			for(int i = 0; i < entryListSize; i++) {
				Entry entry = entryList.get(i);
				
				String name      = entry.name;
				String constName = entry.constName;
				String en        = entry.en;
				String ja        = entry.ja;
				
				String nameValue = String.format("\"%s\"", name);
				String enValue = (en == null) ? "null" : String.format("\"%s\"", en);
				String jaValue = (ja == null) ? "null" : String.format("\"%s\"", ja);
				String comma   = (i == (entryListSize - 1)) ? ";" : ",";
				
				out.println("%s(", constName);
				
				out.println("%s,",  nameValue);
				out.println("%s,",  enValue);
				out.println("%s)%s", jaValue, comma);
				
				out.println();
			}

			out.println();
			out.println("public static final String NAMESPACE = \"%s\";", namespace);
			out.println();
			
			out.println("public final QValue    qName;");
			out.println("public final String    en;");
			out.println("public final String    ja;");
			out.println();
			
			out.println("%s (String name, String en, String ja) {", className);
			
			out.println("this.qName     = new QValue(NAMESPACE, name);");
			out.println("this.en        = en;");
			out.println("this.ja        = ja;");
//			out.println();
//			out.println("LabelData.add(qName, en, ja, this);");
			out.println("}");
			out.println();

//			out.println("private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(%s.class);", className);
//
//			out.println("private static final Map<QValue, TSE_T_CG_LABEL> all = new TreeMap<>();");
//			out.println("static {");
//
//			out.println("for(TSE_T_CG_LABEL e: TSE_T_CG_LABEL.class.getEnumConstants()) {");
//
//			out.println("QValue key = e.qName;");
//			out.println("if (all.containsKey(key)) {");
//			out.println("logger.error(\"Unknow key {}\", key);");
//			out.println("throw new UnexpectedException(\"Duplicate key\");");
//			out.println("} else {");
//			out.println("all.put(key, e);");
//			out.println("}");
//			
//			out.println("}");
//
//			out.println("}");
//			out.println();
//
//			out.println("public static TSE_T_CG_LABEL get(QValue qName) {");
//			out.println("if (all.containsKey(qName)) {");
//			out.println("return  all.get(qName);");
//			out.println("} else {");
//			out.println("logger.error(\"Unknow key {}\", qName);");
//			out.println("throw new UnexpectedException(\"Unknow key\");");
//			out.println("}");
//			out.println("}");
//			out.println();
			
			out.println("}");
		} catch (FileNotFoundException e) {
			String exceptionName = e.getClass().getSimpleName();
			logger.error("{} {}", exceptionName, e);
			throw new UnexpectedException(exceptionName, e);
		}
	}
	
	private static class Entry {
		String name;
		String constName;
		String en;
		String ja;
		Entry(String name) {
			this.name      = name;
			this.constName = StringUtil.toJavaConstName(name);
			this.en        = null;
			this.ja        = null;
		}
	}
	
	public static void main(String[] args) {
		logger.info("START");
		
		List<Label> labelList = Label.load();
		
		Map<String, Map<String, Entry>> namespaceLabelMap = new TreeMap<>();
		//  namespace   label
		for(Label e: labelList) {
			final String namespace = e.namespace;
			final String label     = e.label;
			final String role      = e.role;
			final String lang      = e.lang;
			final String value     = e.value;
			
			// Filter
			if (!role.equals(XBRL.ROLE_LABLE)) continue;
			
			if (!namespaceLabelMap.containsKey(namespace)) {
				namespaceLabelMap.put(namespace, new TreeMap<>());
			}
			Map<String, Entry> entryMap = namespaceLabelMap.get(namespace);
			
			if (!entryMap.containsKey(label)) {
				entryMap.put(label, new Entry(label));
			}
			Entry entry = entryMap.get(label);
			
			switch(lang) {
			case "ja":
				if (entry.ja == null) {
					entry.ja = value;
				} else {
					logger.error("Duplcate lang ja {}cons", e);
					throw new UnexpectedException("Duplcate lang");
				}
				break;
			case "en":
				if (entry.en == null) {
					entry.en = value;
				} else {
					logger.error("Duplcate lang en {}", e);
					throw new UnexpectedException("Duplcate lang");
				}
				break;
			default:
				logger.error("Unexpected lang {}", lang);
				throw new UnexpectedException("Unexpected lang");
			}
		}
		
		for(Map.Entry<String, Map<String, Entry>> entry: namespaceLabelMap.entrySet()) {
			final String             namespace = entry.getKey();
			final Map<String, Entry> entryMap  = entry.getValue();
			
			if (!classNameMap.containsKey(namespace)) continue;
			
			final String className = classNameMap.get(namespace) + "_LABEL";
			
			generateClass(namespace, className, entryMap);
		}
		
		logger.info("STOP");
	}
}
