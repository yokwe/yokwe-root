package yokwe.finance.trade2.rakuten;

import static yokwe.finance.trade2.rakuten.UpdateTransaction.toLocalDate;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import yokwe.finance.trade2.Transaction;
import yokwe.util.CSVUtil;
import yokwe.util.ToString;
import yokwe.util.UnexpectedException;

public class AdjustHistoryUS {
	private static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();
	
	public static void main(String[] args) {
		logger.info("START");
		
		var date = UpdateTransaction.getFileDate();
		logger.info("date  {}", date);

		var file = UpdateTransaction.getAdjustHistoryUS(date);
		var list = CSVUtil.read(AdjustHistoryUS.class).file(file);
		logger.info("load  {}  {}", list.size(), file.getPath());
		
		logger.info("STOP");
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
	
	public enum Type {
		DEOSIT_TRANSFER   ("振替入金"),
		WITHDRAW_TRANSFER ("振替出金"),
		//
		DIVIDEND_STOCK    ("外株配当金"),
		DIVIDEND_BOND     ("外国債券利金"),
		//
		BUY_STOCK         ("米国株式購入"),
		BUY_BOND          ("外国債券購入"),
		BUY_MMF           ("外貨建てＭＭＦ買付"),
		//
		SELL_STOCK        ("米国株式売却"),
		SELL_BOND         ("外国債券売却"),
		SELL_MMF          ("外貨建てＭＭＦ解約"),
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
	
	public enum Currency {
		USD("米ドル"),
		;
		
		public final String string;
		private Currency(String string) {
			this.string = string;
		}
		
		@Override
		public String toString() {
			return string;
		}
	}
	
	//受渡日,約定日,口座区分,取引区分,対象証券名,決済通貨,単価,数量［株 /口］,受渡金額（受取）,受渡金額（受取）[円換算],受渡金額（支払）,受渡金額（支払）[円換算],為替レート,預り金
	//"2024/8/7","2024/8/5","-","振替出金","-","米ドル","-","-","-","-","1.28","184","144.531","0.00"
	
	@CSVUtil.ColumnName("受渡日")                    public String   settlementDate;    // 2025/5/20
	@CSVUtil.ColumnName("約定日")                    public String   tradeDate;         // 2025/5/20
	@CSVUtil.ColumnName("口座区分")                  public Account  account;           // "特定" "-"
	@CSVUtil.ColumnName("取引区分")                  public Type     type;              // 譲渡益税（住民税）
	@CSVUtil.ColumnName("対象証券名")                public String   name;              // name or "-"
	@CSVUtil.ColumnName("決済通貨")                  public Currency currency;          // "米ドル" or "-"
	@CSVUtil.ColumnName("単価")                      public String   unitPrice;         // "3,376.00000" or "-"
	@CSVUtil.ColumnName("数量［株 /口］")            public String   units;             // "2,800" "-"
	@CSVUtil.ColumnName("受渡金額（受取）")          public String   amountDeposit;     // "1,012,800" "-"
	@CSVUtil.ColumnName("受渡金額（受取）[円換算]")  public String   amountDepositJPY;  // "1,012,800" "-"
	@CSVUtil.ColumnName("受渡金額（支払）")          public String   amountWithdraw;    // "1,012,800" "-"
	@CSVUtil.ColumnName("受渡金額（支払）[円換算]")  public String   amountWithdrawJPY; // "1,012,800" "-"
	@CSVUtil.ColumnName("為替レート")                public String   fxRate;            // "143.9"
	@CSVUtil.ColumnName("預り金")                    public String   balance;           // "1,020,568" ""
	
	@Override
	public String toString() {
		return ToString.withFieldName(this);
	}
	@Override
	public boolean equals(Object o) {
		if (o != null && o instanceof AdjustHistoryUS) {
			var that = (AdjustHistoryUS)o;
			
			return this.toString().equals(that.toString());
		}
		return false;
	}
	
	
	static List<Transaction> toTransaction(List<AdjustHistoryUS> list) {
		var ret = new ArrayList<Transaction>();
		for(var e: list) {
			var t = toTransaction(e);
			if (t == null) continue;
			ret.add(toTransaction(e));
		}
		logger.info("ret  {}", ret.size());
		return ret;
	}
	static Transaction toTransaction(AdjustHistoryUS e) {
		var function = functionMap.get(e.type);
		if (function == null) {
			logger.error("Uneexpected type");
			logger.error("  type  {}", e.type);
			throw new UnexpectedException("Uneexpected type");
		}
		return function.apply(e);
	}
	
	private static Map<Type, Function<AdjustHistoryUS, Transaction>> functionMap = Map.ofEntries(
		Map.entry(Type.DEOSIT_TRANSFER,   new Functions.DEOSIT_TRANSFER()),
		Map.entry(Type.WITHDRAW_TRANSFER, new Functions.WITHDRAW_TRANSFER()),
		//
		Map.entry(Type.DIVIDEND_STOCK,    new Functions.DIVIDEND_STOCK()),
		Map.entry(Type.DIVIDEND_BOND,     new Functions.DIVIDEND_BOND()),
		//
		Map.entry(Type.BUY_STOCK,         new Functions.NULL()),
		Map.entry(Type.BUY_BOND,          new Functions.NULL()),
		Map.entry(Type.BUY_MMF,           new Functions.NULL()),
		//
		Map.entry(Type.SELL_STOCK,        new Functions.NULL()),
		Map.entry(Type.SELL_BOND,         new Functions.NULL()),
		Map.entry(Type.SELL_MMF,          new Functions.NULL())
	);
	private static class Functions {
		private static class DEOSIT_TRANSFER implements Function<AdjustHistoryUS, Transaction> {
			@Override
			public Transaction apply(AdjustHistoryUS e) {
				var ret = new Transaction();
				
				ret.settlementDate = toLocalDate(e.settlementDate);
				ret.tradeDate      = toLocalDate(e.tradeDate);
				ret.currency       = Transaction.Currency.USD;
				ret.type           = Transaction.Type.DEPOSIT_TRANSFER;
				ret.asset          = Transaction.Asset.CASH;
				ret.units          = 0;
				ret.amount         = new BigDecimal(e.amountDeposit.replace(",", "")).movePointRight(2).intValue();
				ret.code           = "";
				ret.comment        = e.type.toString();
				
				return ret;
			}
		}
		private static class WITHDRAW_TRANSFER implements Function<AdjustHistoryUS, Transaction> {
			@Override
			public Transaction apply(AdjustHistoryUS e) {
				var ret = new Transaction();
				
				ret.settlementDate = toLocalDate(e.settlementDate);
				ret.tradeDate      = toLocalDate(e.tradeDate);
				ret.currency       = Transaction.Currency.USD;
				ret.type           = Transaction.Type.WITHDRAW_TRANSFER;
				ret.asset          = Transaction.Asset.CASH;
				ret.units          = 0;
				ret.amount         = new BigDecimal(e.amountWithdraw.replace(",", "")).movePointRight(2).intValue();
				ret.code           = "";
				ret.comment        = e.type.toString();
				
				return ret;
			}
		}
		//
		private static class DIVIDEND_STOCK implements Function<AdjustHistoryUS, Transaction> {
			@Override
			public Transaction apply(AdjustHistoryUS e) {
				var ret = new Transaction();
				
				ret.settlementDate = toLocalDate(e.settlementDate);
				ret.tradeDate      = toLocalDate(e.tradeDate);
				ret.currency       = Transaction.Currency.USD;
				ret.type           = Transaction.Type.DIVIDEND;
				ret.asset          = Transaction.Asset.CASH;
				ret.units          = 0;
				ret.amount         = new BigDecimal(e.amountDeposit.replace(",", "")).movePointRight(2).intValue();
				ret.code           = TradeHistoryUS.toStockCode(e.name);
				ret.comment        = TradeHistoryUS.toStockName(ret.code);
				
				return ret;
			}
		}
		private static class DIVIDEND_BOND implements Function<AdjustHistoryUS, Transaction> {
			@Override
			public Transaction apply(AdjustHistoryUS e) {
				var ret = new Transaction();
				
				ret.settlementDate = toLocalDate(e.settlementDate);
				ret.tradeDate      = toLocalDate(e.tradeDate);
				ret.currency       = Transaction.Currency.USD;
				ret.type           = Transaction.Type.DIVIDEND;
				ret.asset          = Transaction.Asset.CASH;
				ret.units          = 0;
				ret.amount         = new BigDecimal(e.amountDeposit.replace(",", "")).movePointRight(2).intValue();
				ret.code           = "";
				ret.comment        = e.name;
				
				return ret;
			}
		}
		//
		private static class NULL implements Function<AdjustHistoryUS, Transaction> {
			@Override
			public Transaction apply(AdjustHistoryUS t) {
				return null;
			}
		}
	}
}
