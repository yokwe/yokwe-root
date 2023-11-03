package yokwe.finance.provider.jpx;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import yokwe.finance.provider.jpx.StockPage.PriceVolume;
import yokwe.finance.type.OHLCV;
import yokwe.finance.type.StockInfoJPType;
import yokwe.util.FileUtil;
import yokwe.util.MarketHoliday;
import yokwe.util.UnexpectedException;
import yokwe.util.http.HttpUtil;

public class UpdateStockPriceJPX2 {
	private static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();
	
	private static final boolean DEBUG_USE_FILE = true;
	
	private static final String USER_AGENT = "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit";
	private static final String REFERER    = "https://www.jpx.co.jp/";
	
	private static String download(String url, String filePath, boolean useFile) {
		final String page;
		{
			File file = new File(filePath);
			if (useFile && file.exists()) {
				page = FileUtil.read().file(file);
			} else {
				HttpUtil.Result result = HttpUtil.getInstance().withUserAgent(USER_AGENT).withReferer(REFERER).download(url);
				if (result == null || result.result == null) {
					logger.error("Unexpected");
					logger.error("  result  {}", result);
					throw new UnexpectedException("Unexpected");
				}
				page = result.result;
				// debug
				if (DEBUG_USE_FILE) logger.info("save  {}  {}", page.length(), file.getPath());
				FileUtil.write().file(file, page);
			}
		}
		return page;
	}
	
	
	private static class Task {
		String     stockCode;
		String     name;
		
		// startDate an stopDate is inclusive
		Task(String stockCode, String name) {
			this.stockCode  = stockCode;
			this.name       = name;
		}
		
		@Override
		public String toString() {
			return String.format("{%s  %s  %s  %s  %s}", stockCode, name);
		}
	}

	
	private static List<Task> getTaskList(LocalDate lastTradingDate, List<StockInfoJPType> list) {
		logger.info("getTaskList");
		
		var taskList = new ArrayList<Task>();
		{
			int countA = 0;
			int countB = 0;
			for(var e: list) {
				var stockCode = e.stockCode;
				var name      = e.name;
				
				Set<LocalDate> set = StorageJPX.StockPriceJPX.getList(stockCode).stream().map(o -> o.date).collect(Collectors.toSet());
				
				if (set.contains(lastTradingDate)) {
					countA++;
				} else {
					taskList.add(new Task(stockCode, name));
					countB++;
				}
			}
			logger.info("countA  {}", countA);
			logger.info("countB  {}", countB);
		}
		return taskList;
	}
	
	
	public static String getURL(String stockCode) {
		String stockCode4 = StockInfoJPType.toStockCode4(stockCode);
		return String.format("https://quote.jpx.co.jp/jpx/template/quote.cgi?F=tmp/stock_detail&MKTN=T&QCODE=%s", stockCode4);
	}
	
	private static int processTask(List<Task> taskList) {
		logger.info("processTask");
		
		int countMod = 0;
		int countA = 0;
		int countB = 0;
		int countC = 0;
		int countD = 0;
		int count  = 0;
		for(var task: taskList) {
			if ((++count % 100) == 1) logger.info("{}  /  {}", count, taskList.size());

			var stockCode = task.stockCode;
			var name      = task.name;
			
			String page;
			{
				var url  = getURL(stockCode);
				var path = StorageJPX.getPath("page", stockCode + ".html");
				page = download(url, path, DEBUG_USE_FILE);
			}
			
			if (page.contains(StockPage.NO_INFORMATION)) {
				logger.warn("no information  {}  {}", stockCode, name);
				countA++;
				continue;
			}
			
			var priceList = PriceVolume.getInstance(page);
			if (priceList == null) {
				logger.warn("priceList is null  {}  {}", stockCode, name);
				countB++;
				continue;
			}
			
			int countChange = 0;
			
			List<OHLCV> list;
			{
				var map = StorageJPX.StockPriceJPX.getMap(stockCode);
				for(var priceVolume: priceList) {
					OHLCV price;
					{
						LocalDate  priceDate = priceVolume.getDate();
						BigDecimal open      = priceVolume.getOpen();
						BigDecimal high      = priceVolume.getHigh();
						BigDecimal low       = priceVolume.getLow();
						BigDecimal close     = priceVolume.getClose();
						long       volume    = priceVolume.volume;
						
						price = new OHLCV();
						price.date   = priceDate;
						
						if (volume == 0 || open == null || high == null || low == null || close == null) {
							price.open = price.high = price.low = price.close  = BigDecimal.ZERO;
							price.volume = 0;
						} else {
							price.open   = open;
							price.high   = high;
							price.low    = low;
							price.close  = close;
							price.volume = volume;
						}
					}
					
					{
						var oldPrice = map.get(price.date);
						if (oldPrice == null) {
							map.put(price.date, price);
							countChange++;
						} else {
							if (oldPrice.equals(price)) {
								//
							} else {
								logger.info("old  {}", oldPrice);
								logger.info("new  {}", price);
								
								map.put(price.date, price);
								countChange++;
							}
						}
					}
				}
				list = map.values().stream().collect(Collectors.toList());
				Collections.sort(list);
			}
			
			if (countChange == 0) {
//				logger.info("no change  {}  {}", stockCode, name);
				countC++;
			} else {
//				logger.info("change  {}  {}", stockCode, name);
				
				BigDecimal lastClose = null;
				for(var price: list) {
					if (price.volume == 0) {
						if (lastClose == null) continue;
						price.open = price.high = price.low = price.close = lastClose;
					}
					lastClose = price.close;
				}
				
				// remove empty element at very first of list
				if (!list.isEmpty()) {
					while(list.get(0).isEmpty()) {
						list.remove(0);
						if (list.isEmpty()) break;
					}
				}
				
				if (list.isEmpty()) {
					logger.warn("list is empty   {}  {}", stockCode, name);
					countD++;
				} else {
					StorageJPX.StockPriceJPX.save(stockCode, list);
					//
					countMod++;
				}
			}
		}
		
		logger.info("countA  {}", countA);
		logger.info("countB  {}", countB);
		logger.info("countC  {}", countC);
		logger.info("countD  {}", countD);
		logger.info("mod     {}", countMod);

		return countMod;
	}
	
	private static void update() {
		var lastTradingDate = MarketHoliday.JP.getLastTradingDate();
		var stockList       = StorageJPX.StockInfoJPX.getList();
		logger.info("date     {}", lastTradingDate);
		logger.info("list     {}", stockList.size());

		for(int count = 1; count < 10; count++) {
			logger.info("try      {}", count);
			logger.info("---");
			
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
	
	public static void main(String[] args) throws IOException {
		logger.info("START");
		
		update();
		
		logger.info("STOP");
	}
}
