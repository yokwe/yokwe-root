package yokwe.util.test;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;

import org.apache.hc.core5.http2.HttpVersionPolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import yokwe.util.http.Download;
import yokwe.util.http.DownloadAsync;
import yokwe.util.http.RequesterBuilder;
import yokwe.util.http.Result;
import yokwe.util.http.Task;

public class T104 {
	private static final Logger logger = LoggerFactory.getLogger(T104.class);
	
	public static void main(String[] args) throws InterruptedException, ExecutionException {
		logger.info("START");
		
		Download download = new DownloadAsync();

		// Configure Requester
		download.setRequesterBuilder(
				RequesterBuilder.custom()
				.setVersionPolicy(HttpVersionPolicy.NEGOTIATE)
				.setSoTimeout(10)
				.setMaxTotal(50)
				.setDefaultMaxPerRoute(50));
		
		// Configure custom header
		download.setUserAgent("Mozilla/5.0 (X11; Linux x86_64) AppleWebKit");
		download.setReferer("https://www.jpx.co.jp/");
		
		// Configure thread count
		download.setThreadCount(30);
		
//		Consumer<Result> consumer = o -> logger.info("XXX {}  {}  {}", o.task.uri, o.body.length, new String(o.body, o.charset));
//		Consumer<Result> consumer = o -> logger.info("XXX {}  {}", o.task.uri, o.body.length);
		Consumer<Result> consumer = o -> {};
		
		{
			List<Stock> stockList = Stock.getList();
			Collections.shuffle(stockList);
			
			for(Stock stock: stockList) {
				String stockCode4 = Stock.toStockCode4(stock.stockCode);
				String uriString = String.format("https://quote.jpx.co.jp/jpx/template/quote.cgi?F=tmp/stock_detail&MKTN=T&QCODE=%s", stockCode4);
				Task task = new Task(uriString, consumer);
				download.addTask(task);
			}
			
			logger.info("BEFORE RUN");
			download.startAndWait();
			logger.info("AFTER  RUN");
			download.showRunCount();
		}
		
		logger.info("STOP");
	}

}
