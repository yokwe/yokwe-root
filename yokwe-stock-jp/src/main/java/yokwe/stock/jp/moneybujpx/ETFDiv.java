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

public class ETFDiv implements Comparable<ETFDiv> {
	private static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();

	public static String getPath(String stockCode) {
		return Storage.MoneyBuJPX.getPath("etf-div", stockCode + ".csv");
	}

	public static void save(String stockCode, Collection<ETFDiv> collection) {
		String path = getPath(stockCode);
		ListUtil.save(ETFDiv.class, path, collection);
	}
	public static void save(String stockCode, List<ETFDiv> list) {
		String path = getPath(stockCode);
		ListUtil.save(ETFDiv.class, path, list);
	}

	public static List<ETFDiv> getList(String stockCode) {
		String path = getPath(stockCode);
		var list = ListUtil.getList(ETFDiv.class, path);
		
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
		ETFDiv[] divArray = divList.toArray(new ETFDiv[0]);
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
    public BigDecimal amount;
    
    public ETFDiv(String date, BigDecimal amount) {
    	this.date      = date;
    	this.amount    = amount;
    }
    public ETFDiv() {
    	this(null, null);
    }
    
    @Override
    public String toString() {
        return StringUtil.toString(this);
    }

	@Override
	public int compareTo(ETFDiv that) {
		return this.date.compareTo(that.date);
	}
	
	@Override
	public boolean equals(Object o) {
		if (o == null) {
			return false;
		} else {
			if (o instanceof ETFDiv) {
				ETFDiv that = (ETFDiv)o;
				return this.compareTo(that) == 0;
			} else {
				return false;
			}
		}
	}
	
	@Override
	public int hashCode() {
		return this.date.hashCode();
	}
}
