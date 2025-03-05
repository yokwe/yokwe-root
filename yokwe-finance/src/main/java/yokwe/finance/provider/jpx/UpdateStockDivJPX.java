package yokwe.finance.provider.jpx;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.apache.hc.core5.http2.HttpVersionPolicy;

import yokwe.finance.type.DailyValue;
import yokwe.finance.type.StockCodeJP;
import yokwe.finance.type.StockInfoJPType;
import yokwe.util.UnexpectedException;
import yokwe.util.http.Download;
import yokwe.util.http.DownloadSync;
import yokwe.util.http.RequesterBuilder;
import yokwe.util.http.StringTask;
import yokwe.util.http.Task;

public class UpdateStockDivJPX {
	private static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();
	
	public static String getPageURL(String stockCode) {
		String stockCode4 = StockCodeJP.toStockCode4(stockCode);
		return String.format("https://quote.jpx.co.jp/jpx/template/tmp/Jkessan.asp?QCODE=%s", stockCode4);
	}
	
	private static LocalDate toLocalDateYYYYMM(String string) {
		// 2023/01
		// 0123456
		if (string.length() == 7 && string.charAt(4) == '/') {
			int yyyy = Integer.valueOf(string.substring(0, 4));
			int mm   = Integer.valueOf(string.substring(5, 7));
			var date = LocalDate.of(yyyy, mm, 1);
			return date.with(TemporalAdjusters.lastDayOfMonth());
		} else {
			logger.error("Unexpected string");
			logger.error("  string  {}!", string);
			throw new UnexpectedException("Unexpected string");
		}
	}
	
	private static class Context {
		// See below link for parameter of synchronized statement
		//   https://rules.sonarsource.com/java/RSPEC-1860/
		private Map<String, List<DailyValue>> divMap;
		//          stockCode
		private int                           buildCount;
		
		private Object divMapLock      = new Object();
		private Object buildCountLock  = new Object();

		
		public void put(String key, List<DailyValue> value) {
			synchronized(divMapLock) {
				divMap.put(key, value);
			}
		}

		public void incrementBuildCount() {
			synchronized(buildCountLock) {
				buildCount = buildCount + 1;
			}
		}
		public int getBuildCount() {
			int ret;
			synchronized(buildCountLock) {
				ret = buildCount;
			}
			return ret;
		}

		public Context() {
			this.divMap     = new TreeMap<>();
			this.buildCount = 0;
		}
	}
	
	private static void buildContextFromPage(Context context, String stockCode, String page) {
		if (page.contains(KessanPage.NO_INFORMATION)) {
			//
		} else {
			var section = KessanPage.Section.getInstance(KessanPage.SECTION_KESSAN, page);
			if (section == null) {
				logger.error("Unexpected page");
				logger.error("  stockCode  {}", stockCode);
				logger.error("  page       {}", page);
				throw new UnexpectedException("Unexpected page");
			}
			
			var divList = new ArrayList<DailyValue>();
			{
				var listA = KessanPage.Kessan_A.getList(section.string);
				var listB = KessanPage.Kessan_B.getList(section.string);
				
				// sanity check
				if (listA.isEmpty() && listB.isEmpty()) {
					logger.error("Unexpected listA and listB are both empty");
					logger.error("  stockCode  {}", stockCode);
					logger.error("  section    {}", section.string);
					throw new UnexpectedException("Unexpected listA and listB are both empty");
				}
				
				if (!listA.isEmpty()) {
					for(var e: listA) {
						if (e.dividendTerm.isEmpty()) continue;
						if (e.dividendTerm.isBlank()) continue;
						
						var date = toLocalDateYYYYMM(e.termEnd);
						var value = new BigDecimal(e.dividendTerm);
						divList.add(new DailyValue(date, value));
					}
				}
				if (!listB.isEmpty()) {
					for(var e: listB) {
						if (e.dividendTerm.isEmpty()) continue;
						if (e.dividendTerm.isBlank()) continue;
						
						var date  = toLocalDateYYYYMM(e.termEnd);
						var value = new BigDecimal(e.dividendTerm);
						divList.add(new DailyValue(date, value));
					}
				}
			}
			// save for later use
			if (!divList.isEmpty()) context.put(stockCode, divList);
		}
	}

	private static class MyConsumer implements Consumer<String> {
		private final Context   context;
		private final String    stockCode;
		
		MyConsumer(Context context, String stockCode) {
			this.context   = context;
			this.stockCode = stockCode;
		}
		@Override
		public void accept(String page) {
			context.incrementBuildCount();
			buildContextFromPage(context, stockCode, page);			
		}
	}

