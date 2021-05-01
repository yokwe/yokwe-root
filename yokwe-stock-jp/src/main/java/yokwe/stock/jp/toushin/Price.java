package yokwe.stock.jp.toushin;

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

	public String date;          // 年月日
	public String basePrice;     // 基準価額(円) = 純資産総額 / (総口数 * 10,000)
	public String netAssetValue; // 純資産総額（百万円）
	public String dividend;      // 分配金
	public String period;        // 決算期
	
	public Price(String date, String price, String netAsset, String dividend, String period) {
		this.date          = date;
		this.basePrice     = price;
		this.netAssetValue = netAsset;
		this.dividend      = dividend;
		this.period        = period;
	}
	public Price() {
		this(null, null, null, null, null);
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