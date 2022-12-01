package yokwe.util.test;

import java.io.IOException;

import org.apache.hc.core5.http.HttpException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import yokwe.util.http.HttpUtil;

public class T111 {
	private static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();
	
	public static final String URL_DOWNLOAD     = "https://disclosure.edinet-fsa.go.jp/E01EW/download?uji.verb=W1E62071EdinetCodeDownload&uji.bean=ee.bean.W1E62071.EEW1E62071Bean&TID=W1E62071&PID=W1E62071&SESSIONKEY=9999&downloadFileName=&lgKbn=2&dflg=0&iflg=0&dispKbn=1";

	public static void main(String[] args) throws IOException, HttpException {
		logger.info("START");
		
		HttpUtil http = HttpUtil.getInstance().withRawData(true);
		
		logger.info("download {}", URL_DOWNLOAD);
		HttpUtil.Result result = http.download(URL_DOWNLOAD);
		logger.info("result {}", result.rawData.length);

        
		logger.info("STOP");
	}
}
