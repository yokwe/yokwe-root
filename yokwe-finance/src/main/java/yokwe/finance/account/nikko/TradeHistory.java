package yokwe.finance.account.nikko;

import java.math.BigDecimal;
import java.time.LocalDate;

import yokwe.util.StringUtil;
import yokwe.util.UnexpectedException;

public class TradeHistory {
	private static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();
	
	public static final LocalDate  NO_DATE   = LocalDate.EPOCH;
	public static final BigDecimal NO_NUMBER = BigDecimal.valueOf(-1);
	
	public static final String ACCOUNT_TYPE_TOKUTEI = "特定";
	
	public static final String NOTE_USE_USD = "外貨決済　通貨:USD";
	public static final String NOTE_USE_JPY = "円貨決済　通貨:USD";
	
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
	
	public enum AccountType {
		NONE    ("-"),
		SPECIAL("特定");
		
		public static AccountType getInstance(String string) {
			for(var e: values()) {
				if (e.string.equals(string)) return e;
			}
			logger.error("Unexpected string");
			logger.error("  string  {}!", string);
			throw new UnexpectedException("Unexpected string");
		}
		
		public final String string;
		private AccountType(String string) {
			this.string = string;
		}
		public String toString() {
			return string;
		}
	}
	
	public final LocalDate   settlementDate;
	public final LocalDate   tradeDate;
	public final Product     product;
	public final Trade       trade;
	public final String      name;
	public final String      code;        // stockCode for stock, isinCode for fund, and proprietary code for bond
	public final AccountType accountType;
	public final BigDecimal  units;
	public final BigDecimal  unitPrice;
	public final BigDecimal  withdraw;
	public final BigDecimal  deposit;
	public final BigDecimal  mrfBalance;
	public final String      note;
	
	public TradeHistory(
		LocalDate  settlementDate,
		LocalDate  tradeDate,
		Product    product,
		Trade      trade,
		String     name,
		String     code,
		AccountType accountType,
		BigDecimal   units,
		BigDecimal unitPrice,
		BigDecimal withdraw,
		BigDecimal deposit,
		BigDecimal mrfBalance,
		String     note
		) {
		this.settlementDate = settlementDate;
		this.tradeDate      = tradeDate;
		this.product        = product;
		this.trade          = trade;
		this.name           = name; 
		this.code           = code;
		this.accountType    = accountType;
		this.units          = units;
		this.unitPrice      = unitPrice;
		this.withdraw       = withdraw;
		this.deposit        = deposit;
		this.mrfBalance     = mrfBalance;
		this.note           = note;
	}
	
	public boolean useUSD() {
		return note.equals(NOTE_USE_USD);
	}
	public boolean useJPY() {
		return note.startsWith(NOTE_USE_JPY);
	}
	
	@Override
	public String toString() {
		return StringUtil.toString(this);
	}
}
