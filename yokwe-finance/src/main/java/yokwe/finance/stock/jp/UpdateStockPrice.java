package yokwe.finance.stock.jp;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.apache.hc.core5.http2.HttpVersionPolicy;

import yokwe.finance.provider.jpx.StockPage.CurrentPriceTime;
import yokwe.finance.provider.jpx.StockPage.HighPrice;
import yokwe.finance.provider.jpx.StockPage.LowPrice;
import yokwe.finance.provider.jpx.StockPage.OpenPrice;
import yokwe.finance.provider.jpx.StockPage.PriceVolume;
import yokwe.finance.provider.jpx.StockPage.TradeVolume;
import yokwe.finance.type.OHLCV;
import yokwe.finance.type.StockInfoJP;
import yokwe.util.MarketHoliday;
import yokwe.util.UnexpectedException;
import yokwe.util.http.Download;
import yokwe.util.http.DownloadSync;
import yokwe.util.http.RequesterBuilder;
import yokwe.util.http.StringTask;
import yokwe.util.http.Task;

public class UpdateStockPrice {
	private static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();
	
	public static String getPageURL(String stockCode) {
		String stockCode4 = StockInfoJP.toStockCode4(stockCode);
		return String.format("https://quote.jpx.co.jp/jpx/template/quote.cgi?F=tmp/stock_detail&MKTN=T&QCODE=%s", stockCode4);
	}

	private static class Context {
		// See below link for parameter of synchronized statement
		//   https://rules.sonarsource.com/java/RSPEC-1860/
		private Map<String, List<PriceVolume>> priceVolumeMap;
		//          stockCode
		private Map<String, OHLCV>             ohlcvMap;
		private int                            buildCount;
		private LocalDate                      today;
		
		private Object priceMapLock        = new Object();
		private Object buildCountLock      = new Object();

		
		public void put(String key, List<PriceVolume> value) {
			synchronized(priceMapLock) {
				priceVolumeMap.put(key, value);
			}
		}
		public void put(String key, OHLCV value) {
			synchronized(priceMapLock) {
				ohlcvMap.put(key, value);
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
			this.priceVolumeMap = new TreeMap<>();
			this.ohlcvMap       = new TreeMap<>();
			this.buildCount     = 0;
		}
	}
	
