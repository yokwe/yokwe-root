package yokwe.stock.us.nasdaq;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Consumer;

import org.apache.hc.core5.http2.HttpVersionPolicy;

import yokwe.stock.us.Storage;
import yokwe.stock.us.nasdaq.UpdatePrice.Symbol;
import yokwe.stock.us.nasdaq.api.Historical;
import yokwe.stock.us.nasdaq.api.Quote;
import yokwe.util.CSVUtil;
import yokwe.util.Market;
import yokwe.util.StringUtil;
import yokwe.util.UnexpectedException;
import yokwe.util.http.Download;
import yokwe.util.http.DownloadSync;
import yokwe.util.http.RequesterBuilder;
import yokwe.util.http.StringTask;
import yokwe.util.http.Task;
import yokwe.util.json.JSON;

public class UpdatePrice2 {
	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(UpdatePrice2.class);

	public static class Request {
		public String symbol;     // normalized symbol like TRNT-A and RDS.A not like TRTN^A and RDS/A
		public String assetClass; // STOCKS or ETF
		
		public Request(String symbol, String assetClass) {
			this.symbol     = symbol;
			this.assetClass = assetClass;
		}

		@Override
		public String toString() {
			return StringUtil.toString(this);
		}
	}

	
	// Context class store result of Task for later use
	private static class Context {
		private Map<String, List<Price>> priceListMap;
		//          symbol
		private Integer                  buildCount;
		
		public Context() {
			priceListMap = new TreeMap<>();
			buildCount   = 0;
		}

		public void put(String key, List<Price> value) {
			synchronized(priceListMap) {
				priceListMap.put(key, value);
			}
		}
		public void incrementBuildCount() {
			synchronized(buildCount) {
				buildCount = buildCount + 1;
			}
		}
		public int getBuildCount() {
			int ret;
			synchronized(buildCount) {
				ret = buildCount;
			}
			return ret;
		}
	}
	
	private static class MyConsumer implements Consumer<String> {
		private final Context context;
		private final String  symbol;
		
		public MyConsumer(Context context, String symbol) {
			this.context = context;
			this.symbol  = symbol;
		}
		@Override
		public void accept(String string) {
			context.incrementBuildCount();
			buildContextFromString(context, symbol, string);			
		}
	}
	
	private static void buildContextFromString(Context context, String symbol, String string) {
		Historical historical = JSON.unmarshal(Historical.class, string);
		
		if (historical.data == null) {
			logger.warn("no data {}", symbol);
			return; // no data
		}
		if (historical.data.tradesTable == null) {
			logger.warn("no tradesTable {}", symbol);
			return; // no data
		}
		if (historical.data.tradesTable.rows == null) {
			logger.warn("no rows {}", symbol);
			return; // no data
		}
		
		List<Price> list = new ArrayList<>();
		
		for(var e: historical.data.tradesTable.rows) {
			// close: "194.39", date: "11/26/2021", high: "196.82", low: "194.19", open: "196.82", volume: "11,113"
			// close: "$17.86", date: "11/26/2021", high: "$18.155", low: "$17.765", open: "$18.03", volume: "1,645,865
			String date  = Quote.convertDate(e.date);
			String open  = e.open.replace("$", "");
			String high  = e.high.replace("$", "");
			String low   = e.low.replace("$", "");
			String close = e.close.replace("$", "");
			String value = e.volume.replace(",", "").replace("N/A", "0");
			
			Price price = new Price(
				symbol,
				date,
				Double.parseDouble(open),
				Double.parseDouble(high),
				Double.parseDouble(low),
				Double.parseDouble(close),
				Long.parseLong(value));
			
			list.add(price);
		}
		context.put(symbol, list);
	}

