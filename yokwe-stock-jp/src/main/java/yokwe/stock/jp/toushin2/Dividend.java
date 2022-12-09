package yokwe.stock.jp.toushin2;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import yokwe.stock.jp.Storage;
import yokwe.util.CSVUtil;
import yokwe.util.StringUtil;

public class Dividend implements Comparable<Dividend>{
	public static final String PREFIX = "div";
	
	public static final String getPath(String isinCode) {
		return Storage.Toushin2.getPath(PREFIX, isinCode + ".csv");
	}
	public static void save(String isinCode, List<Dividend> list) {
		Collections.sort(list);
		CSVUtil.write(Dividend.class).file(getPath(isinCode), list);
	}
	public static List<Dividend> load(String isinCode) {
		return CSVUtil.read(Dividend.class).file(getPath(isinCode));
	}

	// NOTE amount can have fraction value
	public String     date;    // 年月日
	public BigDecimal amount;  // 分配金
	
	public Dividend(LocalDate date, BigDecimal amount) {
		this.date   = date.toString();
		this.amount = amount;
	}
	public Dividend() {
		date   = "";
		amount = BigDecimal.ZERO;
	}
	
	@Override
	public String toString() {
		return StringUtil.toString(this);
	}

	@Override
	public int compareTo(Dividend that) {
		return this.date.compareTo(that.date);
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
		return this.date.hashCode();
	}
}
