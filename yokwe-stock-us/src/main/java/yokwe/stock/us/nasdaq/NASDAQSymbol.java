package yokwe.stock.us.nasdaq;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import yokwe.stock.us.Storage;
import yokwe.stock.us.SymbolInfo;
import yokwe.util.ListUtil;

public class NASDAQSymbol {
	private static final String PATH_FILE = Storage.NASDAQ.getPath("nasdaq-symbol.csv");
	public static String getPath() {
		return PATH_FILE;
	}

	public static void save(Collection<SymbolInfo> collection) {
		ListUtil.save(SymbolInfo.class, getPath(), collection);
	}
	public static void save(List<SymbolInfo> list) {
		ListUtil.save(SymbolInfo.class, getPath(), list);
	}
	
	
	public static List<SymbolInfo> load() {
		return ListUtil.load(SymbolInfo.class, getPath());
	}
	public static List<SymbolInfo> getList() {
		return ListUtil.getList(SymbolInfo.class, getPath());
	}
	
	public static Map<String, SymbolInfo> getMap() {
		var list = ListUtil.getList(SymbolInfo.class, getPath());
		return ListUtil.checkDuplicate(list, o -> o.symbol);
	}
	
	private NASDAQSymbol() {}
}
