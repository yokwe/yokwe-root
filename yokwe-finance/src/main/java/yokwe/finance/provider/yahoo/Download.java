package yokwe.finance.provider.yahoo;

import java.io.StringReader;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.stream.Collectors;

import yokwe.finance.type.DailyValue;
import yokwe.finance.type.OHLCV;
import yokwe.finance.type.StockInfoJPType;
import yokwe.finance.type.StockInfoUSType;
import yokwe.util.CSVUtil;
import yokwe.util.UnexpectedException;
import yokwe.util.http.HttpUtil;

public class Download {
	private static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();
	
	//
	// operation relate to download end point
	//

	private static final String  URL            = "https://query1.finance.yahoo.com/v7/finance/download/";
	private static final Charset CHARSET_UTF_8  = StandardCharsets.UTF_8;
	
	public static class RAW {
		public static class Price {
			@CSVUtil.ColumnName("Date")       LocalDate  date;
			@CSVUtil.ColumnName("Open")       String     open;
			@CSVUtil.ColumnName("High")       String     high;
			@CSVUtil.ColumnName("Low")        String     low;
			@CSVUtil.ColumnName("Close")      String     close;
			@CSVUtil.ColumnName("Adj Close")  String     adjClose;
			@CSVUtil.ColumnName("Volume")     String     volume;
		}
		public static class Dividend {
			@CSVUtil.ColumnName("Date")          LocalDate  date;
			@CSVUtil.ColumnName("Dividends")     BigDecimal amount;
		}
		public static class Split {
			@CSVUtil.ColumnName("Date")          LocalDate  date;
			@CSVUtil.ColumnName("Stock Splits")  String     detail; // FIXME after check format, change type
		}
		public static class CapitalGain {
			@CSVUtil.ColumnName("Date")          LocalDate  date;
			@CSVUtil.ColumnName("Capital Gains") String     detail;  // FIXME after check format, change type
		}
	}
		
	public enum Interval {
		DAILY("1d"),
		WEEKLY("1wk"),
		MONTHLY("1mo");
		
		public final String value;
		private Interval(String value) {
			this.value = value;
		}
	}
	public enum Events {
		PRICE    ("history"), // historical price
		DIVIDEND ("div"),
		SPLIT    ("split"),
		CAPITAL  ("capitalGain");
		
		public final String value;
		
		private Events(String value) {
			this.value = value;
		}
	}
	
	// NOTE period is between period1(inclusive) and period2(exclusive)
	private static String getURL(String stockCode, LocalDate period1, LocalDate period2, Interval interval, Events events) {
		String url = URL + stockCode;
		
		LinkedHashMap<String, String> map = new LinkedHashMap<>();
		map.put("period1",  String.valueOf(period1.atStartOfDay().toEpochSecond(ZoneOffset.UTC)));
		map.put("period2",  String.valueOf(period2.atStartOfDay().toEpochSecond(ZoneOffset.UTC)));
		map.put("interval", interval.value);
		map.put("events",   events.value);
		map.put("includeAdjustedClose", "true");
		String queryString = map.entrySet().stream().map(o -> o.getKey() + "=" + URLEncoder.encode(o.getValue(), CHARSET_UTF_8)).collect(Collectors.joining("&"));
		
		return String.format("%s?%s", url, queryString);
	}
	
