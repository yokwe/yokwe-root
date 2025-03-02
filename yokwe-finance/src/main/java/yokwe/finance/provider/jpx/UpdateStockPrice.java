package yokwe.finance.provider.jpx;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import yokwe.finance.type.OHLCV;
import yokwe.finance.type.StockInfoJPType;
import yokwe.util.CSVUtil;
import yokwe.util.FileUtil;
import yokwe.util.MarketHoliday;
import yokwe.util.StringUtil;
import yokwe.util.UnexpectedException;
import yokwe.util.http.Download;
import yokwe.util.http.DownloadSync;
import yokwe.util.http.RequesterBuilder;
import yokwe.util.http.StringTask;
import yokwe.util.http.Task;
import yokwe.util.json.JSON;
import yokwe.util.json.JSON.Ignore;

public class UpdateStockPrice {
	private static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();
	
	private static final String URL_FORMAT = "https://quote.jpx.co.jp/jpxhp/jcgi/wrap/qjsonp.aspx?F=ctl/stock_detail&qcode=%s";
	private static final String REFERENCE  = "https://quote.jpx.co.jp/jpxhp/main/index.aspx?f=stock_detail&disptype=information&qcode=%s";
	private static final String USER_AGENT = "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit";
	
	private static final String PATH_PRIFIX = "stockDetail";
	private static final String NAME_FORMAT = "%s.json";
	
	private static final List<StockListType> stockList = StorageJPX.StockList.getList();
	private static final int stockListSize = stockList.size();
	
	private static final LocalDate lastTradingDate = MarketHoliday.JP.getLastTradingDate();
	
	
	public static class Result {
		public String   cputime;
		public Section1 section1;
		public int      status;
		public String   ver;
		@Ignore
		public Object   urlparam;
		
		@Override
		public String toString() {
			return StringUtil.toString(this);
		}
	}
	
	public static class Section1 {
		public Map<String, Data> data;
		public int               hitcount;
		public int               status;
		public String            type;
		
		@Override
		public String toString() {
			return StringUtil.toString(this);
		}
	}
	
	public static class Data {
		@Ignore
		public String A_CALC027; // "-"
		@Ignore
        public String A_CALC028; // "-"
		@Ignore
        public String A_CALC029; // "-"
        public String A_HISTDAYL; // "2023/09/06,3865.0,3920.0,3865.0,3915.0,20900,\n"
        public String DHP;  // 高値 "4,120"
        public String DHPT; // 高値 時刻 "09:00"
		@Ignore
        public String DJ;   // 売買代金 "39,077,500"
        public String DLP;  // 低値 "4,100"
        public String DLPT; // 低値 時刻 "09:01"
        public String DOP;  // 始値 "4,120"
        public String DOPT; // 始値 時刻 "09:00"
        public String DPP;  // 現在値 "4,105"
        public String DPPT; // 現在値 時刻 "10:41"
        public String DV;   // 売買高 "9,500"
		@Ignore
        public String DYRP; // 前日比 パーセント "-0.72"
		@Ignore
        public String DYWP; // 前日比 "-30"
		@Ignore
        public String EXCC; // "T"
		@Ignore
        public String EXDV; // "0000"
        public String FLLN; // 名前 "極洋"
		@Ignore
        public String FLLNE; // "KYOKUYO CO., LTD."
		@Ignore
        public String FTRTS; // ""
        public String ISIN; // "JP3257200000"
		@Ignore
        public String JDMKCP; // "-"
		@Ignore
        public String JDSHRK; // "-"
		@Ignore
        public String JSEC; // sector33Code"0050"
        public String LISS; // "ﾌﾟﾗｲﾑ"
		@Ignore
        public String LISSE; // ""
        public String LOSH; // 売買単位 "100"
        public String MKCP; // 時価総額 "49,943,700,205.0"
		@Ignore
        public String NAME; // "極　洋"
		@Ignore
        public String NAMEE; // "KYOKUYO"
		@Ignore
        public String PRP; // 前日終値 "4,135"
		@Ignore
        public String PRSS; // "C"
		@Ignore
        public String PSTS; // ""
        public String QAP;  // 売気配 "4,115"
        public String QAPT; // 売気配 時刻"10:43"
        public String QBP;  // 買気配 "4,105"
        public String QBPT; // 買気配 時刻 "10:43"
        public String SHRK; // 発行済株式数 "12,078,283"
		@Ignore
        public String TTCODE;  // "1301/T"
        public String TTCODE2; // "1301"
        public String YHPD; // 年初来高値 日付 "2024/09/24"
        public String YHPR; // 年初来高値 "4,600"
        public String YLPD; // 年初来安値 日付 "2024/08/05"
        public String YLPR; // 年初来安値 "3,400"
        public String ZXD;  // 売買日付 "2025/02/28"
		@Ignore
        public int    DYWP_FLG; // -1
		@Ignore
        public int    DYRP_FLG; // -1
		@Ignore
        public int    A_CALC029_FLG; // 0
		@Ignore
        public String EXDV_CNV; // ""
		@Ignore
        public String EXDVE_CNV; // ""
		@Ignore
        public Index[] INDEX_INFOMATION; // 0
        public String JSEC_CNV;  // sector33 日本顔 "水産・農林業"
		@Ignore
        public String JSECE_CNV; // sector33 英語 "Fishery, Agriculture & Forestry"
		@Ignore
        public String PSTSE; // ""
        public String LISS_CNV; // "プライム"
		@Ignore
        public String LISSE_CNV; // "Prime"
        
