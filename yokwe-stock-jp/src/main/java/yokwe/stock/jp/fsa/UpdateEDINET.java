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

public class UpdateEDINET {
	private static final org.slf4j.Logger logger = LoggerFactory.getLogger(UpdateEDINET.class);

	public static void main(String[] args) throws IOException {
		logger.info("START");
		
		logger.info("download {}", EDINET.URL_DOWNLOAD);
		HttpUtil http = HttpUtil.getInstance().withRawData(true);
		
		HttpUtil.Result result = http.download(EDINET.URL_DOWNLOAD);
		logger.info("result {}", result.rawData.length);
		
		logger.info("write {} {}", EDINET.PATH_DOWNLOAD, result.rawData.length);
		FileUtil.rawWrite().file(EDINET.PATH_DOWNLOAD, result.rawData);
		
		logger.info("read {}", EDINET.PATH_DOWNLOAD);
		try (ZipFile zipFile = new ZipFile(EDINET.PATH_DOWNLOAD)) {
			Enumeration<? extends ZipEntry> entries = zipFile.entries();

		    while(entries.hasMoreElements()){
		        ZipEntry entry = entries.nextElement();
		        String name = entry.getName();		        
		        logger.info("entry  {}  {}", name, entry.getSize());
		        
		        if (!name.endsWith(".csv")) continue;

		        if (name.equals(EDINET.ENTRY_NAME)) {
			        try (InputStreamReader isr = new InputStreamReader(zipFile.getInputStream(entry), EDINET.CHARSET_DOWNLOAD)) {
			        	// Skip first line
			        	for(;;) {
			        		int c = isr.read();
			        		if (c == -1) break;
			        		if (c == '\n') break;
			        	}
			        	
			        	List<EDINET> list = CSVUtil.read(EDINET.class).file(isr);
			        	
						// Sort before write
						Collections.sort(list);
						
						String path = EDINET.PATH_DATA;
						logger.info("write  {}   {}", path, list.size());
						CSVUtil.write(EDINET.class).file(path, list);
			        }
		        }
		    }
		}

		logger.info("STOP");
	}
}
