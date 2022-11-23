package yokwe.stock.us.nasdaq;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import yokwe.stock.us.Storage;
import yokwe.stock.us.nasdaq.api.AssetClass;
import yokwe.util.CSVUtil;
import yokwe.util.StringUtil;
import yokwe.util.UnexpectedException;

public class NASDAQSymbol implements Comparable<NASDAQSymbol> {
	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(NASDAQSymbol.class);

	private static final String PATH_FILE = Storage.NASDAQ.getPath("nasdaq-symbol.csv");
	public static String getPath() {
		return PATH_FILE;
	}

	public static void save(Collection<NASDAQSymbol> collection) {
		save(new ArrayList<>(collection));
	}
	public static void save(List<NASDAQSymbol> list) {
		// sanity check
		{
			Map<String, NASDAQSymbol> map = new TreeMap<>();
			for(var e: list) {
				String symbol = e.symbol;
				if (map.containsKey(symbol)) {
					logger.warn("Duplicate symbol");
					logger.warn("  old {}", map.get(symbol));
					logger.warn("  new {}", e);
					throw new UnexpectedException("duplicate symbol");
				} else {
					//
				}
			}
		}
		// Sort before save
		Collections.sort(list);
		CSVUtil.write(NASDAQSymbol.class).file(getPath(), list);
	}
	
	
	public static List<NASDAQSymbol> load() {
		return CSVUtil.read(NASDAQSymbol.class).file(getPath());
	}
	public static List<NASDAQSymbol> getList() {
		List<NASDAQSymbol> ret = load();
		return ret == null ? new ArrayList<>() : ret;
	}
	
	
	public static Map<String, NASDAQSymbol> getMap() {
		//            symbol
		Map<String, NASDAQSymbol> ret = new TreeMap<>();
		
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
	public String     name;
	
	public NASDAQSymbol(String symbol, AssetClass assetClass, String name) {
		this.symbol     = symbol.trim().replace('^', '-').replace('/', '.');
		this.assetClass = assetClass;
		this.name       = name;
	}
	public NASDAQSymbol() {
		this("", AssetClass.STOCK, "");
	}
	
	@Override
	public String toString() {
		return StringUtil.toString(this);
	}

	@Override
	public int compareTo(NASDAQSymbol that) {
		return this.symbol.compareTo(that.symbol);
	}
	
}
