package yokwe.finance.report;

import java.time.LocalDate;

import yokwe.util.StringUtil;
import yokwe.util.libreoffice.Sheet;

public class AssetStats {
	
	@Sheet.SheetName("会社")
	@Sheet.HeaderRow(0)
	@Sheet.DataRow(1)
	public static class Company extends Sheet implements Comparable<Company> {
		@Sheet.ColumnName("日付")        public String date       = "";
		@Sheet.ColumnName("合計")        public double total      = 0;
		@Sheet.ColumnName("円")          public double totalJPY   = 0;
		@Sheet.ColumnName("ソニー円")    public double sonyJPY    = 0;
		@Sheet.ColumnName("SMBC円")      public double smbcJPY    = 0;
		@Sheet.ColumnName("PRESTIA円")   public double prestiaJPY = 0;
		@Sheet.ColumnName("SMTB円")      public double smtbJPY    = 0;
		@Sheet.ColumnName("楽天円")      public double rakutenJPY = 0;
		@Sheet.ColumnName("日興円")      public double nikkoJPY   = 0;
		@Sheet.ColumnName("SBI円")       public double sbiJPY     = 0;
		@Sheet.ColumnName("ドル")        public double totalUSD   = 0;
		@Sheet.ColumnName("ソニードル")  public double sonyUSD    = 0;
		@Sheet.ColumnName("SMBCドル")    public double smbcUSD    = 0;
		@Sheet.ColumnName("PRESTIAドル") public double prestiaUSD = 0;
		@Sheet.ColumnName("SMTBドル")    public double smtbUSD    = 0;
		@Sheet.ColumnName("楽天ドル")    public double rakutenUSD = 0;
		@Sheet.ColumnName("日興ドル")    public double nikkoUSD   = 0;
		@Sheet.ColumnName("SBIドル")     public double sbiUSD     = 0;
		
		public Company(
				LocalDate date,
				double total,
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
				this.date       = date.toString();
				this.total      = total;
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
		public int compareTo(Company that) {
			return this.date.compareTo(that.date);
		}
	}
	@Sheet.SheetName("会社円")
	@Sheet.HeaderRow(0)
	@Sheet.DataRow(1)
	public static class CompanyJPY extends Sheet implements Comparable<CompanyJPY> {
		@Sheet.ColumnName("日付")        public String date       = "";
		@Sheet.ColumnName("合計")        public double total      = 0;
		@Sheet.ColumnName("円")          public double totalJPY   = 0;
		@Sheet.ColumnName("ソニー円")    public double sonyJPY    = 0;
		@Sheet.ColumnName("SMBC円")      public double smbcJPY    = 0;
		@Sheet.ColumnName("PRESTIA円")   public double prestiaJPY = 0;
		@Sheet.ColumnName("SMTB円")      public double smtbJPY    = 0;
		@Sheet.ColumnName("楽天円")      public double rakutenJPY = 0;
		@Sheet.ColumnName("日興円")      public double nikkoJPY   = 0;
		@Sheet.ColumnName("SBI円")       public double sbiJPY     = 0;
		@Sheet.ColumnName("ドル")        public double totalUSD   = 0;
		@Sheet.ColumnName("ソニードル")  public double sonyUSD    = 0;
		@Sheet.ColumnName("SMBCドル")    public double smbcUSD    = 0;
		@Sheet.ColumnName("PRESTIAドル") public double prestiaUSD = 0;
		@Sheet.ColumnName("SMTBドル")    public double smtbUSD    = 0;
		@Sheet.ColumnName("楽天ドル")    public double rakutenUSD = 0;
		@Sheet.ColumnName("日興ドル")    public double nikkoUSD   = 0;
		@Sheet.ColumnName("SBIドル")     public double sbiUSD     = 0;
		
