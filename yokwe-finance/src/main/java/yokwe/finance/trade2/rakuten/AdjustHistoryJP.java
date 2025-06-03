package yokwe.finance.trade2.rakuten;

import static yokwe.finance.trade2.rakuten.UpdateTransaction.toLocalDate;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import yokwe.finance.trade2.Transaction;
import yokwe.util.CSVUtil;
import yokwe.util.ToString;
import yokwe.util.UnexpectedException;

public class AdjustHistoryJP {
	private static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();
	
	public static void main(String[] args) {
		logger.info("START");
		
		var date = UpdateTransaction.getFileDate();
		logger.info("date  {}", date);

		var file = UpdateTransaction.getAdjustHistoryJP(date);
		var list = CSVUtil.read(AdjustHistoryJP.class).file(file);
		logger.info("load  {}  {}", list.size(), file.getPath());
		
		logger.info("STOP");
	}
	
	
	public enum Type {
		DEPOSIT_REALTIME  ("リアルタイム入金(三井住友銀行)"),
		DEPOSIT_POINT     ("入金(楽天証券ポイント交換)"),
		DEPOSIT_CAMPAIGN  ("手数料キャンペーン"),
		DEOSIT_TRANSFER   ("振替入金"),
		//
		WITHDRAW          ("通常出金(三井住友銀行)"),
		WITHDRAW_TRANSFER ("振替出金"),
		//
		DIVIDEND_STOCK    ("国内株式配当金"),
		TAX_INCOME        ("譲渡益税（所得税）"),
		TAX_LOCAL         ("譲渡益税（住民税）"),
		TAX_REFUND_INCOME ("還付金（所得税）"),
		TAX_REFUND_LOCAL  ("還付金（住民税）"),
		//
		BUY_STOCK         ("株式購入"),
		BUY_FUND          ("株式投信購入"),
		//
		SELL_STOCK        ("株式売却"),
		SELL_FUND         ("株式投信解約"),
		;
		
		public final String string;
		private Type(String string) {
			this.string = string;
		}
		
		@Override
		public String toString() {
			return string;
		}
	}
	
	public enum Account {
		SPECIAL("特定"),
		NONE   ("-"),
		;
		
		public final String string;
		private Account(String string) {
			this.string = string;
		}
		
		@Override
		public String toString() {
			return string;
		}
	}


	//受渡日,約定日,取引区分,口座区分,対象証券名,単価［円/％］,数量［株/口/額面］,受渡金額（受取）,受渡金額（支払）,預り金（MRF）［円］
	//"2025/5/20","2025/5/20","譲渡益税（住民税）","-","-","-","-","-","3,395","5,103,375"
	
	@CSVUtil.ColumnName("受渡日")               public String  settlementDate;  // 2025/5/20
	@CSVUtil.ColumnName("約定日")               public String  tradeDate;       // 2025/5/20
	@CSVUtil.ColumnName("取引区分")             public Type    type;            // 譲渡益税（住民税）
	@CSVUtil.ColumnName("口座区分")             public Account account;         // "特定" "-"
	@CSVUtil.ColumnName("対象証券名")           public String  name;            // name or "-"
	@CSVUtil.ColumnName("単価［円/％］")        public String  unitPrice;       // "3,376.00000" or "-"
	@CSVUtil.ColumnName("数量［株/口/額面］")   public String  units;           // "2,800" "-"
	@CSVUtil.ColumnName("受渡金額（受取）")     public String  amountDeposit;   // "1,012,800" "-"
	@CSVUtil.ColumnName("受渡金額（支払）")     public String  amountWithdraw;  // "286,300" "-"
	@CSVUtil.ColumnName("預り金（MRF）［円］")  public String  balance;         // "1,020,568" ""
	
