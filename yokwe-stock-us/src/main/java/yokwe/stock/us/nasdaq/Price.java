package yokwe.stock.us.nasdaq;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import yokwe.stock.us.Storage;
import yokwe.util.CSVUtil;
import yokwe.util.DoubleUtil;
import yokwe.util.ToString;
import yokwe.util.UnexpectedException;

public class Price implements Comparable<Price> {
	private static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();

	private static final String PATH_DIR = Storage.NASDAQ.getPath("price");
	public static String getPath(String symbol) {
		return String.format("%s/%s.csv", PATH_DIR, symbol);
	}
	
	
	public static void save(Collection<Price> collection) {
		save(new ArrayList<>(collection));
	}
	public static void save(List<Price> list) {
		if (list.isEmpty()) return;
		String symbol = list.get(0).symbol;
		
		// Sort before save
		Collections.sort(list);
		CSVUtil.write(Price.class).file(getPath(symbol), list);
	}
	
	public static List<Price> getList(String symbol) {
		List<Price> ret = CSVUtil.read(Price.class).file(getPath(symbol));
		return ret == null ? new ArrayList<>() : ret;
	}
	public static Map<String, Price> getMap(String symbol) {
		//            date
		Map<String, Price> ret = new TreeMap<>();
		
		for(var e: getList(symbol)) {
			String date = e.date;
			if (ret.containsKey(date)) {
				logger.error("Duplicate date");
				logger.error("  date {}", date);
				logger.error("  old {}", ret.get(symbol));
				logger.error("  new {}", e);
				throw new UnexpectedException("Duplicate date");
			} else {
				ret.put(date, e);
			}
		}
		
		return ret;
	}

	public String symbol; // normalized symbol like TRNT-A and RDS.A not like TRTN^A and RDS/A
	public String date;   // YYYY-MM-DD
	
	@CSVUtil.DecimalPlaces(4)
	public double open;
	@CSVUtil.DecimalPlaces(4)
	public double high;
	@CSVUtil.DecimalPlaces(4)
	public double low;
	@CSVUtil.DecimalPlaces(4)
	public double close;
	public long   volume;
	
	public Price(String symbol, String date, double open, double high, double low, double close, long volume) {
		this.symbol = symbol;
		this.date   = date;
		this.open   = DoubleUtil.roundPrice(open);
		this.high   = DoubleUtil.roundPrice(high);
		this.low    = DoubleUtil.roundPrice(low);
		this.close  = DoubleUtil.roundPrice(close);
		this.volume = volume;
	}
	public Price() {
		this(null, null, 0, 0, 0, 0, 0);
	}
	
	@Override
	public String toString() {
		return ToString.withFieldName(this);
	}

	@Override
	public int compareTo(Price that) {
		int ret = this.symbol.compareTo(that.symbol);
		if (ret == 0) ret = this.date.compareTo(that.date);
		return ret;
	}
	
	@Override
	public boolean equals(Object o) {
		if (o instanceof Price) {
			Price that = (Price) o;
			return
				this.symbol.equals(that.symbol) &&
				this.date.equals(that.date) &&
				DoubleUtil.isAlmostEqual(this.open,  that.open) &&
				DoubleUtil.isAlmostEqual(this.high,  that.high) &&
				DoubleUtil.isAlmostEqual(this.low,   that.low) &&
				DoubleUtil.isAlmostEqual(this.close, that.close) &&
				this.volume == that.volume;
		} else {
			return false;
		}
	}
}
