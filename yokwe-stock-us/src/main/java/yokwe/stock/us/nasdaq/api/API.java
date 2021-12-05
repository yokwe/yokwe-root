package yokwe.stock.us.nasdaq.api;

import java.io.File;

import yokwe.util.FileUtil;
import yokwe.util.UnexpectedException;
import yokwe.util.http.HttpUtil;
import yokwe.util.json.JSON;

public final class API {	
	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(API.class);

	// https://api.nasdaq.com/api/quote/YYY/dividends?assetclass=etf
	// https://api.nasdaq.com/api/quote/LMT/dividends?assetclass=stocks
	
	// https://api.nasdaq.com/api/quote/LMT/realtime-trades?&limit=5
	
	// https://api.nasdaq.com/api/quote/LMT/extended-trading?assetclass=stocks&markettype=post
	
	// https://api.nasdaq.com/api/quote/LMT/extended-trading?assetclass=stocks&markettype=pre
	
	// https://api.nasdaq.com/api/quote/FR10UK/chart?assetclass=index
	// https://api.nasdaq.com/api/quote/FR10UK/info?assetclass=index
	
	public static String encodeSymbolForURL(String symbol) {
		// TRTN-A => TRTN%5EA
		return symbol.replace("-", "%5E");
	}
	
	public static String convertDate(String string) {
		string = string.trim().replace("N/A", "");
		if (string.isEmpty()) return "";
		
		// 12/27/2021 => 2021-12-07
		String[] mdy = string.split("\\/");
		if (mdy.length != 3) {
			logger.error("Unpexpected");
			logger.error("  date {}", string);
			throw new UnexpectedException("Unpexpected");
		}
		return mdy[2] + "-" + mdy[0] + "-" + mdy[1];
	}
	
	public static final String STOCK = "stocks";
	public static final String ETF   = "etf";
	public static void checkAssetClass(String assetClass) {
		switch(assetClass) {
		case ETF:
		case STOCK:
			break;
		default:
			logger.error("Unexpected assetClass");
			logger.error("  assetClass {}!", assetClass);
			throw new UnexpectedException("Unexpected assetClass");
		}
	}
	
	public static String download(String url, File file) {
		String ret;
		if (file.exists()) {
			ret = FileUtil.read().file(file);
		} else {
			int retryCount = 0;
			int retryLimit = 10;
			for(;;) {
				HttpUtil.Result result = HttpUtil.getInstance().download(url);
				if (result.result == null) {
					// failed to download
					ret = null;
				} else {
					FileUtil.write().file(file, result.result);
					ret = result.result;
				}
				if (ret == null) {
					retryCount++;
					logger.warn("download failed");
					logger.warn("  retry    {}", retryCount);
					logger.warn("  url      {}", url);
					logger.warn("  response {}", result.response);
					if (retryCount == retryLimit) {
						logger.error("Exceed retry limit");
						throw new UnexpectedException("Exceed retry limit");
					}
					// sleep for a while
					try {
						Thread.sleep(1000 * retryCount);
					} catch (InterruptedException e) {
						String exceptionName = e.getClass().getSimpleName();
						logger.error("{} {}", exceptionName, e);
						throw new UnexpectedException(exceptionName, e);
					}
					continue;
				}
				break;
			}
		}
		return ret;
	}
	public static String download(String url, String path) {
		return download(url, new File(path));
	}
	
	public static <E> E getInstance(Class<E> clazz, String url, String path) {
		String string = download(url, path);
		return string == null ? null : JSON.unmarshal(clazz, string);
	}
}
