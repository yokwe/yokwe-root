package yokwe.stock.us;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import yokwe.util.ListUtil;
import yokwe.util.StringUtil;

public class Symbol implements Comparable<Symbol> {
	public static void save(Collection<Symbol> collection, String path) {
		// sanity check
		ListUtil.checkDuplicate(collection, o -> o.symbol);
		ListUtil.save(Symbol.class, path, collection);
	}
	public static void save(List<Symbol> list, String path) {
		// sanity check
		ListUtil.checkDuplicate(list, o -> o.symbol);
		ListUtil.save(Symbol.class, path, list);
	}
	
	public static List<Symbol> load(String path) {
		var list = ListUtil.load(Symbol.class, path);
		// sanity check
		ListUtil.checkDuplicate(list, o -> o.symbol);

		return list;
	}
	public static List<Symbol> getList(String path) {
		var list = ListUtil.getList(Symbol.class, path);
		// sanity check
		ListUtil.checkDuplicate(list, o -> o.symbol);

		return list;
	}
	public static Map<String, Symbol> getMap(String path) {
		//            symbol
		var list = ListUtil.getList(Symbol.class, path);
		return ListUtil.checkDuplicate(list, o -> o.symbol);
	}
	
	private static final String PATH_FILE = Storage.getPath("symbol.csv");
	
	public static String getPath() {
		return PATH_FILE;
	}
	
	public static void save(Collection<Symbol> collection) {
		save(collection, getPath());
	}
	public static void save(List<Symbol> list) {
		save(list, getPath());
	}
	public static List<Symbol> load() {
		return load(getPath());
	}
	public static List<Symbol> getList() {
		return getList(getPath());
	}
	public static Map<String, Symbol> getMap() {
		return getMap(getPath());
	}
	
	
	private static final String PATH_EXTRA_FILE = Storage.getPath("symbol-extra.csv");
	public static String getPathExtra() {
		return PATH_EXTRA_FILE;
	}

	public static List<Symbol> loadExtra() {
		return load(getPathExtra());
	}
	public static List<Symbol> getListExtra() {
		return getList(getPathExtra());
	}
	
	public String symbol; // normalized symbol like TRNT-A and RDS.A not like TRTN^A and RDS/A
	
	public Symbol(String symbol) {
		this.symbol = symbol.trim();
	}
	public Symbol() {
		this("");
	}
	
	@Override
	public int compareTo(Symbol that) {
		return this.symbol.compareTo(that.symbol);
	}

	@Override
	public int hashCode() {
		return symbol.hashCode();
	}
	
	@Override
	public boolean equals(Object o) {
		if (o == null) {
			return false;
		} else {
			if (o instanceof Symbol) {
				Symbol that = (Symbol)o;
				return this.compareTo(that) == 0;
			} else {
				return false;
			}
		}
	}

	@Override
	public String toString() {
		return StringUtil.toString(this);
	}

}