	private static void buildContext(Context context) {
		int threadCount       = 10;
		int maxPerRoute       = 50;
		int maxTotal          = 100;
		int soTimeout         = 30;
		int connectionTimeout = 30;
		int progressInterval  = 1000;
		logger.info("threadCount       {}", threadCount);
		logger.info("maxPerRoute       {}", maxPerRoute);
		logger.info("maxTotal          {}", maxTotal);
		logger.info("soTimeout         {}", soTimeout);
		logger.info("connectionTimeout {}", connectionTimeout);
		logger.info("progressInterval  {}", progressInterval);
		
		RequesterBuilder requesterBuilder = RequesterBuilder.custom()
				.setVersionPolicy(HttpVersionPolicy.NEGOTIATE)
				.setSoTimeout(soTimeout)
				.setMaxTotal(maxTotal)
				.setDefaultMaxPerRoute(maxPerRoute);

//		Download download = new DownloadAsync();
		Download download = new DownloadSync();
		
		download.setRequesterBuilder(requesterBuilder);
		
		// Configure custom header
		download.setUserAgent("Mozilla/5.0 (X11; Linux x86_64) AppleWebKit");
		download.setReferer("https://www.jpx.co.jp/");
		
		// Configure thread count
		download.setThreadCount(threadCount);
		
		// connection timeout in second
		download.setConnectionTimeout(connectionTimeout);
		
		// progress interval
		download.setProgressInterval(progressInterval);
		
		var stockList = StorageJPX.StockList.getList();
		Collections.shuffle(stockList);
		final int stockListSize = stockList.size();
		
		for(var stockInfo: stockList) {
			String stockCode = stockInfo.stockCode;			
			String uriString = getPageURL(stockCode);
			Task   task      = StringTask.get(uriString, new MyConsumer(context, stockCode), StandardCharsets.UTF_8);
			download.addTask(task);
		}

		logger.info("BEFORE RUN");
		download.startAndWait();
		logger.info("AFTER  RUN");
//		download.showRunCount();

		try {
			for(int i = 0; i < 10; i++) {
				int buildCount = context.getBuildCount();
				if (buildCount == stockListSize) break;
				logger.info("buildCount {} / {}", buildCount, stockListSize);
				Thread.sleep(1000);
			}
			{
				int buildCount = context.getBuildCount();
				if (buildCount != stockListSize) {
					logger.error("Unexpected");
					logger.error("  buildCount    {}", buildCount);
					logger.error("  stockListSize {}", stockListSize);
					throw new UnexpectedException("Unexpected");
				}
			}
			logger.info("AFTER  WAIT");
		} catch (InterruptedException e) {
			String exceptionName = e.getClass().getSimpleName();
			logger.error("{} {}", exceptionName, e);
			throw new UnexpectedException(exceptionName, e);
		}
	}
	
	private static void updateDiv(Context context) {
		// update price using list (StockPrice)
		logger.info("updatePrice");
		int count      = 0;
		int countMod   = 0;
		int countTotal = context.divMap.size();
		for(var entry: context.divMap.entrySet()) {
			if ((++count % 1000) == 1) logger.info("{}  /  {}", count, countTotal);
			
			var stockCode = entry.getKey();
			var divList   = entry.getValue();
			
			int countChange = 0;
			
			// build list
			List<DailyValue> list;
			{
				// read existing data in map
				var map = StorageJPX.StockDivJPX.getMap(stockCode);
				
				// replace map with priceVolumeList
				for(var div: divList) {
					var oldDiv = map.get(div.date);
					if (oldDiv == null) {
						map.put(div.date, div);
						countChange++;
					} else {
						if (oldDiv.equals(div)) {
							// same value
						} else {
							logger.warn("not same value");
							logger.warn("  stockCode  {}", stockCode);
							logger.warn("  old        {}", oldDiv);
							logger.warn("  new        {}", div);
						}
					}
				}
				
				list = map.values().stream().collect(Collectors.toList());
				// sort list
				Collections.sort(list);
			}
			if (countChange != 0) countMod++;
						
			StorageJPX.StockDivJPX.save(stockCode, list);
		}
		
		logger.info("count    {}", count);
		logger.info("countMod {}", countMod);
	}

	private static void update() {
		Context context = new Context();
		
		buildContext(context);
		updateDiv(context);
	}
	
	public static void main(String[] args) throws IOException {
		logger.info("START");
		
		update();
		
		logger.info("STOP");
	}
}
