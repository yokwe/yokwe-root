package yokwe.stock.jp.sbi;

import java.util.Collection;
import java.util.List;

import yokwe.stock.jp.Storage;
import yokwe.stock.jp.jpx.Stock;
import yokwe.util.ListUtil;

public class SBIFreeETF {
	private static final String PATH_FILE = Storage.SBI.getPath("sbi-free-etf.csv");
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
