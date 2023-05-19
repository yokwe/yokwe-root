package yokwe.stock.us.nyse;

import java.util.List;

import yokwe.stock.us.nyse.NYSESymbol.Data;
import yokwe.util.UnexpectedException;
import yokwe.util.http.HttpUtil;
import yokwe.util.json.JSON;

public class Filter {
	private static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();

	public static final String URL          = "https://www.nyse.com/api/quotes/filter";
	public static final String BODY_FORMAT  = "{\"instrumentType\":\"%s\",\"pageNumber\":1,\"sortColumn\":\"NORMALIZED_TICKER\",\"sortOrder\":\"ASC\",\"maxResultsPerPage\":10000,\"filterToken\":\"\"}";
	public static final String CONTENT_TYPE = "application/json";

	public enum Kind {
		ETF("EXCHANGE_TRADED_FUND"),
		STOCK("EQUITY");
		
		public final String instrumentType;
		Kind(String instrumentType) {
			this.instrumentType = instrumentType;
		}
	}
	
	public static List<Data> download(Kind kind) {
		logger.info("download {}", kind);
		String body = String.format(BODY_FORMAT, kind.instrumentType);
		
		HttpUtil.Result result = HttpUtil.getInstance().withPost(body, CONTENT_TYPE).download(URL);
		
		if (result == null) {
			throw new UnexpectedException("result == null");
		}
		if (result.result == null) {
			throw new UnexpectedException("result.result == null");
		}
		
		logger.info("result  {}", result.result.length());
		List<Data> list = JSON.getList(Data.class, result.result);
		logger.info("list  {}", list.size());
		return list;
	}
}
