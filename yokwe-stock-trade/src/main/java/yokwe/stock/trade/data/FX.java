package yokwe.stock.trade.data;

import java.util.Collections;
import java.util.List;

import yokwe.stock.trade.Storage;
import yokwe.util.CSVUtil;
import yokwe.util.CSVUtil.DecimalPlaces;

public class FX implements Comparable<FX> {
	public static final String PATH_FILE = Storage.getPath("data", "fx.csv");
	
	public static List<FX> load() {
		return CSVUtil.read(FX.class).file(PATH_FILE);
	}
	
	public static void save(List<FX> list) {
		Collections.sort(list);
		CSVUtil.write(FX.class).file(PATH_FILE, list);
	}


	public String date;
	@DecimalPlaces(2)
	public double usd;
	
	public FX(String date, double usd) {
		this.date = date;
		this.usd  = usd;
	}
	public FX(String date, String usdString) {
		this.date = date;
		this.usd  = Double.parseDouble(usdString);
	}
	
	public FX() {
		this("", 0);
	}
	
	@Override
	public String toString() {
		return String.format("%s %.2f", date, usd);
	}

	@Override
	public int compareTo(FX that) {
		return this.date.compareTo(that.date);
	}

}
