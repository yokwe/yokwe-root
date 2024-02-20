package yokwe.finance.account.nikko;

import java.io.File;
import java.math.BigDecimal;
import java.time.LocalDate;

import yokwe.util.CSVUtil;
import yokwe.util.StringUtil;
import yokwe.util.UnexpectedException;

public class Torireki {
	private static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();
	
	public enum Trade {
		REDEMPTION   ("償還金"),
		RECEIVE_STOCK("入庫"),
		DEPOSIT      ("入金"),
		REINVESTMENT ("再投資"),
		WITHDRAW     ("出金"),
		DIVIDEND     ("分配金*"),
		DIVIDNED_BOND("利金*"),
		SELL         ("売却"),
		BALANCE      ("残高"),
		WITHDRAW_MMF ("解約"),
		BUY          ("買付");
		
		public static Trade getInstance(String string) {
			for(var e: values()) {
				if (e.string.equals(string)) return e;
			}
			logger.error("Unexpected string");
			logger.error("  string  {}!", string);
			throw new UnexpectedException("Unexpected string");
		}
		
		public final String string;
		private Trade(String string) {
			this.string = string;
		}
		public String toString() {
			return string;
		}
	}
	
	public enum Product {
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
		
		public static Product getInstance(String string) {
			for(var e: values()) {
				if (e.string.equals(string)) return e;
			}
			logger.error("Unexpected string");
			logger.error("  string  {}!", string);
			throw new UnexpectedException("Unexpected string");
		}
		
		public final String string;
		private Product(String string) {
			this.string = string;
		}
		public String toString() {
			return string;
		}
	}
	
	public static final LocalDate NO_DATE = LocalDate.EPOCH;
	public static LocalDate toLocalDate(String string) {
		if (string.equals("---")) return NO_DATE;
		// 24/02/15
		// 01234567
		if (string.length() == 8 && string.charAt(2) == '/' && string.charAt(5) == '/') {
			String yy = string.substring(0, 2);
			String mm = string.substring(3, 5);
			String dd = string.substring(6, 8);
			String dateString = "20" + yy + "-" + mm + "-" + dd;
			return LocalDate.parse(dateString);
		}
		logger.error("Unexpected string");
		logger.error("  string  {}!", string);
		throw new UnexpectedException("Unexpected string");
	}
	
	public static final BigDecimal NO_NUMBER = BigDecimal.valueOf(-1);
	public static BigDecimal toBigDecimal(String string) {
		if (string.equals("---")) return NO_NUMBER;
		return new BigDecimal(string);
	}
	
	public static final String ACCOUNT_TYPE_TOKUTEI = "特定";
	
	public static final String NOTE_USE_USD = "外貨決済　通貨:USD";
	public static final String NOTE_USE_JPY = "円貨決済　通貨:USD";
	public static boolean useUSD(String note) {
		return note.equals(NOTE_USE_USD);
	}
	public static boolean useJPY(String note) {
		return note.startsWith(NOTE_USE_JPY);
	}
	
	// "受渡日","約定日","商品等","取引種類","銘柄名（ファンド名）","銘柄コード","口座","数量","単価","支払（出金）","預り（入金）","MRF・お預り金残高","摘要"
	@CSVUtil.ColumnName("受渡日")               public String  settlementDate;  // 24/02/15
	@CSVUtil.ColumnName("約定日")               public String  tradeDate;       // 24/02/15 or ---
	@CSVUtil.ColumnName("商品等")               public Product product;         // or ---
	@CSVUtil.ColumnName("取引種類")             public Trade   trade;           // 
	@CSVUtil.ColumnName("銘柄名（ファンド名）") public String  name;            //
	@CSVUtil.ColumnName("銘柄コード")           public String  code;            // blank or -
	@CSVUtil.ColumnName("口座")                 public String  accountType;     // 特定 or -
	@CSVUtil.ColumnName("数量")                 public String  units;           // number or ---
	@CSVUtil.ColumnName("単価")                 public String  unitPrice;       // number or ---
	@CSVUtil.ColumnName("支払（出金）")         public String  withdraw;        // number or ---
	@CSVUtil.ColumnName("預り（入金）")         public String  deposit;         // number or ---
	@CSVUtil.ColumnName("MRF・お預り金残高")    public String  mrfBalance;      // number or ---
	@CSVUtil.ColumnName("摘要")                 public String  note;            // string or empty
	
	public LocalDate settlementDate() {
		return toLocalDate(settlementDate);
	}
	public LocalDate tradeDate() {
		return toLocalDate(tradeDate);
	}
	public BigDecimal units() {
		return toBigDecimal(units);
	}
	public BigDecimal unitPrice() {
		return toBigDecimal(unitPrice);
	}
	public BigDecimal withdraw() {
		return toBigDecimal(withdraw);
	}
	public BigDecimal deposit() {
		return toBigDecimal(deposit);
	}
	
	
	@Override
	public String toString() {
		return StringUtil.toString(this);
	}
	@Override
	public boolean equals(Object o) {
		if (o instanceof Torireki) {
			Torireki that = (Torireki)o	;
			return
				this.settlementDate.equals(that.settlementDate) &&
				this.tradeDate.equals(that.tradeDate) &&
				this.product.equals(that.product) &&
				this.trade.equals(that.trade) &&
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
		
		var file = new File("tmp/Torireki20240217.csv");
		logger.info("file  {}", file.length());
		var list = CSVUtil.read(Torireki.class).file(file);
		logger.info("list {}", list.size());
		
		logger.info("STOP");
	}
}
