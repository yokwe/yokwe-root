package yokwe.security.japan.test;

import java.io.File;
import java.util.List;

import org.slf4j.LoggerFactory;

import yokwe.security.japan.data.Price;
import yokwe.util.CSVUtil;
import yokwe.util.FileUtil;

public class T028 {
	static final org.slf4j.Logger logger = LoggerFactory.getLogger(T028.class);
	
	public static void main(String[] args) {
		logger.info("START");
		
		{
			for(File file: FileUtil.listFile(Price.PATH_DIR_DATA)) {
				List<Price> list = CSVUtil.read(Price.class).file(file);
				for(Price price: list) {
					if (price.high < price.low) {
						double t = price.low;
						price.low = price.high;
						price.high = t;
					}
				}
				CSVUtil.write(Price.class).file(file, list);
			}
		}
		
		logger.info("STOP");
	}
}
