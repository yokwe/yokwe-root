package yokwe.stock.us.nyse;

import java.util.List;
import java.util.stream.Collectors;

import yokwe.stock.us.nyse.NYSESymbol.Data;
import yokwe.util.UnexpectedException;
import yokwe.util.http.HttpUtil;
import yokwe.util.json.JSON;

public class UpdateNYSESymbol {
	private static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();

	public static final String URL = "https://www.nyse.com/api/quotes/filter";
	public static final String BODY_FORMAT = "{\"instrumentType\":\"%s\",\"pageNumber\":1,\"sortColumn\":\"NORMALIZED_TICKER\",\"sortOrder\":\"ASC\",\"maxResultsPerPage\":10000,\"filterToken\":\"\"}";
	public static final String CONTENT_TYPE = "application/json";

	private static String getBody(String instrumentType) {
		return String.format(BODY_FORMAT, instrumentType);
	}
	
	private static void downloadStock() {
		logger.info("download stock");
		String body = getBody("EQUITY");
		
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
		List<Data> list2 = list.stream().filter(o -> !o.symbolTicker.startsWith("E:")).collect(Collectors.toList());
		logger.info("save  {}  {}", list2.size(), NYSESymbol.Stock.getPath());
		NYSESymbol.Stock.save(list2);
	}

	private static void downloadETF() {
		logger.info("download ETF");
		String body = getBody("EXCHANGE_TRADED_FUND");
		
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
		List<Data> list2 = list.stream().filter(o -> !o.symbolTicker.startsWith("E:")).collect(Collectors.toList());
		logger.info("save  {}  {}", list2.size(), NYSESymbol.ETF.getPath());
		NYSESymbol.ETF.save(list2);
	}
	
	public static void main(String[] args) {
		logger.info("START");
		
		downloadStock();
		downloadETF();

		logger.info("STOP");
	}
}
