package yokwe.finance.provider.nasdaq;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import yokwe.finance.Storage;
import yokwe.finance.provider.nasdaq.api.API;
import yokwe.finance.provider.nasdaq.api.AssetClass;
import yokwe.finance.provider.nasdaq.api.Dividends;
import yokwe.finance.type.StockInfoUS;
import yokwe.finance.type.StockInfoUS.Market;
import yokwe.util.UnexpectedException;

public class UpdateStockDivInfo {
	private static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();
	
	public static long GRACE_PERIOD_IN_HOUR = 5;
	
	private static class Task {
		public String     stockCode;
		public AssetClass assetClass;
		public int        limit;
		
		Task(String stockCode, AssetClass assetClass, int limit) {
			this.stockCode  = stockCode;
			this.assetClass = assetClass;
			this.limit      = limit;
		}
		
		@Override
		public String toString() {
			return String.format("{%s  %s  %d}", stockCode, assetClass, limit);
		}
	}
	
	private static List<Task> getTaskList(List<StockInfoUS> list) {
		var taskList = new ArrayList<Task>();
		
		Instant now = Instant.now();
		
		int countA = 0;
		int countB = 0;
		int countC = 0;
		
		try {
			for (var stockInfo : list) {
				String     stockCode  = stockInfo.stockCode;
				AssetClass assetClass = stockInfo.type.isETF() ? AssetClass.ETF : AssetClass.STOCK;
				int        limit;
				
				Path path = Path.of(StockDivInfo.getPath(stockCode));
				
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

				taskList.add(new Task(stockCode, assetClass, limit));
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
	
	private static LocalDate toLocalDate(String string) {
		if (string.equals(API.NOT_AVAILABLE)) return StockDivInfo.DATE_NOT_AVAILABLE;
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
	
	private static int processTask(List<Task> taskList) {
		int count    = 0;
		int countMod = 0;
		int countA   = 0;
		int countB   = 0;
		
//		Collections.shuffle(taskList);
		for(var task: taskList) {
			String stockCode = task.stockCode;
			if ((++count % 100) == 1) logger.info("{}  /  {}  {}", count, taskList.size(), task);
			
			var list = StockDivInfo.getList(stockCode);
			
			var div = Dividends.getInstance(StockInfoUS.toNASDAQSymbol(stockCode), task.assetClass, task.limit);
			if (div == null || div.data == null || div.data.dividends == null || div.data.dividends.rows == null) {
				if (list.isEmpty()) {
					StockDivInfo.save(stockCode, new ArrayList<StockDivInfo>());
					countA++;
					continue;
				}
			}
			
			var map  = list.stream().collect(Collectors.toMap(o -> o.exOrEffDate, Function.identity()));
			
			int countAdd = 0;
			for(var row: div.data.dividends.rows) {
				// Skip if exOfEffDate is N/A
				if (row.exOrEffDate.equals(API.NOT_AVAILABLE)) continue;
				
				StockDivInfo divInfo = new StockDivInfo();
				divInfo.exOrEffDate     = toLocalDate(row.exOrEffDate);
				divInfo.type            = row.type;
				divInfo.amount          = toBigDecimal(row.amount);
				divInfo.declarationDate = toLocalDate(row.declarationDate);
				divInfo.recordDate      = toLocalDate(row.recordDate);
				divInfo.paymentDate     = toLocalDate(row.paymentDate);
				divInfo.currency        = row.currency;
				
				var key = divInfo.exOrEffDate;
				if (map.containsKey(key)) {
					var old = map.get(key);
					if (old.equals(divInfo)) {
						// ignore same value
						logger.warn("Duplicate     {}  {}", stockCode, key);
					} else {
						logger.error("Duplicate key");
						logger.error("  stockCode  {}", stockCode);
						logger.error("  old        {}", old);
						logger.error("  new        {}", divInfo);
						throw new UnexpectedException("Duplicate key");
					}
				} else {
					map.put(key, divInfo);
					countAdd++;
				}
			}
			StockDivInfo.save(stockCode, map.values());
			countB++;
			
			if (countAdd != 0) countMod++;
		}
		
		logger.info("countA   {}", countA);
		logger.info("countB   {}", countB);
		logger.info("countMod {}", countMod);

		return countMod;
	}
	private static void update(List<StockInfoUS> list) {
		logger.info("start update");
		for(int count = 1; count < 10; count++) {
			logger.info("loop     {}", count);
			
			// build task list
			var taskList = getTaskList(list);
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
	
	private static void check(List<StockInfoUS> list) {
		logger.info("start check");
		int countDiv = 0;
		int countDup = 0;
		for(var stockInfo: list) {
			var stockCode = stockInfo.stockCode;
			
			var divInfoList = StockDivInfo.getList(stockCode);
			if (divInfoList.isEmpty()) continue;
			
			countDiv++;
			boolean hasDuplicate = false;

			var map = new HashMap<LocalDate, StockDivInfo>();
			for(var divInfo: divInfoList) {
				var date = divInfo.exOrEffDate;
				if (map.containsKey(date)) {
					hasDuplicate = true;
					var old = map.get(date);
					if (old.equals(divInfo)) {
						logger.info("same   {}  {}", stockCode, date);
					} else {
						logger.info("diff   {}  {}", stockCode, date);
						logger.info("  old  {}", old);
						logger.info("  new  {}", divInfo);
					}
					break;
				} else {
					map.put(date, divInfo);
				}
			}
			if (hasDuplicate) {
				logger.info("dup       {}", stockCode);
				try {
					Path path = Path.of(StockDivInfo.getPath(stockCode));
					Files.delete(path);
				} catch (IOException e) {
				}
				countDup++;
			}
		}
		logger.info("stock     {}", list.size());
		logger.info("countDiv  {}", countDiv);
		logger.info("countDup  {}", countDup);
		
		if (countDup != 0) System.exit(0);
//		System.exit(0);
	}
	
	public static void main(String[] args) {
		logger.info("START");
		
		// initialize Storage
		Storage.initialize();
		
		logger.info("grace period  {} hours", GRACE_PERIOD_IN_HOUR);
		
		var list = StockInfo.getList().stream().filter(o -> o.market == Market.NASDAQ).collect(Collectors.toList());
		logger.info("list     {}", list.size());
		
		check(list);
		
		update(list);
		
		logger.info("STOP");
	}
}
