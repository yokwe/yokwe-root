package yokwe.finance.provider.nasdaq;

import java.io.File;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import yokwe.finance.provider.nasdaq.api.AssetClass;
import yokwe.finance.provider.nasdaq.api.Dividends;
import yokwe.finance.type.StockInfoUS;
import yokwe.util.UnexpectedException;

public class UpdateStockDivInfo {
	private static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();
	
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
		
		long now = System.currentTimeMillis();
		long oneDay = 24 * 60 * 60 * 1000;
		
		int countA = 0;
		int countB = 0;
		int countC = 0;
		for(var stockInfo: list) {
			String     stockCode  = stockInfo.stockCode;
			AssetClass assetClass = stockInfo.type.isETF() ? AssetClass.ETF : AssetClass.STOCK;
			int        limit;
			
			File file = new File(StockDivInfo.getPath(stockCode));
			if (file.canRead()) {
				long fileTime = file.lastModified();
				if (oneDay < (now - fileTime)) {
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
		logger.info("countA   {}", countA);
		logger.info("countB   {}", countB);
		logger.info("countC   {}", countC);
		logger.info("task     {}", taskList.size());
		
		return taskList;
	}
	
	private static final String NA = "N/A";
	
	private static LocalDate toLocalDate(String string) {
		if (string.equals(NA)) return StockDivInfo.DATE_NA;
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
		int countC   = 0;
		int countD   = 0;
		int countE   = 0;
		int countF   = 0;
		Collections.shuffle(taskList);
		for(var task: taskList) {
			String stockCode = task.stockCode;
			if ((++count % 100) == 1) logger.info("{}  /  {}  {}", count, taskList.size(), task);
			
			var nasdaqSymbol = StockInfo.toNASDAQSymbol(stockCode);
			var div = Dividends.getInstance(nasdaqSymbol, task.assetClass, task.limit);
			if (div == null) {
				logger.warn("div is null  {}", task);
				StockDivInfo.save(stockCode, new ArrayList<StockDivInfo>());
				countA++;
				continue;
			}
			if (div.data == null) {
				logger.warn("div.data is null  {}", task);
				StockDivInfo.save(stockCode, new ArrayList<StockDivInfo>());
				countB++;
				continue;
			}
			if (div.data.dividends == null) {
				logger.warn("div.data.dividends is null  {}", task);
				StockDivInfo.save(stockCode, new ArrayList<StockDivInfo>());
				countC++;
				continue;
			}
			if (div.data.dividends.rows == null) {
//				logger.warn("div.data.tradesTable.rows is null  {}", task);
				StockDivInfo.save(stockCode, new ArrayList<StockDivInfo>());
				countD++;
				continue;
			}
			
			var list = StockDivInfo.getList(stockCode);
			var set  = list.stream().map(o -> o.exOrEffDate).collect(Collectors.toSet());
			
			int countAdd = 0;
			for(var row: div.data.dividends.rows) {
				StockDivInfo divInfo = new StockDivInfo();
				divInfo.exOrEffDate     = toLocalDate(row.exOrEffDate);
				divInfo.type            = row.type;
				divInfo.amount          = toBigDecimal(row.amount);
				divInfo.declarationDate = toLocalDate(row.declarationDate);
				divInfo.recordDate      = toLocalDate(row.recordDate);
				divInfo.paymentDate     = toLocalDate(row.paymentDate);
				divInfo.currency        = row.currency;
				
				if (divInfo.exOrEffDate.equals(StockDivInfo.DATE_NA) && divInfo.recordDate.equals(StockDivInfo.DATE_NA)) {
					logger.error("Unexpected date");
					logger.error("  stockCode   {}", stockCode);
					logger.error("  exOrEffDate {}", divInfo.exOrEffDate);
					logger.error("  recordDate  {}", divInfo.recordDate);
					throw new UnexpectedException("Unexpected date");
				} else {
					if (divInfo.exOrEffDate.equals(StockDivInfo.DATE_NA)) divInfo.exOrEffDate = divInfo.recordDate.minusDays(1);
					if (divInfo.recordDate.equals(StockDivInfo.DATE_NA))  divInfo.recordDate  = divInfo.exOrEffDate.plusDays(1);
				}
				
				if (set.contains(divInfo.exOrEffDate)) continue;
				list.add(divInfo);
				countAdd++;
			}
			countE++;
			if (countAdd != 0) {
				StockDivInfo.save(stockCode, list);
				countMod++;
			}
			if (list.isEmpty()) {
				StockDivInfo.save(stockCode, list);
				countF++;
			}
		}
		
		logger.info("countA   {}", countA);
		logger.info("countB   {}", countB);
		logger.info("countC   {}", countC);
		logger.info("countD   {}", countD);
		logger.info("countE   {}", countE);
		logger.info("countF   {}", countF);

		logger.info("countMod {}", countMod);

		return countMod;
	}
	private static void update() {
		var list = yokwe.finance.stock.us.StockInfo.getList();
		logger.info("list     {}", list.size());

		for(int count = 1; count < 10; count++) {
			logger.info("try      {}", count);
			
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
	
	public static void main(String[] args) {
		logger.info("START");
		
		update();
		
		logger.info("STOP");
	}
}
