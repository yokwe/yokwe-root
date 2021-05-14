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

public class UpdateFund {
	private static final org.slf4j.Logger logger = LoggerFactory.getLogger(UpdateFund.class);

	private static final String URL_DOWNLOAD     = "https://disclosure.edinet-fsa.go.jp/E01EW/download?uji.verb=W1E62071FundCodeDownload&uji.bean=ee.bean.W1E62071.EEW1E62071Bean&TID=W1E62071&PID=W1E62071&SESSIONKEY=9999&downloadFileName=&lgKbn=2&dflg=0&iflg=0&dispKbn=1";
	private static final String CHARSET_DOWNLOAD = "MS932";
	
	private static final String ENTRY_NAME    = "FundcodeDlInfo.csv";
	
	public static void main(String[] args) throws IOException {
		logger.info("START");
		
		logger.info("download {}", URL_DOWNLOAD);
		HttpUtil http = HttpUtil.getInstance().withRawData(true);
		
		HttpUtil.Result result = http.download(URL_DOWNLOAD);
		logger.info("result {}", result.rawData.length);
		
		// TODO Use ZipInputStream instead of ZipFile
		
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
			        	
			        	List<Fund> list = CSVUtil.read(Fund.class).file(isr);
						logger.info("write  {}   {}", list.size(), Fund.getPath());
						Fund.save(list);
			        }
		        }
		    }
		}

		logger.info("STOP");
	}
}
