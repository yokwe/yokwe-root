package yokwe.finance.report;

import yokwe.util.StringUtil;
import yokwe.util.libreoffice.Sheet;

public class AssetStats {
	@Sheet.HeaderRow(0)
	@Sheet.DataRow(1)
	public static class CompanyGeneralReport extends Sheet {
		public static final String SHEET_NAME_VALUE   = "会社ー概要ー金額";
		public static final String SHEET_NAME_PERCENT = "会社ー概要ー割合";
		
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
		public String toString() {
			return StringUtil.toString(this);
		}
	}
	@Sheet.HeaderRow(0)
	@Sheet.DataRow(1)
	public static class DailyCompanyReport extends Sheet {
		public static final String SHEET_NAME_VALUE   = "日付ー会社ー金額";
		public static final String SHEET_NAME_PERCENT = "日付ー会社ー割合";
		
		@Sheet.ColumnName("日付")        public String date    = "";
		
		@Sheet.ColumnName("合計")        public double total   = 0;
		@Sheet.ColumnName("ソニー")      public double sony    = 0;
		@Sheet.ColumnName("三井住友")    public double smbc    = 0;
		@Sheet.ColumnName("PRESTIA")     public double prestia = 0;
		@Sheet.ColumnName("SMTB")        public double smtb    = 0;
		@Sheet.ColumnName("楽天証券")    public double rakuten = 0;
		@Sheet.ColumnName("日興証券")    public double nikko   = 0;
		@Sheet.ColumnName("SBI証券")     public double sbi     = 0;
		
		public DailyCompanyReport(
			String date,
			
			double total,
			double sony,
			double smbc,
			double prestia,
			double smtb,
			double rakuten,
			double nikko,
			double sbi
			) {
			this.date    = date;
			
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
		public String toString() {
			return StringUtil.toString(this);
		}
	}
	@Sheet.HeaderRow(0)
	@Sheet.DataRow(1)
	public static class CompanyProductReport extends Sheet {
		public static final String SHEET_NAME_VALUE   = "会社ー商品ー金額";
		public static final String SHEET_NAME_PERCENT = "会社ー商品ー割合";
		
		@Sheet.ColumnName("会社")         public String company     = "";
		@Sheet.ColumnName("合計")         public double total       = 0;
		
		@Sheet.ColumnName("円資産")       public double totalJPY    = 0;
		@Sheet.ColumnName("円預金")       public double depositJPY  = 0;
		@Sheet.ColumnName("円定期預金")   public double termJPY     = 0;
		@Sheet.ColumnName("円投資信託")   public double fundJPY     = 0;
		@Sheet.ColumnName("円株式")       public double stockJPY    = 0;
		@Sheet.ColumnName("円債権")       public double bondJPY     = 0;
		
		@Sheet.ColumnName("ドル資産")     public double totalUSD    = 0;
		@Sheet.ColumnName("ドル預金")     public double depositUSD  = 0;
		@Sheet.ColumnName("ドル定期預金") public double termUSD     = 0;
		@Sheet.ColumnName("ドル投資信託") public double fundUSD     = 0;
		@Sheet.ColumnName("ドル株式")     public double stockUSD    = 0;
		@Sheet.ColumnName("ドル債権")     public double bondUSD     = 0;
		
		public CompanyProductReport(
			String company,
			double total,
			
			double totalJPY,
			double depositJPY,
			double termJPY,
			double fundJPY,
			double stockJPY,
			double bondJPY,
			
			double totalUSD,
			double depositUSD,
			double termUSD,
			double fundUSD,
			double stockUSD,
			double bondUSD
			) {
			this.company     = company;
			this.total       = total;
			
			this.totalJPY    = totalJPY;
			this.depositJPY  = depositJPY;
			this.termJPY     = termJPY;
			this.fundJPY     = fundJPY;
			this.stockJPY    = stockJPY;
			this.bondJPY     = bondJPY;
			
			this.totalUSD    = totalUSD;
			this.depositUSD  = depositUSD;
			this.termUSD     = termUSD;
			this.fundUSD     = fundUSD;
			this.stockUSD    = stockUSD;
			this.bondUSD     = bondUSD;
		}
		@Override
		public String toString() {
			return StringUtil.toString(this);
		}
	}
	@Sheet.HeaderRow(0)
	@Sheet.DataRow(1)
	public static class DailyProductReport extends Sheet {
		public static final String SHEET_NAME_VALUE   = "日付ー商品ー金額";
		public static final String SHEET_NAME_PERCENT = "日付ー商品ー割合";
		
		@Sheet.ColumnName("日付")         public String date        = "";
		@Sheet.ColumnName("合計")         public double total       = 0;
		
		@Sheet.ColumnName("円資産")       public double totalJPY    = 0;
		@Sheet.ColumnName("円預金")       public double depositJPY  = 0;
		@Sheet.ColumnName("円定期預金")   public double termJPY     = 0;
		@Sheet.ColumnName("円投資信託")   public double fundJPY     = 0;
		@Sheet.ColumnName("円株式")       public double stockJPY    = 0;
		@Sheet.ColumnName("円債権")       public double bondJPY     = 0;
		
		@Sheet.ColumnName("ドル資産")     public double totalUSD    = 0;
		@Sheet.ColumnName("ドル預金")     public double depositUSD  = 0;
		@Sheet.ColumnName("ドル定期預金") public double termUSD     = 0;
		@Sheet.ColumnName("ドル投資信託") public double fundUSD     = 0;
		@Sheet.ColumnName("ドル株式")     public double stockUSD    = 0;
		@Sheet.ColumnName("ドル債権")     public double bondUSD     = 0;
		
		public DailyProductReport(
			String date,
			double total,
			
			double totalJPY,
			double depositJPY,
			double termJPY,
			double fundJPY,
			double stockJPY,
			double bondJPY,
			
			double totalUSD,
			double depositUSD,
			double termUSD,
			double fundUSD,
			double stockUSD,
			double bondUSD
			) {
			this.date        = date;
			this.total       = total;
			
			this.totalJPY    = totalJPY;
			this.depositJPY  = depositJPY;
			this.termJPY     = termJPY;
			this.fundJPY     = fundJPY;
			this.stockJPY    = stockJPY;
			this.bondJPY     = bondJPY;
			
			this.totalUSD    = totalUSD;
			this.depositUSD  = depositUSD;
			this.termUSD     = termUSD;
			this.fundUSD     = fundUSD;
			this.stockUSD    = stockUSD;
			this.bondUSD     = bondUSD;
		}
		@Override
		public String toString() {
			return StringUtil.toString(this);
		}
	}
}
