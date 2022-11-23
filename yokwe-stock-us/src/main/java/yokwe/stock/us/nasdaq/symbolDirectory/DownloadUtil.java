package yokwe.stock.us.nasdaq.symbolDirectory;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import yokwe.util.UnexpectedException;

public class DownloadUtil {
	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(DownloadUtil.class);

	public static URL toURL(String urlString) {
		try {
			URL url = new URL(urlString);
			return url;
		} catch (MalformedURLException e) {
			String exceptionName = e.getClass().getSimpleName();
			logger.error("{} {}", exceptionName, e);
			throw new UnexpectedException(exceptionName, e);
		}
	}
	
	public static byte[] download(URL url) {
		try {
			URLConnection con = url.openConnection();
			try (InputStream is = con.getInputStream()) {
				byte[] ret = is.readAllBytes();
				return ret;
			}
		} catch (IOException e) {
			String exceptionName = e.getClass().getSimpleName();
			logger.error("{} {}", exceptionName, e);
			throw new UnexpectedException(exceptionName, e);
		}
	}
	public static byte[] download(String urlString) {
		return download(toURL(urlString));
	}
}
