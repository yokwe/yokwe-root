package yokwe.stock.jp.jpx;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import yokwe.util.CSVUtil;
import yokwe.util.UnexpectedException;

public class StockInfo implements Comparable<StockInfo> {
	private static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();

	private static final String PATH_FILE = getPath();
	public static String getPath() {
		return JPX.getPath("stock-info.csv");
	}
	
	public static List<StockInfo> getList() {
		List<StockInfo> list = CSVUtil.read(StockInfo.class).file(PATH_FILE);
		if (list == null) {
			list = new ArrayList<>();
		}
		return list;
	}
	private static Map<String, StockInfo> getMap() {
		Map<String, StockInfo> map = new TreeMap<>();
		for(StockInfo stock: getList()) {
			String stockCode = stock.stockCode;
			if (map.containsKey(stockCode)) {
				logger.error("duplicate stockCode {}!", stockCode);
				logger.error("old {}", map.get(stockCode));
				logger.error("new {}", stock);
				throw new UnexpectedException("duplicate stockCode");
			} else {
				map.put(stock.stockCode, stock);
			}
		}
		return map;
	}
	private static Map<String, StockInfo> map = null;
	public static StockInfo get(String stockCode) {
		if (map == null) {
			map = getMap();
		}
		return map.containsKey(stockCode) ? map.get(stockCode) : null;
	}
	public static void save(Collection<StockInfo> collection) {
		save(new ArrayList<>(collection));
	}
	public static void save(List<StockInfo> list) {
		// Sort before save
		Collections.sort(list);
		CSVUtil.write(StockInfo.class).file(PATH_FILE, list);
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