		public CompanyJPY(
				LocalDate date,
				double total,
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
				this.date       = date.toString();
				this.total      = total;
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
		public int compareTo(CompanyJPY that) {
			return this.date.compareTo(that.date);
		}
	}
	
	
	@Sheet.SheetName("商品")
	@Sheet.HeaderRow(0)
	@Sheet.DataRow(1)
	public static class Product extends Sheet implements Comparable<Product> {
		@Sheet.ColumnName("日付")         public String date    = "";
		@Sheet.ColumnName("安全資産")     public double safe    = 0;
		@Sheet.ColumnName("リスク資産")   public double risk    = 0;
		@Sheet.ColumnName("合計")         public double total   = 0;
		//
		@Sheet.ColumnName("合計円")       public double totalJPY   = 0;
		@Sheet.ColumnName("預金円")       public double depositJPY = 0;
		@Sheet.ColumnName("定期円")       public double timeJPY    = 0;
		@Sheet.ColumnName("投資信託円")   public double fundJPY    = 0;
		@Sheet.ColumnName("株式円")       public double stockJPY   = 0;
		//
		@Sheet.ColumnName("合計ドル")     public double totalUSD   = 0;
		@Sheet.ColumnName("預金ドル")     public double depositUSD = 0;
		@Sheet.ColumnName("定期ドル")     public double timeUSD    = 0;
		@Sheet.ColumnName("MMFドル")      public double mmfUSD     = 0;
		@Sheet.ColumnName("投資信託ドル") public double fundUSD    = 0;
		@Sheet.ColumnName("株式ドル")     public double stockUSD   = 0;
		@Sheet.ColumnName("債権ドル")     public double bondUSD    = 0;
		
		public Product(
				LocalDate date,
				double safe,
				double risk,
				double total,
				
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
				this.date       = date.toString();
				this.safe       = safe;
				this.risk       = risk;
				this.total      = total;
				
				this.totalJPY   = totalJPY;
				this.depositJPY = depositJPY;
				this.timeJPY    = timeJPY;
				this.fundJPY    = fundJPY;
				this.stockJPY   = stockJPY;
				
				this.totalUSD   = totalUSD;
				this.depositUSD = depositUSD;
				this.timeUSD    = timeUSD;
				this.mmfUSD     = mmfUSD;
				this.fundUSD    = fundUSD;
				this.stockUSD   = stockUSD;
				this.bondUSD    = bondUSD;
			}

		@Override
		public int compareTo(Product that) {
			return this.date.compareTo(that.date);
		}
	}
	@Sheet.SheetName("商品円")
	@Sheet.HeaderRow(0)
	@Sheet.DataRow(1)
	public static class ProductJPY extends Sheet implements Comparable<ProductJPY> {
		@Sheet.ColumnName("日付")         public String date    = "";
		@Sheet.ColumnName("安全資産")     public double safe    = 0;
		@Sheet.ColumnName("リスク資産")   public double risk    = 0;
		@Sheet.ColumnName("合計")         public double total   = 0;
		//
		@Sheet.ColumnName("合計円")       public double totalJPY   = 0;
		@Sheet.ColumnName("預金円")       public double depositJPY = 0;
		@Sheet.ColumnName("定期円")       public double timeJPY    = 0;
		@Sheet.ColumnName("投資信託円")   public double fundJPY    = 0;
		@Sheet.ColumnName("株式円")       public double stockJPY   = 0;
		//
		@Sheet.ColumnName("合計ドル")     public double totalUSD   = 0;
		@Sheet.ColumnName("預金ドル")     public double depositUSD = 0;
		@Sheet.ColumnName("定期ドル")     public double timeUSD    = 0;
		@Sheet.ColumnName("MMFドル")      public double mmfUSD     = 0;
		@Sheet.ColumnName("投資信託ドル") public double fundUSD    = 0;
		@Sheet.ColumnName("株式ドル")     public double stockUSD   = 0;
		@Sheet.ColumnName("債権ドル")     public double bondUSD    = 0;
		
		public ProductJPY(
				LocalDate date,
				double safe,
				double risk,
				double total,
				
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
				this.date       = date.toString();
				this.safe       = safe;
				this.risk       = risk;
				this.total      = total;
				
				this.totalJPY   = totalJPY;
				this.depositJPY = depositJPY;
				this.timeJPY    = timeJPY;
				this.fundJPY    = fundJPY;
				this.stockJPY   = stockJPY;
				
				this.totalUSD   = totalUSD;
				this.depositUSD = depositUSD;
				this.timeUSD    = timeUSD;
				this.mmfUSD     = mmfUSD;
				this.fundUSD    = fundUSD;
				this.stockUSD   = stockUSD;
				this.bondUSD    = bondUSD;
			}

		@Override
		public int compareTo(ProductJPY that) {
			return this.date.compareTo(that.date);
		}
	}
}
