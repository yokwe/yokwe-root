package yokwe.finance.trade;

import java.math.BigDecimal;
import java.time.LocalDate;

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
	public BigDecimal deposit = BigDecimal.ZERO;
	
	@ColumnName("出金")
	@NumberFormat(SpreadSheet.FORMAT_USD_BLANK)
	public BigDecimal withdraw = BigDecimal.ZERO;
	
	@ColumnName("資金")
	@NumberFormat(SpreadSheet.FORMAT_USD)
	public BigDecimal fundTotal = BigDecimal.ZERO;
	
	@ColumnName("現金")
	@NumberFormat(SpreadSheet.FORMAT_USD)
	public BigDecimal cashTotal = BigDecimal.ZERO;
	
	@ColumnName("証券評価")
	@NumberFormat(SpreadSheet.FORMAT_USD)
	public BigDecimal stockValue = BigDecimal.ZERO;
	
	@ColumnName("証券原価")
	@NumberFormat(SpreadSheet.FORMAT_USD)
	public BigDecimal stockCost = BigDecimal.ZERO;
	
	@ColumnName("未実現損益")
	@NumberFormat(SpreadSheet.FORMAT_USD)
	public BigDecimal unrealizedGain = BigDecimal.ZERO;
	
	@ColumnName("実現損益")
	@NumberFormat(SpreadSheet.FORMAT_USD)
	public BigDecimal realizedGain = BigDecimal.ZERO;
	
	@ColumnName("配当")
	@NumberFormat(SpreadSheet.FORMAT_USD_BLANK)
	public BigDecimal dividend = BigDecimal.ZERO;
	
	@ColumnName("購入")
	@NumberFormat(SpreadSheet.FORMAT_USD_BLANK)
	public BigDecimal buy = BigDecimal.ZERO;
	
	@ColumnName("売却")
	@NumberFormat(SpreadSheet.FORMAT_USD_BLANK)
	public BigDecimal sell = BigDecimal.ZERO;
	
	@ColumnName("売却原価")
	@NumberFormat(SpreadSheet.FORMAT_USD_BLANK)
	public BigDecimal sellCost = BigDecimal.ZERO;
	
	@ColumnName("売却損益")
	@NumberFormat(SpreadSheet.FORMAT_USD_BLANK)
	public BigDecimal sellGain = BigDecimal.ZERO;
	
	@ColumnName("銘柄")
	@NumberFormat(SpreadSheet.FORMAT_STRING)
	public String code = "";
	
	@ColumnName("コメント")
	@NumberFormat(SpreadSheet.FORMAT_STRING)
	public String comment = "";
	
	@Override
	public String toString() {
		return ToString.withFieldName(this);
	}
}
