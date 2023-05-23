package yokwe.util;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

public class FTPUtil {
	private static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();

	//
	// returns null for FileNotFoundException
	//
	public static byte[] download(URL url) {
		try {
			URLConnection con = url.openConnection();
			try (InputStream is = con.getInputStream()) {
				byte[] ret = is.readAllBytes();
				return ret;
			}
		} catch (FileNotFoundException e) {
			logger.warn("FileNotFoundException {}", url);
			return null;
		} catch (IOException e) {
			String exceptionName = e.getClass().getSimpleName();
			logger.error("{} {}", exceptionName, e);
			throw new UnexpectedException(exceptionName, e);
		}
	}
	public static byte[] download(String urlString) {
		try {
			URL url = new URL(urlString);
			return download(url);
		} catch (MalformedURLException e) {
			String exceptionName = e.getClass().getSimpleName();
			logger.error("{} {}", exceptionName, e);
			throw new UnexpectedException(exceptionName, e);
		}
	}
}