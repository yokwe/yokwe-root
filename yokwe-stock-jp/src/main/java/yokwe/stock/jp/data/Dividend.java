package yokwe.stock.jp.data;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import yokwe.util.CSVUtil;
import yokwe.util.CSVUtil.DecimalPlaces;
import yokwe.util.DoubleUtil;

public class Dividend implements Comparable<Dividend> {
	public static final String PATH_DIR_DATA = "tmp/data/dividend";
	public static String getPath(String stockCode) {
		return String.format("%s/%s.csv", PATH_DIR_DATA, stockCode);
	}

	public static List<Dividend> load(String stockCode) {
		String path = getPath(stockCode);
		return CSVUtil.read(Dividend.class).file(path);
	}
	
	public static void save(String stockCode, Collection<Dividend> collection) {
		save(stockCode, new ArrayList<>(collection));
	}
	public static void save(String stockCode, List<Dividend> list) {
		String path = getPath(stockCode);
		
		// Sort before save
		Collections.sort(list);
		CSVUtil.write(Dividend.class).file(path, list);
	}
	
	
	public String date;
	public String stockCode;
	@DecimalPlaces(2)
	public double dividend;
	
	public Dividend(String date, String stockCode, double dividend) {
		this.date      = date;
		this.stockCode = stockCode;
		this.dividend  = dividend;
	}
	
	public Dividend() {
		this("", "", 0);
	}
	
	@Override
	public String toString() {
		return String.format("{%s %5s %8.2f}", date, stockCode, dividend);
	}

	@Override
	public boolean equals(Object o) {
		if (o == null) return false;
		if (o instanceof Dividend) {
			Dividend that = (Dividend)o;
			return this.date.equals(that.date) && this.stockCode.equals(that.stockCode) && DoubleUtil.isAlmostEqual(this.dividend, that.dividend);
		} else {
			return false;
		}
	}
	@Override
	public int compareTo(Dividend that) {
		int ret = this.stockCode.compareTo(that.stockCode);
		if (ret == 0) ret = this.date.compareTo(that.date);
		return ret;
	}
}