		@Override
		public String toString() {
			return StringUtil.toString(this);
		}
	}
	
	public static class Index {
        public String YEAR;     // "2024/09"
        public String FY;       // "中間"
        public String FYE;      // "Company / Interim"
        public String EPS;      // "-"
        public String CPTLPSTK; // "-"
        public String ROE;      // "-"
        public String PER;      // "-"
        public String PBR;      // "-"
        public String DIVD;     // "0.00"
        public String KABU;     // "-"
	}


	public static String getURL(String stockCode) {
		var stockCode4 = StockInfoJPType.toStockCode4(stockCode);
		return String.format(URL_FORMAT, stockCode4);
	}

	private static class Context {
		private final Map<String, String> stringMap = new TreeMap<>();
		private final AtomicInteger       count     = new AtomicInteger();
		
		public void put(String stockCode, String string) {
			synchronized(stringMap) {
				stringMap.put(stockCode, string);
			}
		}

		public void incrementBuildCount() {
			count.addAndGet(1);
		}
		public int getBuildCount() {
			return count.intValue();
		}
	}
	
	private static void buildContextFromPage(Context context, String stockCode, String name, String page) {
		context.put(stockCode, page);
	}

	private static class MyConsumer implements Consumer<String> {
		private final Context   context;
		private final String    stockCode;
		private final String    name;
		
		MyConsumer(Context context, String stockCode, String name) {
			this.context   = context;
			this.stockCode = stockCode;
			this.name      = name;
		}
		@Override
		public void accept(String page) {
			buildContextFromPage(context, stockCode, name, page);
			// save for later use
			var file = StorageJPX.storage.getFile(PATH_PRIFIX, String.format(NAME_FORMAT, stockCode));
			FileUtil.write().file(file, page);
			//
			context.incrementBuildCount();
		}
	}

