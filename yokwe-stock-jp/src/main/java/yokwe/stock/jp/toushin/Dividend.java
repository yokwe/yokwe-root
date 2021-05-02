package yokwe.stock.jp.toushin;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import yokwe.util.CSVUtil;
import yokwe.util.StringUtil;

public class Dividend implements Comparable<Dividend>{
	public static final String PREFIX = "div";
	
	public static final String getPath(String isinCode) {
		return Toushin.getPath(String.format("%s/%s.csv", PREFIX, isinCode));
	}
	public static void save(String isinCode, List<Dividend> list) {
		Collections.sort(list);
		CSVUtil.write(Dividend.class).file(getPath(isinCode), list);
	}
	public static List<Dividend> load(String isinCode) {
		return CSVUtil.read(Dividend.class).file(getPath(isinCode));
	}

	public LocalDate  date;          // 年月日
	public BigDecimal dividend;      // 分配金
	
	public Dividend(LocalDate date, BigDecimal dividend) {
		this.date     = date;
		this.dividend = dividend;
	}
	
	@Override
	public int compareTo(Dividend that) {
		return this.date.compareTo(that.date);
	}
	
	@Override
	public String toString() {
		return StringUtil.toString(this);
	}
}
