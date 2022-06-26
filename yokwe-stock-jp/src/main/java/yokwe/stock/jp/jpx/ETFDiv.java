package yokwe.stock.jp.jpx;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import yokwe.util.CSVUtil;
import yokwe.util.StringUtil;
import yokwe.util.UnexpectedException;

public class ETFDiv implements Comparable<ETFDiv> {
	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(ETFDiv.class);

	public static String getPath(String stockCode) {
		return JPX.getPath(String.format("etf-div/%s.csv", stockCode));
	}

	public static void save(Collection<ETFDiv> collection) {
		save(new ArrayList<>(collection));
	}
	public static void save(List<ETFDiv> list) {
		if (list.isEmpty()) return;
		ETFDiv etfDiv = list.get(0);
		String stockCode = etfDiv.stockCode;
		String path = getPath(stockCode);
		
		// Sort before save
		Collections.sort(list);
		CSVUtil.write(ETFDiv.class).file(path, list);
	}

	public static List<ETFDiv> getList(String stockCode) {
		String path = getPath(stockCode);
		List<ETFDiv> ret = CSVUtil.read(ETFDiv.class).file(path);
		return ret == null ? new ArrayList<>() : ret;
	}
	public static Map<String, ETFDiv> getMap(String stockCode) {
		//            date		
		Map<String, ETFDiv> ret = new TreeMap<>();

		for(ETFDiv etfDiv: getList(stockCode)) {
			String date = etfDiv.payDate;
			if (ret.containsKey(date)) {
				logger.error("duplicate date");
				logger.error("  date {}", date);
				logger.error("  old  {}", ret.get(date));
				logger.error("  new  {}", etfDiv);
				throw new UnexpectedException("duplicate date");
			} else {
				ret.put(date, etfDiv);
			}
		}
		return ret;
	}
	private static Map<String, Map<String, ETFDiv>> cacheMap = new TreeMap<>();
	//                 stockCode   date
	public static ETFDiv getDiv(String stockCode, String date) {
		if (!cacheMap.containsKey(stockCode)) {
			cacheMap.put(stockCode, getMap(stockCode));
		}
		Map<String, ETFDiv> divMap = cacheMap.get(stockCode);
		if (divMap.containsKey(date)) {
			return divMap.get(date);
		} else {
			return null;
		}
	}
	
	
	public String     stockCode;
    public BigDecimal amount;
    public String     payDate;
    
    public ETFDiv(String stockCode, BigDecimal amount, String payDate) {
    	this.stockCode = stockCode;
    	this.amount    = amount;
    	this.payDate   = payDate;
    }
    public ETFDiv() {
    	this(null, null, null);
    }
    
    @Override
    public String toString() {
        return StringUtil.toString(this);
    }

	@Override
	public int compareTo(ETFDiv that) {
		int ret = this.stockCode.compareTo(that.stockCode);
		if (ret == 0) ret = this.payDate.compareTo(that.payDate);
		return ret;
	}
	
	@Override
	public boolean equals(Object o) {
		if (o instanceof ETFDiv) {
			ETFDiv that = (ETFDiv)o;
			return this.stockCode.equals(that.stockCode) && this.amount.equals(that.amount) && this.payDate.equals(that.payDate);
		} else {
			return false;
		}
	}
}