	private static String getString(String stockCode, LocalDate period1, LocalDate period2, Interval interval, Events event) {
		String url = getURL(stockCode, period1, period2, interval, event);
		
		HttpUtil httpUtil = HttpUtil.getInstance();
		HttpUtil.Result result = httpUtil.download(url);
		if (result == null) {
			logger.warn("result is null");
			return null;
		}
		if (result.result == null) {
			logger.warn("result  {}", result.toString());
			return null;
		}
		return result.result;
	}
	
	
	//
	// Price
	//
	private static List<OHLCV> getPrice(String stockCode, LocalDate period1, LocalDate period2, Interval interval) {
		String string = getString(stockCode, period1, period2, interval, Events.PRICE);
		if (string == null) return null;
		
		List<OHLCV> list = new ArrayList<>();
		{
			List<RAW.Price> rawList = CSVUtil.read(RAW.Price.class).file(new StringReader(string));
			
			for(var e: rawList) {
				BigDecimal open   = e.open.equals("null")   ? BigDecimal.ZERO : new BigDecimal(e.open);
				BigDecimal high   = e.high.equals("null")   ? BigDecimal.ZERO : new BigDecimal(e.high);
				BigDecimal low    = e.low.equals("null")    ? BigDecimal.ZERO : new BigDecimal(e.low);
				BigDecimal close  = e.close.equals("null")  ? BigDecimal.ZERO : new BigDecimal(e.close);
				long       volume = e.volume.equals("null") ? 0 : Long.valueOf(e.volume);
				list.add(new OHLCV(e.date, open, high, low, close, volume));
			}
		}
		
		return list;
	}
	private static List<OHLCV> getPrice(String stockCode, LocalDate period1, LocalDate period2) {
		return getPrice(stockCode, period1, period2, Interval.DAILY);
	}
	
	
	//
	// Dividend
	//
	private static List<DailyValue> getDividend(String stockCode, LocalDate period1, LocalDate period2, Interval interval) {
		// sanity check
		if (!period2.isAfter(period1)) {
			logger.error("Unexpected date rage");
			logger.error("  {}  {}  {}", stockCode, period1, period2);
			throw new UnexpectedException("Unexpected date rage");
		}
		
		String string = getString(stockCode, period1, period2, interval, Events.DIVIDEND);
		if (string == null) return null;	

		List<DailyValue> list = new ArrayList<>();
		{
			List<RAW.Dividend> rawList = CSVUtil.read(RAW.Dividend.class).file(new StringReader(string));
			
			for(var e: rawList) {
				BigDecimal amount = e.amount.stripTrailingZeros();
				list.add(new DailyValue(e.date, amount));
			}
		}
		
		return list;
	}
	private static List<DailyValue> getDividend(String stockCode, LocalDate period1, LocalDate period2) {
		return getDividend(stockCode, period1, period2, Interval.DAILY);
	}
	
	//
	// Split
	//
	private static List<Split> getSplit(String stockCode, LocalDate period1, LocalDate period2, Interval interval) {
		String string = getString(stockCode, period1, period2, interval, Events.SPLIT);
		if (string == null) return null;	

		List<Split> list = new ArrayList<>();
		{
			List<RAW.Split> rawList = CSVUtil.read(RAW.Split.class).file(new StringReader(string));
			
			for(var e: rawList) {
				list.add(new Split(e.date, e.detail));
			}
		}
		
		return list;
	}
	private static List<Split> getSplit(String stockCode, LocalDate period1, LocalDate period2) {
		return getSplit(stockCode, period1, period2, Interval.DAILY);
	}
	
	//
	// Capital Gain
	//
	private static List<CapitalGain> getCapitalGain(String stockCode, LocalDate period1, LocalDate period2, Interval interval) {
		String string = getString(stockCode, period1, period2, interval, Events.CAPITAL);
		if (string == null) return null;	
		
		List<CapitalGain> list = new ArrayList<>();
		{
			List<RAW.CapitalGain> rawList = CSVUtil.read(RAW.CapitalGain.class).file(new StringReader(string));
			
			for(var e: rawList) {
				list.add(new CapitalGain(e.date, e.detail));
			}
		}
		
		return list;
	}
	private static List<CapitalGain> getCapitalGain(String stockCode, LocalDate period1, LocalDate period2) {
		return getCapitalGain(stockCode, period1, period2, Interval.DAILY);
	}
	
	public static final class US {
		public static String toYahooStockCode(String stockCode) {
			return StockInfoUSType.toYahooSymbol(stockCode);
		}
		
		public static List<OHLCV> getPrice(String stockCode, LocalDate period1, LocalDate period2) {
			return Download.getPrice(toYahooStockCode(stockCode), period1, period2);
		}
		public static List<DailyValue> getDividend(String stockCode, LocalDate period1, LocalDate period2) {
			return Download.getDividend(toYahooStockCode(stockCode), period1, period2);
		}
		public static List<Split> getSplit(String stockCode, LocalDate period1, LocalDate period2) {
			return Download.getSplit(toYahooStockCode(stockCode), period1, period2);
		}
		public static List<CapitalGain> getCapitalGain(String stockCode, LocalDate period1, LocalDate period2) {
			return Download.getCapitalGain(toYahooStockCode(stockCode), period1, period2);
		}
	}
	public static final class JP {
		public static String toYahooStockCode(String stockCode) {
			return StockInfoJPType.toYahooSymbol(stockCode);
		}
		
