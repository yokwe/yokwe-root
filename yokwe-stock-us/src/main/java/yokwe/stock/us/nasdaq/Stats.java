package yokwe.stock.us.nasdaq;

import java.util.Collections;
import java.util.List;

import yokwe.stock.us.Storage;
import yokwe.util.CSVUtil;
import yokwe.util.StringUtil;

public final class Stats implements Comparable<Stats> {
	private static final String PATH_FILE = Storage.NASDAQ.getPath("stats.csv");

	public static String getPath() {
		return PATH_FILE;
	}
	public static void save(List<Stats> list) {
		// Sort before save
		Collections.sort(list);
		CSVUtil.write(Stats.class).file(getPath(), list);
	}

	public String stockCode;
	public String type;
	public String name;
	public String date;
	
	// current price and volume
	public int    pricec;
	public double price;
	
	// last price
	public double last;
	public double lastPCT;
	
	// stats - sd hv rsi
	//  30 < pricec
	public double sd;
	public double hv;
	// 15 <= pricec
	public double rsi;
	
	// min max
	public double min;
	public double max;
	public double minPCT;
	public double maxPCT;
	
	// volume
	public long   vol;
	// 5 <= pricec
	public long   vol5;
	// 20 <= pricec
	public long   vol21;
	

	public Stats() {
		stockCode = null;
		type      = null;
		name      = null;
		date      = null;
		pricec    = -1;
		price     = -1;
		
		last      = -1;
		lastPCT   = -1;
		
		sd        = -1;
		hv        = -1;
		
		rsi       = -1;
		
		min       = -1;
		max       = -1;
		minPCT    = -1;
		maxPCT    = -1;
		
		vol       = -1;
		vol5      = -1;
		vol21     = -1;
		
	}

	@Override
	public int compareTo(Stats that) {
		return this.stockCode.compareTo(that.stockCode);
	}
	
	@Override
	public String toString() {
		return StringUtil.toString(this);
	}
}
