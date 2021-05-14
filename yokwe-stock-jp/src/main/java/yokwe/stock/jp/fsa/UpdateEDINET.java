package yokwe.stock.jp.fsa;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.slf4j.LoggerFactory;

import yokwe.util.CSVUtil;
import yokwe.util.http.HttpUtil;

public class UpdateEDINET {
	private static final org.slf4j.Logger logger = LoggerFactory.getLogger(UpdateEDINET.class);
	
	private static final String URL_DOWNLOAD     = "https://disclosure.edinet-fsa.go.jp/E01EW/download?uji.verb=W1E62071EdinetCodeDownload&uji.bean=ee.bean.W1E62071.EEW1E62071Bean&TID=W1E62071&PID=W1E62071&SESSIONKEY=9999&downloadFileName=&lgKbn=2&dflg=0&iflg=0&dispKbn=1";
	private static final String CHARSET_DOWNLOAD = "MS932";
	private static final String ENTRY_NAME       = "EdinetcodeDlInfo.csv";


	public static void main(String[] args) throws IOException {
		logger.info("START");
		
		logger.info("download {}", URL_DOWNLOAD);
		HttpUtil http = HttpUtil.getInstance().withRawData(true);
		
		HttpUtil.Result result = http.download(URL_DOWNLOAD);
		logger.info("result {}", result.rawData.length);
		
		try (ZipInputStream zipInputStream = new ZipInputStream(new ByteArrayInputStream(result.rawData))) {
		    for(;;) {
		        ZipEntry zipEntry = zipInputStream.getNextEntry();
		        if (zipEntry == null) break;
		        
		        String name = zipEntry.getName();		        
		        logger.info("entry  {}", name);
		        
		        if (name.equals(ENTRY_NAME)) {
			        try (InputStreamReader isr = new InputStreamReader(new ByteArrayInputStream(zipInputStream.readAllBytes()), CHARSET_DOWNLOAD)) {
			        	// Skip first line
			        	for(;;) {
			        		int c = isr.read();
			        		if (c == -1) break;
			        		if (c == '\n') break;
			        	}
			        	
			        	List<EDINET> list = CSVUtil.read(EDINET.class).file(isr);
			        							
						logger.info("write  {}   {}", list.size(), EDINET.getPath());
						EDINET.save(list);
			        }
		        }
		    }
		}

		logger.info("STOP");
	}
}
