package yokwe.finance.trade;

import java.math.BigDecimal;
import java.time.LocalDate;

import yokwe.util.libreoffice.SpreadSheet;
import yokwe.util.libreoffice.Sheet.ColumnName;
import yokwe.util.libreoffice.Sheet.NumberFormat;

public class AccountReportUSD {
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
	public BigDecimal fund;
	
	@ColumnName("現金")
	@NumberFormat(SpreadSheet.FORMAT_USD)
	public BigDecimal cash;
	
	@ColumnName("証券評価")
	@NumberFormat(SpreadSheet.FORMAT_USD)
	public BigDecimal stockValue;
	
	@ColumnName("証券原価")
	@NumberFormat(SpreadSheet.FORMAT_USD)
	public BigDecimal stockCost;
	
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
	public String symbol;
}
