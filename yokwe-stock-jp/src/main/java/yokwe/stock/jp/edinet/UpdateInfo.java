package yokwe.stock.jp.edinet;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import yokwe.util.ListUtil;
import yokwe.util.UnexpectedException;
import yokwe.util.http.HttpUtil;

public class UpdateInfo {
	private static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();

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
				        	
				        	List<EDINETInfo> list = ListUtil.getList(EDINETInfo.class, isr);
				        							
							logger.info("write  {}   {}", list.size(), EDINETInfo.getPath());
							EDINETInfo.save(list);
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
				        	
				        	List<FundInfo> list = ListUtil.getList(FundInfo.class, isr);
							logger.info("write  {}   {}", list.size(), FundInfo.getPath());
							FundInfo.save(list);
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

		// FIXME download of edinet file and fund file does not work !!
		// FIXME may be time to use Selenium for doenload
		EDNETFile.update(httpUtil);
		FundFile.update(httpUtil);
		
		logger.info("STOP");
	}
}
