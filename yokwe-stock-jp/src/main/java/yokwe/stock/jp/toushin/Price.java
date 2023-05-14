package yokwe.stock.jp.toushin;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;

import yokwe.stock.jp.Storage;
import yokwe.util.ListUtil;
import yokwe.util.StringUtil;

public final class Price implements Comparable<Price>{
	public static final String PREFIX = "price";
	
	public static String getPath() {
		return Storage.Toushin.getPath(PREFIX);
	}
	public static String getPath(String isinCode) {
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
	
	public static final String PREFIX_DELIST = "price-delist";
	public static String getPathDelist() {
		return Storage.Toushin.getPath(PREFIX_DELIST);
	}

	public LocalDate  date;   // 年月日
	public BigDecimal nav;    // 純資産総額（円）
	public BigDecimal price;  // 基準価額(円)
	public BigDecimal units;  // 総口数 = 純資産総額 / 基準価額
	public BigDecimal reinvestedPrice;  // 分配金再投資基準価額

	public Price(LocalDate date, BigDecimal nav, BigDecimal price, BigDecimal reinvestedPrice) {		
		this.date  = date;
		this.nav   = nav;
		this.price = price;
		this.units = this.price.compareTo(BigDecimal.ZERO) != 0 ? nav.divide(this.price, 0, RoundingMode.HALF_UP) : BigDecimal.ZERO;
		this.reinvestedPrice = reinvestedPrice;
	}
	public Price(LocalDate date, BigDecimal nav, BigDecimal price) {
		this(date, nav, price, BigDecimal.ZERO);
	}
	public Price() {
		this(LocalDate.EPOCH, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO);
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
