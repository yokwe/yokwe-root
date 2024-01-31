package yokwe.finance.provider.yahoo;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Function;
import java.util.stream.Collectors;

import yokwe.finance.stock.StorageStock;
import yokwe.finance.type.OHLCV;
import yokwe.finance.type.StockInfoJPType;
import yokwe.util.FileUtil;
import yokwe.util.UnexpectedException;


public class UpdateStockPriceJPYahoo {
	private static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();
	
	private static final long      GRACE_PERIOD_IN_HOUR = 23;
	private static final LocalDate EPOCH_DATE           = LocalDate.of(2010, 1, 1);
	private static final ZoneId    ZONE_ID              = ZoneId.of("Asia/Tokyo");
	private static final long      SLEEP_IN_MILLI       = 1500;
	
	// It takes 105 minutes to update second time.
	
	private static class Task {
		String     stockCode;
		LocalDate  startDate;
		LocalDate  stopDatePlusOne;
		String     name;
		
		// startDate an stopDate is inclusive
		Task(String stockCode, LocalDate startDate, LocalDate stopDatePlusOne, String name) {
			this.stockCode       = stockCode;
			this.startDate       = startDate;
			this.stopDatePlusOne = stopDatePlusOne;
			this.name            = name;
		}
		
		@Override
		public String toString() {
			return String.format("{%s  %s  %s  %s}", stockCode, startDate, stopDatePlusOne, name);
		}
	}
	
	
	private static List<Task> getTaskList(List<StockInfoJPType> stockList) {
		var taskList = new ArrayList<Task>();
		
		Instant now = Instant.now();
		
		int countA = 0;
		int countB = 0;
		int countC = 0;
		int countD = 0;
		int countE = 0;

		for (var stockInfo : stockList) {
			String     stockCode  = stockInfo.stockCode;
			LocalDate  startDate;
			LocalDate  stopDatePlusOne = LocalDate.ofInstant(now, ZONE_ID).plusDays(1);
			
			String path = StorageYahoo.StockPriceJPYahoo.getPath(stockCode);
			if (FileUtil.canRead(path)) {
				Instant  lastModified = FileUtil.getLastModified(path);
				Duration duration     = Duration.between(lastModified, now);
				if (GRACE_PERIOD_IN_HOUR < duration.toHours()) {
					// after grace period
					var list =  StorageYahoo.StockPriceJPYahoo.getList(stockCode);
					if (list.isEmpty()) {
						startDate = EPOCH_DATE;
						countA++;
					} else {
						var lastDate = list.stream().map(o -> o.date).max(LocalDate::compareTo).get();
						startDate = lastDate.plusDays(1);
						
						if (startDate.isEqual(stopDatePlusOne) || startDate.isAfter(stopDatePlusOne)) {
							countB++;
							continue;
						}
						
						countC++;
					}
				} else {
					// within grace period
					countD++;
					continue;
				}
			} else {
				// no file
				startDate = EPOCH_DATE;
				countE++;
			}

			taskList.add(new Task(stockCode, startDate, stopDatePlusOne, stockInfo.name));
		}
		
		logger.info("countA   {}", countA);
		logger.info("countB   {}", countB);
		logger.info("countC   {}", countC);
		logger.info("countD   {}", countD);
		logger.info("countE   {}", countE);
		logger.info("task     {}", taskList.size());
		
		return taskList;
	}
	
	private static int processTask(List<Task> taskList) {
		int count    = 0;
		int countA   = 0;
		int countB   = 0;
		int countMod = 0;
		Collections.shuffle(taskList);
		for(var task: taskList) {
			if ((++count % 100) == 1) logger.info("{}  /  {}", count, taskList.size());
			
			try {
				Thread.sleep(SLEEP_IN_MILLI);
			} catch (InterruptedException e) {
				//
			}
			
			String stockCode = task.stockCode;
			
			var priceList = Download.JP.getPrice(stockCode, task.startDate, task.stopDatePlusOne);
			if (priceList == null) {
				logger.warn("priceList is null  {}", task);
				try {
					Thread.sleep(SLEEP_IN_MILLI * 4);
				} catch (InterruptedException e) {
					//
				}
				countA++;
				continue;
			}
			
			// list has existing values
			var map = StorageYahoo.StockPriceJPYahoo.getList(stockCode).stream().collect(Collectors.toMap(o -> o.date, Function.identity()));
			int countSkip   = 0;
			int countChange = 0;
			// update map
			for(var price: priceList) {
				var oldPrice = map.get(price.date);
				if (oldPrice != null) {
					// unexpected
					if (oldPrice.compareTo(price) == 0) {
						// has same value
						continue;
					} else {
						// different value
						logger.warn("Unexpected price");
						logger.error("  old  {}", oldPrice);
						logger.error("  new  {}", price);
						throw new UnexpectedException("Unexpected price");
					}
				}
				map.put(price.date, price);
				countChange++;
			}
			
			// build list from map
			var list = new ArrayList<OHLCV>(map.size());
			{
				// sort before process
				var mapList = new ArrayList<>(map.values());
				Collections.sort(mapList);
				
				BigDecimal lastClose = null;
				for(var e: mapList) {
					if (e.volume == 0) {
						if (lastClose == null) {
							countSkip++;
							continue;
						} else {
							// use lastClose for open, high, low and close
							e.open = e.high = e.low = e.close = lastClose;
						}
					}
					lastClose = e.close;
					
					list.add(e);
				}
			}
			
			countB++;
			if (countChange != 0) countMod++;
			if (countSkip++ != 0) {
				logger.warn("skip  {}  {}", stockCode, countSkip);
			}


			StorageYahoo.StockPriceJPYahoo.save(stockCode, list);
		}
		
		logger.info("countA   {}", countA);
		logger.info("countB   {}", countB);
		logger.info("countMod {}", countMod);
		
		return countMod;
	}
	
	private static void update() {
		logger.info("GRACE_PERIOD_IN_HOUR  {}", GRACE_PERIOD_IN_HOUR);
		logger.info("EPOCH_DATE            {}", EPOCH_DATE);
		
		// Use StorageStock.StockInfoJP
		var stockList = StorageStock.StockInfoJP.getList();
		logger.info("stock     {}", stockList.size());
		
		for(int count = 1; count < 10; count++) {
			logger.info("try      {}", count);
			
			// build task list
			var taskList = getTaskList(stockList);
			// process task list
			int countMod = processTask(taskList);
			
			if (countMod == 0) break;
			
			try {
				Thread.sleep(SLEEP_IN_MILLI);
			} catch (InterruptedException e) {
				//
			}
			logger.info("retry");
		}
	}
	
	private static void moveUnknownFile() {
		Set<String> validNameSet = new TreeSet<>();
		for(var e: StorageStock.StockInfoJP.getList()) {
			File file = new File(StorageYahoo.StockPriceJPYahoo.getPath(e.stockCode));
			validNameSet.add(file.getName());
		}
		
		FileUtil.moveUnknownFile(validNameSet, StorageYahoo.StockPriceJPYahoo.getPath(), StorageYahoo.StockPriceJPYahoo.getPathDelist());
	}

	public static void main(String[] args) throws IOException {
		logger.info("START");
		
		moveUnknownFile();
		
		update();
		
		logger.info("STOP");
	}
}
