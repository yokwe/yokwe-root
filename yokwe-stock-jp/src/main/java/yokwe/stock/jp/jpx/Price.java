package yokwe.stock.jp.jpx;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import yokwe.stock.jp.Storage;
import yokwe.util.CSVUtil;
import yokwe.util.UnexpectedException;

public class Price implements Comparable<Price> {
	private static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();
	
	private static final String PREFIX = "price";
	public static String getPath() {
		return Storage.JPX.getPath(PREFIX);
	}
	public static String getPath(String stockCode) {
		return Storage.JPX.getPath(PREFIX, stockCode + ".csv");
	}
	
	private static final String PREFIX_DELIST = "price-delist";
	public static String getPathDelist() {
		return Storage.JPX.getPath(PREFIX_DELIST);
	}

	public static void save(Collection<Price> collection) {
		save(new ArrayList<>(collection));
	}
	public static void save(List<Price> list) {
		if (list.isEmpty()) return;
		Price price = list.get(0);
		String stockCode = price.stockCode;
		String path = getPath(stockCode);
		
		// Sort before save
		Collections.sort(list);
		CSVUtil.write(Price.class).file(path, list);
	}
	
	public static List<Price> getList(String stockCode) {
		String path = getPath(stockCode);
		List<Price> ret = CSVUtil.read(Price.class).file(path);
		return ret == null ? new ArrayList<>() : ret;
	}
	public static Map<String, Price> getMap(String stockCode) {
		//            date		
		Map<String, Price> ret = new TreeMap<>();

		for(Price price: getList(stockCode)) {
			String date = price.date;
			if (ret.containsKey(date)) {
				logger.error("duplicate date {}!", date);
				logger.error("old {}", ret.get(date));
				logger.error("new {}", date);
				throw new UnexpectedException("duplicate date");
			} else {
				ret.put(date, price);
			}
		}
		return ret;
	}
	
	private static Map<String, Map<String, Price>> cacheMap = new TreeMap<>();
	//                 stockCode   date
	public static Price getPrice(String stockCode, String date) {
		if (!cacheMap.containsKey(stockCode)) {
			cacheMap.put(stockCode, getMap(stockCode));
		}
		Map<String, Price> priceMap = cacheMap.get(stockCode);
		if (priceMap.containsKey(date)) {
			return priceMap.get(date);
		} else {
			return null;
		}
	}
	

	public String date;      // YYYY-MM-DD
	public String stockCode; // Can be four or five digits
	public double open;
	public double high;
	public double low;
	public double close;
	public long   volume;
	
	public Price(String date, String stockCode, double open, double high, double low, double close, long volume) {
		this.date      = date;
		this.stockCode = stockCode;
		this.open      = open;
		this.high      = high;
		this.low       = low;
		this.close     = close;
		this.volume    = volume;
	}
	public Price() {
		this(null, null, 0, 0, 0, 0, 0);
	}
	
	public String getDate() {
		return this.date;
	}
	
	public String getStockCode() {
		return this.stockCode;
	}

	@Override
	public String toString() {
		return String.format("%s %s %.1f %.1f %.1f %.1f %d", date, stockCode, open, high, low, close, volume);
	}
	
	@Override
	public boolean equals(Object o) {
		if (o == null) return false;
		if (o instanceof Price) {
			Price that = (Price)o;
			return this.compareTo(that) == 0;
		} else {
			return false;
		}
	}
	
	@Override
	public int compareTo(Price that) {
		int ret = this.date.compareTo(that.date);
		if (ret == 0) ret = this.stockCode.compareTo(that.stockCode);
		
		if (ret == 0) {
			int thisValue = (int)Math.round(this.open * 10.0);
			int thatValue = (int)Math.round(that.open * 10.0);
			ret = thisValue - thatValue;
		}
		if (ret == 0) {
			int thisValue = (int)Math.round(this.low * 10.0);
			int thatValue = (int)Math.round(that.low * 10.0);
			ret = thisValue - thatValue;
		}
		if (ret == 0) {
			int thisValue = (int)Math.round(this.high * 10.0);
			int thatValue = (int)Math.round(that.high * 10.0);
			ret = thisValue - thatValue;
		}
		if (ret == 0) {
			int thisValue = (int)Math.round(this.close * 10.0);
			int thatValue = (int)Math.round(that.close * 10.0);
			ret = thisValue - thatValue;
		}
		if (ret == 0) {
			long longRet = this.volume - that.volume;
			if (longRet == 0) ret = 0;
			else ret = (longRet < 0) ? -1 : 1;
		}
		return ret;
	}
}
