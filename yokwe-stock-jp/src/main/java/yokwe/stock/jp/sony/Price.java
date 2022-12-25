package yokwe.stock.jp.sony;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

import yokwe.stock.jp.Storage;
import yokwe.util.ListUtil;

public class Price implements Comparable<Price> {
	private static final String PREFIX = "price";
	public static String getPath(String isinCode) {
		return Storage.Sony.getPath(PREFIX, isinCode + ".csv");
	}

	public static void save(String isinCode, Collection<Price> collection) {
		ListUtil.save(Price.class, getPath(isinCode), collection);
	}
	public static void save(String isinCode, List<Price> list) {
		ListUtil.save(Price.class, getPath(isinCode), list);
	}

	public static List<Price> getList(String isinCode) {
		return ListUtil.getList(Price.class, getPath(isinCode));
	}

	
	public LocalDate  date;     // 基準日
	public String     isinCode;
	public Currency   currency;
	public BigDecimal price;    // 基準価額
	public long       netAsset; // 純資産総額 百万円
	
	public Price(LocalDate date, String isinCode, Currency currency, BigDecimal price, long netAsset) {
		this.date     = date;
		this.isinCode = isinCode;
		this.currency = currency;
		this.price    = price;
		this.netAsset = netAsset;
	}
	public Price() {
		this(null, null, null, null, 0);
	}

	@Override
	public int compareTo(Price that) {
		int ret = this.date.compareTo(that.date);
		if (ret == 0) ret = this.isinCode.compareTo(that.isinCode);
		return ret;
	}
}
