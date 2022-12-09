package yokwe.stock.jp.toushin2;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import yokwe.stock.jp.Storage;
import yokwe.util.CSVUtil;
import yokwe.util.StringUtil;

public class Price implements Comparable<Price>{
	public static final String PREFIX = "price";
	
	public static final String getPath(String isinCode) {
		return Storage.Toushin2.getPath(PREFIX, isinCode + ".csv");
	}
	public static void save(String isinCode, List<Price> list) {
		Collections.sort(list);
		CSVUtil.write(Price.class).file(getPath(isinCode), list);
	}
	public static List<Price> load(String isinCode) {
		return CSVUtil.read(Price.class).file(getPath(isinCode));
	}

	// NOTE amount can have fraction value
	// 年月日	基準価額(円)	純資産総額（百万円）	分配金	決算期

	public String     date;   // 年月日
	public BigDecimal nav;    // 純資産総額（百万円）
	public BigDecimal price;  // 基準価額(円)
	
	public Price(LocalDate date, BigDecimal nav, BigDecimal price) {
		this.date  = date.toString();
		this.nav   = nav;
		this.price = price;
	}
	public Price() {
		date  = "";
		nav   = BigDecimal.ZERO;
		price = BigDecimal.ZERO;
	}
	
	@Override
	public String toString() {
		return StringUtil.toString(this);
	}

	@Override
	public int compareTo(Price that) {
		return this.date.compareTo(that.date);
	}
	@Override
	public boolean equals(Object o) {
		if (o == null) {
			return false;
		} else {
			if (o instanceof Price) {
				Price that = (Price)o;
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
