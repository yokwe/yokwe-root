package yokwe.stock.jp.jpx;

import java.util.List;

import yokwe.util.CSVUtil;

public class Stats {
	private static final String PATH_FILE = getPath();
	public static String getPath() {
		return JPX.getPath("stats.csv");
	}

	public static void save(List<Stats> statsList) {
		CSVUtil.write(Stats.class).file(PATH_FILE, statsList);
	}
	
	public static List<Stats> load() {
		return CSVUtil.read(Stats.class).file(PATH_FILE);
	}

	
	//	public String exchange;
	public String stockCode;
	
	public String name;
	
	public int unit;
	
	// 33業種区分
	public String sector33;
	
	// 17業種区分
	public String sector17;
	
	// last values
	public String date;
	public double price;
	
	// price
	public int    pricec;
	public double sd;
	public double hv;
	public double rsi;
	public double min;
	public double max;
	public double minPCT;
	public double maxPCT;

	// dividend
	public double div;
	public int    divc;
	public double yield;
	
	// volume
	public long   vol;
	public long   vol5;
	public long   vol21;
	
	// price change detection
	public double last;
	public double lastPCT;
	
	// fiscal year end date and half fiscal year end date
	public String endDate1;
	public String endDate2;
	
	// misc
	public long   numberOfIssuedK;
	public long   marketCapM;      // price * numberOfIssued
	public long   tradeCapM;       // price * vol
	public double volPCT;          // vol / numberOfIssued
	public double feb17PCT;
}
