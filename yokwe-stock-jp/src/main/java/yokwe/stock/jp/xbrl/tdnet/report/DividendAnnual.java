package yokwe.stock.jp.xbrl.tdnet.report;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import yokwe.stock.jp.Storage;
import yokwe.util.CSVUtil.DecimalPlaces;
import yokwe.util.DoubleUtil;
import yokwe.util.ListUtil;
import yokwe.util.UnexpectedException;

public class DividendAnnual implements Comparable<DividendAnnual> {
	private static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();

	private static final String PATH_FILE = Storage.XBRL.TDNET.getPath("dividend-annual.csv");
	public static String getPath() {
		return PATH_FILE;
	}

	public static void save(Collection<DividendAnnual> collection) {
		ListUtil.save(DividendAnnual.class, getPath(), collection);
	}
	public static void save(List<DividendAnnual> list) {
		ListUtil.save(DividendAnnual.class, getPath(), list);
	}
	
	public static List<DividendAnnual> getList() {
		return ListUtil.getList(DividendAnnual.class, getPath());
	}
	public static Map<String, DividendAnnual> getMap() {
		//            stockCode 
		Map<String, DividendAnnual> ret = new TreeMap<>();
		//  stockCode

		for(DividendAnnual e: getList()) {
			String stockCode = e.stockCode;
			if (ret.containsKey(stockCode)) {
				logger.error("duplicate stockCode");
				logger.error("  stockCode {}!", stockCode);
				logger.error("  old {}", ret.get(stockCode));
				logger.error("  new {}", e);
				throw new UnexpectedException("duplicate stockCode");
			} else {
				ret.put(stockCode, e);
			}
		}
		
		return ret;
	}
	private static Map<String, DividendAnnual> cacheMap = null;
	//                 stockCode   date
	public static DividendAnnual getDividendAnnual(String stockCode) {
		if (cacheMap == null) {
			cacheMap = getMap();
		}
		if (cacheMap.containsKey(stockCode)) {
			return cacheMap.get(stockCode);
		} else {
			return null;
		}
	}


	
	public String stockCode;   // Can be four or five digits
	public String lastPayDate; // last dividend pay date
	public int    count;       // number of dividend per year
	@DecimalPlaces(2)
	public double dividend;    // annual dividend from latest data
	
	public DividendAnnual(String stockCode, String lastPayDate, int count, double dividend) {
		this.stockCode   = stockCode;
		this.lastPayDate = lastPayDate;
		this.count       = count;
		this.dividend    = dividend;
	}
	public DividendAnnual() {
		this(null, null, 0, 0);
	}

	@Override
	public String toString() {
		return String.format("{%s %s %d %.2f}", stockCode, lastPayDate, count, dividend);
	}

	@Override
	public boolean equals(Object o) {
		if (o == null) {
			return false;
		} else {
			if (o instanceof DividendAnnual) {
				DividendAnnual that = (DividendAnnual)o;
				// Don't consider field file.
				return
					this.stockCode.equals(that.stockCode) &&
					this.lastPayDate.equals(that.lastPayDate) &&
					this.count == that.count &&
					DoubleUtil.isAlmostEqual(this.dividend, that.dividend);
			} else {
				return false;
			}
		}
	}
	@Override
	public int compareTo(DividendAnnual that) {
		int ret = this.stockCode.compareTo(that.stockCode);
		return ret;
	}

}