	private static void buildContext(Context context, List<Request> requestList, LocalDate fromDate, LocalDate toDate) {
		int threadCount       = 10;
		int maxPerRoute       = 50;
		int maxTotal          = 100;
		int soTimeout         = 30;
		int connectionTimeout = 30;
		int progressInterval  = 1000;
		logger.info("threadCount       {}", threadCount);
		logger.info("maxPerRoute       {}", maxPerRoute);
		logger.info("maxTotal          {}", maxTotal);
		logger.info("soTimeout         {}", soTimeout);
		logger.info("connectionTimeout {}", connectionTimeout);
		logger.info("progressInterval  {}", progressInterval);
		
		RequesterBuilder requesterBuilder = RequesterBuilder.custom()
				.setVersionPolicy(HttpVersionPolicy.NEGOTIATE)
				.setSoTimeout(soTimeout)
				.setMaxTotal(maxTotal)
				.setDefaultMaxPerRoute(maxPerRoute);

//		Download download = new DownloadAsync();
		Download download = new DownloadSync();
		
		download.setRequesterBuilder(requesterBuilder);
		
		// Configure custom header
		download.setUserAgent("Mozilla/5.0 (X11; Linux x86_64) AppleWebKit");
		download.setReferer("https://www.jpx.co.jp/");
		
		// Configure thread count
		download.setThreadCount(threadCount);
		
		// connection timeout in second
		download.setConnectionTimeout(connectionTimeout);
		
		// progress interval
		download.setProgressInterval(progressInterval);
		
		// add task to download
		Collections.shuffle(requestList);
		for(Request request: requestList) {
			String symbol = request.symbol;
			String uriString = Historical.getURL(request.assetClass, request.symbol, fromDate, toDate);
			
			Task   task      = StringTask.get(uriString, new MyConsumer(context, symbol));
			download.addTask(task);
		}
		int requestListSize = requestList.size();

		logger.info("BEFORE RUN");
		download.startAndWait();
		logger.info("AFTER  RUN");
//		download.showRunCount();

		try {
			for(int i = 0; i < 10; i++) {
				int buildCount = context.getBuildCount();
				if (buildCount == requestListSize) break;
				logger.info("buildCount {} / {}", buildCount, requestListSize);
				Thread.sleep(1000);
			}
			{
				int buildCount = context.getBuildCount();
				if (buildCount != requestListSize) {
					logger.error("Unexpected");
					logger.error("  buildCount    {}", buildCount);
					logger.error("  stockListSize {}", requestListSize);
					throw new UnexpectedException("Unexpected");
				}
			}
			logger.info("AFTER  WAIT");
		} catch (InterruptedException e) {
			String exceptionName = e.getClass().getSimpleName();
			logger.error("{} {}", exceptionName, e);
			throw new UnexpectedException(exceptionName, e);
		}
	}
	
	public static void main(String[] args) {
		logger.info("START");
						
		// build toDate and fromDate
		LocalDate toDate = Market.getLastTradingDate();
		LocalDate fromDate;
		{
			fromDate = toDate.minusYears(1);
			if (Market.isClosed(fromDate)) {
				fromDate = Market.getPreviousTradeDate(fromDate);
			}
			logger.info("date range {} - {}", fromDate, toDate);
		}
		
		// build symbolList from symbol.csv
		List<Symbol> symbolList = CSVUtil.read(Symbol.class).file(Storage.NASDAQ.getPath("symbol.csv"));
		logger.info("symbol    {}", symbolList.size());
		
		// build requestList
		List<Request> requestList = new ArrayList<>();
		{
			List<String>       unknownList = new ArrayList<>();
			Map<String, Stock> stockMap    = Stock.getMap();

			for(var e: symbolList) {
				String symbol = e.symbol;
				if (stockMap.containsKey(symbol)) {
					Stock stock = stockMap.get(symbol);

					requestList.add(new Request(stock.symbol, stock.assetClass));
				} else {
					unknownList.add(symbol);
				}
			}
			logger.info("unknown   {} {}", unknownList.size(), unknownList);
			logger.info("request   {}", requestList.size());
		}
		
		Context context = new Context();
		buildContext(context, requestList, fromDate, toDate);
		logger.info("result    {}", context.priceListMap.size());

		for(var e: context.priceListMap.entrySet()) {
			String      symbol    = e.getKey();
			List<Price> priceList = e.getValue();
			
			if (priceList == null) {
				logger.warn("failed to get price of {}", symbol);
			} else {
				Price.save(priceList);
			}
		}
		
		logger.info("STOP");		
	}
}
