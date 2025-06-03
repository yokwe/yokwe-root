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

public class TradeHistoryFB {
	private static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();
	
	public static void main(String[] args) {
		logger.info("START");
		
		var date = UpdateTransaction.getFileDate();
		logger.info("date  {}", date);

		var file = UpdateTransaction.getTradeHistoryFB(date);
		var list = CSVUtil.read(TradeHistoryFB.class).file(file);
		logger.info("load  {}  {}", list.size(), file.getPath());
		
		logger.info("STOP");
	}
	
	
	public enum BondType {
		FOREIGN_BOND("外国債券"),
		;
		
		public final String string;
		private BondType(String string) {
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
	
	public enum Type {
		BUY ("買付"),
		SELL("売付"),
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


	//約定日,受渡日,種類,銘柄コード,銘柄名,口座,通貨,決済通貨,償還日,年利率（税引前）,売買区分,数量（額面）,約定単価,適用為替レート,経過利子,受渡金額
	//"2023/8/4","2023/8/8","外国債券","A00396006","三井住友フィナンシャルグループ 米ドル建債券 3.446% 2027/01/11","特定","米ドル","米ドル","2027/1/11","3.45 %","買付","2,000","95.49 %","143.88","5.17","1,914.97"
	
	@CSVUtil.ColumnName("約定日")           public String   tradeDate;         // 2025/5/20
	@CSVUtil.ColumnName("受渡日")           public String   settlementDate;    // 2025/5/20
	@CSVUtil.ColumnName("種類")             public BondType bondType;          // 外国債券
	@CSVUtil.ColumnName("銘柄コード")       public String   bondCode;          // A00396006
	@CSVUtil.ColumnName("銘柄名")           public String   name;              // "三井住友フィナンシャルグループ 米ドル建債券 3.446% 2027/01/11"
	@CSVUtil.ColumnName("口座")             public Account  account;           // "特定"
	@CSVUtil.ColumnName("通貨")             public Currency currency;          // "米ドル" or "-"
	@CSVUtil.ColumnName("決済通貨")         public Currency tradeCurrency;     // "米ドル" or "-"
	@CSVUtil.ColumnName("償還日")           public String   redemptionDate;    // 2027/1/11
	@CSVUtil.ColumnName("年利率（税引前）") public String   interestRate;      // "3.45 %"
	@CSVUtil.ColumnName("売買区分")         public Type     type;              // "買付" "売付"
	@CSVUtil.ColumnName("数量（額面）")     public String   units;             // "2,000"
	@CSVUtil.ColumnName("約定単価")         public String   unitPrice;         // "95.49 %"
	@CSVUtil.ColumnName("適用為替レート")   public String   fxRate;            // "143.88"
	@CSVUtil.ColumnName("経過利子")         public String   accuredInterest;   // "5.17"
	@CSVUtil.ColumnName("受渡金額")         public String   amount;            // "1,914.97"
	
	@Override
	public String toString() {
		return ToString.withFieldName(this);
	}
	
	
	static List<Transaction> toTransaction(List<TradeHistoryFB> list) {
		var ret = new ArrayList<Transaction>();
		for(var e: list) {
			var t = toTransaction(e);
			if (t == null) continue;
			ret.add(toTransaction(e));
		}
		logger.info("ret  {}", ret.size());
		return ret;
	}
	static Transaction toTransaction(TradeHistoryFB e) {
		var function = functionMap.get(e.type);
		if (function == null) {
			logger.error("Uneexpected type");
			logger.error("  type  {}", e.type);
			throw new UnexpectedException("Uneexpected type");
		}
		return function.apply(e);
	}
	
	private static Map<Type, Function<TradeHistoryFB, Transaction>> functionMap = Map.ofEntries(
		Map.entry(Type.BUY,  new Functions.BUY()),
		Map.entry(Type.SELL, new Functions.SELL())
	);
	private static class Functions {
		private static class BUY implements Function<TradeHistoryFB, Transaction> {
			@Override
			public Transaction apply(TradeHistoryFB e) {
				var ret = new Transaction();
				
				ret.settlementDate = toLocalDate(e.settlementDate);
				ret.tradeDate      = toLocalDate(e.tradeDate);
				ret.currency       = Transaction.Currency.USD;
				ret.type           = Transaction.Type.BUY;
				ret.asset          = Transaction.Asset.BOND_US;
				ret.units          = Integer.valueOf(e.units.replace(",", ""));
				ret.amount         = new BigDecimal(e.amount.replace(",", "")).negate().movePointRight(2).intValue();
				ret.code           = "";
				ret.comment        = e.name;
				
				return ret;
			}
		}
		private static class SELL implements Function<TradeHistoryFB, Transaction> {
			@Override
			public Transaction apply(TradeHistoryFB e) {
				var ret = new Transaction();
				
				ret.settlementDate = toLocalDate(e.settlementDate);
				ret.tradeDate      = toLocalDate(e.tradeDate);
				ret.currency       = Transaction.Currency.USD;
				ret.type           = Transaction.Type.SELL;
				ret.asset          = Transaction.Asset.BOND_US;
				ret.units          = Integer.valueOf(e.units.replace(",", ""));
				ret.amount         = new BigDecimal(e.amount.replace(",", "")).movePointRight(2).intValue();
				ret.code           = "";
				ret.comment        = e.name;
				
				return ret;
			}
		}
	}
}
