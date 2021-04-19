package yokwe.stock.jp.data;

import java.util.List;

import yokwe.util.CSVUtil;
import yokwe.util.libreoffice.Sheet;
import yokwe.util.libreoffice.SpreadSheet;

@Sheet.SheetName("stats-jp")
@Sheet.HeaderRow(0)
@Sheet.DataRow(1)
public class StatsJP extends Sheet {
	public static final String PATH_FILE        = "tmp/data/stats-jp.csv";

	public static void save(List<StatsJP> statsList) {
		CSVUtil.write(StatsJP.class).file(PATH_FILE, statsList);
	}
	
	public static List<StatsJP> load() {
		return CSVUtil.read(StatsJP.class).file(PATH_FILE);
	}

	
	//	public String exchange;
	@ColumnName("stockCode")
	@NumberFormat(SpreadSheet.FORMAT_STRING)
	public String stockCode;
	
	@ColumnName("name")
	@NumberFormat(SpreadSheet.FORMAT_STRING)
	public String name;
	
	@ColumnName("unit")
	@NumberFormat(SpreadSheet.FORMAT_INTEGER)
	public int unit;
	
	@ColumnName("sector33") // 33業種区分
	@NumberFormat(SpreadSheet.FORMAT_STRING)
	public String sector33;
	
	@ColumnName("sector17") // 17業種区分
	@NumberFormat(SpreadSheet.FORMAT_STRING)
	public String sector17;
	
	
	// last values
	@ColumnName("date")
	@NumberFormat(SpreadSheet.FORMAT_DATE)
	public String date;
	
	@ColumnName("price")
	@NumberFormat(SpreadSheet.FORMAT_INTEGER)
	public double price;
	
	// price
	@ColumnName("pricec")
	@NumberFormat(SpreadSheet.FORMAT_INTEGER)
	public int    pricec;
	
	@ColumnName("sd")
	@NumberFormat("#,##0.0000")
	public double sd;
	
	@ColumnName("hv")
	@NumberFormat("#,##0.0000")
	public double hv;
	
	@ColumnName("rsi")
	@NumberFormat(SpreadSheet.FORMAT_INTEGER)
	public double rsi;

	@ColumnName("min")
	@NumberFormat(SpreadSheet.FORMAT_INTEGER)
	public double min;
	
	@ColumnName("max")
	@NumberFormat(SpreadSheet.FORMAT_INTEGER)
	public double max;
	
	@ColumnName("minpct")
	@NumberFormat(SpreadSheet.FORMAT_PERCENT)
	public double minPCT;
	
	@ColumnName("maxpct")
	@NumberFormat(SpreadSheet.FORMAT_PERCENT)
	public double maxPCT;

	// dividend
	@ColumnName("div")
	@NumberFormat(SpreadSheet.FORMAT_INTEGER)
	public double div;
	
	@ColumnName("divc")
	@NumberFormat(SpreadSheet.FORMAT_INTEGER)
	public int    divc;
	
	@ColumnName("yield")
	@NumberFormat(SpreadSheet.FORMAT_PERCENT)
	public double yield;
	
	// volume
	@ColumnName("vol")
	@NumberFormat(SpreadSheet.FORMAT_INTEGER)
	public long   vol;
	
	@ColumnName("vol5")
	@NumberFormat(SpreadSheet.FORMAT_INTEGER)
	public long   vol5;
	
	@ColumnName("vol21")
	@NumberFormat(SpreadSheet.FORMAT_INTEGER)
	public long   vol21;
	
	// price change detection
	@ColumnName("last")
	@NumberFormat(SpreadSheet.FORMAT_INTEGER)
	public double last;
	
	@ColumnName("lastpct")
	@NumberFormat(SpreadSheet.FORMAT_PERCENT)
	public double lastPCT;
	
	@ColumnName("endDate1")
	@NumberFormat(SpreadSheet.FORMAT_STRING)
	public String endDate1;
	
	@ColumnName("endDate2")
	@NumberFormat(SpreadSheet.FORMAT_STRING)
	public String endDate2;
	
	@ColumnName("numberOfIssued")
	@NumberFormat(SpreadSheet.FORMAT_INTEGER)
	public long   numberOfIssuedK;
	
	@ColumnName("marketCap")
	@NumberFormat(SpreadSheet.FORMAT_INTEGER)
	public long   marketCapM; // price * numberOfIssued

	@ColumnName("tradeCap")
	@NumberFormat(SpreadSheet.FORMAT_INTEGER)
	public long   tradeCapM; // price * vol

	@ColumnName("volpct")
	@NumberFormat(SpreadSheet.FORMAT_PERCENT)
	public double volPCT; // vol / numberOfIssued
	
	@ColumnName("feb17pct")
	@NumberFormat(SpreadSheet.FORMAT_PERCENT)
	public double feb17PCT;
}
