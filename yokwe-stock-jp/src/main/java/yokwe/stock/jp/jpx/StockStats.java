package yokwe.stock.jp.jpx;

import java.math.BigDecimal;
import java.util.List;

import yokwe.stock.jp.Storage;
import yokwe.util.ListUtil;
import yokwe.util.libreoffice.Sheet;

@Sheet.SheetName("stock-stats")
@Sheet.HeaderRow(0)
@Sheet.DataRow(1)
public class StockStats extends Sheet implements Comparable<StockStats> {
	private static final String PATH_FILE = Storage.JPX.getPath("stock-stats.csv");
	public static final String getPath() {
		return PATH_FILE;
	}

	public static void save(List<StockStats> list) {
		ListUtil.save(StockStats.class, getPath(), list);
	}
	
	public static List<StockStats> load() {
		return ListUtil.load(StockStats.class, getPath());
	}

	@Sheet.ColumnName("stockCode") public String     stockCode;
	@Sheet.ColumnName("isinCode")  public String     isinCode;
	@Sheet.ColumnName("name")      public String     name;
	@Sheet.ColumnName("category")  public String     category;
	@Sheet.ColumnName("unit")      public int        unit;
	@Sheet.ColumnName("issued")    public BigDecimal issued;

	// price
	@Sheet.ColumnName("priceDate")    public String     priceDate;
	@Sheet.ColumnName("pricec")       public int        pricec;
	@Sheet.ColumnName("price")        public BigDecimal price;
	@Sheet.ColumnName("marketCap")    public BigDecimal marketCap;
	@Sheet.ColumnName("priceLastPCT") public BigDecimal priceLastPCT;
	@Sheet.ColumnName("priceMinPCT")  public BigDecimal priceMinPCT;
	@Sheet.ColumnName("priceMaxPCT")  public BigDecimal priceMaxPCT;
	@Sheet.ColumnName("sd")           public BigDecimal sd;
	@Sheet.ColumnName("hv")           public BigDecimal hv;
	@Sheet.ColumnName("rsi")          public BigDecimal rsi;

	// dividend
	@Sheet.ColumnName("divc")      public int        divc;
	@Sheet.ColumnName("divLast")   public BigDecimal divLast;
	@Sheet.ColumnName("div1Y")     public BigDecimal div1Y;
	@Sheet.ColumnName("yieldLast") public BigDecimal yieldLast;
	@Sheet.ColumnName("yield1Y")   public BigDecimal yield1Y;
    
    // trade volume
    @Sheet.ColumnName("vol")   public BigDecimal vol;
    @Sheet.ColumnName("vol5")  public BigDecimal vol5;
    @Sheet.ColumnName("vol21") public BigDecimal vol21;
    
    // free etf
    @Sheet.ColumnName("sbi")      public int sbi;
    @Sheet.ColumnName("rakuten")  public int rakuten;


	@Override
	public int compareTo(StockStats that) {
		return this.stockCode.compareTo(that.stockCode);
	}

}
