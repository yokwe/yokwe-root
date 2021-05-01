package yokwe.stock.jp.jpx;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.hc.core5.http2.HttpVersionPolicy;
import org.slf4j.LoggerFactory;

import yokwe.stock.jp.data.Price;
import yokwe.util.FileUtil;
import yokwe.util.UnexpectedException;
import yokwe.util.http.Download;
import yokwe.util.http.DownloadAsync;
import yokwe.util.http.FileTask;
import yokwe.util.http.RequesterBuilder;
import yokwe.util.http.Task;


public class DownloadStockPage {
	private static final org.slf4j.Logger logger = LoggerFactory.getLogger(DownloadStockPage.class);
	
	private static void delist() {
		List<Stock> list = Stock.getList();
		Set<String> stockCodeSet = list.stream().map(o -> o.stockCode).collect(Collectors.toSet());
		
		List<File> fileList = FileUtil.listFile(new File(Price.PATH_DIR_DATA));
		
		File dirDeslist = new File(Price.PATH_DIR_DATA_DELIST);
		if (!dirDeslist.exists()) {
			dirDeslist.mkdirs();
		}
		
		String suffix;
		{
			LocalDateTime localDateTime = LocalDateTime.now();
			suffix = String.format("%4d%02d%02d-%02d%02d%02d",
					localDateTime.getYear(), localDateTime.getMonthValue(), localDateTime.getDayOfMonth(),
					localDateTime.getHour(), localDateTime.getMinute(), localDateTime.getSecond());
		}
		
		try {
			for(File file: fileList) {
				String stockCode = file.getName().replace(".csv", "");
				if (!stockCodeSet.contains(stockCode)) {
					// move file to delist
					File newFile = new File(dirDeslist, String.format("%s.csv-%s", stockCode, suffix));
					
					logger.info("delist {} -> {}",file.getPath(), newFile.getPath());
					Files.move(file.toPath(), newFile.toPath());
				}
			}
		} catch (IOException e) {
			String exceptionName = e.getClass().getSimpleName();
			logger.error("{} {}", exceptionName, e);
			throw new UnexpectedException(exceptionName, e);
		}
	}
	
	public static void main(String[] args) {
		logger.info("START");

		// delist obsolete price file
		delist();

		// download
		{
			RequesterBuilder requesterBuilder = RequesterBuilder.custom()
					.setVersionPolicy(HttpVersionPolicy.NEGOTIATE)
					.setSoTimeout(10)
					.setMaxTotal(50)
					.setDefaultMaxPerRoute(50);

			Download download = new DownloadAsync();
//			Download download = new DownloadSync();
			
			download.setRequesterBuilder(requesterBuilder);
			
			// Configure custom header
			download.setUserAgent("Mozilla/5.0 (X11; Linux x86_64) AppleWebKit");
			download.setReferer("https://www.jpx.co.jp/");
			
			// Configure thread count
			download.setThreadCount(50);

			List<Stock> stockList = Stock.getList();
			Collections.shuffle(stockList);
			
			for(Stock stock: stockList) {
				String stockCode = stock.stockCode;
				String uriString = StockPage.getPageURL(stockCode);
				File   file      = StockPage.getPageFile(stock.stockCode);
				
				Task task = FileTask.get(uriString, file);
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
