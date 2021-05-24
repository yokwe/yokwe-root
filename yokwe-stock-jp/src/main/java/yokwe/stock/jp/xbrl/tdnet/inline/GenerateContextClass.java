package yokwe.stock.jp.xbrl.tdnet.inline;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import jakarta.xml.bind.JAXBException;

import org.slf4j.LoggerFactory;

import yokwe.stock.jp.tdnet.TDNET;
import yokwe.util.AutoIndentPrintWriter;
import yokwe.util.StringUtil;

public class GenerateContextClass {
	static final org.slf4j.Logger logger = LoggerFactory.getLogger(GenerateContextClass.class);
		
	private static final String PATH_FILE = "src/main/java/yokwe/stock/jp/xbrl/tdnet/inline/Context.java";

	public static void main(String[] args) throws IOException, JAXBException {
		logger.info("START");
		
		Set<String> all = new TreeSet<>();
		
		{
			List<File> fileList = TDNET.getSummaryFileList();
			logger.info("fileList {}", fileList.size());
			
			int count = 0;
			int size = fileList.size();
			for(File file : fileList) {
				if ((count % 1000) == 0) {
					logger.info("{} {}", String.format("%5d / %5d  %5d", count, size, all.size()), file.getName());
				}
				count++;
				Document document = Document.getInstance(file);
				
				all.addAll(document.getContextSet());
			}
		}
		logger.info("all     {}", all.size());
		
		// build data
		List<String> stringValueList   = new ArrayList<>(all);
		List<String> javaConstNameList = new ArrayList<>();
		int maxStringLength = 0;
		int maxJavaConstLength = 0;
		for(String stringValue: stringValueList) {
			String javaConstName = StringUtil.toJavaConstName(stringValue);
			javaConstNameList.add(javaConstName);
			
			maxStringLength    = Math.max(maxStringLength, stringValue.length());
			maxJavaConstLength = Math.max(maxJavaConstLength, javaConstName.length());
		}
		
		// 		YEAR_END_MEMBER                 ("YearEndMember");
		String format = String.format("%%-%ds (\"%%s\")%%s", maxJavaConstLength);
		
		// Output
		
		try (AutoIndentPrintWriter out = new AutoIndentPrintWriter(new PrintWriter(PATH_FILE))) {
			out.println("package yokwe.stock.jp.xbrl.tdnet.inline;");
			out.println();
			out.println("public enum Context {");

			for(int i = 0; i < stringValueList.size(); i++) {
				String commaOrSemi = (i == stringValueList.size() - 1) ? ";" : ",";
				out.println(String.format(format, javaConstNameList.get(i), stringValueList.get(i), commaOrSemi));
			}
			out.println();
			
			out.println("public final String value;");
			out.println();
			out.println("Context(String value) {");
			out.println("this.value = value;");
			out.println("}");
			out.println();
			out.println("@Override");
			out.println("public String toString() {");
			out.println("return value;");
			out.println("}");

			out.println("}");
		}
		
		logger.info("STOP");
	}
}
