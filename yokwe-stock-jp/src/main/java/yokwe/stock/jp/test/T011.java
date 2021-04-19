package yokwe.security.japan.test;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.LoggerFactory;

import yokwe.security.japan.xbrl.inline.Context;
import yokwe.security.japan.xbrl.inline.Document;
import yokwe.security.japan.xbrl.inline.InlineXBRL;
import yokwe.security.japan.xbrl.taxonomy.TSE_ED_T_LABEL;

public class T011 {
	static final org.slf4j.Logger logger = LoggerFactory.getLogger(T011.class);
	
	public static void main(String[] args) throws IOException {
		logger.info("START");
		
		File dir = new File("tmp/TD2019111900049/XBRLData/Summary");
//		File dir = new File("tmp/TD2019111900049/XBRLData/Attachment");
		
		for(File file: dir.listFiles(o -> o.getName().endsWith("htm"))) {
			logger.info("==== {}", file.getName());
//			XMLUtil.buildStream(file).filter(o -> o.qName.startsWith("ix:header")).forEach(o -> logger.info("{}", o));
//			XMLUtil.buildStream(file).filter(o -> o.qName.startsWith("ix:")).filter(o -> !o.qName.equals("ix:header")).filter(o -> !o.qName.equals("ix:resources")).filter(o -> !o.qName.equals("ix:hidden")).filter(o -> !o.qName.equals("ix:references")).forEach(o -> logger.info("{}", Value.getInstance(o)));
			
//			XMLUtil.buildStream(file).filter(o -> InlineXBRL.canGetInstance(o)).
//			forEach(o -> {InlineXBRL ix = InlineXBRL.getInstance(o); logger.info("{}", LabelData.getJA(ix.qName), ix);});
//				forEach(o -> {InlineXBRL ix = InlineXBRL.getInstance(o); logger.info("{}", ix);});
			
			Document map = Document.getInstance(file);
//			map.dump();
			
			{
//				List<InlineXBRL> list = map.getList(TSE_ED_T_LABEL.DIVIDEND_PER_SHARE).stream().filter(InlineXBRL::notNullFilter).filter(InlineXBRL.contextFilter()).collect(Collectors.toList());
				List<InlineXBRL> list = map.getList(TSE_ED_T_LABEL.DIVIDEND_PER_SHARE.qName).stream().filter(InlineXBRL.contextIncludeAll(Context.CURRENT_YEAR_DURATION, Context.RESULT_MEMBER)).collect(Collectors.toList());
				for(int i = 0; i < list.size(); i++) {
					logger.info("  {}  {}", i, list.get(i));
				}
			}
			
//			{
//				Set<String> allSet = new TreeSet<>();
//				map.getList().stream().forEach(o -> allSet.addAll(o.contextSet));
//				
//				List<String> list = new ArrayList<String>(allSet);
//				for(int i = 0;u i < list.size(); i++) {
//					String e = list.get(i);
//					String j = StringUtil.toJavaConstName(e);
//					logger.info("  {}(\"{}\"), ", j, e);
//				}
//			}
		}
				
		logger.info("STOP");
	}
}
