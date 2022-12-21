package yokwe.stock.jp.toushin;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;

import yokwe.stock.jp.Storage;
import yokwe.util.ListUtil;
import yokwe.util.StringUtil;

public class Price implements Comparable<Price>{
	public static final String PREFIX = "price";
	
	public static final String getPath(String isinCode) {
		return Storage.Toushin.getPath(PREFIX, isinCode + ".csv");
	}
	public static void save(String isinCode, List<Price> list) {
		ListUtil.save(Price.class, getPath(isinCode), list);
	}
	public static List<Price> load(String isinCode) {
		return ListUtil.load(Price.class, getPath(isinCode));
	}
	public static List<Price> getList(String isinCode) {
		return ListUtil.getList(Price.class, getPath(isinCode));
	}

	// NOTE amount can have fraction value
	// 年月日	基準価額(円)	純資産総額（百万円）	分配金	決算期

	public LocalDate  date;   // 年月日
	public BigDecimal nav;    // 純資産総額（百万円）
	public BigDecimal price;  // 基準価額(円)
	public BigDecimal units;  // 総口数 = 純資産総額 / 基準価額
	

	private static final BigDecimal MILLION = BigDecimal.valueOf(1_000_000);
	public Price(LocalDate date, BigDecimal nav, BigDecimal price) {		
		this.date  = date;
		this.nav   = nav.multiply(MILLION);
		this.price = price;
		this.units = this.nav.divide(this.price, 0, RoundingMode.HALF_UP);
	}
	private static final LocalDate DEFAULT_DATE = LocalDate.of(1980, 1, 1);
	public Price() {
		date  = DEFAULT_DATE;
		nav   = BigDecimal.ZERO;
		price = BigDecimal.ZERO;
		units = BigDecimal.ZERO;
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
