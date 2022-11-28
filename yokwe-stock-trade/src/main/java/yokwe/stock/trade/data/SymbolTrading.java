package yokwe.stock.trade.data;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import yokwe.stock.trade.Storage;
import yokwe.util.CSVUtil;
import yokwe.util.StringUtil;

public class SymbolTrading implements Comparable<SymbolTrading> {
	public static final String PATH = Storage.Data.getPath("symbol-trading.csv");
	public static final String getPath() {
		return PATH;
	}
	
	public static void save(Collection<SymbolTrading> collection, String path) {
		save(new ArrayList<>(collection), path);
	}
	public static void save(List<SymbolTrading> list) {
		// Sort before save
		Collections.sort(list);
		CSVUtil.write(SymbolTrading.class).file(PATH, list);
	}
	
	public static List<SymbolTrading> load() {
		return CSVUtil.read(SymbolTrading.class).file(PATH);
	}
	public static List<SymbolTrading> getList() {
		List<SymbolTrading> ret = load();
		return (ret == null) ? new ArrayList<>() : ret;
	}
	

	public String symbol;
	public String nasdaq;
	public String extra;
	public String monex;
	public String sbi;
	public String rakuten;
	public String name;
	
	public SymbolTrading (
		String symbol,
		String nasdaq,
		String extra,
		String monex,
		String sbi,
		String rakuten,
		String name
		) {
		this.symbol  = symbol;
		this.nasdaq  = nasdaq;
		this.extra   = extra;
		this.monex   = monex;
		this.sbi     = sbi;
		this.rakuten = rakuten;
		this.name    = name;
	}
	public SymbolTrading() {
		this(null, null, null, null, null, null, null);
	}
	
	@Override
	public String toString() {
		return StringUtil.toString(this);
	}
	
	@Override
	public int compareTo(SymbolTrading that) {
		return this.symbol.compareTo(that.symbol);
	}
}
