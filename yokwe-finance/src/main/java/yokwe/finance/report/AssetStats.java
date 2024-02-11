package yokwe.finance.report;

import java.time.LocalDate;

import yokwe.util.StringUtil;
import yokwe.util.libreoffice.Sheet;

public class AssetStats {
	@Sheet.SheetName("会社ー概要レポート")
	@Sheet.HeaderRow(0)
	@Sheet.DataRow(1)
	public static class CompanyGeneralReport extends Sheet implements Comparable<CompanyGeneralReport> {
		@Sheet.ColumnName("会社")       public String company = "";
		@Sheet.ColumnName("合計")       public double total   = 0;
		@Sheet.ColumnName("円資産")     public double jpy     = 0;
		@Sheet.ColumnName("ドル資産円") public double usdJPY  = 0;
		@Sheet.ColumnName("ドル資産")   public double usd     = 0;
		@Sheet.ColumnName("安全資産")   public double safe    = 0;
		@Sheet.ColumnName("非安全資産") public double unsafe  = 0;
		@Sheet.ColumnName("預金")       public double deposit = 0;
		@Sheet.ColumnName("定期預金")   public double term    = 0;
		@Sheet.ColumnName("投資信託")   public double fund    = 0;
		@Sheet.ColumnName("株式")       public double stock   = 0;
		@Sheet.ColumnName("債権")       public double bond    = 0;

		public CompanyGeneralReport(
			String company,
			double total,
			double jpy,
			double usdJPY,
			double usd,
			double safe,
			double unsafe,
			double deposit,
			double term,
			double fund,
			double stock,
			double bond
			) {
			this.company = company;
			this.total   = total;
			this.jpy     = jpy;
			this.usdJPY  = usdJPY;
			this.usd     = usd;
			this.safe    = safe;
			this.unsafe  = unsafe;
			this.deposit = deposit;
			this.term    = term;
			this.fund    = fund;
			this.stock   = stock;
			this.bond    = bond;
		}

		@Override
		public int compareTo(CompanyGeneralReport that) {
			return this.company.compareTo(that.company);
		}
		@Override
		public String toString() {
			return StringUtil.toString(this);
		}
	}
	@Sheet.SheetName("会社ー概要ー割合レポート")
	@Sheet.HeaderRow(0)
	@Sheet.DataRow(1)
	public static class CompanyGeneraPercentReport extends Sheet implements Comparable<CompanyGeneraPercentReport> {
		@Sheet.ColumnName("会社")       public String company = "";
		@Sheet.ColumnName("合計")       public double total   = 0;
		@Sheet.ColumnName("円資産")     public double jpy     = 0;
		@Sheet.ColumnName("ドル資産円") public double usdJPY  = 0;
		@Sheet.ColumnName("ドル資産")   public double usd     = 0;
		@Sheet.ColumnName("安全資産")   public double safe    = 0;
		@Sheet.ColumnName("非安全資産") public double unsafe  = 0;
		@Sheet.ColumnName("預金")       public double deposit = 0;
		@Sheet.ColumnName("定期預金")   public double term    = 0;
		@Sheet.ColumnName("投資信託")   public double fund    = 0;
		@Sheet.ColumnName("株式")       public double stock   = 0;
		@Sheet.ColumnName("債権")       public double bond    = 0;

		public CompanyGeneraPercentReport(
			String company,
			double total,
			double jpy,
			double usdJPY,
			double usd,
			double safe,
			double unsafe,
			double deposit,
			double term,
			double fund,
			double stock,
			double bond
			) {
			this.company = company;
			this.total   = total;
			this.jpy     = jpy;
			this.usdJPY  = usdJPY;
			this.usd     = usd;
			this.safe    = safe;
			this.unsafe  = unsafe;
			this.deposit = deposit;
			this.term    = term;
			this.fund    = fund;
			this.stock   = stock;
			this.bond    = bond;
		}

		@Override
		public int compareTo(CompanyGeneraPercentReport that) {
			return this.company.compareTo(that.company);
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
	@Sheet.SheetName("会社レポート円")
	@Sheet.HeaderRow(0)
	@Sheet.DataRow(1)
	public static class CompanyReportJPY extends Sheet implements Comparable<CompanyReportJPY> {
		@Sheet.ColumnName("日付")      public String date    = "";
		
		@Sheet.ColumnName("合計")        public double total   = 0;
		@Sheet.ColumnName("ソニー")      public double sony    = 0;
		@Sheet.ColumnName("三井住友")    public double smbc    = 0;
		@Sheet.ColumnName("PRESTIA")     public double prestia = 0;
		@Sheet.ColumnName("SMTB")        public double smtb    = 0;
		@Sheet.ColumnName("楽天証券")    public double rakuten = 0;
		@Sheet.ColumnName("日興証券")    public double nikko   = 0;
		@Sheet.ColumnName("SBI証券")     public double sbi     = 0;
		
		public CompanyReportJPY(
			LocalDate date,
			
			double total,
			double sony,
			double smbc,
			double prestia,
			double smtb,
			double rakuten,
			double nikko,
			double sbi
			) {
			this.date    = date.toString();
			
			this.total   = total;
			this.sony    = sony;
			this.smbc    = smbc;
			this.prestia = prestia;
			this.smtb    = smtb;
			this.rakuten = rakuten;
			this.nikko   = nikko;
			this.sbi     = sbi;
		}

		@Override
		public int compareTo(CompanyReportJPY that) {
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
		@Sheet.ColumnName("円預金")       public double depositJPY  = 0;
		@Sheet.ColumnName("円定期預金")   public double timeJPY     = 0;
		@Sheet.ColumnName("円投資信託")   public double fundJPY     = 0;
		@Sheet.ColumnName("円株式")       public double stockJPY    = 0;
		@Sheet.ColumnName("円債権")       public double bondJPY     = 0;
		
		@Sheet.ColumnName("ドル資産")     public double totalUSD    = 0;
		@Sheet.ColumnName("ドル預金")     public double depositUSD  = 0;
		@Sheet.ColumnName("ドル定期預金") public double timeUSD     = 0;
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
			double bondJPY,
			
			double totalUSD,
			double depositUSD,
			double timeUSD,
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
			this.bondJPY     = bondJPY;
			
			this.totalUSD    = totalUSD;
			this.depositUSD  = depositUSD;
			this.timeUSD     = timeUSD;
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
	@Sheet.SheetName("商品レポート円")
	@Sheet.HeaderRow(0)
	@Sheet.DataRow(1)
	public static class ProductReportJPY extends Sheet implements Comparable<ProductReportJPY> {
		@Sheet.ColumnName("日付")       public String date        = "";
		
		@Sheet.ColumnName("合計")       public double total    = 0;
		@Sheet.ColumnName("預金")   	public double deposit  = 0;
		@Sheet.ColumnName("定期預金")   public double time     = 0;
		@Sheet.ColumnName("投資信託")   public double fund     = 0;
		@Sheet.ColumnName("株式")       public double stock    = 0;
		@Sheet.ColumnName("債権")       public double bond     = 0;
		
		public ProductReportJPY(
			LocalDate date,
			
			double total,
			double deposit,
			double time,
			double fund,
			double stock,
			double bond
			) {
			this.date        = date.toString();
			
			this.total    = total;
			this.deposit  = deposit;
			this.time     = time;
			this.fund     = fund;
			this.stock    = stock;
			this.bond     = bond;
		}

		@Override
		public int compareTo(ProductReportJPY that) {
			return this.date.compareTo(that.date);
		}
		@Override
		public String toString() {
			return StringUtil.toString(this);
		}
	}
}
