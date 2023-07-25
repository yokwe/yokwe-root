package yokwe.stock.jp.toushin;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import yokwe.stock.jp.Storage;
import yokwe.util.ListUtil;
import yokwe.util.StringUtil;
import yokwe.util.libreoffice.Sheet;
import yokwe.util.libreoffice.SpreadSheet;

@Sheet.SheetName("stats")
@Sheet.HeaderRow(0)
@Sheet.DataRow(1)
public class Stats extends Sheet implements Comparable<Stats> {
	private static final String PATH_FILE = Storage.Toushin.getPath("stats.csv");
	public static final String getPath() {
		return PATH_FILE;
	}
	public static void save(List<Stats> list) {
		ListUtil.save(Stats.class, getPath(), list);
	}
	public static List<Stats> load() {
		return ListUtil.load(Stats.class, getPath());
	}
	
	@Sheet.ColumnName("isinコード")      public String isinCode;
	@Sheet.ColumnName("ファンドコード")  public String fundCode;
	@Sheet.ColumnName("銘柄コード")      public String stockCode;
	
	@Sheet.ColumnName("設定日")  public LocalDate inception;
	@Sheet.ColumnName("償還日")  public LocalDate redemption;
	
	@Sheet.ColumnName("年月") public BigDecimal age; // yy.mm
	
	@Sheet.ColumnName("投資対象")    public String investingAsset;
	@Sheet.ColumnName("投資地域")    public String investingArea;
	@Sheet.ColumnName("ファンド型")  public String indexFundType;
	
	@NumberFormat(SpreadSheet.FORMAT_PERCENT)
	@Sheet.ColumnName("管理費")      public BigDecimal expenseRatio;
	@NumberFormat(SpreadSheet.FORMAT_PERCENT)
	@Sheet.ColumnName("購入費最大")  public BigDecimal buyFeeMax;
	@NumberFormat(SpreadSheet.FORMAT_NUMBER_MILLION)
	@Sheet.ColumnName("資産総額")    public BigDecimal nav;
	@Sheet.ColumnName("配当回数")    public int        divc;
	@NumberFormat(SpreadSheet.FORMAT_NUMBER0)
	@Sheet.ColumnName("RSI")    public BigDecimal rsi;
	
	@NumberFormat(SpreadSheet.FORMAT_NUMBER3)
	@Sheet.ColumnName("sd1年")  public BigDecimal sd1Y;
	@NumberFormat(SpreadSheet.FORMAT_NUMBER3)
	@Sheet.ColumnName("sd3年")  public BigDecimal sd3Y;
	@NumberFormat(SpreadSheet.FORMAT_NUMBER3)
	@Sheet.ColumnName("sd5年")  public BigDecimal sd5Y;
	@NumberFormat(SpreadSheet.FORMAT_NUMBER3)
	@Sheet.ColumnName("sd10年") public BigDecimal sd10Y;

	@NumberFormat(SpreadSheet.FORMAT_PERCENT2)
	@Sheet.ColumnName("収益1年")  public BigDecimal ror1Y;
	@NumberFormat(SpreadSheet.FORMAT_PERCENT2)
	@Sheet.ColumnName("収益3年")  public BigDecimal ror3Y;
	@NumberFormat(SpreadSheet.FORMAT_PERCENT2)
	@Sheet.ColumnName("収益5年")  public BigDecimal ror5Y;
	@NumberFormat(SpreadSheet.FORMAT_PERCENT2)
	@Sheet.ColumnName("収益10年") public BigDecimal ror10Y;
	
	@NumberFormat(SpreadSheet.FORMAT_NUMBER0)
	@Sheet.ColumnName("配当1年")  public BigDecimal div1Y;
	@NumberFormat(SpreadSheet.FORMAT_NUMBER0)
	@Sheet.ColumnName("配当3年")  public BigDecimal div3Y;
	@NumberFormat(SpreadSheet.FORMAT_NUMBER0)
	@Sheet.ColumnName("配当5年")  public BigDecimal div5Y;
	@NumberFormat(SpreadSheet.FORMAT_NUMBER0)
	@Sheet.ColumnName("配当10年") public BigDecimal div10Y;
	
	@NumberFormat(SpreadSheet.FORMAT_PERCENT2)
	@Sheet.ColumnName("利回り1年")  public BigDecimal yield1Y;
	@NumberFormat(SpreadSheet.FORMAT_PERCENT2)
	@Sheet.ColumnName("利回り3年")  public BigDecimal yield3Y;
	@NumberFormat(SpreadSheet.FORMAT_PERCENT2)
	@Sheet.ColumnName("利回り5年")  public BigDecimal yield5Y;
	@NumberFormat(SpreadSheet.FORMAT_PERCENT2)
	@Sheet.ColumnName("利回り10年") public BigDecimal yield10Y;
	
	@NumberFormat(SpreadSheet.FORMAT_PERCENT0)
	@Sheet.ColumnName("配当品質1年")  public BigDecimal divQ1Y;
	@NumberFormat(SpreadSheet.FORMAT_PERCENT0)
	@Sheet.ColumnName("配当品質3年")  public BigDecimal divQ3Y;
	@NumberFormat(SpreadSheet.FORMAT_PERCENT0)
	@Sheet.ColumnName("配当品質5年")  public BigDecimal divQ5Y;
	@NumberFormat(SpreadSheet.FORMAT_PERCENT0)
	@Sheet.ColumnName("配当品質10年") public BigDecimal divQ10Y;
	
	//
	@Sheet.ColumnName("名前")     public String name;
	@Sheet.ColumnName("|")        public String bar = "|";
	
	@NumberFormat(SpreadSheet.FORMAT_PERCENT2)
	@Sheet.ColumnName("GMO")     public BigDecimal gmo;
	@NumberFormat(SpreadSheet.FORMAT_PERCENT2)
	@Sheet.ColumnName("日興")   public BigDecimal nikko;
	@NumberFormat(SpreadSheet.FORMAT_PERCENT2)
	@Sheet.ColumnName("野村")   public BigDecimal nomura;
	@NumberFormat(SpreadSheet.FORMAT_PERCENT2)
	@Sheet.ColumnName("楽天") public BigDecimal rakuten;
	@NumberFormat(SpreadSheet.FORMAT_PERCENT2)
	@Sheet.ColumnName("SBI")     public BigDecimal sbi;
	@NumberFormat(SpreadSheet.FORMAT_PERCENT2)
	@Sheet.ColumnName("ソニー")    public BigDecimal sony;
	
	
    @Override
    public String toString() {
        return StringUtil.toString(this);
    }
    @Override
    public int compareTo(Stats that) {
    	return this.isinCode.compareTo(that.isinCode);
    }
    @Override
    public boolean equals(Object o) {
    	if (o == null) {
    		return false;
    	} else {
    		if (o instanceof Fund) {
    			Stats that = (Stats)o;
    			return this.compareTo(that) == 0;
    		} else {
    			return false;
    		}
    	}
    }
    @Override
    public int hashCode() {
    	return this.isinCode.hashCode();
    }

}
