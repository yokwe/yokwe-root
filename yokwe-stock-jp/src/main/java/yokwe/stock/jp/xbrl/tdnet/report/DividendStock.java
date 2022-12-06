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

public class DividendStock implements Comparable<DividendStock> {
	private static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();

	public static String getPath() {
		return TDNET.getPath("dividend-stock.csv");
	}

	public static void save(Collection<DividendStock> collection) {
		save(new ArrayList<>(collection));
	}
	public static void save(List<DividendStock> list) {
		if (list.isEmpty()) return;
		String path = getPath();
		
		// Sort before save
		Collections.sort(list);
		CSVUtil.write(DividendStock.class).file(path, list);
	}
	
	public static List<DividendStock> getList() {
		String path = getPath();
		List<DividendStock> ret = CSVUtil.read(DividendStock.class).file(path);
		return ret == null ? new ArrayList<>() : ret;
	}
	public static Map<String, Map<String, DividendStock>> getMap() {
		//            sotckCode   date
		Map<String, Map<String, DividendStock>> divMap = new TreeMap<>();
		//  stockCode   key

		for(DividendStock dividend: getList()) {
			String stockCode = dividend.stockCode;
			String key = String.format("%s-%d", dividend.payDate, dividend.quarter);
			if (divMap.containsKey(dividend.stockCode)) {
				Map<String, DividendStock> map = divMap.get(stockCode);
				if (map.containsKey(key)) {
					logger.error("duplicate key {}!", key);
					logger.error("old {}", divMap.get(key));
					logger.error("new {}", dividend);
					throw new UnexpectedException("duplicate date");
				} else {
					map.put(key, dividend);
				}
			} else {
				Map<String, DividendStock> map = new TreeMap<>();
				map.put(key, dividend);
				divMap.put(stockCode, map);
			}
		}
		
		Map<String, Map<String, DividendStock>> ret = new TreeMap<>();
		for(var e: divMap.entrySet()) {
			String                     key   = e.getKey();
			Map<String, DividendStock> value = new TreeMap<>();
			
			for(var ee: e.getValue().values()) {
				value.put(ee.payDate, ee);
			}
			ret.put(key, value);
		}
		
		return ret;
	}
	
	private static Map<String, Map<String, DividendStock>> cacheMap = null;
	//                 stockCode   date
	public static DividendStock getDividend(String stockCode, String date) {
		if (cacheMap == null) {
			cacheMap = getMap();
		}
		Map<String, DividendStock> map = cacheMap.get(stockCode);
		if (map.containsKey(date)) {
			return map.get(date);
		} else {
			return null;
		}
	}
	
	
	public String stockCode; // Can be four or five digits
	// year end date in format YYYY-MM-DD
	public String yearEnd;
	// value can be 1-4
	public int    quarter;
	// pay date of dividend in format YYYY-MM-DD
	public String payDate;
	// value of dividend
	@DecimalPlaces(2)
	public double dividend;
	// actual or forecast annual dividend
	@DecimalPlaces(2)
	public double annualDividend;
	// file name of data source
	public String filename;

	public DividendStock(String stockCode, String yearEnd, int quarter, String date, double dividend, double annualDividend, String filename) {
		this.stockCode      = stockCode;
		this.yearEnd        = yearEnd;
		this.quarter        = quarter;
		this.payDate        = date;
		this.dividend       = dividend;
		this.annualDividend = annualDividend;
		this.filename       = filename;
	}
	public DividendStock() {
		this(null, null, 0, null, 0, 0, null);
	}

	@Override
	public String toString() {
		return String.format("{%s %s %d %s %.2f %.2f %s}", stockCode, yearEnd, quarter, payDate, dividend, annualDividend, filename);
	}

	@Override
	public boolean equals(Object o) {
		if (o == null) {
			return false;
		} else {
			if (o instanceof DividendStock) {
				DividendStock that = (DividendStock)o;
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
	public int compareTo(DividendStock that) {
		int ret = this.stockCode.compareTo(that.stockCode);
		if (ret == 0) ret = this.yearEnd.compareTo(that.yearEnd);
		if (ret == 0) ret = this.quarter - that.quarter;
		if (ret == 0) ret = this.filename.compareTo(that.filename);
		return ret;
	}
}
