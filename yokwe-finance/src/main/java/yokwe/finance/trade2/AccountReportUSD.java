package yokwe.finance.trade2;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import yokwe.util.ToString;
import yokwe.util.libreoffice.Sheet;
import yokwe.util.libreoffice.SpreadSheet;

@Sheet.SheetName("account-report-usd")
@Sheet.HeaderRow(0)
@Sheet.DataRow(1)
public class AccountReportUSD extends Sheet {
	@ColumnName("年月日")
	@NumberFormat(SpreadSheet.FORMAT_DATE)
	public LocalDate date;
	
	@ColumnName("入金")
	@NumberFormat(SpreadSheet.FORMAT_USD_BLANK)
	public BigDecimal deposit;
	
	@ColumnName("出金")
	@NumberFormat(SpreadSheet.FORMAT_USD_BLANK)
	public BigDecimal withdraw;
	
	@ColumnName("資金")
	@NumberFormat(SpreadSheet.FORMAT_USD)
	public BigDecimal fundTotal;
	
	@ColumnName("現金")
	@NumberFormat(SpreadSheet.FORMAT_USD)
	public BigDecimal cashTotal;
	
	@ColumnName("証券評価")
	@NumberFormat(SpreadSheet.FORMAT_USD)
	public BigDecimal stockValue;
	
	@ColumnName("証券原価")
	@NumberFormat(SpreadSheet.FORMAT_USD)
	public BigDecimal stockCost;
	
	@ColumnName("未実現損益")
	@NumberFormat(SpreadSheet.FORMAT_USD)
	public BigDecimal unrealizedGain;
	
	@ColumnName("実現損益")
	@NumberFormat(SpreadSheet.FORMAT_USD)
	public BigDecimal realizedGain;
	
	@ColumnName("配当")
	@NumberFormat(SpreadSheet.FORMAT_USD_BLANK)
	public BigDecimal dividend;
	
	@ColumnName("購入")
	@NumberFormat(SpreadSheet.FORMAT_USD_BLANK)
	public BigDecimal buy;
	
	@ColumnName("売却")
	@NumberFormat(SpreadSheet.FORMAT_USD_BLANK)
	public BigDecimal sell;
	
	@ColumnName("売却原価")
	@NumberFormat(SpreadSheet.FORMAT_USD_BLANK)
	public BigDecimal sellCost;
	
	@ColumnName("売却損益")
	@NumberFormat(SpreadSheet.FORMAT_USD_BLANK)
	public BigDecimal sellGain;
	
	@ColumnName("銘柄")
	@NumberFormat(SpreadSheet.FORMAT_STRING)
	public String code;
	
	@ColumnName("コメント")
	@NumberFormat(SpreadSheet.FORMAT_STRING)
	public String comment;
	
	public AccountReportUSD() {
		this.date           = null;
		this.deposit        = BigDecimal.ZERO;
		this.withdraw       = BigDecimal.ZERO;
		this.fundTotal      = BigDecimal.ZERO;
		this.cashTotal      = BigDecimal.ZERO;
		this.stockValue     = BigDecimal.ZERO;
		this.stockCost      = BigDecimal.ZERO;
		this.unrealizedGain = BigDecimal.ZERO;
		this.realizedGain   = BigDecimal.ZERO;
		this.dividend       = BigDecimal.ZERO;
		this.buy            = BigDecimal.ZERO;
		this.sell           = BigDecimal.ZERO;
		this.sellCost       = BigDecimal.ZERO;
		this.sellGain       = BigDecimal.ZERO;
		this.code           = "";
		this.comment        = "";
	}
	
	public AccountReportUSD(AccountReport that) {
		this.date           = that.date;
		this.deposit        = BigDecimal.valueOf(that.deposit).movePointLeft(2);
		this.withdraw       = BigDecimal.valueOf(that.withdraw).movePointLeft(2);
		this.fundTotal      = BigDecimal.valueOf(that.fundTotal).movePointLeft(2);
		this.cashTotal      = BigDecimal.valueOf(that.cashTotal).movePointLeft(2);
		this.stockValue     = BigDecimal.valueOf(that.stockValue).movePointLeft(2);
		this.stockCost      = BigDecimal.valueOf(that.stockCost).movePointLeft(2);
		this.unrealizedGain = BigDecimal.valueOf(that.unrealizedGain).movePointLeft(2);
		this.realizedGain   = BigDecimal.valueOf(that.realizedGain).movePointLeft(2);
		this.dividend       = BigDecimal.valueOf(that.dividend).movePointLeft(2);
		this.buy            = BigDecimal.valueOf(that.buy).movePointLeft(2);
		this.sell           = BigDecimal.valueOf(that.sell).movePointLeft(2);
		this.sellCost       = BigDecimal.valueOf(that.sellCost).movePointLeft(2);
		this.sellGain       = BigDecimal.valueOf(that.sellGain).movePointLeft(2);
		this.code           = that.code;
		this.comment        = that.comment;
	}
	
	@Override
	public String toString() {
		return ToString.withFieldName(this);
	}
	
	public static AccountReportUSD toAccountReportUSD(AccountReport that) {
		return new AccountReportUSD(that);
	}
	
	public static List<AccountReportUSD> toAccountReportUSD(List<AccountReport> list) {
		return list.stream().map(o -> new AccountReportUSD(o)).collect(Collectors.toList());
	}
}
