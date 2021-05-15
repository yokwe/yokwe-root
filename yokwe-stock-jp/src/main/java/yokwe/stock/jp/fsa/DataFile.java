package yokwe.stock.jp.fsa;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.slf4j.LoggerFactory;

import yokwe.util.CSVUtil;
import yokwe.util.UnexpectedException;
import yokwe.util.http.HttpUtil;

public class DataFile {
	static final org.slf4j.Logger logger = LoggerFactory.getLogger(DataFile.class);

	private static class EDNETFile {
		private static final String URL_DOWNLOAD     = "https://disclosure.edinet-fsa.go.jp/E01EW/download?uji.verb=W1E62071EdinetCodeDownload&uji.bean=ee.bean.W1E62071.EEW1E62071Bean&TID=W1E62071&PID=W1E62071&SESSIONKEY=9999&downloadFileName=&lgKbn=2&dflg=0&iflg=0&dispKbn=1";
		private static final String CHARSET_DOWNLOAD = "MS932";
		private static final String ENTRY_NAME       = "EdinetcodeDlInfo.csv";

		private static void update(HttpUtil httpUtil) {
			logger.info("update EDINET");

			HttpUtil.Result result = httpUtil.download(URL_DOWNLOAD);
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
			} catch (IOException e) {
				String exceptionName = e.getClass().getSimpleName();
				logger.error("{} {}", exceptionName, e);
				throw new UnexpectedException(exceptionName, e);
			}

		}
	}
	
	private static class FundFile {
		private static final String URL_DOWNLOAD     = "https://disclosure.edinet-fsa.go.jp/E01EW/download?uji.verb=W1E62071FundCodeDownload&uji.bean=ee.bean.W1E62071.EEW1E62071Bean&TID=W1E62071&PID=W1E62071&SESSIONKEY=9999&downloadFileName=&lgKbn=2&dflg=0&iflg=0&dispKbn=1";
		private static final String CHARSET_DOWNLOAD = "MS932";
		private static final String ENTRY_NAME       = "FundcodeDlInfo.csv";

		private static void update(HttpUtil httpUtil) {
			logger.info("update Fund");
			HttpUtil.Result result = httpUtil.download(URL_DOWNLOAD);
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
				        	
				        	List<Fund> list = CSVUtil.read(Fund.class).file(isr);
							logger.info("write  {}   {}", list.size(), Fund.getPath());
							Fund.save(list);
				        }
			        }
			    }
			} catch (IOException e) {
				String exceptionName = e.getClass().getSimpleName();
				logger.error("{} {}", exceptionName, e);
				throw new UnexpectedException(exceptionName, e);
			}
		}
	}
	
	public static void main(String[] args) {
		logger.info("START");
		
		HttpUtil httpUtil = HttpUtil.getInstance().withRawData(true);

		EDNETFile.update(httpUtil);
		FundFile.update(httpUtil);
		
		logger.info("STOP");
	}
}
