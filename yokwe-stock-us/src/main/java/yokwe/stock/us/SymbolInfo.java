package yokwe.stock.us;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import yokwe.stock.us.nasdaq.api.AssetClass;
import yokwe.util.ListUtil;
import yokwe.util.StringUtil;
import yokwe.util.UnexpectedException;

public final class SymbolInfo implements Comparable<SymbolInfo> {
	private static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();
	
	public static Map<String, SymbolInfo> checkDuplicate(Collection<SymbolInfo> collection) {
		return ListUtil.checkDuplicate(collection, SymbolInfo::getKey);
	}
	public static Map<String, SymbolInfo> checkDuplicate(List<SymbolInfo> list) {
		return ListUtil.checkDuplicate(list, SymbolInfo::getKey);
	}
	
	public static void save(Collection<SymbolInfo> collection, String path) {
		// sanity check
		checkDuplicate(collection);
		ListUtil.save(SymbolInfo.class, path, collection);
	}
	public static void save(List<SymbolInfo> list, String path) {
		// sanity check
		checkDuplicate(list);
		ListUtil.save(SymbolInfo.class, path, list);
	}
	
	public static List<SymbolInfo> load(String path) {
		var list = ListUtil.load(SymbolInfo.class, path);
		// sanity check
		checkDuplicate(list);
		return list;
	}
	public static List<SymbolInfo> getList(String path) {
		var list = ListUtil.getList(SymbolInfo.class, path);
		// sanity check
		checkDuplicate(list);
		return list;
	}
	public static Map<String, SymbolInfo> getMap(String path) {
		var list = ListUtil.getList(SymbolInfo.class, path);
		return checkDuplicate(list);
	}

	public enum Type {
		STOCK("S"), ETF("E");
		
		public final String value;
		Type(String value) {
			this.value = value;
		}
		
		public AssetClass toAssetClass() {
			switch(this) {
			case STOCK:
				return AssetClass.STOCK;
			case ETF:
				return AssetClass.ETF;
			default:
				logger.error("Unexpected value");
				logger.error("  value {}", this);
				throw new UnexpectedException("Unexpected value");
			}
		}
	}
	
	public String symbol; // normalized symbol like TRNT-A and RDS.A not like TRTN^A and RDS/A
	public Type   type;
	public String name;
	
	public SymbolInfo(String symbol, Type type, String name) {
		this.symbol = symbol.trim();
		this.type   = type;
		this.name   = name;
	}
	public SymbolInfo() {
		this("", Type.STOCK, "");
	}
	
	public String getKey() {
		return this.symbol;
	}

	@Override
	public String toString() {
		return StringUtil.toString(this);
	}

	@Override
	public int compareTo(SymbolInfo that) {
		return this.getKey().compareTo(that.getKey());
	}

	@Override
	public int hashCode() {
		return this.getKey().hashCode();
	}
	
	@Override
	public boolean equals(Object o) {
		if (o == null) {
			return false;
		} else {
			if (o instanceof SymbolInfo) {
				SymbolInfo that = (SymbolInfo)o;
				return this.compareTo(that) == 0;
			} else {
				return false;
			}
		}
	}
}
