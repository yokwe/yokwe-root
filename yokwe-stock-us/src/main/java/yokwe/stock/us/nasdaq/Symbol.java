package yokwe.stock.us.nasdaq;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import yokwe.stock.us.Storage;
import yokwe.stock.us.nasdaq.api.AssetClass;
import yokwe.util.CSVUtil;
import yokwe.util.StringUtil;
import yokwe.util.UnexpectedException;

public class Symbol implements Comparable<Symbol> {
	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(Symbol.class);

	private static final String PATH_FILE = Storage.NASDAQ.getPath("symbol.csv");
	public static String getPath() {
		return PATH_FILE;
	}
	
	public static void save(Collection<Symbol> collection) {
		save(new ArrayList<>(collection));
	}
	public static void save(List<Symbol> list) {
		// Sort before save
		Collections.sort(list);
		CSVUtil.write(Symbol.class).file(getPath(), list);
	}
	
	public static List<Symbol> load() {
		return CSVUtil.read(Symbol.class).file(getPath());
	}
	public static List<Symbol> getList() {
		List<Symbol> ret = load();
		return ret == null ? new ArrayList<>() : ret;
	}
	
	
	private static final String PATH_EXTRA_FILE = Storage.NASDAQ.getPath("symbol-extra.csv");
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
	
	public static Map<String, Symbol> getMap() {
		//            symbol
		Map<String, Symbol> ret = new TreeMap<>();
		
		for(var e: getList()) {
			String symbol = e.symbol;
			if (ret.containsKey(symbol)) {
				logger.error("Duplicate symbol");
				logger.error("  date {}", symbol);
				logger.error("  old {}", ret.get(symbol));
				logger.error("  new {}", e);
				throw new UnexpectedException("Duplicate symbol");
			} else {
				ret.put(symbol, e);
			}
		}
		return ret;
	}
	
	public String     symbol; // normalized symbol like TRNT-A and RDS.A not like TRTN^A and RDS/A
	public AssetClass assetClass;
	public String     name;   // name of stock to analyze equality of symbol
	
	public Symbol(String symbol, AssetClass assetClass, String name) {
		this.symbol     = symbol.trim();
		this.assetClass = assetClass;
		this.name       = name.trim();
	}
	public Symbol() {
		this("", AssetClass.STOCK, "");
	}
	
	@Override
	public int compareTo(Symbol that) {
		return this.symbol.compareTo(that.symbol);
	}

	@Override
	public String toString() {
		return StringUtil.toString(this);
	}

}
