package yokwe.finance.provider.jpx;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

import yokwe.finance.type.StockCodeJP;
import yokwe.util.UnexpectedException;
import yokwe.util.http.Download;
import yokwe.util.http.DownloadSync;
import yokwe.util.http.RequesterBuilder;
import yokwe.util.http.StringTask;
import yokwe.util.http.Task;

public class UpdateStockDetail {
	private static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();
	
	private static final String URL_FORMAT = "https://quote.jpx.co.jp/jpxhp/jcgi/wrap/qjsonp.aspx?F=ctl/stock_detail&qcode=%s";
	private static final String REFERENCE  = "https://quote.jpx.co.jp/jpxhp/main/index.aspx?f=stock_detail&disptype=information&qcode=%s";
	private static final String USER_AGENT = "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit";
	
	private static final List<StockListType> stockList = StorageJPX.StockList.getList();
	private static final int stockListSize = stockList.size();
	
	public static String getURL(String stockCode) {
		var stockCode4 = StockCodeJP.toStockCode4(stockCode);
		return String.format(URL_FORMAT, stockCode4);
	}

	private static class Context {
		private final AtomicInteger count = new AtomicInteger();
		
		public void incrementBuildCount() {
			count.addAndGet(1);
		}
		public int getBuildCount() {
			return count.intValue();
		}
	}
	
	private static class MyConsumer implements Consumer<String> {
		private final Context   context;
		private final String    stockCode;
		
		MyConsumer(Context context, String stockCode) {
			this.context   = context;
			this.stockCode = stockCode;
		}
		@Override
		public void accept(String page) {
			// save for later use
			StorageJPX.StockDetailJSON.save(stockCode, page);
			//
			context.incrementBuildCount();
		}
	}

	public static void downloadFile() {
		int threadCount       = 20;
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
				.setSoTimeout(soTimeout)
				.setMaxTotal(maxTotal)
				.setDefaultMaxPerRoute(maxPerRoute);

//		Download download = new DownloadAsync();
		Download download = new DownloadSync();
		
		download.setRequesterBuilder(requesterBuilder);
		
		// Configure custom header
		download.setUserAgent(USER_AGENT);
		download.setReferer(REFERENCE);
		
		// Configure thread count
		download.setThreadCount(threadCount);
		
		// connection timeout in second
		download.setConnectionTimeout(connectionTimeout);
		
		// progress interval
		download.setProgressInterval(progressInterval);
		
		Context context = new Context();
		
		Collections.shuffle(stockList);
		for(var stock: stockList) {
			String stockCode = stock.stockCode;			
			String uriString = getURL(stockCode);
			Task   task      = StringTask.get(uriString, new MyConsumer(context, stockCode));
			download.addTask(task);
		}
		Collections.sort(stockList);

		logger.info("BEFORE RUN");
		download.startAndWait();
		logger.info("AFTER  RUN");
//		download.showRunCount();

		try {
			for(int i = 0; i < 10; i++) {
				int buildCount = context.getBuildCount();
				if (buildCount == stockListSize) break;
				logger.info("buildCount {} / {}", buildCount, stockListSize);
				Thread.sleep(1000);
			}
			{
				int buildCount = context.getBuildCount();
				if (buildCount != stockListSize) {
					logger.error("Unexpected");
					logger.error("  buildCount    {}", buildCount);
					logger.error("  stockListSize {}", stockListSize);
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
	
	private static void update() {
		downloadFile();
	}
	
	public static void main(String[] args) {
		logger.info("START");
		
		update();
		
		logger.info("STOP");
	}
}
