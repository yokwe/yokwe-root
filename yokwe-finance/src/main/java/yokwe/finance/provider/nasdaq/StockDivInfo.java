package yokwe.finance.provider.nasdaq;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

import yokwe.finance.Storage;
import yokwe.util.ListUtil;
import yokwe.util.StringUtil;

public class StockDivInfo implements Comparable<StockDivInfo> {
	private static final String PREFIX = "stock-div-info";
	
	private static final Storage storage = Storage.provider_nasdaq;
	
	public static final LocalDate DATE_NA = LocalDate.of(2099, 1, 1);
	
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
	
	public static void save(String stockCode, Collection<StockDivInfo> collection) {
		ListUtil.save(StockDivInfo.class, getPath(stockCode), collection);
	}
	public static void save(String stockCode, List<StockDivInfo> list) {
		ListUtil.save(StockDivInfo.class, getPath(stockCode), list);
	}
	
	public static List<StockDivInfo> getList(String stockCode) {
		return ListUtil.getList(StockDivInfo.class, getPath(stockCode));
	}
	
	public LocalDate  exOrEffDate;
	public String     type;
	public BigDecimal amount;
	public LocalDate  declarationDate;
	public LocalDate  recordDate;
	public LocalDate  paymentDate;
	public String     currency;
	
	@Override
	public String toString() {
		return StringUtil.toString(this);
	}
	@Override
	public int compareTo(StockDivInfo that) {
		return this.exOrEffDate.compareTo(that.exOrEffDate);
	}
	@Override
	public boolean equals(Object o) {
		if (o instanceof StockDivInfo) {
			StockDivInfo that = (StockDivInfo)o;
			return
				this.exOrEffDate.equals(that.exOrEffDate) &&
				this.type.equals(that.type) &&
				this.amount.equals(that.amount) &&
				this.declarationDate.equals(that.declarationDate) &&
				this.recordDate.equals(that.recordDate) &&
				this.paymentDate.equals(that.paymentDate) &&
				this.currency.equals(that.currency);
		} else {
			return false;
		}
	}
}
