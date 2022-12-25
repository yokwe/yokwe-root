package yokwe.stock.jp.sony;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

import yokwe.stock.jp.Storage;
import yokwe.util.ListUtil;

public class Dividend implements Comparable<Dividend> {
	private static final String PREFIX = "dividend";
	public static String getPath(String stockCode) {
		return Storage.Sony.getPath(PREFIX, stockCode + ".csv");
	}

	public static void save(String isinCode, Collection<Dividend> collection) {
		ListUtil.save(Dividend.class, getPath(isinCode), collection);
	}
	public static void save(String isinCode, List<Dividend> list) {
		ListUtil.save(Dividend.class, getPath(isinCode), list);
	}

	public static List<Dividend> getList(String isinCode) {
		return ListUtil.getList(Dividend.class, getPath(isinCode));
	}

	
	public LocalDate  date;
	public String     isinCode;
	public Currency   currency;
	public BigDecimal dividend;
	
	public Dividend(LocalDate date, String isinCode, Currency currency, BigDecimal dividend) {
		this.date     = date;
		this.isinCode = isinCode;
		this.currency = currency;
		this.dividend = dividend;
	}
	public Dividend() {
		this(null, null, null, null);
	}

	@Override
	public int compareTo(Dividend that) {
		int ret = this.date.compareTo(that.date);
		if (ret == 0) ret = this.isinCode.compareTo(that.isinCode);
		return ret;
	}
}
