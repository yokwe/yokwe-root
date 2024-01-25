package yokwe.finance.report;

import java.time.LocalDate;

import yokwe.util.StringUtil;
import yokwe.util.libreoffice.Sheet;

public class AssetStats {
	
	@Sheet.SheetName("SUMMARY_PRODUCT")
	@Sheet.HeaderRow(0)
	@Sheet.DataRow(1)
	public static class SummaryProduct extends Sheet implements Comparable<SummaryProduct> {
		@Sheet.ColumnName("日付")        public String date    = "";
		
		@Sheet.ColumnName("日本円")      public double jpy     = 0;
		@Sheet.ColumnName("米ドル")      public double usd     = 0;
		@Sheet.ColumnName("合計")        public double total   = 0;
		@Sheet.ColumnName("安全資産")    public double safe    = 0;
		@Sheet.ColumnName("リスク資産")  public double risk    = 0;
		@Sheet.ColumnName("預金")        public double deposit = 0;
		@Sheet.ColumnName("定期預金")    public double time    = 0;
		@Sheet.ColumnName("MMF")         public double mmf     = 0;
		@Sheet.ColumnName("投資信託")    public double fund    = 0;
		@Sheet.ColumnName("債権")        public double bond    = 0;
		@Sheet.ColumnName("株式")        public double stock   = 0;
		
		public SummaryProduct(
			LocalDate date,
			double jpy,
			double usd,
			double total,
			double safe,
			double risk,
			double deposit,
			double time,
			double mmf,
			double fund,
			double bond,
			double stock
			) {
			this.date    = date.toString();
			this.jpy     = jpy;
			this.usd     = usd;
			this.total   = total;
			this.safe    = safe;
			this.risk    = risk;
			this.deposit = deposit;
			this.time    = time;
			this.mmf     = mmf;
			this.fund    = fund;
			this.bond    = bond;
			this.stock   = stock;
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
	
	// ソニー銀行	三井住友銀行	プレスティア	三井住友信託	楽天証券	日興証券	SBI証券
	@Sheet.SheetName("SUMMARY_COMPANY")
	@Sheet.HeaderRow(0)
	@Sheet.DataRow(1)
	public static class SummaryCompany extends Sheet implements Comparable<SummaryCompany> {
		@Sheet.ColumnName("日付")        public String date        = "";
		
		@Sheet.ColumnName("日本円")        public double jpy     = 0;
		@Sheet.ColumnName("米ドル")        public double usd     = 0;
		@Sheet.ColumnName("合計")          public double total   = 0;
		@Sheet.ColumnName("ソニー銀行")    public double sony    = 0;
		@Sheet.ColumnName("三井住友銀行")  public double smbc    = 0;
		@Sheet.ColumnName("プレスティア")  public double prestia = 0;
		@Sheet.ColumnName("三井住友信託")  public double smtb    = 0;
		@Sheet.ColumnName("楽天証券")      public double rakuten = 0;
		@Sheet.ColumnName("日興証券")      public double nikko   = 0;
		@Sheet.ColumnName("SBI証券")       public double sbi     = 0;
		
		public SummaryCompany(
			LocalDate date,
			double jpy,
			double usd,
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
			this.jpy     = jpy;
			this.usd     = usd;
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

}
