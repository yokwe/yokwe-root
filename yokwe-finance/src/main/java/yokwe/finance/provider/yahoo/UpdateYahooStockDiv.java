package yokwe.finance.provider.yahoo;

import java.io.IOException;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import yokwe.finance.type.StockInfoJPType;
import yokwe.finance.type.StockInfoUSType;
import yokwe.util.MarketHoliday;

public class UpdateYahooStockDiv {
	private static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();
	
	// NOTE dividend value of US stock is truncated to 3 fraction digits.
	// NOTE NADAQ dividend value is more accurate

	// NOTE dividend value of JP stock contains wrong value
	// NOTE dividend date of JP stock is end of term not ex-day nor pay day
	
	private static int SLEEP_IN_MILLI = 1500;
	
	private static boolean SKIP_EMPTY = false;
	
	private static LocalDate FIRST_DATE = LocalDate.of(2010, 1, 1);
	
	private static class Task {
		String    stockCode;
		LocalDate firstDate;
		LocalDate lastDatePlusOne;
		
		Task(String stockCode, LocalDate firstDate, LocalDate lastDatePlusOne) {
			this.stockCode       = stockCode;
			this.firstDate       = firstDate;
			this.lastDatePlusOne = lastDatePlusOne;
		}
	}
	
	private static void buildTaskList(List<Task> taskList, LocalDate lastTradingDate, List<String> list) {
		logger.info("list    {}", list.size());
		
		int countA = 0;
		int countB = 0;
		int countC = 0;
		int countD = 0;
		
		for(var stockCode: list) {
			var map = YahooStockDiv.getMap(stockCode);
			
			LocalDate firstDate;
			LocalDate lastDatePlusOne = lastTradingDate.plusDays(1);
			
			var dateList = map.keySet().stream().collect(Collectors.toList());
			Collections.sort(dateList);
			
			if (dateList.size() == 0) {
				firstDate = FIRST_DATE;
				countA++;
				if (SKIP_EMPTY) continue;
			} else if (dateList.size() == 1) {
				var  dateB = dateList.get(dateList.size() - 1);
				firstDate = dateB.plusDays(1);
				countB++;
			} else {
				var  dateA = dateList.get(dateList.size() - 2);
				var  dateB = dateList.get(dateList.size() - 1);
				
				long durationA = ChronoUnit.DAYS.between(dateA, dateB);
				long durationB = ChronoUnit.DAYS.between(dateB, lastTradingDate);
				
				if (durationA < 370 && durationB < durationA) {
					countC++;
					continue;
				} else {
					firstDate = dateB.plusDays(1);
					countD++;
				}
			}
			
			taskList.add(new Task(stockCode, firstDate, lastDatePlusOne));
		}
		
		logger.info("countA  {}", countA);
		logger.info("countB  {}", countB);
		logger.info("countC  {}", countC);
		logger.info("countD  {}", countD);
		logger.info("task    {}", taskList.size());
	}
	
	private static void update() {
		var taskList = new ArrayList<Task>();
		
		{
			var lastTradingDate = MarketHoliday.US.getLastTradingDate();
			var list = yokwe.finance.stock.us.StockInfoUS.getList().stream().map(o -> StockInfoUSType.toYahooSymbol(o.stockCode)).collect(Collectors.toList());
			buildTaskList(taskList, lastTradingDate, list);
		}
		{
			var lastTradingDate = MarketHoliday.JP.getLastTradingDate();
			var list = yokwe.finance.stock.jp.StockInfoJP.getList().stream().map(o -> StockInfoJPType.toYahooSymbol(o.stockCode)).collect(Collectors.toList());
			buildTaskList(taskList, lastTradingDate, list);
		}
		
		Collections.shuffle(taskList);
		int count = 0;
		for(var task: taskList) {
			if ((++count % 100) == 1) logger.info("{}  /  {}", count, taskList.size());
//			logger.info("{}  {}  /  {}", label, count, taskList.size());
			
			var divList = Download.getDividend(task.stockCode, task.firstDate, task.lastDatePlusOne);
			if (divList.isEmpty()) continue;
			
			var map = YahooStockDiv.getMap(task.stockCode);
			for(var div: divList) {
				map.put(div.date, div);
			}
			YahooStockDiv.save(task.stockCode, map.values());
			
			// sleep
			try {
				Thread.sleep(SLEEP_IN_MILLI);
			} catch (InterruptedException ie) {
				//
			}
		}

	}
	
	public static void main(String[] args) throws IOException {
		logger.info("START");
		
//		update();
		
		logger.info("STOP");
	}
}
