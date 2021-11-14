package yokwe.stock.jp.xbrl.tdnet.report;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import yokwe.stock.jp.xbrl.tdnet.TDNET;
import yokwe.util.CSVUtil;
import yokwe.util.CSVUtil.DecimalPlaces;
import yokwe.util.DoubleUtil;
import yokwe.util.UnexpectedException;

public class Dividend implements Comparable<Dividend> {
	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(Dividend.class);

	public static String getPath() {
		return TDNET.getPath("dividend.csv");
	}

	public static void save(Collection<Dividend> collection) {
		save(new ArrayList<>(collection));
	}
	public static void save(List<Dividend> list) {
		if (list.isEmpty()) return;
		String path = getPath();
		
		// Sort before save
		Collections.sort(list);
		CSVUtil.write(Dividend.class).file(path, list);
	}
	
	public static List<Dividend> getList() {
		String path = getPath();
		List<Dividend> ret = CSVUtil.read(Dividend.class).file(path);
		return ret == null ? new ArrayList<>() : ret;
	}
	public static Map<String, Map<String, Dividend>> getMap() {
		//            sotckCode   date
		Map<String, Map<String, Dividend>> divMap = new TreeMap<>();
		//  stockCode   key

		for(Dividend dividend: getList()) {
			String stockCode = dividend.stockCode;
			String key = String.format("%s-%d", dividend.yearEnd, dividend.quarter);
			if (divMap.containsKey(dividend.stockCode)) {
				Map<String, Dividend> map = divMap.get(stockCode);
				if (map.containsKey(key)) {
					logger.error("duplicate key {}!", key);
					logger.error("old {}", divMap.get(key));
					logger.error("new {}", dividend);
					throw new UnexpectedException("duplicate date");
				} else {
					map.put(key, dividend);
				}
			} else {
				Map<String, Dividend> map = new TreeMap<>();
				map.put(key, dividend);
				divMap.put(stockCode, map);
			}
		}
		
		Map<String, Map<String, Dividend>> ret = new TreeMap<>();
		for(var e: divMap.entrySet()) {
			String                     key   = e.getKey();
			Map<String, Dividend> value = new TreeMap<>();
			
			for(var ee: e.getValue().values()) {
				value.put(ee.payDate, ee);
			}
			ret.put(key, value);
		}
		
		return ret;
	}
	
	private static Map<String, Map<String, Dividend>> cacheMap = null;
	//                 stockCode   date
	public static Dividend getDividend(String stockCode, String date) {
		if (cacheMap == null) {
			cacheMap = getMap();
		}
		Map<String, Dividend> map = cacheMap.get(stockCode);
		if (map.containsKey(date)) {
			return map.get(date);
		} else {
			return null;
		}
	}
	
	
	public String stockCode; // Can be four or five digits
	// year end date in format YYYY-MM-DD
	public String yearEnd;
	// value can be 1-4 for stock, 0 for REIT
	public int    quarter;
	// pay date of dividend in format YYYY-MM-DD
	public String payDate;
	// value of dividend
	@DecimalPlaces(2)
	public double dividend;
	// file name of data source
	public String filename;

	public Dividend(String stockCode, String yearEnd, int quarter, String payDate, double dividend, String filename) {
		this.stockCode      = stockCode;
		this.yearEnd        = yearEnd;
		this.quarter        = quarter;
		this.payDate        = payDate;
		this.dividend       = dividend;
		this.filename       = filename;
	}
	public Dividend() {
		this(null, null, 0, null, 0, null);
	}

	@Override
	public String toString() {
		return String.format("{%s %s %d %s %.2f %s}", stockCode, yearEnd, quarter, payDate, dividend, filename);
	}

	@Override
	public boolean equals(Object o) {
		if (o == null) {
			return false;
		} else {
			if (o instanceof Dividend) {
				Dividend that = (Dividend)o;
				// Don't consider field file.
				return
					this.stockCode.equals(that.stockCode) &&
					this.yearEnd.equals(that.yearEnd) &&
					this.quarter == that.quarter &&
					this.payDate.equals(that.payDate) &&
					DoubleUtil.isAlmostEqual(this.dividend, that.dividend);
			} else {
				return false;
			}
		}
	}
	@Override
	public int compareTo(Dividend that) {
		int ret = this.stockCode.compareTo(that.stockCode);
		if (ret == 0) ret = this.yearEnd.compareTo(that.yearEnd);
		if (ret == 0) ret = this.quarter - that.quarter;
		if (ret == 0) ret = this.filename.compareTo(that.filename);
		return ret;
	}
}
