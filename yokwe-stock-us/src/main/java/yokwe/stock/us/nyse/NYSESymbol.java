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
		public String instrumentType;
		public String micCode;
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

		@Override
		public int compareTo(Data that) {
			return this.normalizedTicker.compareTo(that.normalizedTicker);
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
}
