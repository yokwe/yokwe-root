package yokwe.stock.jp.data;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.slf4j.LoggerFactory;

import yokwe.util.UnexpectedException;
import yokwe.util.CSVUtil;

public class StockInfo implements Comparable<StockInfo> {
	private static final org.slf4j.Logger logger = LoggerFactory.getLogger(StockInfo.class);

	public static final String PATH_FILE = "tmp/data/stock-info.csv";

	private static List<StockInfo> list = null;
	public static List<StockInfo> getList() {
		if (list == null) {
			list = CSVUtil.read(StockInfo.class).file(PATH_FILE);
			if (list == null) {
				list = new ArrayList<>();
			}
		}
		return list;
	}
	private static Map<String, StockInfo> map = null;
	public static Map<String, StockInfo> getMap() {
		if (map == null) {
			map = new TreeMap<>();
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
		}
		return map;
	}
	public static StockInfo get(String stockCode) {
		Map<String, StockInfo> map = getMap();
		if (map.containsKey(stockCode)) {
			return map.get(stockCode);
		} else {
			return null;
		}
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
