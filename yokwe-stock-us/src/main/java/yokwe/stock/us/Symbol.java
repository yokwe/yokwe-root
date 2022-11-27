package yokwe.stock.us;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import yokwe.util.CSVUtil;
import yokwe.util.StringUtil;
import yokwe.util.UnexpectedException;

public class Symbol implements Comparable<Symbol> {
	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(Symbol.class);

	private static void checkDuplicate(List<Symbol> list) {
		Map<String, Symbol> map = new HashMap<>();
		for(var e: list) {
			String symbol = e.symbol;
			if (map.containsKey(symbol)) {
				logger.error("Duplicate symbol");
				logger.error("  old {}", map.get(symbol));
				logger.error("  new {}", e);
				throw new UnexpectedException("Duplicate symbol");
			} else {
				map.put(symbol, e);
			}
		}
	}

	public static void save(Collection<Symbol> collection, String path) {
		save(new ArrayList<>(collection), path);
	}
	public static void save(List<Symbol> list, String path) {
		// sanity check
		checkDuplicate(list);
		
		// Sort before save
		Collections.sort(list);
		CSVUtil.write(Symbol.class).file(path, list);
	}
	
	public static List<Symbol> load(String path) {
		var list = CSVUtil.read(Symbol.class).file(path);
		// sanity check
		if (list != null) checkDuplicate(list);

		return list;
	}
	public static List<Symbol> getList(String path) {
		List<Symbol> ret = load(path);
		return ret == null ? new ArrayList<>() : ret;
	}
	public static Map<String, Symbol> getMap(String path) {
		//            symbol
		Map<String, Symbol> ret = new TreeMap<>();
		
		for(var e: getList(path)) {
			ret.put(e.symbol, e);
		}
		return ret;
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
		return CSVUtil.read(Symbol.class).file(getPathExtra());
	}
	public static List<Symbol> getListExtra() {
		List<Symbol> ret = loadExtra();
		return ret == null ? new ArrayList<>() : ret;
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
