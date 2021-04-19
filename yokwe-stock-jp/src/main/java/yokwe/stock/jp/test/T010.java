package yokwe.security.japan.test;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;

import javax.xml.bind.JAXB;

import org.slf4j.LoggerFactory;

import yokwe.security.japan.xbrl.Taxonomy;
import yokwe.security.japan.xbrl.label.Linkbase;
import yokwe.util.FileUtil;

public class T010 {
	static final org.slf4j.Logger logger = LoggerFactory.getLogger(T010.class);
	
	public static void main(String[] args) throws IOException {
		logger.info("START");
		
		String[] dirPathArray = {
				Taxonomy.PATH_DIR_TSE_ED_T,
				Taxonomy.PATH_DIR_TSE_CG_T,
		};
		for(String dirPath: dirPathArray) {
			File dir = new File(dirPath);
			for(File file: dir.listFiles(o -> o.getName().contains("-lab"))) {
				logger.info("path     {}", file.getPath());
				String data = FileUtil.read().file(file);
				
				Linkbase linkbase = JAXB.unmarshal(new StringReader(data), Linkbase.class);
				logger.info("linkbase {}", linkbase);
				linkbase.stats();
			}
		}
				
		logger.info("STOP");
	}
}

// tmp/61_taxonomy/tse-ed-2014-01-12/taxonomy/jp/tse/tdnet/ed/t/2014-01-12/tse-ed-t-2014-01-12-lab-en.xml
/*
12:46:26.969 [main] INFO  T010     - START
12:46:27.416 [main] INFO  T010     - path     tmp/61_taxonomy/tse-ed-2014-01-12/taxonomy/jp/tse/tdnet/ed/t/2014-01-12/tse-ed-t-2014-01-12-lab-en.xml
12:46:27.423 [main] INFO  T010     - linkbase {EXTENDED LINK  loc 825  label 1783  labelArc 1783}
12:46:27.427 [main] INFO  Linkbase - ==== stats
12:46:27.427 [main] INFO  Linkbase - stats EN  LABLE                            751
12:46:27.427 [main] INFO  Linkbase - stats EN  VERBOSE_LABEL                    751
12:46:27.427 [main] INFO  Linkbase - stats EN  QUARTERLY_VERBOSE_LABEL           86
12:46:27.428 [main] INFO  Linkbase - stats EN  INTERIM_VERBOSE_LABEL             65
12:46:27.428 [main] INFO  Linkbase - stats EN  NON_CONSOLIDATED_LABEL             6
12:46:27.428 [main] INFO  Linkbase - stats EN  NON_CONSOLIDATED_VERBOSELABEL      6
12:46:27.428 [main] INFO  Linkbase - stats EN  QUARTERLYLABEL                    69
12:46:27.429 [main] INFO  Linkbase - stats EN  INTERLIM_LABEL                    49
12:46:27.429 [main] INFO  T010     - STOP
*/

// tmp/61_taxonomy/tse-ed-2014-01-12/taxonomy/jp/tse/tdnet/ed/t/2014-01-12/tse-ed-t-2014-01-12-lab.xml
/*
12:46:54.075 [main] INFO  T010     - START
12:46:54.610 [main] INFO  T010     - path     tmp/61_taxonomy/tse-ed-2014-01-12/taxonomy/jp/tse/tdnet/ed/t/2014-01-12/tse-ed-t-2014-01-12-lab.xml
12:46:54.615 [main] INFO  T010     - linkbase {EXTENDED LINK  loc 760  label 1807  labelArc 1807}
12:46:54.621 [main] INFO  Linkbase - ==== stats
12:46:54.621 [main] INFO  Linkbase - stats EN  LABLE                              9
12:46:54.622 [main] INFO  Linkbase - stats EN  VERBOSE_LABEL                      9
12:46:54.622 [main] INFO  Linkbase - stats JA  LABLE                            760
12:46:54.622 [main] INFO  Linkbase - stats JA  VERBOSE_LABEL                    760
12:46:54.622 [main] INFO  Linkbase - stats JA  QUARTERLY_VERBOSE_LABEL           86
12:46:54.622 [main] INFO  Linkbase - stats JA  INTERIM_VERBOSE_LABEL             65
12:46:54.623 [main] INFO  Linkbase - stats JA  QUARTERLYLABEL                    69
12:46:54.623 [main] INFO  Linkbase - stats JA  INTERLIM_LABEL                    49
12:46:54.623 [main] INFO  T010     - STOP
*/
