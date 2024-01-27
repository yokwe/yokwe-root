package yokwe.finance.report;

import java.time.LocalDate;

import yokwe.util.StringUtil;
import yokwe.util.libreoffice.Sheet;

public class AssetStats {
	
	@Sheet.SheetName("会社レポート")
	@Sheet.HeaderRow(0)
	@Sheet.DataRow(1)
	public static class CompanyReport extends Sheet implements Comparable<CompanyReport> {
		@Sheet.ColumnName("日付")      public String date    = "";
		
		@Sheet.ColumnName("円資産")        public double totalJPY   = 0;
		@Sheet.ColumnName("ソニー円")      public double sonyJPY    = 0;
		@Sheet.ColumnName("三井住友円")    public double smbcJPY    = 0;
		@Sheet.ColumnName("PRESTIA円")     public double prestiaJPY = 0;
		@Sheet.ColumnName("SMTB円")        public double smtbJPY    = 0;
		@Sheet.ColumnName("楽天証券円")    public double rakutenJPY = 0;
		@Sheet.ColumnName("日興証券円")    public double nikkoJPY   = 0;
		@Sheet.ColumnName("SBI証券円")     public double sbiJPY     = 0;
		
		@Sheet.ColumnName("ドル資産")      public double totalUSD   = 0;
		@Sheet.ColumnName("ソニードル")    public double sonyUSD    = 0;
		@Sheet.ColumnName("三井住友ドル")  public double smbcUSD    = 0;
		@Sheet.ColumnName("PRESTIAドル")   public double prestiaUSD = 0;
		@Sheet.ColumnName("SMTBドル")      public double smtbUSD    = 0;
		@Sheet.ColumnName("楽天証券ドル")  public double rakutenUSD = 0;
		@Sheet.ColumnName("日興証券ドル")  public double nikkoUSD   = 0;
		@Sheet.ColumnName("SBI証券ドル")   public double sbiUSD     = 0;
		
		public CompanyReport(
			LocalDate date,
			
			double totalJPY,
			double sonyJPY,
			double smbcJPY,
			double prestiaJPY,
			double smtbJPY,
			double rakutenJPY,
			double nikkoJPY,
			double sbiJPY,
			
			double totalUSD,
			double sonyUSD,
			double smbcUSD,
			double prestiaUSD,
			double smtbUSD,
			double rakutenUSD,
			double nikkoUSD,
			double sbiUSD
			) {
			this.date    = date.toString();
			
			this.totalJPY   = totalJPY;
			this.sonyJPY    = sonyJPY;
			this.smbcJPY    = smbcJPY;
			this.prestiaJPY = prestiaJPY;
			this.smtbJPY    = smtbJPY;
			this.rakutenJPY = rakutenJPY;
			this.nikkoJPY   = nikkoJPY;
			this.sbiJPY     = sbiJPY;
			
			this.totalUSD   = totalUSD;
			this.sonyUSD    = sonyUSD;
			this.smbcUSD    = smbcUSD;
			this.prestiaUSD = prestiaUSD;
			this.smtbUSD    = smtbUSD;
			this.rakutenUSD = rakutenUSD;
			this.nikkoUSD   = nikkoUSD;
			this.sbiUSD     = sbiUSD;
		}

		@Override
		public int compareTo(CompanyReport that) {
			return this.date.compareTo(that.date);
		}
		@Override
		public String toString() {
			return StringUtil.toString(this);
		}
	}
	
	@Sheet.SheetName("商品レポート")
	@Sheet.HeaderRow(0)
	@Sheet.DataRow(1)
	public static class ProductReport extends Sheet implements Comparable<ProductReport> {
		@Sheet.ColumnName("日付")         public String date        = "";
		
		@Sheet.ColumnName("円資産")       public double totalJPY    = 0;
		@Sheet.ColumnName("円普通預金")   public double depositJPY  = 0;
		@Sheet.ColumnName("円定期預金")   public double timeJPY     = 0;
		@Sheet.ColumnName("円投資信託")   public double fundJPY     = 0;
		@Sheet.ColumnName("円株式")       public double stockJPY    = 0;
		
		@Sheet.ColumnName("ドル資産")     public double totalUSD    = 0;
		@Sheet.ColumnName("ドル普通預金") public double depositUSD  = 0;
		@Sheet.ColumnName("ドル定期預金") public double timeUSD     = 0;
		@Sheet.ColumnName("ドルMMF")      public double mmfUSD      = 0;
		@Sheet.ColumnName("ドル投資信託") public double fundUSD     = 0;
		@Sheet.ColumnName("ドル株式")     public double stockUSD    = 0;
		@Sheet.ColumnName("ドル債権")     public double bondUSD     = 0;
		
		public ProductReport(
			LocalDate date,
			
			double totalJPY,
			double depositJPY,
			double timeJPY,
			double fundJPY,
			double stockJPY,
			
			double totalUSD,
			double depositUSD,
			double timeUSD,
			double mmfUSD,
			double fundUSD,
			double stockUSD,
			double bondUSD
			) {
			this.date        = date.toString();
			
			this.totalJPY    = totalJPY;
			this.depositJPY  = depositJPY;
			this.timeJPY     = timeJPY;
			this.fundJPY     = fundJPY;
			this.stockJPY    = stockJPY;
			
			this.totalUSD    = totalUSD;
			this.depositUSD  = depositUSD;
			this.timeUSD     = timeUSD;
			this.mmfUSD      = mmfUSD;
			this.fundUSD     = fundUSD;
			this.stockUSD    = stockUSD;
			this.bondUSD     = bondUSD;
		}

		@Override
		public int compareTo(ProductReport that) {
			return this.date.compareTo(that.date);
		}
		@Override
		public String toString() {
			return StringUtil.toString(this);
		}
	}
	
	@Sheet.SheetName("概要レポート")
	@Sheet.HeaderRow(0)
	@Sheet.DataRow(1)
	public static class GeneralReport extends Sheet implements Comparable<GeneralReport> {
		@Sheet.ColumnName("日付")       public String date    = "";
		@Sheet.ColumnName("合計")       public double total   = 0;
		@Sheet.ColumnName("円資産")     public double jpy     = 0;
		@Sheet.ColumnName("ドル資産")   public double usd     = 0;
		@Sheet.ColumnName("ドルレート") public double usdRate = 0;
		@Sheet.ColumnName("ドル資産円") public double usdJPY  = 0;
		@Sheet.ColumnName("安全資産")   public double safe    = 0;
		@Sheet.ColumnName("非安全資産") public double unsafe  = 0;
		
		public GeneralReport(
			LocalDate date,
			double total,
			double jpy,
			double usd,
			double usdRate,
			double usdJPY,
			double safe,
			double unsafe
			) {
			this.date    = date.toString();
			this.total   = total;
			this.jpy     = jpy;
			this.usd     = usd;
			this.usdRate = usdRate;
			this.usdJPY  = usdJPY;
			this.safe    = safe;
			this.unsafe  = unsafe;
		}

		@Override
		public int compareTo(GeneralReport that) {
			return this.date.compareTo(that.date);
		}
		@Override
		public String toString() {
			return StringUtil.toString(this);
		}
	}
}
