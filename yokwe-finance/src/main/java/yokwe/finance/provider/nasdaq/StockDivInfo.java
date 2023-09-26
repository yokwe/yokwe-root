package yokwe.finance.provider.nasdaq;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import yokwe.finance.Storage;
import yokwe.util.CSVUtil;
import yokwe.util.StringUtil;

public class StockDivInfo {
	private static final String PREFIX = "stock-div-info";
	
	private static final Storage storage = Storage.provider_nasdaq;
	
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
//		ListUtil.save(StockDivInfo.class, getPath(stockCode), collection);
		CSVUtil.write(StockDivInfo.class).file(getPath(stockCode), collection);
	}
	public static void save(String stockCode, List<StockDivInfo> list) {
//		ListUtil.save(StockDivInfo.class, getPath(stockCode), list);
		CSVUtil.write(StockDivInfo.class).file(getPath(stockCode), list);
	}
	
	public static List<StockDivInfo> getList(String stockCode) {
		var result = CSVUtil.read(StockDivInfo.class).file(getPath(stockCode));
		return result != null ? result : new ArrayList<>();
	}
	
	public String exOrEffDate;
	public String type;
	public String amount;
	public String declarationDate;
	public String recordDate;
	public String paymentDate;
	public String currency;
	
	@Override
	public String toString() {
		return StringUtil.toString(this);
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
