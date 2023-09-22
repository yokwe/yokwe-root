package yokwe.finance.provider.jita;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

import yokwe.finance.Storage;
import yokwe.util.ListUtil;

public class DivPrice implements Comparable<DivPrice> {
	private static final String PREFIX = "div-price";
	
	private static final Storage storage = Storage.provider_jita;
	
	public static String getPath() {
		return storage.getPath(PREFIX);
	}
	public static String getPath(String stockCode) {
		return storage.getPath(PREFIX, stockCode + ".csv");
	}
	
	private static final String PREFIX_DELIST = PREFIX + "-delist";
	public static String getPathDelist() {
		return storage.getPath(PREFIX_DELIST);
	}
	
	public static void save(String stockCode, Collection<DivPrice> collection) {
		String path = getPath(stockCode);
		ListUtil.save(DivPrice.class, path, collection);
	}
	public static void save(String stockCode, List<DivPrice> list) {
		String path = getPath(stockCode);
		ListUtil.save(DivPrice.class, path, list);
	}
	
	public static List<DivPrice> getList(String stockCode) {
		String path = getPath(stockCode);
		return ListUtil.getList(DivPrice.class, path);
	}
	
	public final LocalDate  date;    // 年月日
	public final BigDecimal price;   // 基準価額(円)
	public final BigDecimal nav;     // 純資産総額（百万円）
	public final String     div;     // 分配金
	public final String     period;  // 決算期
	
	public DivPrice(
		LocalDate  date,
		BigDecimal price,
		BigDecimal nav,
		String     div,
		String     period
		) {
		this.date   = date;
		this.price  = price;
		this.nav    = nav;
		this.div    = div;
		this.period = period;
	}
	
	@Override
	public int compareTo(DivPrice that) {
		return this.date.compareTo(that.date);
	}
	
	@Override
	public String toString() {
		return String.format("{%s  %s  %s  \"%s\"  \"%s\"}", date, price.toPlainString(), nav.toPlainString(), div, period);
	}
}
