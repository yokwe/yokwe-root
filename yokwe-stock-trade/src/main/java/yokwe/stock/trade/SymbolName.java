package yokwe.stock.trade;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import yokwe.util.CSVUtil;
import yokwe.util.StringUtil;
import yokwe.util.UnexpectedException;

public class SymbolName implements Comparable<SymbolName> {
	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(SymbolName.class);

	private static void checkDuplicate(List<SymbolName> list) {
		Map<String, SymbolName> map = new HashMap<>();
		for(var e: list) {
			String symbol = e.symbol;
			if (map.containsKey(symbol)) {
				logger.error("Duplicate symbol");
				logger.error("  old {}", map.get(symbol));
				logger.error("  new {}", e);
				throw new UnexpectedException("Duplicate StockUS");
			} else {
				map.put(symbol, e);
			}
		}
	}

	public static void save(Collection<SymbolName> collection, String path) {
		save(new ArrayList<>(collection), path);
	}
	public static void save(List<SymbolName> list, String path) {
		// sanity check
		checkDuplicate(list);
		
		// Sort before save
		Collections.sort(list);
		CSVUtil.write(SymbolName.class).file(path, list);
	}
	
	public static List<SymbolName> load(String path) {
		var list = CSVUtil.read(SymbolName.class).file(path);
		// sanity check
		if (list != null) checkDuplicate(list);

		return list;
	}
	public static List<SymbolName> getList(String path) {
		List<SymbolName> ret = load(path);
		return ret == null ? new ArrayList<>() : ret;
	}
	
	
	public String symbol;
	public String name;

	public SymbolName(String StockUS, String name) {
		this.symbol = StockUS.trim();
		this.name   = name.trim();
	}
	public SymbolName() {
		this("", "");
	}
	
	@Override
	public String toString() {
		return StringUtil.toString(this);
	}
	
	@Override
	public boolean equals(Object o) {
		if (o instanceof SymbolName) {
			SymbolName that = (SymbolName)o;
			return this.symbol.equals(that.symbol);
		} else {
			return false;
		}
	}
	
	@Override
	public int compareTo(SymbolName that) {
		return this.symbol.compareTo(that.symbol);
	}

}