	public static void buildContext(Context context) {
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
		download.setReferer(REFERENCE);
		
		// Configure thread count
		download.setThreadCount(threadCount);
		
		// connection timeout in second
		download.setConnectionTimeout(connectionTimeout);
		
		// progress interval
		download.setProgressInterval(progressInterval);
		
		var list = new ArrayList<StockListType>(stockList);
		Collections.shuffle(list);
		for(var stock: list) {
			String stockCode = stock.stockCode;			
			String uriString = getURL(stockCode);
			String name      = stock.name;
			Task   task      = StringTask.get(uriString, new MyConsumer(context, stockCode, name));
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
	
	private static List<OHLCV> getPriceList(Data data) {
		List<OHLCV> priceList = new ArrayList<>();
		{
			var stockCode = StockInfoJPType.toStockCode5(data.TTCODE2);
//			logger.info("stock  {}  {}", stockCode, data.FLLN);

			BigDecimal o = null;
			BigDecimal h = null;
			BigDecimal l = null;
			BigDecimal c = null;
			
			for(var ohlcvString: data.A_HISTDAYL.split("\\n")) {
				String[] valueString = ohlcvString.split(",");
				if (valueString.length < 6) {
					logger.error("Unexpected ohlcvString");
					logger.error("  stockCode    {}", stockCode);
					logger.error("  valueString  {}", valueString.length);
					logger.error("  ohlcvString  {}", ohlcvString);
					throw new UnexpectedException("Unexpected ohlcvString");
				}
				var dateString = valueString[0];
				var oString    = valueString[1];
				var hString    = valueString[2];
				var lString    = valueString[3];
				var cString    = valueString[4];
				var vString    = valueString[5];
				
				var date = LocalDate.parse(dateString.replace('/', '-'));
				var v = Long.parseLong(vString);
				if (v == 0) {
					if (o == null) continue;
					// use last value
				} else {
					o = new BigDecimal(oString);
					h = new BigDecimal(hString);
					l = new BigDecimal(lString);
					c = new BigDecimal(cString);
				}
				
				priceList.add(new OHLCV(date, o, h, l, c, v));
			}
		}
		return priceList;
	}
	
	private static File getPriceFile(String stockCode) {
		return StorageJPX.storage.getFile("stockPrice", String.format("%s.csv", stockCode));
	}
	
	private static void updatePrice(String string) {
		var result = JSON.unmarshal(Result.class, string);
		
		for(var data: result.section1.data.values()) {
			var stockCode = StockInfoJPType.toStockCode5(data.TTCODE2);
			var file = getPriceFile(stockCode);
			var oldList   = CSVUtil.read(OHLCV.class).file(file);
			var oldMap    = oldList.stream().collect(Collectors.toMap(o -> o.date, o -> o));

			var newList   = getPriceList(data);
			Collections.sort(newList);
			var newMap    = newList.stream().collect(Collectors.toMap(o -> o.date, o -> o));
			
			boolean needsMergeList = true;
			{
				var date = MarketHoliday.JP.getPreviousTradingDate(lastTradingDate);
				
				if (oldMap.containsKey(date) && newMap.containsKey(date)) {
					var oldPrice = oldMap.get(date);
					var newPrice = newMap.get(date);
					
					if (oldPrice.equals(newPrice)) {
						// expected
					} else {
						// not expected
						logger.info("price changed  {}  {}  {}", stockCode, oldPrice, newPrice);
						needsMergeList = false;
						break;
					}
				}
			}
			
			if (needsMergeList) {
				for(var oldPrice: oldList) {
					if (newMap.containsKey(oldPrice.date)) {
						// exist in newList
						var newPrice = newMap.get(oldPrice.date);
						if (newPrice.equals(oldPrice)) {
							// expected
						} else {
							// not expected
							logger.error("Unexpected price");
							logger.error("  sockCode  {}", stockCode);
							logger.error("  oldPrice  {}", oldPrice);
							logger.error("  newPrice  {}", newPrice);
							throw new UnexpectedException("Unexpected price");
						}
					} else {
						// not exist in newList
						newList.add(oldPrice);
					}
				}
			}
//			logger.info("save  {}  {}", priceList.size(), priceFile.getPath());
			CSVUtil.write(OHLCV.class).file(file, newList);
		}
	}
	
	public static void updatePrice(Context context) {
		// update price using list (StockPrice)
		logger.info("updatePrice");
		
		int count = 0;
		for(var string: context.stringMap.values()) {
			if ((++count % 1000) == 1) logger.info("{}  /  {}", count, stockListSize);
			updatePrice(string);
		}
	}
	public static void updatePrice() {
		// update price using list (StockPrice)
		logger.info("updatePrice");
		
		int count = 0;
		for(var stock: stockList) {
			if ((++count % 1000) == 1) logger.info("{}  /  {}", count, stockListSize);

			var file   = StorageJPX.storage.getFile(PATH_PRIFIX, String.format(NAME_FORMAT, stock.stockCode));
			var string = FileUtil.read().file(file);
			updatePrice(string);
		}
	}
	
	private static void update() {
		Context context = new Context();
		buildContext(context);
		updatePrice(context);
		
		// use file in stockPrice
//		updatePrice();
	}
	
	public static void main(String[] args) throws IOException {
		logger.info("START");
		
		update();
		
		logger.info("STOP");
	}
}
