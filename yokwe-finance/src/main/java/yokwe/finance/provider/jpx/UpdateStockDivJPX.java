package yokwe.finance.provider.jpx;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import yokwe.finance.type.DailyValue;
import yokwe.finance.type.StockCodeJP;
import yokwe.util.UnexpectedException;
import yokwe.util.http.Download;
import yokwe.util.http.DownloadSync;
import yokwe.util.http.RequesterBuilder;
import yokwe.util.http.StringTask;
import yokwe.util.http.Task;
import yokwe.util.json.JSON;
import yokwe.util.json.JSON.Optional;

public class UpdateStockDivJPX {
	private static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();
	
	private static final String USER_AGENT = "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit";
	private static final String REFERENCE  = "https://quote.jpx.co.jp/jpxhp/main/index.aspx?f=stock_detail&disptype=information&qcode=%s";

	private static final List<StockListType> stockList = StorageJPX.StockList.getList();
	private static final int stockListSize = stockList.size();

	public static class Kessan {
		public static class Section0 {
			@Optional
			public String FLLN;     //"極洋",
			@Optional
			public String FLLNE;    // "KYOKUYO CO., LTD.",
			@Optional
			public String K_KUBUN;  // "1",
			@Optional
			public String TTCODE2;  // "1301",
			public int    hitcount; // 1
		}
		public static class Section1 {
			@Optional
			public Data[] data;
			public int    hitcount; // 1
		}
		public static class Data implements Comparable<Data> {
            public String ALK_EDDATE_Y;    // 2023/03      年度末月
            public String ALK_EDDATEM;     // 2023/03      当期期末月
            //
            public String ALK_CASHVAL;     // 1,196,230    現金等期末残高（百万円）
            public String ALK_CPTL;        // 431,119      純資産（百万円）
            public String ALK_CPTLPSTK;    // 3,751.95     1株当たり純資産（円）
            public String ALK_DIVD;        // 154.00       1株当たり配当金（円）
            public String ALK_DIVDH;       // 40.00        四半期末配当金（円）
            public String ALK_EPS;         // 74.67        1株当たり当期純利益（円）
            public String ALK_FINCCF;      // -18,068      財務キャッシュフロー（百万円）
            public String ALK_FREECF;      // 152,557      営業+投資キャッシュフロー（百万円）
            public String ALK_INVCF;       // 213,939      投資キャッシュフロー（百万円）
            public String ALK_KIKAN2;      // 9            ？
            public String ALK_KIKAN2E_CNV; // full-year    ？
            public String ALK_KIKAN2_CNV;  // 通期         決算期
            public String ALK_NETP;        // 8,719        当期純利益（百万円）
            public String ALK_OPESALE;     // -            経常収益（百万円）
            public String ALK_OPRT;        // -            ？
            public String ALK_ORDP;        // 7,356        経常利益（百万円）
            public String ALK_ORDSALE;     // 4.0          ？
            public String ALK_ROE;         // 1.90         自己資本利益率
            public String ALK_SALE;        // 183,292      経常収益（百万円）
            public String ALK_SALECF;      // -61,382      営業キャッシュフロー（百万円）
            public String ALK_TOTLASET;    // 7,184,070    純資産
            public String ALS_SECC;        // 1            ？
            public String ALS_SECCE_CNV;   // Consolidated ？
            public String ALS_SECC_CNV;    // 連結         連単種別
            
			@Override
			public int compareTo(Data that) {
				int ret = this.ALK_EDDATE_Y.compareTo(that.ALK_EDDATE_Y);
				if (ret == 0) ret = this.ALK_EDDATEM.compareTo(that.ALK_EDDATEM);
				return ret;
			}

		}
		
		public Section0 section0;
		public Section1 section1;
		
		public boolean isEmpty() {
			return section0.hitcount == 0 && section1.hitcount == 0;
		}
	}
	
	private static String getURL(String stockCode) {
		String stockCode4 = StockCodeJP.toStockCode4(stockCode);
		return String.format("https://quote.jpx.co.jp/jpxhp/jcgi/wrap/kessan.asp?qcode=%s", stockCode4);
	}
	
	private static class Context {
		private final AtomicInteger count = new AtomicInteger();
		
		public void incrementBuildCount() {
			count.addAndGet(1);
		}
		public int getBuildCount() {
			return count.intValue();
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
			// save for later use
			StorageJPX.KessanJSON.save(stockCode, page);
			//
			context.incrementBuildCount();
		}
	}
	
	private static LocalDate toLocalDate(String string) {
		String[] token = string.split("/");
		if (token.length == 2) {
			int yyyy = Integer.valueOf(token[0]);
			int mm   = Integer.valueOf(token[1]);
			var date = LocalDate.of(yyyy, mm, 1);
			return date.with(TemporalAdjusters.lastDayOfMonth());
		}
		logger.error("Unexpected string");
		logger.error("  {}!", string);
		throw new UnexpectedException("Unexpected string");
	}

	private static void download() {
		int threadCount       = 20;
		int maxPerRoute       = 50;
		int maxTotal          = 100;
		int soTimeout         = 60;
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
		
		Context context = new Context();
		
		Collections.shuffle(stockList);
		for(var stock: stockList) {
			String stockCode = stock.stockCode;			
			String uriString = getURL(stockCode);
			Task   task      = StringTask.get(uriString, new MyConsumer(context, stockCode));
			download.addTask(task);
		}
		Collections.sort(stockList);

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
	
	private static void update() {
		int count = 0;
		for(var stock: stockList) {
			if ((++count % 1000) == 1) logger.info("{}  /  {}", count, stockListSize);
//			logger.info("{}  /  {}  {}", ++count, stockListSize, stock.stockCode);
			String stockCode = stock.stockCode;
			var string = StorageJPX.KessanJSON.load(stockCode);
			var kessan = JSON.unmarshal(Kessan.class, string);
			if (kessan.isEmpty()) continue;
			
			int countModify = 0;
			Map<LocalDate, BigDecimal> map = StorageJPX.StockDivJPX.getList(stockCode).stream().collect(Collectors.toMap(o -> o.date, o -> o.value));
			
			for(var e: kessan.section1.data) {
				if (e.ALK_DIVDH.equals("-")) continue;
				//
				var date     = toLocalDate(e.ALK_EDDATEM);
				var newValue = new BigDecimal(e.ALK_DIVDH.replaceAll(",", ""));
				var oldValue = map.put(date, newValue);
				if (oldValue == null) {
					// new
					countModify++;
				} else {
					// old
					if (oldValue.compareTo(newValue) == 0) {
						// same value
					} else {
						// not same value
						countModify++;
						logger.warn("value changed  {}  {}  {}  {}  {}", date, oldValue, newValue, stock.stockCode, stock.name);
					}
				}
			}
			if (countModify != 0) {
				var list = map.entrySet().stream().map(o -> new DailyValue(o.getKey(), o.getValue())).collect(Collectors.toList());
//				logger.info("save  {}  {}", list.size(), StorageJPX.StockDivJPX.getPath(stockCode));
				StorageJPX.StockDivJPX.save(stockCode, list);
			}
		}
	}

	private static void process() {
		download();
		update();
	}
	
	public static void main(String[] args) throws IOException {
		logger.info("START");
		
		process();
		
		logger.info("STOP");
	}
}
