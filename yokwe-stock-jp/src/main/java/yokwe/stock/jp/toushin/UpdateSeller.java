package yokwe.stock.jp.toushin;

import java.net.URI;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.slf4j.LoggerFactory;

import yokwe.util.UnexpectedException;
import yokwe.util.http.HttpUtil;

public class UpdateSeller {
	static final org.slf4j.Logger logger = LoggerFactory.getLogger(UpdateSeller.class);

	// https://toushin-lib.fwg.ne.jp/FdsWeb/FDST030000/company-search
	
	public static void init() {
//		System.setProperty("http.proxyHost", "192.168.86.40");
//		System.setProperty("http.proxyPort", "9090");
//		System.setProperty("https.proxyHost", "192.168.86.40");
//		System.setProperty("https.proxyPort", "9090");
		
        try {
            TrustManager[] trustManager = new TrustManager[] {
                	(TrustManager) new X509TrustManager() {
        	            public java.security.cert.X509Certificate[] getAcceptedIssuers() { return null; }
        	            public void checkClientTrusted(X509Certificate[] certs, String authType) { }
        	            public void checkServerTrusted(X509Certificate[] certs, String authType) { }

                } };

        	
            SSLContext sslContext = SSLContext.getInstance("SSL");
            sslContext.init(null, trustManager, new SecureRandom());

            HttpsURLConnection.setDefaultSSLSocketFactory(sslContext.getSocketFactory());
        } catch (NoSuchAlgorithmException | KeyManagementException e) {
			String exceptionName = e.getClass().getSimpleName();
			logger.error("{} {}", exceptionName, e);
			throw new UnexpectedException(exceptionName, e);
		}
	}

	
	public static void main(String[] args) {
		logger.info("START");
		
		init();
		
		String isinCode = "JP90C0009VE0";
		
		HttpUtil httpUtil = HttpUtil.getInstance();

		// https://toushin-lib.fwg.ne.jp/FdsWeb/FDST030000?isinCd=JP90C0009VE0
		String referer   = String.format("https://toushin-lib.fwg.ne.jp/FdsWeb/FDST030000?isinCd=%s", isinCode);
		String urlString = "https://toushin-lib.fwg.ne.jp/FdsWeb/FDST030000/company-search";
		URI uri;
		try {
			uri = new URI(urlString);
		} catch (URISyntaxException e) {
			String exceptionName = e.getClass().getSimpleName();
			logger.error("{} {}", exceptionName, e);
			throw new UnexpectedException(exceptionName, e);
		}
		
		{
			HttpUtil.Result result = httpUtil
				.withHeader("Accept",           "*/*")
				.withHeader("X-Requested-With", "XMLHttpRequest")
				.withHeader("Origin",           uri.getHost())
				.withHeader("Referer",          referer)
				.withHeader("Accept-Encoding",  "gzip, deflate")
				.withHeader("Accept-Language",  "ja")
				.withPost("isinCd=JP90C0009VE0", "application/x-www-form-urlencoded; charset=UTF-8")
				.download(urlString);
			
			logger.info("result {} {}", result.result.length(), result.response);
			logger.info("  {}", result.result);
		}
		
		logger.info("STOP");
	}
}
