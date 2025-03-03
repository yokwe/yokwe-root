package yokwe.finance.account.nikko;

import java.math.BigDecimal;
import java.time.LocalDate;

import yokwe.finance.account.nikko.TradeHistory.AccountType;
import yokwe.finance.account.nikko.TradeHistory.Product;
import yokwe.finance.account.nikko.TradeHistory.Trade;
import yokwe.util.CSVUtil;
import yokwe.util.StringUtil;
import yokwe.util.UnexpectedException;

public class Torireki {
	private static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();
	
	public static LocalDate toLocalDate(String string) {
		if (string.equals("---")) return TradeHistory.NO_DATE;
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
	
	public static BigDecimal toBigDecimal(String string) {
		if (string.equals("---")) return TradeHistory.NO_NUMBER;
		return new BigDecimal(string.replace(",", ""));
	}
	
	// "受渡日","約定日","商品等","取引種類","銘柄名（ファンド名）","銘柄コード","口座","数量","単価","支払（出金）","預り（入金）","MRF・お預り金残高","摘要"
	@CSVUtil.ColumnName("受渡日")               public String      settlementDate;  // 24/02/15
	@CSVUtil.ColumnName("約定日")               public String      tradeDate;       // 24/02/15 or ---
	@CSVUtil.ColumnName("商品等")               public Product     product;         // or ---
	@CSVUtil.ColumnName("取引種類")             public Trade       trade;           // 
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
	
	public TradeHistory toTradeHistory() {
		LocalDate   settlementDate = toLocalDate(this.settlementDate);
		LocalDate   tradeDate      = toLocalDate(this.tradeDate);
		Product     product        = this.product;
		Trade       trade          = this.trade;
		String      name           = this.name;
		String      code           = this.code;
		AccountType accountType    = this.accountType;
		BigDecimal  units          = toBigDecimal(this.units);
		BigDecimal  unitPrice      = toBigDecimal(this.unitPrice);
		BigDecimal  withdraw       = toBigDecimal(this.withdraw);
		BigDecimal  deposit        = toBigDecimal(this.deposit);
		BigDecimal  mrfBalance     = toBigDecimal(this.mrfBalance);
		String      note           = this.note;
		
		return new TradeHistory(
			settlementDate,
			tradeDate,
			product,
			trade,
			name,
			code,
			accountType,
			units,
			unitPrice,
			withdraw,
			deposit,
			mrfBalance,
			note
		);
	}
}
