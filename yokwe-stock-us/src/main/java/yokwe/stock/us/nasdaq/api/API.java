package yokwe.stock.us.nasdaq.api;

import yokwe.util.StringUtil;
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
		if (string.isEmpty()) return ""; // FIXME
		
		// 12/27/2021 => 2021-12-07
		String[] mdy = string.split("\\/");
		if (mdy.length != 3) {
			logger.error("Unpexpected");
			logger.error("  date {}", string);
			throw new UnexpectedException("Unpexpected");
		}
		return mdy[2] + "-" + mdy[0] + "-" + mdy[1];
	}
	
	public static final String STOCK = "stock";
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
	
	public static <E> E getInstance(Class<E> clazz, String url) {
		HttpUtil.Result result = HttpUtil.getInstance().download(url);
		return result == null ? null : JSON.unmarshal(clazz, result.result);
	}
	
	public interface GetURL {
		public String get(String symbol, String assetClass);
	}
	
	public static <E> E getInstance(Class<E> clazz, GetURL getURL, String symbol, String assetClass) {
		checkAssetClass(assetClass);
		return getInstance(clazz, getURL.get(symbol, assetClass));
	}

}
