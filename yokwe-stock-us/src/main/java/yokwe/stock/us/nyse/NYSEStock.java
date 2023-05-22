package yokwe.stock.us.nyse;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import yokwe.stock.us.Stock;
import yokwe.stock.us.Storage;
import yokwe.util.ListUtil;

public class NYSEStock {
	private static final String PATH_FILE = Storage.NYSE.getPath("nyse-stock.csv");
	public static String getPath() {
		return PATH_FILE;
	}
	public static void save(Collection<Stock> collection) {
		ListUtil.save(Stock.class, getPath(), collection);
	}
	public static void save(List<Stock> list) {
		ListUtil.save(Stock.class, getPath(), list);
	}
	public static List<Stock> load() {
		return ListUtil.load(Stock.class, getPath());
	}
	public static List<Stock> getList() {
		return ListUtil.getList(Stock.class, getPath());
	}
	public static Map<String, Stock> getMap() {
		return ListUtil.checkDuplicate(getList(), Stock::getKey);
	}
	
}
