package yokwe.stock.jp.moneybujpx;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import yokwe.stock.jp.Storage;
import yokwe.util.ListUtil;
import yokwe.util.StringUtil;
import yokwe.util.UnexpectedException;

public class Dividend implements Comparable<Dividend> {
	private static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();

	public static String getPath(String stockCode) {
		return Storage.MoneyBuJPX.getPath("div", stockCode + ".csv");
	}

	public static void save(String stockCode, Collection<Dividend> collection) {
		String path = getPath(stockCode);
		ListUtil.save(Dividend.class, path, collection);
	}
	public static void save(String stockCode, List<Dividend> list) {
		String path = getPath(stockCode);
		ListUtil.save(Dividend.class, path, list);
	}

	public static List<Dividend> getList(String stockCode) {
		String path = getPath(stockCode);
		var list = ListUtil.getList(Dividend.class, path);
		
		// sanity check
		for(var div: list) {
			double value = div.amount.doubleValue();
			if (Double.isFinite(value)) continue;
			
			logger.error("Contains not number");
			logger.error("  stockCode {}", stockCode);
			logger.error("  list      {}", list);
			throw new UnexpectedException("Contains not number");
		}

		return list;
	}
	
	public static double getAnnual(ETF etf) {
		String stockCode = etf.stockCode;
		int    divFreq   = etf.divFreq;
		
		if (divFreq == 0) return 0;
		
		var divList = getList(stockCode);
		Dividend[] divArray = divList.toArray(new Dividend[0]);
		int divCount = divArray.length;
		
		if (divCount == 0) return 0;
		
		double annual = 0;

		if (divFreq <= divCount) {
			// use latest amount in divList for one year
			int offset = divCount - divFreq;
			for(int i = 0; i < divFreq; i++) {
				annual += divArray[offset + i].amount.doubleValue();
			}
		} else {
			// estimate amount
			for(int i = 0; i < divCount; i++) {
				annual += divArray[i].amount.doubleValue();;
			}
			// calculate average dividend
			annual /= divCount;
			// estimate annual from average dividend
			annual *= divFreq;
		}

		return annual;
	}
	
	public static Map<String, Double> getAnnualMap() {
	//                symbol  annualDividend
		var map = new TreeMap<String, Double>();
		
		var list = ETF.getList();
		for(var e: list) {
			double annual = getAnnual(e);
			map.put(e.stockCode, annual);
		}
		
		return map;
	}
	
	
    public String     date;
	public String     stockCode;
    public BigDecimal amount;
    
    public Dividend(String date, String stockCode, BigDecimal amount) {
    	this.date      = date;
    	this.stockCode = stockCode;
    	this.amount    = amount;
    }
    public Dividend() {
    	this(null, null, null);
    }
    
    @Override
    public String toString() {
        return StringUtil.toString(this);
    }

	@Override
	public int compareTo(Dividend that) {
		int ret = this.stockCode.compareTo(that.stockCode);
		if (ret == 0) ret = this.date.compareTo(that.date);
		return ret;
	}
	
	@Override
	public boolean equals(Object o) {
		if (o == null) {
			return false;
		} else {
			if (o instanceof Dividend) {
				Dividend that = (Dividend)o;
				return this.compareTo(that) == 0;
			} else {
				return false;
			}
		}
	}
	
	@Override
	public int hashCode() {
		return this.stockCode.hashCode() ^ this.date.hashCode();
	}
}
