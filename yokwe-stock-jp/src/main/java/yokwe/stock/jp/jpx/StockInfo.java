package yokwe.stock.jp.jpx;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import yokwe.stock.jp.Storage;
import yokwe.util.ListUtil;

public class StockInfo implements Comparable<StockInfo> {
	private static final String PATH_FILE = Storage.JPX.getPath("stock-info.csv");
	public static String getPath() {
		return PATH_FILE;
	}
	
	public static List<StockInfo> getList() {
		return ListUtil.getList(StockInfo.class, getPath());
	}
	private static Map<String, StockInfo> getMap() {
		//             stockCode
		var list = getList();
		ListUtil.checkDuplicate(list);
		return list.stream().collect(Collectors.toMap(o -> o.stockCode, o -> o));
	}
	private static Map<String, StockInfo> map = null;
	public static StockInfo get(String stockCode) {
		if (map == null) {
			map = getMap();
		}
		return map.containsKey(stockCode) ? map.get(stockCode) : null;
	}
	public static void save(Collection<StockInfo> collection) {
		ListUtil.save(StockInfo.class, getPath(), collection);
	}
	public static void save(List<StockInfo> list) {
		ListUtil.save(StockInfo.class, getPath(), list);
	}

	public String stockCode; // コード 4 or 5 digits
	public String isin;      // ISINコード
	public int    tradeUnit; // 売買単位
	public long   issued;    // 発行済株式数
	
	public StockInfo(String stockCode, String isin, int tradeUnit, long issued) {
		this.stockCode = stockCode;
		this.isin      = isin;
		this.tradeUnit = tradeUnit;
		this.issued    = issued;
	}
	public StockInfo() {
		this(null, null, 0, 0);
	}
	
	@Override
	public String toString() {
		return String.format("{%s %s %d %d %s}", stockCode, isin, tradeUnit, issued);
	}
	
	@Override
	public int compareTo(StockInfo that) {
		int ret = this.stockCode.compareTo(that.stockCode);
		return ret;
	}
}
