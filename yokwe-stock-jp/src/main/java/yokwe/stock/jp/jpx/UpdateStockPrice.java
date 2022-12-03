package yokwe.stock.jp.jpx;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.function.Consumer;

import org.apache.hc.core5.http2.HttpVersionPolicy;

import yokwe.stock.jp.jpx.StockPage.BuyPriceTime;
import yokwe.stock.jp.jpx.StockPage.CompanyInfo;
import yokwe.stock.jp.jpx.StockPage.CurrentPriceTime;
import yokwe.stock.jp.jpx.StockPage.HighPrice;
import yokwe.stock.jp.jpx.StockPage.Issued;
import yokwe.stock.jp.jpx.StockPage.LastClosePrice;
import yokwe.stock.jp.jpx.StockPage.LowPrice;
import yokwe.stock.jp.jpx.StockPage.OpenPrice;
import yokwe.stock.jp.jpx.StockPage.PriceVolume;
import yokwe.stock.jp.jpx.StockPage.SellPriceTime;
import yokwe.stock.jp.jpx.StockPage.TradeUnit;
import yokwe.stock.jp.jpx.StockPage.TradeValue;
import yokwe.stock.jp.jpx.StockPage.TradeVolume;
import yokwe.util.MarketHoliday;
import yokwe.util.UnexpectedException;
import yokwe.util.http.Download;
import yokwe.util.http.DownloadSync;
import yokwe.util.http.RequesterBuilder;
import yokwe.util.http.StringTask;
import yokwe.util.http.Task;

public class UpdateStockPrice {
	static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(UpdateStockPrice.class);
	
	private static String getPageURL(String stockCode) {
		String stockCode4 = Stock.toStockCode4(stockCode);
		return String.format("https://quote.jpx.co.jp/jpx/template/quote.cgi?F=tmp/stock_detail&MKTN=T&QCODE=%s", stockCode4);
	}

	private static class Context {
		private List<StockPrice>               stockPriceList;
		private List<StockInfo>                stockInfoList;
		private Map<String, List<PriceVolume>> priceVolumeMap;
		private Integer                        buildCount;
				
		public void add(StockPrice newValue) {
			synchronized(stockPriceList) {
				stockPriceList.add(newValue);
			}
		}
		public void add(StockInfo newValue) {
			synchronized(stockInfoList) {
				stockInfoList.add(newValue);
			}
		}
		public void put(String key, List<PriceVolume> value) {
			synchronized(priceVolumeMap) {
				priceVolumeMap.put(key, value);
			}
		}

		public void incrementBuildCount() {
			synchronized(buildCount) {
				buildCount = buildCount + 1;
			}
		}
		public int getBuildCount() {
			int ret;
			synchronized(buildCount) {
				ret = buildCount;
			}
			return ret;
		}

		public Context() {
			this.stockPriceList = new ArrayList<>();
			this.stockInfoList  = new ArrayList<>();
			this.priceVolumeMap = new TreeMap<>();
			this.buildCount     = 0;
		}
	}
	
