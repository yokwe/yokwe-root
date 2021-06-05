package yokwe.stock.jp.data;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import yokwe.util.CSVUtil;
import yokwe.util.CSVUtil.DecimalPlaces;
import yokwe.util.DoubleUtil;

public class DividendStock implements Comparable<DividendStock> {
	public static final String PATH_FILE = "tmp/data/dividend-stock.csv"; // FIXME

	public static List<DividendStock> load() {
		return CSVUtil.read(DividendStock.class).file(PATH_FILE);
	}
	public static void save(Collection<DividendStock> collection) {
		List<DividendStock> list = new ArrayList<>(collection);
		save(list);
	}
	public static void save(List<DividendStock> list) {
		// Sort before save
		Collections.sort(list);
		CSVUtil.write(DividendStock.class).file(PATH_FILE, list);
	}


	public String stockCode; // Can be four or five digits
	public String yearEnd;   // YYYY-MM-DD
	public int    quarter;
	public String date;      // YYYY-MM-DD
	@DecimalPlaces(2)
	public double dividend;
	
	public String filename; // file name of data source

	public DividendStock(String stockCode, String yearEnd, int quarter, String date, double dividend, String filename) {
		this.stockCode = stockCode;
		this.yearEnd   = yearEnd;
		this.quarter   = quarter;
		this.date      = date;
		this.dividend  = dividend;
		this.filename  = filename;
	}
	public DividendStock() {
		this(null, null, 0, null, 0, null);
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
					this.date.equals(that.date) &&
					DoubleUtil.isAlmostEqual(this.dividend, that.dividend);
			} else {
				return false;
			}
		}
	}
	@Override
	public String toString() {
		return String.format("{%s %s %d %s %.2f %s}", stockCode, yearEnd, quarter, date, dividend, filename);
	}
	@Override
	public int compareTo(DividendStock that) {
		int ret = this.stockCode.compareTo(that.stockCode);
		if (ret == 0) ret = this.yearEnd.compareTo(that.yearEnd);
		if (ret == 0) ret = this.quarter - that.quarter;
		return ret;
	}

}