	private static void buildContextFromPage(Context context, String stockCode, String page) {
		if (page.contains("指定された銘柄が見つかりません")) {
			//
		} else {
			CurrentPriceTime  currentPrice = CurrentPriceTime.getInstance(page);
			OpenPrice         openPrice    = OpenPrice.getInstance(page);
			HighPrice         highPrice    = HighPrice.getInstance(page);
			LowPrice          lowPrice     = LowPrice.getInstance(page);
			TradeVolume       tradeVolume  = TradeVolume.getInstance(page);
			List<PriceVolume> priceList    = PriceVolume.getInstance(page);
			
			// sanity check
			if (currentPrice == null || openPrice == null || highPrice == null || lowPrice == null || tradeVolume == null || priceList.size() == 0) {
				logger.warn("Unexpected page");
				logger.warn("  stockCode {}", stockCode);

				if (currentPrice == null)  logger.warn("  currentPrice is null");
				if (openPrice == null)     logger.warn("  openPrice is null");
				if (highPrice == null)     logger.warn("  highPrice is null");
				if (lowPrice == null)      logger.warn("  lowPrice is null");
				if (tradeVolume == null)   logger.warn("  tradeVolume is null");
				if (priceList.size() == 0) logger.warn("  priceVolumeList is null");
			} else {
				if (tradeVolume.value.isPresent()) {
					var open   = new BigDecimal(openPrice.value.get().replace(",", ""));
					var high   = new BigDecimal(highPrice.value.get().replace(",", ""));
					var low    = new BigDecimal(lowPrice.value.get().replace(",", ""));
					var close  = new BigDecimal(currentPrice.price.get().replace(",", ""));
					var volume = Long.parseLong(tradeVolume.value.get().replace(",", ""));
					context.put(stockCode, new OHLCV(context.today, open, high, low, close, volume));
				}
			}
			
			// save for later use
			if (priceList.size() != 0) context.put(stockCode, priceList);
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
		
		// NOTE Use StockInfo
		List<StockInfoJP> stockInfoList = StockInfo.getList();
		Collections.shuffle(stockInfoList);
		final int stockListSize = stockInfoList.size();
		
		LocalDate today = LocalDate.now();
		if (MarketHoliday.JP.isClosed(today)) {
			today = MarketHoliday.JP.getPreviousTradingDate(today);
		}
		logger.info("today  {}", today);
		context.today = today;
		
		for(var stockInfo: stockInfoList) {
			String stockCode = stockInfo.stockCode;			
			String uriString = getPageURL(stockCode);
			Task   task      = StringTask.get(uriString, new MyConsumer(context, stockCode));
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
		logger.info("updatePrice");
		int count      = 0;
		int countMod   = 0;
		int countTotal = context.priceVolumeMap.size();
		for(var entry: context.priceVolumeMap.entrySet()) {
			if ((++count % 1000) == 1) logger.info("{}  /  {}", count, countTotal);
			
			var stockCode       = entry.getKey();
			var priceVolumeList = entry.getValue();
			
			// build list from priceVolueList
			List<OHLCV> list = new ArrayList<>();
			{
				var map = new HashMap<LocalDate, OHLCV>();
				for(PriceVolume priceVolume: priceVolumeList) {
					LocalDate  priceDate = priceVolume.getDate();
					BigDecimal open      = priceVolume.getOpen();
					BigDecimal high      = priceVolume.getHigh();
					BigDecimal low       = priceVolume.getLow();
					BigDecimal close     = priceVolume.getClose();
					long       volume    = priceVolume.volume;
					
					OHLCV price = new OHLCV();
					price.date   = priceDate;
					
					if (volume == 0 || open == null || high == null || low == null || close == null) {
						price.open   = BigDecimal.ZERO;
						price.high   = BigDecimal.ZERO;
						price.low    = BigDecimal.ZERO;
						price.close  = BigDecimal.ZERO;
						price.volume = 0;
					} else {
						price.open   = open;
						price.high   = high;
						price.low    = low;
						price.close  = close;
						price.volume = volume;
					}
					
					map.put(priceDate, price);
				}
				
				// today price
				OHLCV today = context.ohlcvMap.get(stockCode);
				if (today != null) {
					// if today data is not in map, add today data.
					if (!map.containsKey(today.date)) map.put(today.date, today);
				}
				
				list = map.values().stream().collect(Collectors.toList());
				Collections.sort(list);
			}
			
			// merger map with list
			var map = StockPrice.getMap(stockCode);
			{
				int        countChange = 0;
				BigDecimal lastClose   = null;
				for(var price: list) {
					var date     = price.date;
					var oldPrice = map.get(date);
					
					if (oldPrice == null) {
						// new entry
						if (price.volume == 0) {
							if (lastClose == null) continue;
							
							price.open  = lastClose;
							price.high  = lastClose;
							price.low   = lastClose;
							price.close = lastClose;
						}
						// add new entry
						map.put(date, price);
						//
						lastClose = price.close;
						countChange++;
					} else {
						// existing entry
						// sanity check
						if (oldPrice.equals(price)) {
							// same value
						} else {
							if (price.volume == 0 && oldPrice.volume == 0) {
								// expected
								//   price of no volume is using previous close price
							} else {
								// expected
								//   price of today is real time, so value can be changed
								//   price of today is not updated same day
								//   stock has split
								
								// new price has correct value
								map.put(date, price);
								countChange++;
							}
						}
						// use close of oldPrice
						lastClose = oldPrice.close;
					}
				}
				if (countChange != 0) countMod++;
			}

			StockPrice.save(stockCode, map.values());
		}
		
		logger.info("count    {}", count);
		logger.info("countMod {}", countMod);
	}

	private static void update() {
		Context context = new Context();
		
		buildContext(context);
		updatePrice(context);
	}
	
	public static void main(String[] args) throws IOException {
		logger.info("START");
		
		update();
		
		logger.info("STOP");
	}
}
