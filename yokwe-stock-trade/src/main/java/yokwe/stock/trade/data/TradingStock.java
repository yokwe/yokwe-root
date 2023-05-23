package yokwe.stock.trade.data;

import java.util.Collection;
import java.util.List;

import yokwe.stock.trade.Storage;
import yokwe.stock.us.Stock;
import yokwe.util.ListUtil;
import yokwe.util.StringUtil;

public class TradingStock implements Comparable<TradingStock> {
	private static final String PATH_FILE = Storage.Data.getPath("trading-stock.csv");
	public static String getPath() {
		return PATH_FILE;
	}
	public static void save(List<TradingStock> list) {
		ListUtil.save(TradingStock.class, getPath(), list);
	}
	public static List<TradingStock> load() {
		return ListUtil.load(TradingStock.class, getPath());
	}
	public static List<TradingStock> getList() {
		return ListUtil.getList(TradingStock.class, getPath());
	}
	
	public String symbol;
	public String monex;
	public String sbi;
	public String rakuten;
	public String type;
	public String name;

	public TradingStock (
		String symbol,
		String monex,
		String sbi,
		String rakuten,
		String type,
		String name
		) {
		this.symbol  = symbol;
		this.monex   = monex;
		this.sbi     = sbi;
		this.rakuten = rakuten;
		this.type    = type;
		this.name    = name;
	}
	
	public String getKey() {
		return this.symbol;
	}
	@Override
	public String toString() {
		return StringUtil.toString(this);
	}
	@Override
	public int compareTo(TradingStock that) {
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
			if (o instanceof TradingStock) {
				TradingStock that = (TradingStock)o;
				return this.compareTo(that) == 0;
			} else {
				return false;
			}
		}
	}

}
