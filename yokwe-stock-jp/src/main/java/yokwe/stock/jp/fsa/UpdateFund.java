package yokwe.stock.jp.fsa;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.slf4j.LoggerFactory;

import yokwe.util.CSVUtil;
import yokwe.util.FileUtil;
import yokwe.util.http.HttpUtil;

public class UpdateFund {
	private static final org.slf4j.Logger logger = LoggerFactory.getLogger(UpdateFund.class);

	public static void main(String[] args) throws IOException {
		logger.info("START");
		
		logger.info("download {}", Fund.URL_DOWNLOAD);
		HttpUtil http = HttpUtil.getInstance().withRawData(true);
		
		HttpUtil.Result result = http.download(Fund.URL_DOWNLOAD);
		logger.info("result {}", result.rawData.length);
		
		logger.info("write {} {}", Fund.PATH_DOWNLOAD, result.rawData.length);
		FileUtil.rawWrite().file(Fund.PATH_DOWNLOAD, result.rawData);
		
		logger.info("read {}", Fund.PATH_DOWNLOAD);
		try (ZipFile zipFile = new ZipFile(Fund.PATH_DOWNLOAD)) {
			Enumeration<? extends ZipEntry> entries = zipFile.entries();

		    while(entries.hasMoreElements()){
		        ZipEntry entry = entries.nextElement();
		        String name = entry.getName();		        
		        logger.info("entry  {}  {}", name, entry.getSize());
		        
		        if (!name.endsWith(".csv")) continue;

		        if (name.equals(Fund.ENTRY_NAME)) {
			        try (InputStreamReader isr = new InputStreamReader(zipFile.getInputStream(entry), Fund.CHARSET_DOWNLOAD)) {
			        	// Skip first line
			        	for(;;) {
			        		int c = isr.read();
			        		if (c == -1) break;
			        		if (c == '\n') break;
			        	}
			        	
			        	List<Fund> list = CSVUtil.read(Fund.class).file(isr);
			        	
						// Sort before write
						Collections.sort(list);
						
						String path = Fund.PATH_DATA;
						logger.info("write  {}   {}", path, list.size());
						CSVUtil.write(Fund.class).file(path, list);
			        }
		        }
		    }
		}

		logger.info("STOP");
	}
}
