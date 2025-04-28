package yokwe.finance.provider.jpx;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.chrono.ChronoLocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import yokwe.finance.type.StockCodeJP;
import yokwe.util.ToString;
import yokwe.util.UnexpectedException;
import yokwe.util.http.Download;
import yokwe.util.http.DownloadSync;
import yokwe.util.http.FileTask;
import yokwe.util.http.RequesterBuilder;
import yokwe.util.json.JSON;
import yokwe.util.json.JSON.Ignore;

public class UpdateIntraDayStockPrice {
	private static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();
	
	private static final String URL_FORMAT = "https://quote.jpx.co.jp/jpxhp/chartapi/jcgi/qjsonp.cgi?F=json/ja_stk_hist_i&quote=%s/T";
	private static final String REFERER    = "https://quote.jpx.co.jp/jpxhp/main/index.aspx?f=stock_detail&disptype=information&qcode=%s";
	private static final String USER_AGENT = "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit";
	
	public static class Result {
		public String   cputime;
		public Section1 section1;
		public int      status;
		public String   ver;
		
		@Override
		public String toString() {
			return ToString.withFieldName(this);
		}
	}
	
	public static class Section1 {
		public Map<String, Data> data;
		public int               hitcount;
		public int               status;
		public String            type;
		
		@Override
		public String toString() {
			return ToString.withFieldName(this);
		}
	}

	public static class Data {
        public String DPG; // "0058",
        
        public String HISTMDATE1;  // "2025/04/08",
        public String HISTMDATE2;  // "2025/04/07",
        public String HISTMDATE3;  // "2025/04/04",
        public String HISTMDATE4;  // "2025/04/03",
        public String HISTMDATE5;  // "2025/04/02",
        public String HISTMDATE6;  // "2025/04/01",
        public String HISTMDATE7;  // "2025/03/31",
        public String HISTMDATE8;  // "2025/03/28",
        public String HISTMDATE9;  // "2025/03/27",
        public String HISTMDATE10; // "2025/03/26",
        
        public String[][] HISTMIN1;
        public String[][] HISTMIN2;
        public String[][] HISTMIN3;
        public String[][] HISTMIN4;
        public String[][] HISTMIN5;
        public String[][] HISTMIN6;
        public String HISTMIN7;
        public String HISTMIN8;
        public String HISTMIN9;
        public String HISTMIN10;
        @Ignore
        public String HISTMIN11;
        
        public String LOSH;    //"100",
        public String MPFU;    // "0.1",
        public String NAME;    // "ＮＴＴ",
        public String PRP;     // "138.9",
        public String TTCODE;  // "9432/T",
        public String TTCODE2; // "9432",
        public String TZ;      // "+09:00",
        public String ZXD;     //"2025/04/08"
        
		@Override
		public String toString() {
			return ToString.withFieldName(this);
		}
	}
	
	public static void main(String[] args) {
		logger.info("START");
		
		process();
		
		logger.info("STOP");
	}
	
	
	
	private static void process() {
		var stockList = StorageJPX.StockList.getList();
		
		download(stockList);
		update(stockList);
	}
	
	
	private static void download(List<StockListType> stockList) {
		int threadCount       = 20;
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
				.setSoTimeout(soTimeout)
				.setMaxTotal(maxTotal)
				.setDefaultMaxPerRoute(maxPerRoute);

//		Download download = new DownloadAsync();
		Download download = new DownloadSync();
		
		download.setRequesterBuilder(requesterBuilder);
		
		// Configure custom header
		download.setUserAgent(USER_AGENT);
		download.setReferer(REFERER);
		
		// Configure thread count
		download.setThreadCount(threadCount);
		
		// connection timeout in second
		download.setConnectionTimeout(connectionTimeout);
		
		// progress interval
		download.setProgressInterval(progressInterval);
		
		Collections.shuffle(stockList);
		for(var stock: stockList) {
			var stockCode  = stock.stockCode;
			var uriString  = String.format(URL_FORMAT, StockCodeJP.toStockCode4(stockCode));
			var file = StorageJPX.IntraDayStockPriceJSON.getFile(stockCode);
			
			var task = FileTask.get(uriString, file);
			download.addTask(task);
		}
		Collections.sort(stockList);

		logger.info("BEFORE RUN");
		download.startAndWait();
		logger.info("AFTER  RUN");
//		download.showRunCount();
	}
	