	@Override
	public String toString() {
		return ToString.withFieldName(this);
	}
	@Override
	public boolean equals(Object o) {
		if (o != null && o instanceof AdjustHistoryJP) {
			var that = (AdjustHistoryJP)o;
			
			return this.toString().equals(that.toString());
		}
		return false;
	}
	
	
	static List<Transaction> toTransaction(List<AdjustHistoryJP> list) {
		var ret = new ArrayList<Transaction>();
		for(var e: list) {
			var t = toTransaction(e);
			if (t == null) continue;
			ret.add(toTransaction(e));
		}
		logger.info("ret  {}", ret.size());
		return ret;
	}
	
	static Transaction toTransaction(AdjustHistoryJP e) {
		var function = functionMap.get(e.type);
		if (function == null) {
			logger.error("Uneexpected type");
			logger.error("  type  {}", e.type);
			throw new UnexpectedException("Uneexpected type");
		}
		return function.apply(e);
	}
	
	private static Map<Type, Function<AdjustHistoryJP, Transaction>> functionMap = Map.ofEntries(
		Map.entry(Type.DEPOSIT_REALTIME,  new Functions.DEPOSIT()),
		Map.entry(Type.DEPOSIT_POINT,     new Functions.DEPOSIT()),
		Map.entry(Type.DEPOSIT_CAMPAIGN,  new Functions.DEPOSIT()),
		Map.entry(Type.DEOSIT_TRANSFER,   new Functions.DEOSIT_TRANSFER()),
		//
		Map.entry(Type.WITHDRAW,          new Functions.WITHDRAW()),
		Map.entry(Type.WITHDRAW_TRANSFER, new Functions.WITHDRAW_TRANSFER()),
		//
		Map.entry(Type.DIVIDEND_STOCK,    new Functions.DIVIDEND_STOCK()),
		Map.entry(Type.TAX_INCOME,        new Functions.TAX()),
		Map.entry(Type.TAX_LOCAL,         new Functions.TAX()),
		Map.entry(Type.TAX_REFUND_INCOME, new Functions.TAX_REFUND()),
		Map.entry(Type.TAX_REFUND_LOCAL,  new Functions.TAX_REFUND()),
		//
		Map.entry(Type.BUY_STOCK,         new Functions.NULL()),
		Map.entry(Type.BUY_FUND,          new Functions.NULL()),
		//
		Map.entry(Type.SELL_STOCK,        new Functions.NULL()),
		Map.entry(Type.SELL_FUND,         new Functions.NULL())
	);
	private static class Functions {
		private static class DEPOSIT implements Function<AdjustHistoryJP, Transaction> {
			@Override
			public Transaction apply(AdjustHistoryJP e) {
				var ret = new Transaction();
				
				ret.settlementDate = toLocalDate(e.settlementDate);
				ret.tradeDate      = toLocalDate(e.tradeDate);
				ret.currency       = Transaction.Currency.JPY;
				ret.type           = Transaction.Type.DEPOSIT;
				ret.asset          = Transaction.Asset.CASH;
				ret.units          = 0;
				ret.amount         = Integer.valueOf(e.amountDeposit.replace(",", ""));
				ret.code           = "";
				ret.comment        = e.type.toString();
				
				return ret;
			}
		}
		private static class DEOSIT_TRANSFER implements Function<AdjustHistoryJP, Transaction> {
			@Override
			public Transaction apply(AdjustHistoryJP e) {
				var ret = new Transaction();
				
				ret.settlementDate = toLocalDate(e.settlementDate);
				ret.tradeDate      = toLocalDate(e.tradeDate);
				ret.currency       = Transaction.Currency.JPY;
				ret.type           = Transaction.Type.DEPOSIT_TRANSFER;
				ret.asset          = Transaction.Asset.CASH;
				ret.units          = 0;
				ret.amount         = Integer.valueOf(e.amountDeposit.replace(",", ""));
				ret.code           = "";
				ret.comment        = e.type.toString();
				
				return ret;
			}
		}
		//
		private static class WITHDRAW implements Function<AdjustHistoryJP, Transaction> {
			@Override
			public Transaction apply(AdjustHistoryJP e) {
				var ret = new Transaction();
				
				ret.settlementDate = toLocalDate(e.settlementDate);
				ret.tradeDate      = toLocalDate(e.tradeDate);
				ret.currency       = Transaction.Currency.JPY;
				ret.type           = Transaction.Type.WITHDRAW;
				ret.asset          = Transaction.Asset.CASH;
				ret.units          = 0;
				ret.amount         = -Integer.valueOf(e.amountWithdraw.replace(",", ""));
				ret.code           = "";
				ret.comment        = e.type.toString();
				
				return ret;
			}
		}
		private static class WITHDRAW_TRANSFER implements Function<AdjustHistoryJP, Transaction> {
			@Override
			public Transaction apply(AdjustHistoryJP e) {
				var ret = new Transaction();
				
				ret.settlementDate = toLocalDate(e.settlementDate);
				ret.tradeDate      = toLocalDate(e.tradeDate);
				ret.currency       = Transaction.Currency.JPY;
				ret.type           = Transaction.Type.WITHDRAW_TRANSFER;
				ret.asset          = Transaction.Asset.CASH;
				ret.units          = 0;
				ret.amount         = -Integer.valueOf(e.amountWithdraw.replace(",", ""));
				ret.code           = "";
				ret.comment        = e.type.toString();
				
				return ret;
			}
		}
		//
		private static class DIVIDEND_STOCK implements Function<AdjustHistoryJP, Transaction> {
			@Override
			public Transaction apply(AdjustHistoryJP e) {
				var ret = new Transaction();
				
				ret.settlementDate = toLocalDate(e.settlementDate);
				ret.tradeDate      = toLocalDate(e.tradeDate);
				ret.currency       = Transaction.Currency.JPY;
				ret.type           = Transaction.Type.DIVIDEND;
				ret.asset          = Transaction.Asset.CASH;
				ret.units          = 0;
				ret.amount         = Integer.valueOf(e.amountDeposit.replace(",", ""));
				ret.code           = TradeHistoryJP.toStockCode(e.name);
				ret.comment        = TradeHistoryJP.toStockName(ret.code);
				
				return ret;
			}
		}
		private static class TAX implements Function<AdjustHistoryJP, Transaction> {
			@Override
			public Transaction apply(AdjustHistoryJP e) {
				var ret = new Transaction();
				
				ret.settlementDate = toLocalDate(e.settlementDate);
				ret.tradeDate      = toLocalDate(e.tradeDate);
				ret.currency       = Transaction.Currency.JPY;
				ret.type           = Transaction.Type.TAX;
				ret.asset          = Transaction.Asset.CASH;
				ret.units          = 0;
				ret.amount         = -Integer.valueOf(e.amountWithdraw.replace(",", ""));
				ret.code           = "";
				ret.comment        = e.type.toString();
				
				return ret;
			}
		}
		private static class TAX_REFUND implements Function<AdjustHistoryJP, Transaction> {
			@Override
			public Transaction apply(AdjustHistoryJP e) {
				var ret = new Transaction();
				
				ret.settlementDate = toLocalDate(e.settlementDate);
				ret.tradeDate      = toLocalDate(e.tradeDate);
				ret.currency       = Transaction.Currency.JPY;
				ret.type           = Transaction.Type.TAX;
				ret.asset          = Transaction.Asset.CASH;
				ret.units          = 0;
				ret.amount         = Integer.valueOf(e.amountDeposit.replace(",", ""));
				ret.code           = "";
				ret.comment        = e.type.toString();
				
				return ret;
			}
		}
		//
		private static class NULL implements Function<AdjustHistoryJP, Transaction> {
			@Override
			public Transaction apply(AdjustHistoryJP t) {
				return null;
			}
		}
	}
	
}
