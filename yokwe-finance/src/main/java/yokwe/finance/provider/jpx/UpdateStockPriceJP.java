package yokwe.finance.provider.jpx;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Consumer;

import org.apache.hc.core5.http2.HttpVersionPolicy;

import yokwe.finance.provider.jpx.StockPage.CompanyInfo;
import yokwe.finance.provider.jpx.StockPage.Issued;
import yokwe.finance.provider.jpx.StockPage.PriceVolume;
import yokwe.finance.provider.jpx.StockPage.TradeUnit;
import yokwe.finance.stock.StockInfoJP;
import yokwe.finance.stock.StockPriceJP;
import yokwe.finance.type.OHLCV;
import yokwe.util.MarketHoliday;
import yokwe.util.UnexpectedException;
import yokwe.util.http.Download;
import yokwe.util.http.DownloadSync;
import yokwe.util.http.RequesterBuilder;
import yokwe.util.http.StringTask;
import yokwe.util.http.Task;

public class UpdateStockPriceJP {
	private static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();
	
	private static String getPageURL(String stockCode) {
		String stockCode4 = StockInfoJP.toStockCode4(stockCode);
		return String.format("https://quote.jpx.co.jp/jpx/template/quote.cgi?F=tmp/stock_detail&MKTN=T&QCODE=%s", stockCode4);
	}

	private static class Context {
		// See below link for parameter of synchronized statement
		//   https://rules.sonarsource.com/java/RSPEC-1860/
		private List<JPXStockInfo>             stockInfoList;
		private Map<String, List<PriceVolume>> priceVolumeMap;
		//          stockCode
		private int                            buildCount;
		
		private Object stockInfoListLock = new Object();
		private Object priceMapLock      = new Object();
		private Object buildCountLock    = new Object();

		
		public void add(JPXStockInfo newValue) {
			synchronized(stockInfoListLock) {
				stockInfoList.add(newValue);
			}
		}
		public void put(String key, List<PriceVolume> value) {
			synchronized(priceMapLock) {
				priceVolumeMap.put(key, value);
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
			this.stockInfoList = new ArrayList<>();
			this.priceVolumeMap      = new TreeMap<>();
			this.buildCount    = 0;
		}
	}
	
	private static void buildContextFromPage(Context context, LocalDateTime dateTime, String stockCode, String page) {
		if (page.contains("指定された銘柄が見つかりません")) {
			//
		} else {
			CompanyInfo       companyInfo = CompanyInfo.getInstance(page);
			TradeUnit         tradeUnit   = TradeUnit.getInstance(page);
			Issued            issued      = Issued.getInstance(page);
			List<PriceVolume> priceList   = PriceVolume.getInstance(page);
			
			// sanity check
			if (
				companyInfo == null ||
				tradeUnit   == null ||
				issued      == null ||
				priceList.size() == 0) {
				logger.warn("Unexpected page");
				logger.warn("  stockCode {}", stockCode);

				if (companyInfo == null)   logger.warn("  companyInfo is null");
				if (tradeUnit   == null)   logger.warn("  tradeUnit is null");
				if (issued      == null)   logger.warn("  issued is null");
				if (priceList.size() == 0) logger.warn("  priceVolumeList is null");
			}
			
			// save for later use
			if (priceList.size() != 0) context.put(stockCode, priceList);

			// build stockInfoList
			if (companyInfo != null && tradeUnit != null && issued != null) {
				JPXStockInfo stockInfo = new JPXStockInfo(stockCode, companyInfo.isin, tradeUnit.value, issued.value);
				context.add(stockInfo);
			}
		}
	}

	private static class MyConsumer implements Consumer<String> {
		private final Context       context;
		private final String        stockCode;
		private final LocalDateTime dateTime;
		
