package yokwe.finance.report;

import java.time.LocalDate;

import yokwe.util.StringUtil;
import yokwe.util.libreoffice.Sheet;

public class AssetStats {
	
	@Sheet.SheetName("サマリー会社")
	@Sheet.HeaderRow(0)
	@Sheet.DataRow(1)
	public static class SummaryCompany extends Sheet implements Comparable<SummaryCompany> {
		@Sheet.ColumnName("日付")      public String date    = "";
		@Sheet.ColumnName("合計")      public double total   = 0;
		@Sheet.ColumnName("ソニー")    public double sony    = 0;
		@Sheet.ColumnName("三井住友")  public double smbc    = 0;
		@Sheet.ColumnName("PRESTIA")   public double prestia = 0;
		@Sheet.ColumnName("SMTB")      public double smtb    = 0;
		@Sheet.ColumnName("楽天証券")  public double rakuten = 0;
		@Sheet.ColumnName("日興証券")  public double nikko   = 0;
		@Sheet.ColumnName("SBI証券")   public double sbi     = 0;
		
		public SummaryCompany(
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
		public int compareTo(SummaryCompany that) {
			return this.date.compareTo(that.date);
		}
		@Override
		public String toString() {
			return StringUtil.toString(this);
		}
	}
	
	@Sheet.SheetName("サマリー商品")
	@Sheet.HeaderRow(0)
	@Sheet.DataRow(1)
	public static class SummaryProduct extends Sheet implements Comparable<SummaryProduct> {
		@Sheet.ColumnName("日付")         public String date        = "";
		@Sheet.ColumnName("合計")         public double total       = 0;
		
		@Sheet.ColumnName("円資産")       public double totalJPY    = 0;
		@Sheet.ColumnName("円普通預金")   public double depositJPY  = 0;
		@Sheet.ColumnName("円定期預金")   public double timeJPY     = 0;
		@Sheet.ColumnName("円投資信託")   public double fundJPY     = 0;
		@Sheet.ColumnName("円株式")       public double stockJPY    = 0;
		
		@Sheet.ColumnName("ドル資産")     public double totalUSD    = 0;
		@Sheet.ColumnName("ドルレート")   public double rateUSD     = 0;
		@Sheet.ColumnName("ドル資産円")   public double totalUSDJPY = 0;
		@Sheet.ColumnName("ドル普通預金") public double depositUSD  = 0;
		@Sheet.ColumnName("ドル定期預金") public double timeUSD     = 0;
		@Sheet.ColumnName("ドルMMF")      public double mmfUSD      = 0;
		@Sheet.ColumnName("ドル投資信託") public double fundUSD     = 0;
		@Sheet.ColumnName("ドル株式")     public double stockUSD    = 0;
		@Sheet.ColumnName("ドル債権")     public double bondUSD     = 0;
		
		public SummaryProduct(
			LocalDate date,
			double total,
			double totalJPY,
			double depositJPY,
			double timeJPY,
			double fundJPY,
			double stockJPY,
			double totalUSD,
			double rateUSD,
			double totalUSDJPY,
			double depositUSD,
			double timeUSD,
			double mmfUSD,
			double fundUSD,
			double stockUSD,
			double bondUSD
			) {
			this.date        = date.toString();
			this.total       = total;
			this.totalJPY    = totalJPY;
			this.depositJPY  = depositJPY;
			this.timeJPY     = timeJPY;
			this.fundJPY     = fundJPY;
			this.stockJPY    = stockJPY;
			this.totalUSD    = totalUSD;
			this.rateUSD     = rateUSD;
			this.totalUSDJPY = totalUSDJPY;
			this.depositUSD  = depositUSD;
			this.timeUSD     = timeUSD;
			this.mmfUSD      = mmfUSD;
			this.fundUSD     = fundUSD;
			this.stockUSD    = stockUSD;
			this.bondUSD     = bondUSD;
		}

		@Override
		public int compareTo(SummaryProduct that) {
			return this.date.compareTo(that.date);
		}
		@Override
		public String toString() {
			return StringUtil.toString(this);
		}
	}
	
	@Sheet.SheetName("サマリーカテゴリ")
	@Sheet.HeaderRow(0)
	@Sheet.DataRow(1)
	public static class SummaryCategory extends Sheet implements Comparable<SummaryCategory> {
		@Sheet.ColumnName("日付")       public String date    = "";
		@Sheet.ColumnName("合計")       public double total   = 0;
		@Sheet.ColumnName("円資産")     public double jpy     = 0;
		@Sheet.ColumnName("ドル資産")   public double usd     = 0;
		@Sheet.ColumnName("ドルレート") public double usdRate = 0;
		@Sheet.ColumnName("ドル資産円") public double usdJPY  = 0;
		@Sheet.ColumnName("安全資産")   public double safe    = 0;
		@Sheet.ColumnName("非安全資産") public double unsafe  = 0;
		
		public SummaryCategory(
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
		public int compareTo(SummaryCategory that) {
			return this.date.compareTo(that.date);
		}
		@Override
		public String toString() {
			return StringUtil.toString(this);
		}
	}
}
