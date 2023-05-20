package yokwe.stock.jp.jpx;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

import yokwe.stock.jp.Storage;
import yokwe.util.ListUtil;

public class Price implements Comparable<Price> {
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

	public static void save(String stockCode, Collection<Price> collection) {
		String path = getPath(stockCode);
		ListUtil.save(Price.class, path, collection);
	}
	public static void save(String stockCode, List<Price> list) {
		String path = getPath(stockCode);
		ListUtil.save(Price.class, path, list);
	}
	
	public static List<Price> getList(String stockCode) {
		String path = getPath(stockCode);
		return ListUtil.getList(Price.class, path);
	}
	public static Map<String, Price> getMap(String stockCode) {
		//            date
		var list = getList(stockCode);
		return ListUtil.checkDuplicate(list, o -> o.date);
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