		MyConsumer(Context context, String stockCode, LocalDateTime dateTime) {
			this.context   = context;
			this.stockCode = stockCode;
			this.dateTime  = dateTime;
		}
		@Override
		public void accept(String page) {
			context.incrementBuildCount();
			buildContextFromPage(context, dateTime, stockCode, page);			
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

		List<StockInfoJP> stockInfoList = StockInfoJP.getList();
		Collections.shuffle(stockInfoList);
		final int stockListSize = stockInfoList.size();
		
		LocalDateTime dateTime = LocalDateTime.now();
		
		for(var stockInfo: stockInfoList) {
			String stockCode = stockInfo.stockCode;
			String uriString = getPageURL(stockCode);
			Task   task      = StringTask.get(uriString, new MyConsumer(context, stockCode, dateTime));
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
	private static void updatePrice(Context context) {
		// update price using list (StockPrice)
		int count       = 0;
		int countUpdate = 0;
		int countSkip   = 0;
		int countZero   = 0;
		int countTotal  = context.stockInfoList.size();
		for(var entry: context.priceVolumeMap.entrySet()) {
			String            stockCode       = entry.getKey();
			List<PriceVolume> priceVolumeList = entry.getValue();
			
			if ((count % 1000) == 0) {
				logger.info("{}", String.format("%4d / %4d  %s", count, countTotal, stockCode));
			}
			count++;
			
			// Map of price of stock
			TreeMap<LocalDate, OHLCV> priceMap = new TreeMap<>();
			
			// Update priceMap with existing data
			for(OHLCV price: StockPriceJP.getList(stockCode)) {
				// remove bogus data
				if (MarketHoliday.JP.isClosed(price.date)) continue;
				
				priceMap.put(price.date, price);
			}
			
			// Update priceMap with priceVolumeList
			//   Because if stock split, historical price must be adjusted.
			{
				BigDecimal lastClose = null;
				for(PriceVolume priceVolume: priceVolumeList) {
					LocalDate  priceDate = priceVolume.getDate();
					BigDecimal open      = priceVolume.getOpen();
					BigDecimal high      = priceVolume.getHigh();
					BigDecimal low       = priceVolume.getLow();
					BigDecimal close     = priceVolume.getClose();
					long       volume    = priceVolume.volume;
					
					OHLCV price;
					if (priceMap.containsKey(priceDate)) {
						price = priceMap.get(priceDate);
					} else {
						price = new OHLCV();
						// 	public Price(String date, String stockCode, double open, double high, double low, double close, long volume) {
						price.date  = priceDate;
					}
						
					if (volume == 0) {
						// use lastClose as each price
						price.open   = lastClose;
						price.high   = lastClose;
						price.low    = lastClose;
						price.close  = lastClose;
						price.volume = 0;
					} else {
						if (open != null && high != null && low != null && close != null) {
							price.open   = open;
							price.high   = high;
							price.low    = low;
							price.close  = close;
							price.volume = volume;
						} else {
							logger.error("Unexpected");
							logger.error("  priceVolume {}", priceVolume);
							throw new UnexpectedException("Unexpected");
						}
					}
					
					if (price.close != null) {
						priceMap.put(priceDate, price);
						lastClose = price.close;
					}
				}
			}
			
			StockPriceJP.save(stockCode, priceMap.values());
		}
		
		logger.info("countTotal  {}", String.format("%4d", countTotal));
		logger.info("countSkip   {}", String.format("%4d", countSkip));
		logger.info("countZero   {}", String.format("%4d", countZero));
		logger.info("countUpdate {}", String.format("%4d", countUpdate));
	}

	private static void updateFiles() {
		Context context = new Context();
		
		buildContext(context);

		// Save data
		{
			List<JPXStockInfo> list = context.stockInfoList;
			logger.info("save {} {}", JPXStockInfo.getPath(),  list.size());
			JPXStockInfo.save(list);
		}

		// update each price file
		updatePrice(context);
	}
	
//	private static void moveUnknownFile() {
//		Set<String> validNameSet = new TreeSet<>();
//		for(var e: StockInfoJP.getList()) {
//			File file = new File(StockPriceJP.getPath(e.stockCode));
//			validNameSet.add(file.getName());
//		}
//		
//		// price
//		FileUtil.moveUnknownFile(validNameSet, StockPriceJP.getPath(), StockPriceJP.getPathDelist());
//	}
	
	public static void main(String[] args) throws IOException {
		logger.info("START");
		
		LocalDate today = LocalDate.now();
		if (MarketHoliday.JP.isClosed(today)) {
			logger.warn("market is closed today");
		} else {
			updateFiles();
		}
//		moveUnknownFile();
		
		logger.info("STOP");
	}
}
