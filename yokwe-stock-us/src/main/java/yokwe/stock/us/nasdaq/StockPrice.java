package yokwe.stock.us.nasdaq;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import yokwe.stock.us.Storage;
import yokwe.util.CSVUtil;
import yokwe.util.StringUtil;
import yokwe.util.UnexpectedException;

public class StockPrice implements Comparable<StockPrice> {
	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(StockPrice.class);

	private static final String PATH_FILE = Storage.NASDAQ.getPath("stock-price.csv");
	
	public static final String DEFAULT_DATE = "1970-01-01";
	
	public static String getPath() {
		return PATH_FILE;
	}
	
	public static void save(Collection<StockPrice> collection) {
		save(new ArrayList<>(collection));
	}
	public static void save(List<StockPrice> list) {
		// Sort before save
		Collections.sort(list);
		CSVUtil.write(StockPrice.class).file(getPath(), list);
	}
	
	public static List<StockPrice> load() {
		return CSVUtil.read(StockPrice.class).file(getPath());
	}
	public static List<StockPrice> getList() {
		List<StockPrice> ret = load();
		return ret == null ? new ArrayList<>() : ret;
	}
	
	public static Map<String, StockPrice> getMap() {
		//            symbol
		Map<String, StockPrice> ret = new TreeMap<>();
		
		for(var e: getList()) {
			String symbol = e.symbol;
			if (ret.containsKey(symbol)) {
				logger.error("Duplicate symbol");
				logger.error("  date {}", symbol);
				logger.error("  old {}", ret.get(symbol));
				logger.error("  new {}", e);
				throw new UnexpectedException("Duplicate symbol");
			} else {
				ret.put(symbol, e);
			}
		}
		return ret;
	}


	public String symbol;
	public String dateFirst; // first date
	public String dateLast;  // last date
	@CSVUtil.DecimalPlaces(4)
	public double closeLast;     // 0.00 for no data
	
	public StockPrice(String symbol, String dateFirst, String dateLast, double close) {
		this.symbol    = symbol;
		this.dateFirst = dateFirst;
		this.dateLast  = dateLast;
		this.close     = close;
	}
	public StockPrice() {
		this("", DEFAULT_DATE, DEFAULT_DATE, 0);
	}
	
	public boolean isEmpty() {
		return dateFirst.equals(DEFAULT_DATE) && dateLast.equals(DEFAULT_DATE);
	}
	
	@Override
	public String toString() {
		return StringUtil.toString(this);
	}
	@Override
	public int compareTo(StockPrice that) {
		return this.symbol.compareTo(that.symbol);
	}
}
