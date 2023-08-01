package yokwe.stock.jp.yahoo;

import java.util.Collection;
import java.util.List;

import yokwe.stock.jp.Storage;
import yokwe.util.ListUtil;
import yokwe.util.yahoo.finance.Price;

public class YahooPrice {
	private static final String PREFIX = "price";
	public static String getPath() {
		return Storage.Yahoo.getPath(PREFIX);
	}
	public static String getPath(String stockCode) {
		return Storage.Yahoo.getPath(PREFIX, stockCode + ".csv");
	}
	
	private static final String PREFIX_DELIST = "price-delist";
	public static String getPathDelist() {
		return Storage.Yahoo.getPath(PREFIX_DELIST);
	}

	public static void save(String stockCode, Collection<Price> collection) {
		String path = getPath(stockCode);
		ListUtil.save(Price.class, path, collection);
	}
	public static void save(String stockCode, List<Price> list) {
		String path = getPath(stockCode);
		ListUtil.save(Price.class, path, list);
	}
	
	public static List<Price> getList(String stockCode) {
		String path = getPath(stockCode);
		return ListUtil.getList(Price.class, path);
	}

}
