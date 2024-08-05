package yokwe.finance.report;

import yokwe.util.StringUtil;
import yokwe.util.libreoffice.Sheet;

@Sheet.SheetName("stock-stats")
@Sheet.HeaderRow(0)
@Sheet.DataRow(1)
public final class StockStatsJP extends Sheet implements Comparable<StockStatsJP> {
	@Sheet.ColumnName("stockCode") public String stockCode;
	
	@Sheet.ColumnName("type")     public String type;
	@Sheet.ColumnName("sector")   public String sector;
	@Sheet.ColumnName("industry") public String industry;
	@Sheet.ColumnName("name")     public String name;
	@Sheet.ColumnName("date")     public String date;
	
	@Sheet.ColumnName("marketCap") public long   marketCap;
	
	// current price and volume
	@Sheet.ColumnName("pricec") public int    pricec;
	@Sheet.ColumnName("price")  public double price;
	// last price
	@Sheet.ColumnName("last")   public double last;
	
	// dividend
	@Sheet.ColumnName("divc")          public int    divc;
	@Sheet.ColumnName("lastDiv")       public double lastDiv;
	@Sheet.ColumnName("forwardYield")  public double forwardYield;
	@Sheet.ColumnName("annualDiv")     public double annualDiv;
	@Sheet.ColumnName("trailingYield") public double trailingYield;
	
	// rate of return
	@Sheet.ColumnName("rorPrice")        public double rorPrice;
	@Sheet.ColumnName("rorReinvested")   public double rorReinvested;
	@Sheet.ColumnName("rorNoReinvested") public double rorNoReinvested;
	
	// stats - sd hv rsi
	//  30 < pricec
	@Sheet.ColumnName("sd")  public double sd;
	@Sheet.ColumnName("hv")  public double hv;
	// 15 <= pricec
	@Sheet.ColumnName("rsi14") public double rsi14;
	@Sheet.ColumnName("rsi7")  public double rsi7;
	
	// min max
	@Sheet.ColumnName("min")   public double min;
	@Sheet.ColumnName("max")   public double max;
	@Sheet.ColumnName("minY3") public double minY3;
	@Sheet.ColumnName("maxY3") public double maxY3;
	
	// volume
	@Sheet.ColumnName("vol")  public long   vol;
	// 5 <= pricec
	@Sheet.ColumnName("vol5") public long   vol5;
	// 20 <= pricec
	@Sheet.ColumnName("vol21") public long  vol21;
	
	@Sheet.ColumnName("nisa") public String nisa;
	
	public StockStatsJP() {
		stockCode = null;
		
		type      = null;
		name      = null;
		date      = null;
		pricec    = -1;
		price     = -1;
		
		last      = -1;
		
		divc          = -1;
		lastDiv       = -1;
		forwardYield  = -1;
		annualDiv     = -1;
		trailingYield = -1;
		
		sd        = -1;
		hv        = -1;
		
		rsi14     = -1;
		rsi7      = -1;
		
		min       = -1;
		max       = -1;
		minY3     = -1;
		maxY3     = -1;
		
		vol       = -1;
		vol5      = -1;
		vol21     = -1;
		
	}

	@Override
	public int compareTo(StockStatsJP that) {
		return this.stockCode.compareTo(that.stockCode);
	}
	
	@Override
	public String toString() {
		return StringUtil.toString(this);
	}
}