		public static List<OHLCV> getPrice(String stockCode, LocalDate period1, LocalDate period2) {
			var list = Download.getPrice(toYahooStockCode(stockCode), period1, period2);
			// normalize value
			for(var e: list) {
				e.open  = e.open.setScale(1, RoundingMode.HALF_EVEN);
				e.high  = e.high.setScale(1, RoundingMode.HALF_EVEN);
				e.low   = e.low.setScale(1, RoundingMode.HALF_EVEN);
				e.close = e.close.setScale(1, RoundingMode.HALF_EVEN);
			}
			return list;
		}
		public static List<DailyValue> getDividend(String stockCode, LocalDate period1, LocalDate period2) {
			return Download.getDividend(toYahooStockCode(stockCode), period1, period2);
		}
		public static List<Split> getSplit(String stockCode, LocalDate period1, LocalDate period2) {
			return Download.getSplit(toYahooStockCode(stockCode), period1, period2);
		}
		public static List<CapitalGain> getCapitalGain(String stockCode, LocalDate period1, LocalDate period2) {
			return Download.getCapitalGain(toYahooStockCode(stockCode), period1, period2);
		}
	}
	

	
//	static void testPrice() {
//		String methodName = ClassUtil.getCallerMethodName();
//		
//		String stockCode = "AMZN";
//		int year = 2023;
//		int month = 6;
//		LocalDate period1 = LocalDate.of(year, month, 01);
//		LocalDate period2 = LocalDate.of(year, month, 10);
//		LocalDate period3 = LocalDate.of(year, month, 20);
//		
//		var list12 = getPrice(stockCode, period1, period2);
//		var list23 = getPrice(stockCode, period2, period3);
//		
//		for(var e: list12) {
//			logger.info("{}  list12  {}  {}", methodName, stockCode, e);
//		}
//		for(var e: list23) {
//			logger.info("{}  list23  {}  {}", methodName, stockCode, e);
//		}
//	}
//	
//	static void testDividend() {
//		String methodName = ClassUtil.getCallerMethodName();
//
//		String stockCode = "QQQ";
//		LocalDate period1 = LocalDate.of(2000, 1, 1);
//		LocalDate period2 = LocalDate.now();
//		
//		var list12 = getDividend(stockCode, period1, period2);
//		
//		for(var e: list12) {
//			logger.info("{}  list12  {}  {}", methodName, stockCode, e);
//		}
//	}
//	
//	static void testSplit() {
//		String methodName = ClassUtil.getCallerMethodName();
//
//		// CRF 2008-12-23 1:2
//		// CRF 2014-12-29 1:4
//		// AMZN 2022-06-06 20:1
//		String stockCode = "AMZN";
//		int year = 2022;
//		int month = 5;
//		LocalDate period1 = LocalDate.of(year, month + 0, 1);
//		LocalDate period2 = LocalDate.of(year, month + 1, 1);
//		LocalDate period3 = LocalDate.of(year, month + 2, 1);
//		
//		var list12 = getSplit(stockCode, period1, period2);
//		var list23 = getSplit(stockCode, period2, period3);
//		
//		for(var e: list12) {
//			logger.info("{}  list12  {}  {}", methodName, stockCode, e);
//		}
//		for(var e: list23) {
//			logger.info("{}  list23  {}  {}", methodName, stockCode, e);
//		}
//	}
//	static void testCapitalGain() {
//		String methodName = ClassUtil.getCallerMethodName();
//		
//		String stockCode = "CRF";
//		LocalDate period1 = LocalDate.of(1980, 1, 1);
//		LocalDate period2 = LocalDate.now();
//		
//		var list12 = getCapitalGain(stockCode, period1, period2);
//		
//		for(var e: list12) {
//			logger.info("{}  list12  {}  {}", methodName, stockCode, e);
//		}
//	}
//	public static void main(String[] args) {
//		logger.info("START");
//		
//		testPrice();
//		testDividend();
//		testSplit();
//		testCapitalGain();
//		
//		logger.info("STOP");
//	}
}
