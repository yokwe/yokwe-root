package yokwe.finance.report;

import yokwe.util.StringUtil;
import yokwe.util.libreoffice.Sheet;

@Sheet.SheetName("stock-stats")
@Sheet.HeaderRow(0)
@Sheet.DataRow(1)
public final class StockStatsUS extends Sheet implements Comparable<StockStatsUS> {
	@Sheet.ColumnName("stockCode") public String stockCode;
	
	@Sheet.ColumnName("type")     public String type;
	@Sheet.ColumnName("sector")   public String sector;
	@Sheet.ColumnName("industry") public String industry;
	@Sheet.ColumnName("name")     public String name;
	@Sheet.ColumnName("date")     public String date;
	
	// current price and volume
	@Sheet.ColumnName("pricec") public int    pricec;
	@Sheet.ColumnName("price")  public double price;
	// last price
	@Sheet.ColumnName("last")   public double last;
	
	// stats - sd hv rsi
	//  30 < pricec
	@Sheet.ColumnName("sd")  public double sd;
	@Sheet.ColumnName("hv")  public double hv;
	// 15 <= pricec
	@Sheet.ColumnName("rsi") public double rsi;
	
	// min max
	@Sheet.ColumnName("min") public double min;
	@Sheet.ColumnName("max") public double max;
	
	// dividend
	@Sheet.ColumnName("divc")  public int    divc;
	@Sheet.ColumnName("yield") public double yield;

	// volume
	@Sheet.ColumnName("vol")  public long   vol;
	// 5 <= pricec
	@Sheet.ColumnName("vol5") public long   vol5;
	// 20 <= pricec
	@Sheet.ColumnName("vol21") public long  vol21;
	
	@Sheet.ColumnName("MONEX")  public String monex;
	@Sheet.ColumnName("SBI")    public String sbi;
	@Sheet.ColumnName("楽天")   public String rakuten;
	@Sheet.ColumnName("日興")   public String nikko;
	@Sheet.ColumnName("MOOMOO") public String moomoo;

	public StockStatsUS() {
		stockCode = null;
		
		monex     = null;
		sbi       = null;
		rakuten   = null;
		nikko     = null;

		type      = null;
		name      = null;
		date      = null;
		pricec    = -1;
		price     = -1;
		
		last      = -1;
		
		sd        = -1;
		hv        = -1;
		
		rsi       = -1;
		
		min       = -1;
		max       = -1;
		
		divc      = -1;
		yield     = -1;
		
		vol       = -1;
		vol5      = -1;
		vol21     = -1;
		
	}

	@Override
	public int compareTo(StockStatsUS that) {
		return this.stockCode.compareTo(that.stockCode);
	}
	
	@Override
	public String toString() {
		return StringUtil.toString(this);
	}
}
