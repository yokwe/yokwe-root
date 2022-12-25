package yokwe.stock.jp.jpx;

import java.util.List;

import yokwe.stock.jp.Storage;
import yokwe.util.ListUtil;
import yokwe.util.libreoffice.Sheet;

@Sheet.SheetName("stats")
@Sheet.HeaderRow(0)
@Sheet.DataRow(1)
public class Stats extends Sheet implements Comparable<Stats>  {
	private static final String PATH_FILE = Storage.JPX.getPath("stats.csv");
	public static String getPath() {
		return PATH_FILE;
	}

	public static void save(List<Stats> statsList) {
		ListUtil.save(Stats.class, getPath(), statsList);
	}
	
	public static List<Stats> load() {
		return ListUtil.load(Stats.class, getPath());
	}

	
	//	public String exchange;
	@Sheet.ColumnName("stockCode")
	public String stockCode;
	
	@Sheet.ColumnName("name")
	public String name;
	
	@Sheet.ColumnName("unit")
	public int unit;
	
	// 33業種区分
	@Sheet.ColumnName("sector33")
	public String sector33;
	
	// 17業種区分
	@Sheet.ColumnName("sector17")
	public String sector17;
	
	// last values
	@Sheet.ColumnName("date")
	public String date;
	@Sheet.ColumnName("price")
	public double price;
	
	// price
	@Sheet.ColumnName("pricec")
	public int    pricec;
	@Sheet.ColumnName("sd")
	public double sd;
	@Sheet.ColumnName("hv")
	public double hv;
	@Sheet.ColumnName("rsi")
	public double rsi;
	@Sheet.ColumnName("min")
	public double min;
	@Sheet.ColumnName("max")
	public double max;
	@Sheet.ColumnName("minPCT")
	public double minPCT;
	@Sheet.ColumnName("maxPCT")
	public double maxPCT;

	// dividend
	@Sheet.ColumnName("div")
	public double div;
	@Sheet.ColumnName("divc")
	public int    divc;
	@Sheet.ColumnName("yield")
	public double yield;
	
	// volume
	@Sheet.ColumnName("vol")
	public long   vol;
	@Sheet.ColumnName("vol5")
	public long   vol5;
	@Sheet.ColumnName("vol21")
	public long   vol21;
	
	// price change detection
	@Sheet.ColumnName("last")
	public double last;
	@Sheet.ColumnName("lastPCT")
	public double lastPCT;
	
	// fiscal year end date and half fiscal year end date
	@Sheet.ColumnName("endDate1")
	public String endDate1;
	@Sheet.ColumnName("endDate2")
	public String endDate2;
	
	// misc
	@Sheet.ColumnName("numberOfIssuedK")
	public long   numberOfIssuedK;
	@Sheet.ColumnName("marketCapM")
	public long   marketCapM;      // price * numberOfIssued
	@Sheet.ColumnName("tradeCapM")
	public long   tradeCapM;       // price * vol
	@Sheet.ColumnName("volPCT")
	public double volPCT;          // vol / numberOfIssued
	@Sheet.ColumnName("feb17PCT")
	public double feb17PCT;
	
	@Override
	public int compareTo(Stats that) {
		return this.stockCode.compareTo(that.stockCode);
	}
}
