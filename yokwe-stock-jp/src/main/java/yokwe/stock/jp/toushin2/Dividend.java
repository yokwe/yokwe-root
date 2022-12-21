package yokwe.stock.jp.toushin2;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import yokwe.stock.jp.Storage;
import yokwe.util.ListUtil;
import yokwe.util.StringUtil;

public class Dividend implements Comparable<Dividend>{
	public static final String PREFIX = "div";
	
	public static final String getPath(String isinCode) {
		return Storage.Toushin2.getPath(PREFIX, isinCode + ".csv");
	}
	public static void save(String isinCode, List<Dividend> list) {
		ListUtil.save(Dividend.class, getPath(isinCode), list);
	}
	public static List<Dividend> load(String isinCode) {
		return ListUtil.load(Dividend.class, getPath(isinCode));
	}
	public static List<Dividend> getList(String isinCode) {
		return ListUtil.getList(Dividend.class, getPath(isinCode));
	}

	// NOTE amount can have fraction value
	public LocalDate  date;    // 年月日
	public BigDecimal amount;  // 分配金
	
	public Dividend(LocalDate date, BigDecimal amount) {
		this.date   = date;
		this.amount = amount;
	}
	private static final LocalDate DEFAULT_DATE = LocalDate.of(1980, 1, 1);
	public Dividend() {
		date   = DEFAULT_DATE;
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
