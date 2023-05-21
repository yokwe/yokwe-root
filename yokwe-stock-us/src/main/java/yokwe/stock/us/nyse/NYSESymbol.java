package yokwe.stock.us.nyse;

import java.util.Collection;
import java.util.List;

import yokwe.stock.us.Storage;
import yokwe.util.ListUtil;
import yokwe.util.StringUtil;

public final class NYSESymbol implements Comparable<NYSESymbol> {
	private static final String PATH_FILE = Storage.NYSE.getPath("symbol.csv");
	public static String getPath() {
		return PATH_FILE;
	}
	public static void save(Collection<NYSESymbol> collection) {
		ListUtil.save(NYSESymbol.class, getPath(), collection);
	}
	public static void save(List<NYSESymbol> list) {
		ListUtil.save(NYSESymbol.class, getPath(), list);
	}
	public static List<NYSESymbol> load() {
		return ListUtil.load(NYSESymbol.class, getPath());
	}
	public static List<NYSESymbol> getList() {
		return ListUtil.getList(NYSESymbol.class, getPath());
	}
	
	public enum Market {
		BATS,
		NASDAQ,
		NYSE,
	}
	public enum Type {
		CEF    (true,  false), // CLOSED_END_FUND
		COMMON (true,  false), // COMMON_STOCK
		ADR    (true,  false), // DEPOSITORY_RECEIPT
		ETF    (false, true),  // EXCHANGE_TRADED_FUND
		ETN    (true,  false), // EXCHANGE_TRADED_NOTE
		LP     (true,  false), // LIMITED_PARTNERSHIP
		PREF   (true,  false), // PREFERRED_STOCK
		REIT   (true,  false), // REIT
		TRUST  (false, false), // TRUST
		UNIT   (false, false), // UNIT
		UBI    (false, false); // UNITS_OF_BENEFICIAL_INTEREST

		public final boolean stock;
		public final boolean etf;
		
		Type(boolean stock, boolean etf) {
			this.stock = stock;
			this.etf   = etf;
		}
	}
	
	public String symbol;
	public Market market;
	public Type   type;
	public String name;
	
	public NYSESymbol(String symbol, Market market, Type type, String name) {
		this.symbol = symbol;
		this.market = market;
		this.type   = type;
		this.name   = name;
	}

	@Override
	public String toString() {
		return StringUtil.toString(this);
	}
	public String getKey() {
		return symbol;
	}
	@Override
	public int compareTo(NYSESymbol that) {
		return this.getKey().compareTo(that.getKey());
	}
	@Override
	public int hashCode() {
		return getKey().hashCode();
	}
	@Override
	public boolean equals(Object o) {
		if (o != null) {
			if (o instanceof NYSESymbol) {
				NYSESymbol that = (NYSESymbol)o;
				return this.compareTo(that) == 0;
			} else {
				return false;
			}
		} else {
			return false;
		}
	}
}
