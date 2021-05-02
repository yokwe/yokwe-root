package yokwe.stock.jp.toushin;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import yokwe.util.CSVUtil;
import yokwe.util.StringUtil;

public class Price implements Comparable<Price> {
	public static final String PREFIX = "price";
	
	public static final String getPath(String isinCode) {
		String path = String.format("%s/%s.csv", PREFIX, isinCode);
		return Toushin.getPath(path);
	}
	public static void save(String isinCode, List<Price> list) {
		Collections.sort(list);
		CSVUtil.write(Price.class).file(getPath(isinCode), list);
	}
	public static List<Price> load(String isinCode) {
		return CSVUtil.read(Price.class).file(getPath(isinCode));
	}

	// 年月日,基準価額(円),純資産総額（百万円）,分配金,決算期
	// 2014年11月19日,12417,5688,,
	// 2014年11月20日,12526,5756,0.00,1

	public LocalDate  date;          // 年月日
	public BigDecimal basePrice;     // 基準価額(円) = 純資産総額 / (総口数 * 10,000)
	public BigDecimal netAssetValue; // 純資産総額（百万円）
	public BigDecimal totalUnits;    // 総口数
	
	public Price(LocalDate date, BigDecimal basePprice, BigDecimal netAssetValue, BigDecimal totalUnits) {
		this.date          = date;
		this.basePrice     = basePprice;
		this.netAssetValue = netAssetValue;
		this.totalUnits    = totalUnits;
	}
	public Price() {
		this(null, null, null, null);
	}
	
	@Override
	public int compareTo(Price that) {
		return this.date.compareTo(that.date);
	}
	
	@Override
	public String toString() {
		return StringUtil.toString(this);
	}
}