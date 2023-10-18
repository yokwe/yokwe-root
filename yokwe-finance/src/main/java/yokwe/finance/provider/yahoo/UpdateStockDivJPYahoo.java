package yokwe.finance.provider.yahoo;

import java.io.File;
import java.io.IOException;
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
import yokwe.finance.type.StockInfoJPType;
import yokwe.util.FileUtil;
import yokwe.util.UnexpectedException;


public class UpdateStockDivJPYahoo {
	private static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();
	
	private static final long      GRACE_PERIOD_IN_DAYS = 7;
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
			
			String path = StorageYahoo.StockDivJPYahoo.getPath(stockCode);
			if (FileUtil.canRead(path)) {
				Instant  lastModified = FileUtil.getLastModified(path);
				Duration duration     = Duration.between(lastModified, now);
				if (GRACE_PERIOD_IN_DAYS < duration.toDays()) {
					// after grace period
					var list =  StorageYahoo.StockDivJPYahoo.getList(stockCode);
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
		int countC   = 0;
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
			
			var divList = Download.getDividend(StockInfoJPType.toYahooSymbol(stockCode), task.startDate, task.stopDatePlusOne);
			if (divList == null) {
				logger.warn("divList is null  {}", task);
				try {
					Thread.sleep(SLEEP_IN_MILLI * 4);
				} catch (InterruptedException e) {
					//
				}
				countA++;
				continue;
			}
			
			// list has existing values
			var list = StorageYahoo.StockDivJPYahoo.getList(stockCode);
			if (divList.isEmpty()) {
				if (list.isEmpty()) {
					// Update last modified time of file
					StorageYahoo.StockDivJPYahoo.save(stockCode, list);
				}
				countB++;
				continue;
			}
			
			var map = list.stream().collect(Collectors.toMap(o -> o.date, Function.identity()));
			int countChange =0;
			for(var div: divList) {
				var oldDiv = map.get(div.date);
				if (oldDiv != null) {
					// unexpected
					if (oldDiv.compareTo(div) == 0) {
						// has same value
						continue;
					} else {
						// different value
						logger.warn("Unexpected div");
						logger.error("  old  {}", oldDiv);
						logger.error("  new  {}", div);
						throw new UnexpectedException("Unexpected div");
					}
				}
				map.put(div.date, div);
				countChange++;
			}
			countC++;
			if (countChange != 0) countMod++;

			StorageYahoo.StockDivJPYahoo.save(stockCode, map.values());
		}
		
		logger.info("countA   {}", countA);
		logger.info("countB   {}", countB);
		logger.info("countC   {}", countC);
		logger.info("countMod {}", countMod);
		
		return countMod;
	}
	
	private static void update() {
		logger.info("grace period  {} days", GRACE_PERIOD_IN_DAYS);

		// Use StockInfo of stock us
		var stockList = StorageStock.StockInfoJP.getList().stream().filter(o -> o.type.isDomesticStock() || o.type.isForeignStock()).collect(Collectors.toList());
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
			File file = new File(StorageYahoo.StockDivJPYahoo.getPath(e.stockCode));
			validNameSet.add(file.getName());
		}
		
		FileUtil.moveUnknownFile(validNameSet, StorageYahoo.StockDivJPYahoo.getPath(), StorageYahoo.StockDivJPYahoo.getPathDelist());
	}

	public static void main(String[] args) throws IOException {
		logger.info("START");
		
		moveUnknownFile();
		
		update();
		
		logger.info("STOP");
	}
}
