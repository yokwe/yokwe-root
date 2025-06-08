package yokwe.finance.trade2;

import java.time.LocalDate;

import yokwe.util.ToString;
import yokwe.util.libreoffice.Sheet;
import yokwe.util.libreoffice.SpreadSheet;

@Sheet.SheetName("account-report")
@Sheet.HeaderRow(0)
@Sheet.DataRow(1)
public class AccountReport extends Sheet {
	@ColumnName("年月日")
	@NumberFormat(SpreadSheet.FORMAT_DATE)
	public LocalDate date;
	
	@ColumnName("入金")
	@NumberFormat(SpreadSheet.FORMAT_JPY_BLANK)
	public int deposit = 0;
	
	@ColumnName("出金")
	@NumberFormat(SpreadSheet.FORMAT_JPY_BLANK)
	public int withdraw = 0;
	
	@ColumnName("資金")
	@NumberFormat(SpreadSheet.FORMAT_JPY)
	public int fundTotal = 0;
	
	@ColumnName("現金")
	@NumberFormat(SpreadSheet.FORMAT_JPY)
	public int cashTotal = 0;
	
	@ColumnName("証券評価")
	@NumberFormat(SpreadSheet.FORMAT_JPY)
	public int stockValue = 0;
	
	@ColumnName("証券原価")
	@NumberFormat(SpreadSheet.FORMAT_JPY)
	public int stockCost = 0;
	
	@ColumnName("未実現損益")
	@NumberFormat(SpreadSheet.FORMAT_JPY)
	public int unrealizedGain = 0;
	
	@ColumnName("実現損益")
	@NumberFormat(SpreadSheet.FORMAT_JPY)
	public int realizedGain = 0;
	
	@ColumnName("配当")
	@NumberFormat(SpreadSheet.FORMAT_JPY_BLANK)
	public int dividend = 0;
	
	@ColumnName("購入")
	@NumberFormat(SpreadSheet.FORMAT_JPY_BLANK)
	public int buy = 0;
	
	@ColumnName("売却")
	@NumberFormat(SpreadSheet.FORMAT_JPY_BLANK)
	public int sell = 0;
	
	@ColumnName("売却原価")
	@NumberFormat(SpreadSheet.FORMAT_JPY_BLANK)
	public int sellCost = 0;
	
	@ColumnName("売却損益")
	@NumberFormat(SpreadSheet.FORMAT_JPY_BLANK)
	public int sellGain = 0;
	
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
