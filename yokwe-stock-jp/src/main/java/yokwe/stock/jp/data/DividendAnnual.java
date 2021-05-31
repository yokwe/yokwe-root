package yokwe.stock.jp.data;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import yokwe.stock.jp.tdnet.SummaryFilename;
import yokwe.util.CSVUtil;
import yokwe.util.CSVUtil.DecimalPlaces;
import yokwe.util.DoubleUtil;
import yokwe.util.UnexpectedException;

public class DividendAnnual implements Comparable<DividendAnnual> {
	static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(DividendAnnual.class);

	public static final String PATH_FILE = "tmp/data/dividend-annual.csv"; // FIXME

	private static List<DividendAnnual> list = null;
	public static List<DividendAnnual> getList() {
		if (list == null) {
			list = CSVUtil.read(DividendAnnual.class).file(PATH_FILE);
		}
		return list;
	}
	private static Map<String, DividendAnnual> map = null;
	public static Map<String, DividendAnnual> getMap() {
		if (map == null) {
			map = new TreeMap<>();
			for(DividendAnnual e: getList()) {
				String key = e.stockCode;
				if (map.containsKey(key)) {
					logger.error("Duplicate key {}", key);
					logger.error("  old {}", map.get(key));
					logger.error("  new {}", e);
					throw new UnexpectedException("Duplicate key");
				} else {
					map.put(key, e);
				}
			}
		}
		return map;
	}
	public static DividendAnnual get(String stockCode) {
		Map<String, DividendAnnual> map = getMap();
		if (map.containsKey(stockCode)) {
			return map.get(stockCode);
		} else {
			return null;
		}
	}
	
	public static void save(Collection<DividendAnnual> collection) {
		List<DividendAnnual> list = new ArrayList<>(collection);
		save(list);
	}
	public static void save(List<DividendAnnual> list) {
		// Sort before save
		Collections.sort(list);
		CSVUtil.write(DividendAnnual.class).file(PATH_FILE, list);
	}
	
	
	public String stockCode;
	@DecimalPlaces(2)
	public double dividend;
	public int    count;
	
	public String yearEnd;   // YYYY-MM-DD
	public int    quarter;   // 1-4

	public SummaryFilename filename; // file name of data source

	public DividendAnnual(String stockCode, double dividend, int count, String yearEnd, int quarter, SummaryFilename filename) {
		this.stockCode = stockCode;
		this.dividend  = dividend;
		this.count     = count;
		this.yearEnd   = yearEnd;
		this.quarter   = quarter;
		this.filename  = filename;
	}
	
	public DividendAnnual() {
		this(null, 0, 0, null, 0, null);
	}
	
	@Override
	public String toString() {
		return String.format("{%5s %8.2f %2d %s %d %s}", stockCode, dividend, count, yearEnd, quarter, filename);
	}

	@Override
	public boolean equals(Object o) {
		if (o == null) return false;
		if (o instanceof DividendAnnual) {
			DividendAnnual that = (DividendAnnual)o;
			return
				this.stockCode.equals(that.stockCode) &&
				DoubleUtil.isAlmostEqual(this.dividend, that.dividend) &&
				this.yearEnd.equals(that.yearEnd) &&
				this.quarter == that.quarter;
		} else {
			return false;
		}
	}
	@Override
	public int compareTo(DividendAnnual that) {
		int ret = this.stockCode.compareTo(that.stockCode);
		if (ret == 0) ret = this.yearEnd.compareTo(that.yearEnd);
		if (ret == 0) ret = Integer.compare(this.quarter, that.quarter);
		return ret;
	}
}
