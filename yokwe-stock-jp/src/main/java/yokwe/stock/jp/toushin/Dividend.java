package yokwe.stock.jp.toushin;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import yokwe.stock.jp.Storage;
import yokwe.util.ListUtil;
import yokwe.util.ToString;

public class Dividend implements Comparable<Dividend>{
	public static final String PREFIX = "div";
	
	public static String getPath() {
		return Storage.Toushin.getPath(PREFIX);
	}
	public static String getPath(String isinCode) {
		return Storage.Toushin.getPath(PREFIX, isinCode + ".csv");
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
	public static Map<LocalDate, Dividend> getMap(String isinCode) {
		return ListUtil.checkDuplicate(getList(isinCode), o -> o.date);
	}
	
	public static final String PREFIX_DELIST = "div-delist";
	public static String getPathDelist() {
		return Storage.Toushin.getPath(PREFIX_DELIST);
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
		return ToString.withFieldName(this);
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
