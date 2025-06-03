package yokwe.finance.trade.nikko;

import java.util.Arrays;

import yokwe.util.CSVUtil;
import yokwe.util.ToString;

public class Torireki {
	private static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();
	
	public enum ProductType {
		// ---, MRF・MMFなど, 償還金（外国債券）, 入出金, 分配金（外国投信）, 利金（外国債券）, 国内投資信託（累積投資）, 外国債券, 外国株式, 外貨建MMF
		NONE            ("---"),
		MRF             ("MRF・MMFなど"),
		REDEMPTION_BOND ("償還金（外国債券）"),
		WITHDRAW_DEPOSIT("入出金"),
		DIVIDEND_FUND   ("分配金（外国投信）"),
		DIVIDEND_BOND   ("利金（外国債券）"),
		FUND            ("国内投資信託（累積投資）"),
		FOREIGN_BOND    ("外国債券"),
		FOREIGN_STOCK   ("外国株式"),
		FOREIGN_MMF     ("外貨建MMF");
		
		public final String string;
		private ProductType(String string) {
			this.string = string;
		}
		
		@Override
		public String toString() {
			return string;
		}
	}

	public enum TradeType {
		DEPOSIT      ("入金"),
		WITHDRAW     ("出金"),
		TRANSFER     ("振替"),
		BALANCE      ("残高"),
		// stock
		RECEIVE_STOCK("入庫"),
		BUY          ("買付"),
		REINVESTMENT ("再投資"),
		SELL         ("売却"),
		DIVIDEND     ("分配金*"),
		// fund
		DIVIDNED_BOND("利金*"),
		REDEMPTION   ("償還金"),
		WITHDRAW_MMF ("解約"),
		;
		
		public final String string;
		private TradeType(String string) {
			this.string = string;
		}
		
		@Override
		public String toString() {
			return string;
		}
	}
	
	public enum AccountType {
		NONE   ("-"),
		SPECIAL("特定");
		
		public final String string;
		private AccountType(String string) {
			this.string = string;
		}
		
		@Override
		public String toString() {
			return string;
		}
	}

	
	//"受渡日","約定日","商品等","取引種類","銘柄名（ファンド名）","銘柄コード","口座","数量","単価","支払（出金）","預り（入金）","MRF・お預り金残高","摘要"
	//"25/05/13","25/05/12","外貨建MMF","買付","米ドルＭＭＦ","-","特定","37,319","0.01","373.19","---","---","外貨決済　通貨:USD"

	@CSVUtil.ColumnName("受渡日")               public String      settlementDate;  // 24/02/15
	@CSVUtil.ColumnName("約定日")               public String      tradeDate;       // 24/02/15 or ---
	@CSVUtil.ColumnName("商品等")               public ProductType productType;         // or ---
	@CSVUtil.ColumnName("取引種類")             public TradeType   tradeType;       // 
	@CSVUtil.ColumnName("銘柄名（ファンド名）") public String      name;            //
	@CSVUtil.ColumnName("銘柄コード")           public String      code;            // blank or -
	@CSVUtil.ColumnName("口座")                 public AccountType accountType;     // 特定 or -
	@CSVUtil.ColumnName("数量")                 public String      units;           // number or ---
	@CSVUtil.ColumnName("単価")                 public String      unitPrice;       // number or ---
	@CSVUtil.ColumnName("支払（出金）")         public String      withdraw;        // number or ---
	@CSVUtil.ColumnName("預り（入金）")         public String      deposit;         // number or ---
	@CSVUtil.ColumnName("MRF・お預り金残高")    public String      mrfBalance;      // number or ---
	@CSVUtil.ColumnName("摘要")                 public String      note;            // string or empty
	
	@Override
	public String toString() {
		return ToString.withFieldName(this);
	}
	@Override
	public boolean equals(Object o) {
		if (o instanceof Torireki) {
			Torireki that = (Torireki)o	;
			return
				this.settlementDate.equals(that.settlementDate) &&
				this.tradeDate.equals(that.tradeDate) &&
				this.productType.equals(that.productType) &&
				this.tradeType.equals(that.tradeType) &&
				this.name.equals(that.name) &&
				this.code.equals(that.code) &&
				this.accountType.equals(that.accountType) &&
				this.units.equals(that.units) &&
				this.unitPrice.equals(that.unitPrice) &&
				this.withdraw.equals(that.withdraw) &&
				this.deposit.equals(that.deposit) &&
				this.mrfBalance.equals(that.mrfBalance) &&
				this.note.equals(that.note);
		} else {
			return false;
		}
	}
	
	public static void main(String[] args) {
		logger.info("START");
		
		var files = StorageNikko.storage.getFile("download").listFiles((d, n) -> n.startsWith("Torireki") && n.endsWith(".csv"));
		Arrays.sort(files);
		for(var file: files) {
			var list = CSVUtil.read(Torireki.class).file(file);
			logger.info("load  {}  {}", list.size(), file.getPath());
		}
		
		logger.info("STOP");
	}
}
