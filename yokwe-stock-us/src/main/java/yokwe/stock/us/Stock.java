package yokwe.stock.us;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import yokwe.util.ListUtil;
import yokwe.util.StringUtil;

public final class Stock implements Comparable<Stock> {
	public static final String FILE_NAME = "stock.csv";
	
	public static String getPath() {
		return Storage.getPath(FILE_NAME);
	}
	
	public static void save(Collection<Stock> collection) {
		// sanity check
		ListUtil.save(Stock.class, getPath(), collection);
	}
	public static void save(List<Stock> list) {
		// sanity check
		ListUtil.save(Stock.class, getPath(), list);
	}
	
	public static List<Stock> load() {
		return ListUtil.load(Stock.class, getPath());
	}
	public static List<Stock> getList() {
		return ListUtil.getList(Stock.class, getPath());
	}
	public static Map<String, Stock> getMap() {
		return ListUtil.checkDuplicate(getList(), Stock::getKey);
	}

	public enum Market {
		BATS,
		NASDAQ,
		NYSE,
		IEXG,
	}
	public enum SimpleType {
		STOCK,
		ETF,
		OTHER,
	}
	public enum Type {
		CEF    (SimpleType.STOCK), // CLOSED_END_FUND
		COMMON (SimpleType.STOCK), // COMMON_STOCK
		ADR    (SimpleType.STOCK), // DEPOSITORY_RECEIPT
		ETF    (SimpleType.ETF),   // EXCHANGE_TRADED_FUND
		ETN    (SimpleType.STOCK), // EXCHANGE_TRADED_NOTE
		LP     (SimpleType.STOCK), // LIMITED_PARTNERSHIP
		PREF   (SimpleType.STOCK), // PREFERRED_STOCK
		REIT   (SimpleType.STOCK), // REIT
		TRUST  (SimpleType.STOCK), // TRUST
		
		UNIT   (SimpleType.OTHER), // UNIT
		UBI    (SimpleType.OTHER); // UNITS_OF_BENEFICIAL_INTEREST

		public final SimpleType simpleType;
		
		Type(SimpleType simpleType) {
			this.simpleType = simpleType;
		}
	}
	
	public String symbol; // normalized symbol like TRNT-A and RDS.A not like TRTN^A and RDS/A
	public Market market;
	public Type   type;
	public String name;
	
	public Stock(String symbol, Market market, Type type, String name) {
		this.symbol = symbol.trim();
		this.market = market;
		this.type   = type;
		this.name   = name;
	}
	public Stock() {
		this("", Market.IEXG, Type.COMMON, "");
	}
	
	public String getKey() {
		return this.symbol;
	}

	@Override
	public String toString() {
		return StringUtil.toString(this);
	}

	@Override
	public int compareTo(Stock that) {
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
			if (o instanceof Stock) {
				Stock that = (Stock)o;
				return this.compareTo(that) == 0;
			} else {
				return false;
			}
		}
	}
}
