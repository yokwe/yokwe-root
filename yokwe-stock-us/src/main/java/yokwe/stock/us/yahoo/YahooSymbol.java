package yokwe.stock.us.yahoo;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import yokwe.stock.us.Storage;
import yokwe.util.ListUtil;
import yokwe.util.yahoo.finance.Symbol;

public class YahooSymbol {
	private static final String PATH = Storage.Yahoo.getPath("yahoo-symbol.csv");
	public static String getPath() {
		return PATH;
	}
	
	public static void save(Collection<Symbol> collection) {
		ListUtil.save(Symbol.class, getPath(), collection);
	}
	public static void save(List<Symbol> list) {
		ListUtil.save(Symbol.class, getPath(), list);
	}
	public static List<Symbol> getList() {
		return ListUtil.getList(Symbol.class, getPath());
	}
	public static Map<String, Symbol> getMap() {
		//            symbol
		return ListUtil.checkDuplicate(getList(), o -> o.symbol);
	}

}
