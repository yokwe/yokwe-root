package yokwe.stock.us.nyse;

import java.util.Collection;
import java.util.List;

import yokwe.stock.us.Storage;
import yokwe.util.ListUtil;
import yokwe.util.StringUtil;

public final class NYSESymbol {
	public static final class Data implements Comparable<Data> {		
		public String exchangeId;
		public String instrumentName;
		public Type   instrumentType;
		public MIC    micCode;
		public String normalizedTicker;
		public String symbolEsignalTicker;
		public String symbolExchangeTicker;
		public String symbolTicker;
		public int    total;
		public String url;
		
		public Data() {}
		
		@Override
		public String toString() {
			return StringUtil.toString(this);
		}

		public String getKey() {
			return normalizedTicker;
		}
		@Override
		public int compareTo(Data that) {
			return this.getKey().compareTo(that.getKey());
		}
		@Override
		public int hashCode() {
			return getKey().hashCode();
		}
		@Override
		public boolean equals(Object o) {
			if (o != null) {
				if (o instanceof Data) {
					Data that = (Data)o;
					return this.compareTo(that) == 0;
				} else {
					return false;
				}
			} else {
				return false;
			}
		}
	}
	
	public static final class Stock {
		private static final String PATH_FILE = Storage.NYSE.getPath("stock.csv");

		public static String getPath() {
			return PATH_FILE;
		}
		public static void save(Collection<Data> collection) {
			ListUtil.save(Data.class, getPath(), collection);
		}
		public static void save(List<Data> list) {
			ListUtil.save(Data.class, getPath(), list);
		}
		public static List<Data> load() {
			return ListUtil.load(Data.class, getPath());
		}
		public static List<Data> getList() {
			return ListUtil.getList(Data.class, getPath());
		}
	}
	
	public static final class ETF {
		private static final String PATH_FILE = Storage.NYSE.getPath("etf.csv");

		public static String getPath() {
			return PATH_FILE;
		}
		public static void save(Collection<Data> collection) {
			ListUtil.save(Data.class, getPath(), collection);
		}
		public static void save(List<Data> list) {
			ListUtil.save(Data.class, getPath(), list);
		}
		public static List<Data> load() {
			return ListUtil.load(Data.class, getPath());
		}
		public static List<Data> getList() {
			return ListUtil.getList(Data.class, getPath());
		}
	}
	
	
	public enum Type {
		CEF    ("CLOSED_END_FUND",              true,  false),
		COMMON ("COMMON_STOCK",                 true,  false),
		ADR    ("DEPOSITORY_RECEIPT",           true,  false),
		ETF    ("EXCHANGE_TRADED_FUND",         false, true),
		ETN    ("EXCHANGE_TRADED_NOTE",         true,  false),
		LP     ("LIMITED_PARTNERSHIP",          true,  false),
		PREF   ("PREFERRED_STOCK",              true,  false),
		REIT   ("REIT",                         true,  false),
		TRUST  ("TRUST",                        false, false),
		UNIT   ("UNIT",                         false, false),
		UBI    ("UNITS_OF_BENEFICIAL_INTEREST", false, false);
		
		public final String value;
		public final boolean stock;
		public final boolean etf;
		
		Type(String value, boolean stock, boolean etf) {
			this.value = value;
			this.stock = stock;
			this.etf   = etf;
		}
		
		@Override
		public String toString() {
			return value;
		}
	}
	
	// Market Identifier Code
	public enum MIC {
		ARCX("ARCX", true,  false), // NYSE ARCA
		BATS("BATS", false, false), // CBOE BZX U.S. EQUITIES EXCHANGE
		XASE("XASE", true,  false), // NYSE MKT LLC
		XNCM("XNCM", false, true),  // NASDAQ CAPITAL MARKET
		XNGS("XNGS", false, true),  // NASDAQ/NGS (GLOBAL SELECT MARKET)
		XNMS("XNMS", false, true),  // NASDAQ/NMS (GLOBAL MARKET)
		XNYS("XNYS", true,  false); // NEW YORK STOCK EXCHANGE, INC.

		public final String  value;
		public final boolean nyse;
		public final boolean nasdaq;
		MIC(String value, boolean nyse, boolean nasdaq) {
			this.value  = value;
			this.nyse   = nyse;
			this.nasdaq = nasdaq;
		}
		
		@Override
		public String toString() {
			return value;
		}
		
		public boolean isNYSE() {
			return nyse;
		}
		public boolean isNASDAQ() {
			return nasdaq;
		}
	}
	

	public static final class Symbol implements Comparable<Symbol> {
		private static final String PATH_FILE = Storage.NYSE.getPath("symbol.csv");
		public static String getPath() {
			return PATH_FILE;
		}
		public static void save(Collection<Symbol> collection) {
			ListUtil.save(Symbol.class, getPath(), collection);
		}
		public static void save(List<Symbol> list) {
			ListUtil.save(Symbol.class, getPath(), list);
		}
		public static List<Symbol> load() {
			return ListUtil.load(Symbol.class, getPath());
		}
		public static List<Symbol> getList() {
			return ListUtil.getList(Symbol.class, getPath());
		}
		
		public String symbol;
		public MIC    market;
		public Type   type;
		public String name;
		
		public Symbol(String symbol, MIC market, Type type, String name) {
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
		public int compareTo(Symbol that) {
			return this.getKey().compareTo(that.getKey());
		}
		@Override
		public int hashCode() {
			return getKey().hashCode();
		}
		@Override
		public boolean equals(Object o) {
			if (o != null) {
				if (o instanceof Symbol) {
					Symbol that = (Symbol)o;
					return this.compareTo(that) == 0;
				} else {
					return false;
				}
			} else {
				return false;
			}
		}
	}

}
