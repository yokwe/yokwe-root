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

public class StockDividend implements Comparable<StockDividend> {
	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(StockDividend.class);

	private static final String PATH_FILE = Storage.NASDAQ.getPath("stock-dividend.csv");
	
	public static final String DEFAULT_DATE = "1970-01-01";
	
	public static String getPath() {
		return PATH_FILE;
	}
	
	public static void save(Collection<StockDividend> collection) {
		save(new ArrayList<>(collection));
	}
	public static void save(List<StockDividend> list) {
		// Sort before save
		Collections.sort(list);
		CSVUtil.write(StockDividend.class).file(getPath(), list);
	}
	
	public static List<StockDividend> load() {
		return CSVUtil.read(StockDividend.class).file(getPath());
	}
	public static List<StockDividend> getList() {
		List<StockDividend> ret = load();
		return ret == null ? new ArrayList<>() : ret;
	}
	
	public static Map<String, StockDividend> getMap() {
		//            symbol
		Map<String, StockDividend> ret = new TreeMap<>();
		
		for(var e: getList()) {
			String symbol = e.symbol;
			if (ret.containsKey(symbol)) {
				logger.error("Duplicate symbol");
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
	public String lastExDate;  // last date
	@CSVUtil.DecimalPlaces(6)
	public double annual;     // annualDividend -- last 1 year from today
	public int    count;      // dividend per year -- last 1 year from today
	
	public StockDividend(String symbol, String lastExDate, double annual, int count) {
		this.symbol     = symbol;
		this.lastExDate = lastExDate;
		this.annual     = annual;
		this.count      = 0;
	}
	public StockDividend() {
		this("", DEFAULT_DATE, 0, 0);
	}
	public StockDividend(String symbol) {
		this(symbol, DEFAULT_DATE, 0, 0);
	}

	public boolean isEmpty() {
		return lastExDate.equals(DEFAULT_DATE);
	}
	
	@Override
	public String toString() {
		return StringUtil.toString(this);
	}
	@Override
	public int compareTo(StockDividend that) {
		return this.symbol.compareTo(that.symbol);
	}

}
