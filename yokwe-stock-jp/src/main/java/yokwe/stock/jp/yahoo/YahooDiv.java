package yokwe.stock.jp.yahoo;

import java.util.Collection;
import java.util.List;

import yokwe.stock.jp.Storage;
import yokwe.util.ListUtil;
import yokwe.util.yahoo.finance.Dividend;

public class YahooDiv {
	private static final String PREFIX = "div";
	public static String getPath() {
		return Storage.Yahoo.getPath(PREFIX);
	}
	public static String getPath(String stockCode) {
		return Storage.Yahoo.getPath(PREFIX, stockCode + ".csv");
	}
	
	private static final String PREFIX_DELIST = "div-delist";
	public static String getPathDelist() {
		return Storage.Yahoo.getPath(PREFIX_DELIST);
	}

	public static void save(String stockCode, Collection<Dividend> collection) {
		String path = getPath(stockCode);
		ListUtil.save(Dividend.class, path, collection);
	}
	public static void save(String stockCode, List<Dividend> list) {
		String path = getPath(stockCode);
		ListUtil.save(Dividend.class, path, list);
	}
	
	public static List<Dividend> getList(String stockCode) {
		String path = getPath(stockCode);
		return ListUtil.getList(Dividend.class, path);
	}

}
