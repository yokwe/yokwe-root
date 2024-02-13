package yokwe.finance.report;

import yokwe.util.StringUtil;
import yokwe.util.libreoffice.Sheet;

public class AssetStats {
	@Sheet.HeaderRow(0)
	@Sheet.DataRow(1)
	public static class DailyCompanyOverviewReport extends Sheet {
		public static final String SHEET_NAME_VALUE   = "日付ー会社ー概要ー金額";
		public static final String SHEET_NAME_PERCENT = "日付ー会社ー概要ー割合";
		
		@Sheet.ColumnName("日付")       public String date    = "";
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
		
		public DailyCompanyOverviewReport() {}
		public DailyCompanyOverviewReport(
			String date,
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
			this.date    = date;
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
		public void add(DailyCompanyOverviewReport that) {
			this.total   += that.total;
			this.jpy     += that.jpy;
			this.usdJPY  += that.usdJPY;
			this.usd     += that.usd;
			this.safe    += that.safe;
			this.unsafe  += that.unsafe;
			this.deposit += that.deposit;
			this.term    += that.term;
			this.fund    += that.fund;
			this.stock   += that.stock;
			this.bond    += that.bond;
		}
		public DailyCompanyOverviewReport percent(double grandTotal) {
			var ret = new DailyCompanyOverviewReport();
			ret.date    = this.date;
			ret.company = this.company;
			ret.total   = this.total   / grandTotal;
			
			ret.jpy     = this.jpy     / grandTotal;
			ret.usdJPY  = this.usdJPY  / grandTotal;
			ret.usd     = 0;
			ret.safe    = this.safe    / grandTotal;
			ret.unsafe  = this.unsafe  / grandTotal;
			ret.deposit = this.deposit / grandTotal;
			ret.term    = this.term    / grandTotal;
			ret.fund    = this.fund    / grandTotal;
			ret.stock   = this.stock   / grandTotal;
			ret.bond    = this.bond    / grandTotal;
			
			return ret;
		}
		@Override
		public String toString() {
			return StringUtil.toString(this);
		}
	}
	@Sheet.HeaderRow(0)
	@Sheet.DataRow(1)
	public static class DailyCompanyProductReport extends Sheet {
		public static final String SHEET_NAME_VALUE   = "日付ー会社ー商品ー金額";
		public static final String SHEET_NAME_PERCENT = "日付ー会社ー商品ー割合";
		
		@Sheet.ColumnName("日付")         public String date        = "";
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
		
		public DailyCompanyProductReport() {}
		
		public DailyCompanyProductReport(
			String date,
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
			this.date        = date;
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
		public void add(DailyCompanyProductReport that) {
			this.total       += that.total;
			
			this.totalJPY    += that.totalJPY;
			this.depositJPY  += that.depositJPY;
			this.termJPY     += that.termJPY;
			this.fundJPY     += that.fundJPY;
			this.stockJPY    += that.stockJPY;
			this.bondJPY     += that.bondJPY;
			
			this.totalUSD    += that.totalUSD;
			this.depositUSD  += that.depositUSD;
			this.termUSD     += that.termUSD;
			this.fundUSD     += that.fundUSD;
			this.stockUSD    += that.stockUSD;
			this.bondUSD     += that.bondUSD;
		}
		public DailyCompanyProductReport percent(double grandTotal, double usdRate) {
			var ret = new DailyCompanyProductReport();
			ret.date       = this.date;
			ret.company    = this.company;
			ret.total      = this.total / grandTotal;
			
			ret.totalJPY   = this.totalJPY   / grandTotal;
			ret.depositJPY = this.depositJPY / grandTotal;
			ret.termJPY    = this.termJPY    / grandTotal;
			ret.fundJPY    = this.fundJPY    / grandTotal;
			ret.stockJPY   = this.stockJPY   / grandTotal;
			ret.bondJPY    = this.bondJPY    / grandTotal;
			
			ret.totalUSD   = this.totalUSD   * usdRate / grandTotal;
			ret.depositUSD = this.depositUSD * usdRate / grandTotal;
			ret.termUSD    = this.termUSD    * usdRate / grandTotal;
			ret.fundUSD    = this.fundUSD    * usdRate / grandTotal;
			ret.stockUSD   = this.stockUSD   * usdRate / grandTotal;
			ret.bondUSD    = this.bondUSD    * usdRate / grandTotal;
			
			return ret;
		}
		@Override
		public String toString() {
			return StringUtil.toString(this);
		}
	}
	@Sheet.HeaderRow(0)
	@Sheet.DataRow(1)
	public static class DailyTotalCompanyReport extends Sheet {
		public static final String SHEET_NAME_VALUE   = "日付ー合計ー会社ー金額";
		public static final String SHEET_NAME_PERCENT = "日付ー合計ー会社ー割合";
		
		@Sheet.ColumnName("日付")        public String date    = "";
		@Sheet.ColumnName("合計")        public double total   = 0;
		
		@Sheet.ColumnName("ソニー")      public double sony    = 0;
		@Sheet.ColumnName("三井住友")    public double smbc    = 0;
		@Sheet.ColumnName("PRESTIA")     public double prestia = 0;
		@Sheet.ColumnName("SMTB")        public double smtb    = 0;
		@Sheet.ColumnName("楽天証券")    public double rakuten = 0;
		@Sheet.ColumnName("日興証券")    public double nikko   = 0;
		@Sheet.ColumnName("SBI証券")     public double sbi     = 0;
		
		public DailyTotalCompanyReport(
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
		public DailyTotalCompanyReport() {}
		public DailyTotalCompanyReport percent(double grandTotal) {
			var ret = new DailyTotalCompanyReport();
			ret.date    = this.date;
			ret.total   = this.total   / grandTotal;
			
			ret.sony    = this.sony    / grandTotal;
			ret.smbc    = this.smbc    / grandTotal;
			ret.prestia = this.prestia / grandTotal;
			ret.smtb    = this.smtb    / grandTotal;
			ret.rakuten = this.rakuten / grandTotal;
			ret.nikko   = this.nikko   / grandTotal;
			ret.sbi     = this.sbi     / grandTotal;
			
			return ret;
		}
		@Override
		public String toString() {
			return StringUtil.toString(this);
		}
	}
}
