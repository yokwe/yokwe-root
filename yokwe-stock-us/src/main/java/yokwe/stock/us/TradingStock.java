package yokwe.stock.us;

import java.util.List;
import java.util.Map;

import yokwe.stock.us.Stock.Type;
import yokwe.util.ListUtil;
import yokwe.util.StringUtil;

public class TradingStock implements Comparable<TradingStock> {
	private static final String PATH_FILE = Storage.getPath("trading-stock.csv");
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
	public static Map<String, TradingStock> getMap() {
		var list = getList();
		return ListUtil.checkDuplicate(list, TradingStock::getKey);
	}
	
	public String symbol;
	public String monex;
	public String sbi;
	public String rakuten;
	public String nikko;
	public String moomoo;
	public Type   type;
	public String name;

	public TradingStock (
		String symbol,
		String monex,
		String sbi,
		String rakuten,
		String nikko,
		String moomoo,
		Type   type,
		String name
		) {
		this.symbol  = symbol;
		this.monex   = monex;
		this.sbi     = sbi;
		this.rakuten = rakuten;
		this.nikko   = nikko;
		this.moomoo  = moomoo;
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