	private static final DateTimeFormatter FORMAT_HHMM = DateTimeFormatter.ofPattern("HH:mm");
	private static void buildContextFromPage(Context context, LocalDateTime dateTime, String stockCode, String page) {
		if (page.contains("指定された銘柄が見つかりません")) {
			//
		} else {
			CompanyInfo      companyInfo      = CompanyInfo.getInstance(page);
			TradeUnit        tradeUnit        = TradeUnit.getInstance(page);
			Issued           issued           = Issued.getInstance(page);
			CurrentPriceTime currentPriceTime = CurrentPriceTime.getInstance(page);
			BuyPriceTime     buyPriceTime     = BuyPriceTime.getInstance(page);
			SellPriceTime    sellPriceTime    = SellPriceTime.getInstance(page);
			OpenPrice        openPrice        = OpenPrice.getInstance(page);
			HighPrice        highPrice        = HighPrice.getInstance(page);
			LowPrice         lowPrice         = LowPrice.getInstance(page);
			TradeVolume      tradeVolume      = TradeVolume.getInstance(page);
			TradeValue       tradeValue       = TradeValue.getInstance(page);
			LastClosePrice   lastClosePrice   = LastClosePrice.getInstance(page);
			List<PriceVolume> priceVolumeList = PriceVolume.getInstance(page);
			
			// save for later use
			context.put(stockCode, priceVolumeList);

			// build stockInfoList
			{
				// String stockCode, String isin, int tradeUnit, long issued
				long issuedValue;
				if (issued.value == null) {
					logger.warn("issued.value is null  {}", stockCode);
					issuedValue = 0;
				} else {
					issuedValue = issued.value;
				}
				StockInfo stockInfo = new StockInfo(stockCode, companyInfo.isin, tradeUnit.value, issuedValue);
				context.add(stockInfo);
			}

			String date = dateTime.toLocalDate().toString();
			String time = dateTime.toLocalTime().format(FORMAT_HHMM);
			
			String price     = currentPriceTime.price.orElse("");
			String priceTime = currentPriceTime.time.orElse("");
			
			String sell      = sellPriceTime.price.orElse("");
			String sellTime  = sellPriceTime.time.orElse("");
			
			String buy       = buyPriceTime.price.orElse("");
			String buyTime   = buyPriceTime.time.orElse("");
			
			String open      = openPrice.value.orElse("");
			String high      = highPrice.value.orElse("");
			String low       = lowPrice.value.orElse("");
			
			String volume    = tradeVolume.value.orElse("");
			String trade     = tradeValue.value.orElse("");
			
			String lastClose = lastClosePrice.value.orElse("");

			StockPrice stockPrice = new StockPrice(
					date,
					time,
					stockCode,
					
					price,
					priceTime,
					
					sell,
					sellTime,
					
					buy,
					buyTime,
					
					open,
					high,
					low,
					
					volume,
					trade,
					
					lastClose
				);
			context.add(stockPrice);
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

		List<Stock> stockList = Stock.getList();
		Collections.shuffle(stockList);
		final int stockListSize = stockList.size();
		
		LocalDateTime dateTime = LocalDateTime.now();
		
		for(Stock stock: stockList) {
			String stockCode = stock.stockCode;
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
		int countTotal  = context.stockPriceList.size();
		for(StockPrice stockPrice: context.stockPriceList) {
			String date      = stockPrice.date;
			String stockCode = stockPrice.stockCode;
			
			if ((count % 1000) == 0) {
				logger.info("{}", String.format("%4d / %4d  %s", count, countTotal, stockCode));
			}
			count++;
			
			// Map of price of stock
			TreeMap<String, Price> priceMap = new TreeMap<>();
			//      date
			for(Price price: Price.getList(stockCode)) {
				if (MarketHoliday.JP.isClosed(price.date)) continue;
				
				priceMap.put(price.date, price);
			}
			
			// Update priceMap with priceVolumeList
			//   Because if stock split, historical price must be adjusted.
			{
				double lastClose = -1;
				for(PriceVolume priceVolume: context.priceVolumeMap.get(stockCode)) {
					String           priceDate = priceVolume.getDate();
					Optional<String> open      = priceVolume.open;
					Optional<String> high      = priceVolume.high;
					Optional<String> low       = priceVolume.low;
					Optional<String> close     = priceVolume.close;
					long             volume    = priceVolume.volume;
					
					Price price;
					if (priceMap.containsKey(priceDate)) {
						price = priceMap.get(priceDate);
					} else {
						price = new Price();
						// 	public Price(String date, String stockCode, double open, double high, double low, double close, long volume) {
						price.date      = priceDate;
						price.stockCode = stockCode;
					}
						
					if (volume == 0) {
						// use lastClose as each price
						price.open   = lastClose;
						price.high   = lastClose;
						price.low    = lastClose;
						price.close  = lastClose;
						price.volume = 0;
					} else {
						if (open.isPresent() && high.isPresent() && low.isPresent() && close.isPresent()) {
							price.open   = Double.parseDouble(open.get());
							price.high   = Double.parseDouble(high.get());
							price.low    = Double.parseDouble(low.get());
							price.close  = Double.parseDouble(close.get());
							price.volume = volume;
						} else {
							logger.error("Unexpected");
							logger.error("  priceVolume {}", priceVolume);
							throw new UnexpectedException("Unexpected");
						}
					}
					
					if (price.close != -1) {
						priceMap.put(priceDate, price);
						lastClose = price.close;
					}
				}
			}
			
			// update priceMap with data with stockPrice
			{
				double open;
				double high;
				double low;
				double close;
				long   volume;
				if (stockPrice.open.isEmpty() || stockPrice.high.isEmpty() || stockPrice.low.isEmpty() || stockPrice.price.isEmpty() || stockPrice.volume.isEmpty()) {
					// Cannot get lastPrice, skip to this entry
					if (priceMap.isEmpty()) {
						countSkip++;
						continue;
					}
					
					Price lastPrice;
					if (priceMap.containsKey(date)) {
						lastPrice = priceMap.get(date);
					} else {
						lastPrice = priceMap.lastEntry().getValue();
					}
					open   = lastPrice.close;
					high   = lastPrice.close;
					low    = lastPrice.close;
					close  = lastPrice.close;
					volume = stockPrice.volume.isEmpty() ? 0 : Long.parseLong(stockPrice.volume);
					countZero++;
				} else {
					open   = Double.parseDouble(stockPrice.open);
					high   = Double.parseDouble(stockPrice.high);
					low    = Double.parseDouble(stockPrice.low);
					close  = Double.parseDouble(stockPrice.price);
					volume = Long.parseLong(stockPrice.volume);
					countUpdate++;
				}

				if (!MarketHoliday.JP.isClosed(date)) {
					Price price = new Price(date, stockCode, open, high, low, close, volume);
					// Over write old entry or add new entry
					priceMap.put(date, price);
				}
			}
			
			Price.save(priceMap.values());
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
			List<StockPrice> list = context.stockPriceList;
			logger.info("save {} {}", StockPrice.getPath(), list.size());
			StockPrice.save(list);
		}
		
		{
			List<StockInfo> list = context.stockInfoList;
			logger.info("save {} {}",StockInfo.getPath(),  list.size());
			StockInfo.save(list);
		}

		// update each price file
		updatePrice(context);
	}
	
	public static void main(String[] args) throws IOException {
		logger.info("START");
		
		LocalDate today = LocalDate.now();
		if (MarketHoliday.JP.isClosed(today)) {
			logger.warn("market is closed today");
		} else {
			updateFiles();
		}

		logger.info("STOP");
	}
}
