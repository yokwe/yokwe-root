package yokwe.stock.trade.monex;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import yokwe.stock.trade.Storage;
import yokwe.util.CSVUtil;

public class StockUS implements Comparable<StockUS> {
	private static final String PATH_FILE =  Storage.Monex.getPath("monex-stock-us.csv");
	
	public static String getPath() {
		return PATH_FILE;
	}
	
	public static void save(Collection<StockUS> collection) {
		save(new ArrayList<>(collection));
	}
	public static void save(List<StockUS> list) {
		// Sort before save
		Collections.sort(list);
		CSVUtil.write(StockUS.class).file(getPath(), list);
	}
	
	public static List<StockUS> getList() {
		List<StockUS> ret = CSVUtil.read(StockUS.class).file(getPath());
		return ret == null ? new ArrayList<>() : ret;
	}

	public String symbol;
	public String name;

	public StockUS(String symbol, String name) {
		this.symbol = symbol.trim();
		this.name   = name.trim();
	}
	public StockUS() {
		this("", "");
	}
	
	@Override
	public int compareTo(StockUS that) {
		return this.symbol.compareTo(that.symbol);
	}

}