	private static void update(Map<ChronoLocalDateTime<?>, OHLCVDateTime> map, String stockCode, String histmdate, String histmin) {
		if (histmin.isEmpty()) return;
		
		var date = LocalDate.parse(histmdate.replace('/', '-'));
		
		for(var data: histmin.split("\\\\n")) {
			var e = data.split(",");
			// sanity check
			if (e.length != 7) {
				logger.error("Unexpected data");
				logger.error("  e  {}!", ToString.withoutFieldName(e));
				throw new UnexpectedException("Unexpected data");
			}
			update(map, stockCode, date, e);
		}
	}
	private static void update(Map<ChronoLocalDateTime<?>, OHLCVDateTime> map, String stockCode, String histmdate, String[][] histmin) {
		if (histmdate.isEmpty()) return;
		
		var date = LocalDate.parse(histmdate.replace('/', '-'));
		
		for(var e: histmin) {
			update(map, stockCode, date, e);
		}
	}
	
	private static void update(Map<ChronoLocalDateTime<?>, OHLCVDateTime> map, String stockCode, LocalDate date, String[] e) {
		// sanity check
		if (e.length != 7) {
			logger.error("Unexpected data");
			logger.error("  e  {}!", ToString.withoutFieldName(e));
			throw new UnexpectedException("Unexpected data");
		}
		var time   = LocalTime.parse(e[0]);
		var open   = new BigDecimal(e[1]);
		var close  = new BigDecimal(e[2]);
		var high   = new BigDecimal(e[3]);
		var low    = new BigDecimal(e[4]);
		var volume = Long.valueOf(e[6]);
		
		var ohlcv = new OHLCVDateTime(LocalDateTime.of(date, time), open, high, low, close, volume);
		var old = map.put(ohlcv.dateTime, ohlcv);
		if (old == null) {
			// new entry
		} else {
			// existing entry
			if (old.equals(ohlcv)) {
				// expected
			} else {
				logger.error("Unexpected data");
				logger.error("  stock  {}", stockCode);
				logger.error("  new    {}", ohlcv);
				logger.error("  old    {}", old);
				throw new UnexpectedException("Unexpected data");
			}
		}
	}
	private static void update(List<StockListType> stockList) {
		int count = 0;
		for(var stock: stockList) {
			if ((++count % 1000) == 1) logger.info("{}  /  {}  {}", count, stockList.size(), stock.stockCode);
			
			var string = StorageJPX.IntraDayStockPriceJSON.load(stock.stockCode);
			var result = JSON.unmarshal(Result.class, string);
			{
				for(var data: result.section1.data.values()) {
					var stockCode = StockCodeJP.toStockCode5(data.TTCODE2);
					
					Map<ChronoLocalDateTime<?>, OHLCVDateTime> map;
					{
						var list = StorageJPX.IntraDayStockPrice.getList(stockCode);
						// remove last element of list. value of last element can be changed at next invocation
						if (!list.isEmpty()) {
							list.remove(list.size() - 1);
						}
						map = list.stream().collect(Collectors.toMap(o -> o.getKey(), Function.identity()));
					}
					
					update(map, stockCode, data.HISTMDATE1, data.HISTMIN1);
					update(map, stockCode, data.HISTMDATE2, data.HISTMIN2);
					update(map, stockCode, data.HISTMDATE3, data.HISTMIN3);
					update(map, stockCode, data.HISTMDATE4, data.HISTMIN4);
					update(map, stockCode, data.HISTMDATE5, data.HISTMIN5);
					update(map, stockCode, data.HISTMDATE6, data.HISTMIN6);
					
					update(map, stockCode, data.HISTMDATE7,  data.HISTMIN7);
					update(map, stockCode, data.HISTMDATE8,  data.HISTMIN8);
					update(map, stockCode, data.HISTMDATE9,  data.HISTMIN9);
					update(map, stockCode, data.HISTMDATE10, data.HISTMIN10);
					
					StorageJPX.IntraDayStockPrice.save(stockCode, map.values());
				}
			}
		}
	}
}
