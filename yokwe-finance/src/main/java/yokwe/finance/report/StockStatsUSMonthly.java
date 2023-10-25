package yokwe.finance.report;

import java.math.BigDecimal;

import yokwe.util.StringUtil;
import yokwe.util.libreoffice.Sheet;
import yokwe.util.libreoffice.SpreadSheet;

@Sheet.SheetName("stock-stats")
@Sheet.HeaderRow(0)
@Sheet.DataRow(1)
public class StockStatsUSMonthly extends Sheet implements Comparable<StockStatsUSMonthly> {
	@Sheet.ColumnName("stockCode") public String stockCode;
	
	@Sheet.ColumnName("type")     public String type;
	@Sheet.ColumnName("sector")   public String sector;
	@Sheet.ColumnName("industry") public String industry;
	
	@Sheet.ColumnName("年月") public BigDecimal age; // yy.mm
	@Sheet.ColumnName("配当回数")    public int        divc;
	@Sheet.NumberFormat(SpreadSheet.FORMAT_NUMBER0)
	@Sheet.ColumnName("RSI")    public BigDecimal rsi;
	
	@Sheet.NumberFormat(SpreadSheet.FORMAT_PERCENT2)
	@Sheet.ColumnName("sd1年")  public BigDecimal sd1Y;
	@Sheet.NumberFormat(SpreadSheet.FORMAT_PERCENT2)
	@Sheet.ColumnName("sd3年")  public BigDecimal sd3Y;
	@Sheet.NumberFormat(SpreadSheet.FORMAT_PERCENT2)
	@Sheet.ColumnName("sd5年")  public BigDecimal sd5Y;
	@Sheet.NumberFormat(SpreadSheet.FORMAT_PERCENT2)
	@Sheet.ColumnName("sd10年") public BigDecimal sd10Y;

	@Sheet.NumberFormat(SpreadSheet.FORMAT_PERCENT2)
	@Sheet.ColumnName("収益1年")  public BigDecimal ror1Y;
	@Sheet.NumberFormat(SpreadSheet.FORMAT_PERCENT2)
	@Sheet.ColumnName("収益3年")  public BigDecimal ror3Y;
	@Sheet.NumberFormat(SpreadSheet.FORMAT_PERCENT2)
	@Sheet.ColumnName("収益5年")  public BigDecimal ror5Y;
	@Sheet.NumberFormat(SpreadSheet.FORMAT_PERCENT2)
	@Sheet.ColumnName("収益10年") public BigDecimal ror10Y;
	
	@Sheet.NumberFormat(SpreadSheet.FORMAT_NUMBER0)
	@Sheet.ColumnName("配当1年")  public BigDecimal div1Y;
	@Sheet.NumberFormat(SpreadSheet.FORMAT_NUMBER0)
	@Sheet.ColumnName("配当3年")  public BigDecimal div3Y;
	@Sheet.NumberFormat(SpreadSheet.FORMAT_NUMBER0)
	@Sheet.ColumnName("配当5年")  public BigDecimal div5Y;
	@Sheet.NumberFormat(SpreadSheet.FORMAT_NUMBER0)
	@Sheet.ColumnName("配当10年") public BigDecimal div10Y;
	
	@Sheet.NumberFormat(SpreadSheet.FORMAT_PERCENT2)
	@Sheet.ColumnName("利回り1年")  public BigDecimal yield1Y;
	@Sheet.NumberFormat(SpreadSheet.FORMAT_PERCENT2)
	@Sheet.ColumnName("利回り3年")  public BigDecimal yield3Y;
	@Sheet.NumberFormat(SpreadSheet.FORMAT_PERCENT2)
	@Sheet.ColumnName("利回り5年")  public BigDecimal yield5Y;
	@Sheet.NumberFormat(SpreadSheet.FORMAT_PERCENT2)
	@Sheet.ColumnName("利回り10年") public BigDecimal yield10Y;
	
	//
	@Sheet.ColumnName("名前")     public String name;
	@Sheet.ColumnName("|")        public String bar = "|";
	
	@Sheet.ColumnName("MONEX")  public String monex;
	@Sheet.ColumnName("SBI")    public String sbi;
	@Sheet.ColumnName("楽天")   public String rakuten;
	@Sheet.ColumnName("日興")   public String nikko;
	@Sheet.ColumnName("MOOMOO") public String moomoo;
	
	
    @Override
    public String toString() {
        return StringUtil.toString(this);
    }
    @Override
    public int compareTo(StockStatsUSMonthly that) {
    	return this.stockCode.compareTo(that.stockCode);
    }
    @Override
    public int hashCode() {
    	return this.stockCode.hashCode();
    }
}
