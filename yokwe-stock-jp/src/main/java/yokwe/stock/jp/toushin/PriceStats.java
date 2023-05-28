package yokwe.stock.jp.toushin;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import yokwe.stock.jp.Storage;
import yokwe.util.ListUtil;
import yokwe.util.StringUtil;

public class PriceStats implements Comparable<PriceStats> {
	public static final String PREFIX = "price-stats";
	
	public static String getPath() {
		return Storage.Toushin.getPath(PREFIX);
	}
	public static String getPath(String isinCode) {
		return Storage.Toushin.getPath(PREFIX, isinCode + ".csv");
	}
	public static void save(String isinCode, List<PriceStats> list) {
		ListUtil.save(PriceStats.class, getPath(isinCode), list);
	}
	public static List<PriceStats> load(String isinCode) {
		return ListUtil.load(PriceStats.class, getPath(isinCode));
	}
	public static List<PriceStats> getList(String isinCode) {
		return ListUtil.getList(PriceStats.class, getPath(isinCode));
	}
	
	public LocalDate  date;   // 年月日
	public BigDecimal nav;    // 純資産総額（円）
	public BigDecimal price;  // 基準価額(円)
	public BigDecimal units;  // 総口数 = 純資産総額 / 基準価額
	
	public BigDecimal totalDiv;         // 分配金合計
	public BigDecimal reinvestedPrice;  // 分配金再投資基準価額
	public BigDecimal logReturn;        // 対数収益率
	public BigDecimal simpleReturn;     // 収益率

	public PriceStats(
		LocalDate  date,
		BigDecimal nav,
		BigDecimal price,
		BigDecimal units,
		
		BigDecimal totalDiv,
		BigDecimal reinvestedPrice,
		BigDecimal logReturn,
		BigDecimal simpleReturn
		) {		
		this.date  = date;
		this.nav   = nav;
		this.price = price;
		this.units = units;
		
		this.totalDiv        = totalDiv;
		this.reinvestedPrice = reinvestedPrice;
		this.logReturn       = logReturn;
		this.simpleReturn    = simpleReturn;
	}

	@Override
	public String toString() {
		return StringUtil.toString(this);
	}
	@Override
	public int compareTo(PriceStats that) {
		return this.date.compareTo(that.date);
	}
	@Override
	public boolean equals(Object o) {
		if (o == null) {
			return false;
		} else {
			if (o instanceof PriceStats) {
				PriceStats that = (PriceStats)o;
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
