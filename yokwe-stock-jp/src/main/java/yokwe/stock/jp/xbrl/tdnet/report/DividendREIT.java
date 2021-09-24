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

public class DividendREIT implements Comparable<DividendREIT> {
	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(DividendREIT.class);

	public static String getPath() {
		return TDNET.getPath("dividend-reit.csv");
	}

	public static void save(Collection<DividendREIT> collection) {
		save(new ArrayList<>(collection));
	}
	public static void save(List<DividendREIT> list) {
		if (list.isEmpty()) return;
		String path = getPath();
		
		// Sort before save
		Collections.sort(list);
		CSVUtil.write(DividendREIT.class).file(path, list);
	}
	
	public static List<DividendREIT> getList() {
		String path = getPath();
		List<DividendREIT> ret = CSVUtil.read(DividendREIT.class).file(path);
		return ret == null ? new ArrayList<>() : ret;
	}
	public static Map<String, Map<String, DividendREIT>> getMap() {
		//            sotckCode   date
		Map<String, Map<String, DividendREIT>> divMap = new TreeMap<>();
		//  stockCode   key

		for(DividendREIT dividend: getList()) {
			String stockCode = dividend.stockCode;
			String key = String.format("%s", dividend.payDate);
			if (divMap.containsKey(dividend.stockCode)) {
				Map<String, DividendREIT> map = divMap.get(stockCode);
				if (map.containsKey(key)) {
					logger.error("duplicate key {}!", key);
					logger.error("old {}", divMap.get(key));
					logger.error("new {}", dividend);
					throw new UnexpectedException("duplicate date");
				} else {
					map.put(key, dividend);
				}
			} else {
				Map<String, DividendREIT> map = new TreeMap<>();
				map.put(key, dividend);
				divMap.put(stockCode, map);
			}
		}
		
		Map<String, Map<String, DividendREIT>> ret = new TreeMap<>();
		for(var e: divMap.entrySet()) {
			String                     key   = e.getKey();
			Map<String, DividendREIT> value = new TreeMap<>();
			
			for(var ee: e.getValue().values()) {
				value.put(ee.payDate, ee);
			}
			ret.put(key, value);
		}
		
		return ret;
	}
	
	private static Map<String, Map<String, DividendREIT>> cacheMap = null;
	//                 stockCode   date
	public static DividendREIT getDividend(String stockCode, String date) {
		if (cacheMap == null) {
			cacheMap = getMap();
		}
		Map<String, DividendREIT> map = cacheMap.get(stockCode);
		if (map.containsKey(date)) {
			return map.get(date);
		} else {
			return null;
		}
	}
	
	
	public String stockCode; // Can be four or five digits
	// year end date in format YYYY-MM-DD
	public String yearEnd;
	// pay date of dividend in format YYYY-MM-DD
	public String payDate;
	// value of dividend
	@DecimalPlaces(2)
	public double dividend;
	// forecast next dividend
	@DecimalPlaces(2)
	public double nextDividendForecast;
	// forecast next next dividend
	@DecimalPlaces(2)
	public double nextNextDividendForecast;
	// file name of data source
	public String filename;

	public DividendREIT(String stockCode, String yearEnd, String date, double dividend, double nextDividendForecast, double nextNextDividendForecast, String filename) {
		this.stockCode                = stockCode;
		this.yearEnd                  = yearEnd;
		this.payDate                  = date;
		this.dividend                 = dividend;
		this.nextDividendForecast     = nextDividendForecast;
		this.nextNextDividendForecast = nextNextDividendForecast;
		this.filename                 = filename;
	}
	public DividendREIT() {
		this(null, null, null, 0, 0, 0, null);
	}

	@Override
	public String toString() {
		return String.format("{%s %s %s %.2f %.2f %.2f %s}", stockCode, yearEnd, payDate, dividend, nextDividendForecast, nextNextDividendForecast, filename);
	}

	@Override
	public boolean equals(Object o) {
		if (o == null) {
			return false;
		} else {
			if (o instanceof DividendREIT) {
				DividendREIT that = (DividendREIT)o;
				// Don't consider field file.
				return
					this.stockCode.equals(that.stockCode) &&
					this.yearEnd.equals(that.yearEnd) &&
					this.payDate.equals(that.payDate) &&
					DoubleUtil.isAlmostEqual(this.dividend, that.dividend);
			} else {
				return false;
			}
		}
	}
	@Override
	public int compareTo(DividendREIT that) {
		int ret = this.stockCode.compareTo(that.stockCode);
		if (ret == 0) ret = this.yearEnd.compareTo(that.yearEnd);
		if (ret == 0) ret = this.filename.compareTo(that.filename);
		return ret;
	}
}
