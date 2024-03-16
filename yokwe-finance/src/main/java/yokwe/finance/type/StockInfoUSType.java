package yokwe.finance.type;

import yokwe.util.StringUtil;

public final class StockInfoUSType implements Comparable<StockInfoUSType> {
	public static String toYahooSymbol(String symbol) {
		return symbol.replace("-", "-P").replace(".", "-");
	}
	
	public static String toNASDAQSymbol(String stockCode) {
		return stockCode.replace("-", ".PR"); // BC-A => BC.PRA
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
		ETN    (SimpleType.ETF),   // EXCHANGE_TRADED_NOTE
		LP     (SimpleType.STOCK), // LIMITED_PARTNERSHIP
		PREF   (SimpleType.STOCK), // PREFERRED_STOCK
		REIT   (SimpleType.STOCK), // REIT
		TRUST  (SimpleType.STOCK), // TRUST
		
		UNIT   (SimpleType.OTHER), // UNIT
		UBI    (SimpleType.OTHER), // UNITS_OF_BENEFICIAL_INTEREST
		
		WARRANT(SimpleType.OTHER); // WARRANT

		public final SimpleType simpleType;
		
		Type(SimpleType simpleType) {
			this.simpleType = simpleType;
		}
		
		public boolean isETF() {
			return simpleType == SimpleType.ETF;
		}
		public boolean isStock() {
			return simpleType == SimpleType.STOCK;
		}
	}
	
	public final String stockCode; // normalized symbol like TRNT-A and RDS.A not like TRTN^A and RDS/A
	public final Market market;
	public final Type   type;
	public final String name;
	
	public StockInfoUSType(String symbol, Market market, Type type, String name) {
		this.stockCode = symbol.trim();
		this.market = market;
		this.type   = type;
		this.name   = name;
	}
		
	
	@Override
	public String toString() {
		return StringUtil.toString(this);
	}

	public String getKey() {
		return this.stockCode;
	}
	@Override
	public int compareTo(StockInfoUSType that) {
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
			if (o instanceof StockInfoUSType) {
				StockInfoUSType that = (StockInfoUSType)o;
				return this.compareTo(that) == 0;
			} else {
				return false;
			}
		}
	}
}
