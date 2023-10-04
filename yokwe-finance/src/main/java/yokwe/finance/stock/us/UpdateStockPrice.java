package yokwe.finance.stock.us;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import yokwe.finance.provider.nasdaq.api.API;
import yokwe.finance.provider.nasdaq.api.AssetClass;
import yokwe.finance.provider.nasdaq.api.Historical;
import yokwe.finance.type.OHLCV;
import yokwe.finance.type.StockInfoUS;
import yokwe.util.FileUtil;
import yokwe.util.MarketHoliday;
import yokwe.util.UnexpectedException;

public class UpdateStockPrice {
	private static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();
	
	// Historical returns 10 years data maximum
	private static LocalDate EPOCH_DATE = LocalDate.of(2010, 1, 1);
	
	private static class Task {
		String     stockCode;
		AssetClass assetClass;
		LocalDate  startDate;
		LocalDate  stopDate;
		String     name;
		
		// startDate an stopDate is inclusive
		Task(String stockCode, AssetClass assetClass, LocalDate startDate, LocalDate stopDate, String name) {
			this.stockCode  = stockCode;
			this.assetClass = assetClass;
			this.startDate  = startDate;
			this.stopDate   = stopDate;
			this.name       = name;
		}
		
		@Override
		public String toString() {
			return String.format("{%s  %s  %s  %s  %s}", stockCode, assetClass, startDate, stopDate, name);
		}
	}
	
	private static List<Task> getTaskList(LocalDate lastTradingDate, List<StockInfoUS> stockList) {
		var taskList = new ArrayList<Task>();
		{
			int countA = 0;
			int countB = 0;
			int countC = 0;
			for(var e: stockList) {
				String     stockCode  = e.stockCode;
				AssetClass assetClass = e.type.isETF() ? AssetClass.ETF : AssetClass.STOCK;
				LocalDate  startDate;
				LocalDate  stopDate   = lastTradingDate;
				String     name       = e.name;
				
				var list = StockPrice.getList(stockCode);
				if (list.isEmpty()) {
					startDate = EPOCH_DATE;
					countA++;
				} else {
					var lastDate = list.stream().map(o -> o.date).max(LocalDate::compareTo).get();
					if (lastDate.isEqual(lastTradingDate)) {
						// already processed
						countB++;
						continue;
					} else {
						// needs data after lastDate
						startDate = lastDate.plusDays(1);
						countC++;
					}
				}
				taskList.add(new Task(stockCode, assetClass, startDate, stopDate, name));
			}
			logger.info("countA   {}", countA);
			logger.info("countB   {}", countB);
			logger.info("countC   {}", countC);
			logger.info("task     {}", taskList.size());
		}
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
			
			// If starDate equals stopDate, Historical.getInstance() returns no data.
			// So temporary decrease startDate and keep modified startDate as skipDate
			// When enumerate row, skip data if date is same as skipDate
			LocalDate skipDate = null;
			if (task.startDate.equals(task.stopDate)) {
				task.startDate = task.startDate.minusDays(1);
				skipDate = task.startDate;
			}
			
			var historical = Historical.getInstance(stockCode, task.assetClass, task.startDate, task.stopDate);
			if (historical == null || historical.data == null || historical.data.tradesTable == null || historical.data.tradesTable.rows == null) {
				countA++;
				continue;
			}
			
			// read existing data
			var list = StockPrice.getList(stockCode);
			var set  = list.stream().map(o -> o.date).collect(Collectors.toSet());
			int countAdd = 0;
			for(var row: historical.data.tradesTable.rows) {
				LocalDate  date   = LocalDate.parse(API.convertDate(row.date));
				
				// hack for the case startDate equals stopDate, see above comment
				if (skipDate != null && date.equals(skipDate)) {
//					logger.info("skipDate  {}", skipDate);
					continue;
				}
				
				BigDecimal open   = new BigDecimal(row.open.replace(",", "").replace("$", ""));
				BigDecimal high   = new BigDecimal(row.high.replace(",", "").replace("$", ""));
				BigDecimal low    = new BigDecimal(row.low.replace(",", "").replace("$", ""));
				BigDecimal close  = new BigDecimal(row.close.replace(",", "").replace("$", ""));
				long       volume = Long.parseLong(row.volume.replace(",", "").replace(API.NOT_AVAILABLE, "0"));
				
				var price = new OHLCV(date, open, high, low, close, volume);
				
				if (set.contains(date)) {
					logger.error("Unexpected date");
					logger.error("  date  {}", date);
					logger.error("  task  {}", task);
					throw new UnexpectedException("Unexpected date");
				} else {
					list.add(price);
					countAdd++;
				}
			}
			countB++;

//			logger.info("save  {}  {}", list.size(), StockPrice.getPath(stockCode));
			if (countAdd != 0) {
				StockPrice.save(stockCode, list);
				countMod++;
			}
		}
		logger.info("countA   {}", countA);
		logger.info("countB   {}", countB);

		logger.info("countMod {}", countMod);
		
		return countMod;
	}
	
	
	private static void update() {
		var lastTradingDate = MarketHoliday.US.getLastTradingDate();
		logger.info("date     {}", lastTradingDate);
		var stockList       = StockInfo.getList();
		logger.info("list     {}", stockList.size());

		for(int count = 1; count < 10; count++) {
			logger.info("try      {}", count);
			
			// build task list
			var taskList = getTaskList(lastTradingDate, stockList);
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
		for(var e: yokwe.finance.stock.us.StockInfo.getList()) {
			File file = new File(StockPrice.getPath(e.stockCode));
			validNameSet.add(file.getName());
		}
		
		FileUtil.moveUnknownFile(validNameSet, StockPrice.getPath(), StockPrice.getPathDelist());
	}
	
	
	public static void main(String[] args) throws IOException {
		logger.info("START");
		
		moveUnknownFile();
		
		update();
		
		logger.info("STOP");
	}
}
