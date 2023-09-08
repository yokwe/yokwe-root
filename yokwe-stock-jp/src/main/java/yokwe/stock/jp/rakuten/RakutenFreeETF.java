package yokwe.stock.jp.rakuten;

import java.util.Collection;
import java.util.List;

import yokwe.stock.jp.Storage;
import yokwe.stock.jp.jpx.Stock;
import yokwe.util.ListUtil;

public class RakutenFreeETF {
	private static final String PATH_FILE = Storage.Rakuten.getPath("rakuten-free-etf.csv");
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
}
