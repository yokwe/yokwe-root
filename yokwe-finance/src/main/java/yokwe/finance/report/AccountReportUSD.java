package yokwe.finance.report;

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
	public final LocalDate date;
	
	@ColumnName("入金")
	@NumberFormat(SpreadSheet.FORMAT_USD_BLANK)
	public final BigDecimal deposit;
	
	@ColumnName("出金")
	@NumberFormat(SpreadSheet.FORMAT_USD_BLANK)
	public final BigDecimal withdraw;
	
	@ColumnName("資金")
	@NumberFormat(SpreadSheet.FORMAT_USD)
	public final BigDecimal fundTotal;
	
	@ColumnName("現金")
	@NumberFormat(SpreadSheet.FORMAT_USD)
	public final BigDecimal cashTotal;
	
	@ColumnName("証券評価")
	@NumberFormat(SpreadSheet.FORMAT_USD)
	public final BigDecimal stockValue;
	
	@ColumnName("証券原価")
	@NumberFormat(SpreadSheet.FORMAT_USD)
	public final BigDecimal stockCost;
	
	@ColumnName("未実現損益")
	@NumberFormat(SpreadSheet.FORMAT_USD)
	public final BigDecimal unrealizedGain;
	
	@ColumnName("実現損益")
	@NumberFormat(SpreadSheet.FORMAT_USD)
	public final BigDecimal realizedGain;
	
	@ColumnName("配当")
	@NumberFormat(SpreadSheet.FORMAT_USD_BLANK)
	public final BigDecimal dividend;
	
	@ColumnName("購入")
	@NumberFormat(SpreadSheet.FORMAT_USD_BLANK)
	public final BigDecimal buy;
	
	@ColumnName("売却")
	@NumberFormat(SpreadSheet.FORMAT_USD_BLANK)
	public final BigDecimal sell;
	
	@ColumnName("売却原価")
	@NumberFormat(SpreadSheet.FORMAT_USD_BLANK)
	public final BigDecimal sellCost;
	
	@ColumnName("売却損益")
	@NumberFormat(SpreadSheet.FORMAT_USD_BLANK)
	public final BigDecimal sellGain;
	
	@ColumnName("銘柄")
	@NumberFormat(SpreadSheet.FORMAT_STRING)
	public final String code;
	
	@ColumnName("コメント")
	@NumberFormat(SpreadSheet.FORMAT_STRING)
	public final String comment;
	
	public AccountReportUSD(AccountReportJPY that) {
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
	
	public static AccountReportUSD toAccountReportUSD(AccountReportJPY that) {
		return new AccountReportUSD(that);
	}
	
	public static List<AccountReportUSD> toAccountReportUSD(List<AccountReportJPY> list) {
		return list.stream().map(o -> new AccountReportUSD(o)).collect(Collectors.toList());
	}
}
