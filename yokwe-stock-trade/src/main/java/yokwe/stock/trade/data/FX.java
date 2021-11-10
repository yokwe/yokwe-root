package yokwe.stock.trade.data;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

import yokwe.stock.trade.Storage;
import yokwe.util.CSVUtil;

public class FX implements Comparable<FX> {
	public static final String PATH_FILE = Storage.getPath("data", "fx.csv");
	
	public static List<FX> load() {
		return CSVUtil.read(FX.class).file(PATH_FILE);
	}
	
	public static void save(List<FX> list) {
		Collections.sort(list);
		CSVUtil.write(FX.class).file(PATH_FILE, list);
	}


	public String     date;
	public BigDecimal usd;
	
	public FX(String date, BigDecimal usd) {
		this.date = date;
		this.usd  = usd;
	}
	public FX(String date, String usdString) {
		this.date = date;
		this.usd  = new BigDecimal(usdString);
	}
	
	public FX() {
		this("", BigDecimal.ZERO);
	}
	
	@Override
	public String toString() {
		return String.format("%s %s", date, usd.toPlainString());
	}

	@Override
	public int compareTo(FX that) {
		return this.date.compareTo(that.date);
	}

}
