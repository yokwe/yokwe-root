package yokwe.finance.stock.us;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Function;
import java.util.stream.Collectors;

import yokwe.finance.provider.nasdaq.api.AssetClass;
import yokwe.finance.provider.nasdaq.api.Dividends;
import yokwe.finance.type.DailyValue;
import yokwe.finance.type.StockInfoUS;
import yokwe.util.FileUtil;
import yokwe.util.UnexpectedException;

public class UpdateStockDiv {
	private static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();
	
	
	public static long GRACE_PERIOD_IN_HOUR = 5;
	
	private static LocalDate toLocalDate(String string) {
		// 03/22/2019
		// 0123456789
		if (string.length() == 10 && string.charAt(2) == '/' && string.charAt(5) == '/') {
			int m = Integer.parseInt(string.substring(0, 2), 10);
			int d = Integer.parseInt(string.substring(3, 5), 10);
			int y = Integer.parseInt(string.substring(6), 10);
			return LocalDate.of(y, m, d);
		} else {
			logger.error("Unexpected string");
			logger.error("  string !{}!", string);
			throw new UnexpectedException("Unexpected string");
		}
	}
	private static BigDecimal toBigDecimal(String string) {
		return new BigDecimal(string.replace(",", "").replace("$", ""));
	}

	
	private static class Task {
		String     stockCode;
		AssetClass assetClass;
		int        limit;
		String     name;
		
		// startDate an stopDate is inclusive
		Task(String stockCode, AssetClass assetClass, int limit, String name) {
			this.stockCode  = stockCode;
			this.assetClass = assetClass;
			this.limit      = limit;
			this.name       = name;
		}
		
		@Override
		public String toString() {
			return String.format("{%s  %s  %d  %s}", stockCode, assetClass, limit, name);
		}
	}
	
	private static List<Task> getTaskList(List<StockInfoUS> stockList) {
		var taskList = new ArrayList<Task>();
		
		Instant now = Instant.now();
		
		int countA = 0;
		int countB = 0;
		int countC = 0;
		
		try {
			for (var stockInfo : stockList) {
				String     stockCode  = stockInfo.stockCode;
				AssetClass assetClass = stockInfo.type.isETF() ? AssetClass.ETF : AssetClass.STOCK;
				int        limit;
				
				Path path = Path.of(StockDiv.getPath(stockCode));
				
				if (Files.isReadable(path)) {
					Instant  lastModified = Files.getLastModifiedTime(path).toInstant();
					Duration duration     = Duration.between(lastModified, now);
					if (GRACE_PERIOD_IN_HOUR < duration.toHours()) {
						limit = 1;
						countA++;
					} else {
						countB++;
						continue;
					}
				} else {
					limit = 9999;
					countC++;
				}

				taskList.add(new Task(stockCode, assetClass, limit, stockInfo.name));
			}
		} catch (IOException e) {
			// catch exception from Files.getLastModifiedTime
			String exceptionName = e.getClass().getSimpleName();
			logger.error("{} {}", exceptionName, e);
			throw new UnexpectedException(exceptionName, e);
		}
		
		logger.info("countA   {}", countA);
		logger.info("countB   {}", countB);
		logger.info("countC   {}", countC);
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
			String stockCode = task.stockCode;
			if ((++count % 100) == 1) logger.info("{}  /  {}  {}", count, taskList.size(), task);
			
			var list = StockDiv.getList(stockCode);

			var div = Dividends.getInstance(StockInfoUS.toNASDAQSymbol(stockCode), task.assetClass, task.limit);
			if (div == null || div.data == null || div.data.dividends == null || div.data.dividends.rows == null) {
				if (list.isEmpty()) {
					// Write file to update last modified time
					StockDiv.save(stockCode, new ArrayList<DailyValue>());
				}
				countA++;
				continue;
			}
			
			int countAdd = 0;
			
			var myMap = new HashMap<LocalDate, DailyValue>();
			// build myMap
			for(var row: div.data.dividends.rows) {
				// Skip if exOfEffDate is N/A
				if (row.exOrEffDate.equals(Dividends.NOT_AVAILABLE)) continue;
				
				LocalDate  date  = toLocalDate(row.exOrEffDate);
				BigDecimal value = toBigDecimal(row.amount);
				
				if (myMap.containsKey(date)) {
					DailyValue old = myMap.get(date);
					old.value = old.value.add(value);
				} else {
					myMap.put(date, new DailyValue(date, value));
				}
			}
			
			var map = list.stream().collect(Collectors.toMap(o -> o.date, Function.identity()));
			// update map using mayMap
			for(var entry: myMap.entrySet()) {
				var date    = entry.getKey();
				var newValue = entry.getValue();
				
				if (map.containsKey(date)) {
					var oldValue = map.get(date);
					if (newValue.equals(oldValue)) {
						//
					} else {
						logger.error("Unexpected oldValue");
						logger.error("  task      {}", task);
						logger.error("  oldValue  {}", oldValue);
						logger.error("  newValue  {}", newValue);
						throw new UnexpectedException("Unexpected oldValue");
					}
				} else {
					map.put(date, newValue);
					countAdd++;
				}
			}
			
			StockDiv.save(stockCode, map.values());
			countB++;
			
			if (countAdd != 0) countMod++;
		}
		
		logger.info("countA   {}", countA);
		logger.info("countB   {}", countB);
		logger.info("countMod {}", countMod);

		return countMod;
	}
	
	private static void update() {
		// Use StockInfo of stock us
		var stockList = StockInfo.getList();
		logger.info("list     {}", stockList.size());

		for(int count = 1; count < 10; count++) {
			logger.info("try      {}", count);
			
			// build task list
			var taskList = getTaskList(stockList);
			// process task list
			int countMod = processTask(taskList);
			
			if (countMod == 0) break;
			
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				//
			}
			logger.info("retry");
		}
	}
	
	
	private static void moveUnknownFile() {
		Set<String> validNameSet = new TreeSet<>();
		for(var e: StockInfo.getList()) {
			File file = new File(StockDiv.getPath(e.stockCode));
			validNameSet.add(file.getName());
		}
		
		FileUtil.moveUnknownFile(validNameSet, StockDiv.getPath(), StockDiv.getPathDelist());
	}

	
	public static void main(String[] args) throws IOException {
		logger.info("START");
		
		moveUnknownFile();
		
		update();
		
		logger.info("STOP");
	}
}
