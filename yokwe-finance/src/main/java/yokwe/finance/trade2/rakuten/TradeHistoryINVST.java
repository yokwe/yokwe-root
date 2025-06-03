package yokwe.finance.trade2.rakuten;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import yokwe.finance.trade2.Transaction;
import yokwe.util.CSVUtil;
import yokwe.util.ToString;
import yokwe.util.UnexpectedException;

public class TradeHistoryINVST {
	private static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();
	
	public static void main(String[] args) {
		logger.info("START");
		
		var date = UpdateTransaction.getFileDate();
		logger.info("date  {}", date);

		var file = UpdateTransaction.getTradeHistoryINVST(date);
		var list = CSVUtil.read(TradeHistoryINVST.class).file(file);
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
		BUY     ("買付"),
		SELL    ("解約"),
		REINVEST("再投資"),
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
		JPY("円"),
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

	
	//約定日,受渡日,ファンド名,分配金,口座,取引,買付方法,数量［口］,単価,経費,為替レート,受付金額[現地通貨],受渡金額/(ポイント利用)[円],決済通貨
	//"2023/10/30","2023/11/6","東京海上セレクション・物価連動国債(うんよう博士)","再投資型","特定","買付","通常","830,358","12,043","0","-","-","1,000,000","円"
	
	@CSVUtil.ColumnName("約定日")                      public String   tradeDate;         // 2025/5/20
	@CSVUtil.ColumnName("受渡日")                      public String   settlementDate;    // 2025/5/20
	@CSVUtil.ColumnName("ファンド名")                  public String   fundName;          // "東京海上セレクション・物価連動国債(うんよう博士)"
	@CSVUtil.ColumnName("分配金")                      public String   fundType;          // "再投資型"
	@CSVUtil.ColumnName("口座")                        public Account  account;           // "特定"
	@CSVUtil.ColumnName("取引")                        public Type     type;              // "買付" "売付"
	@CSVUtil.ColumnName("買付方法")                    public String   buyType;           // "通常"
	@CSVUtil.ColumnName("数量［口］")                  public String   units;             // "830,358"
	@CSVUtil.ColumnName("単価")                        public String   unitPrice;         // "12,043"
	@CSVUtil.ColumnName("経費")                        public String   fee;               // "0"
	@CSVUtil.ColumnName("為替レート")                  public String   fxRate;            // "-"
	@CSVUtil.ColumnName("受付金額[現地通貨]")          public String   deliveryAmount;    // "-"
	@CSVUtil.ColumnName("受渡金額/(ポイント利用)[円]") public String   deliveryAmountJPY; // "1,000,000"
	@CSVUtil.ColumnName("決済通貨")                    public Currency currency;          // "円"
	
	@Override
	public String toString() {
		return ToString.withFieldName(this);
	}
	
	
	static List<Transaction> toTransaction(List<TradeHistoryINVST> list) {
		var ret = new ArrayList<Transaction>();
		for(var e: list) {
			var t = toTransaction(e);
			if (t == null) continue;
			ret.add(toTransaction(e));
		}
		logger.info("ret  {}", ret.size());
		return ret;
	}
	static Transaction toTransaction(TradeHistoryINVST e) {
		var function = functionMap.get(e.type);
		if (function == null) {
			logger.error("Uneexpected type");
			logger.error("  type  {}", e.type);
			throw new UnexpectedException("Uneexpected type");
		}
		return function.apply(e);
	}
	private static Map<Type, Function<TradeHistoryINVST, Transaction>> functionMap = Map.ofEntries(
		Map.entry(Type.BUY,      new Functions.BUY()),
		Map.entry(Type.SELL,     new Functions.SELL()),
		Map.entry(Type.REINVEST, new Functions.REINVEST())
	);
	private static class Functions {
		private static class BUY implements Function<TradeHistoryINVST, Transaction> {
			@Override
			public Transaction apply(TradeHistoryINVST t) {
				return null; // FIXME
			}
		}
		private static class SELL implements Function<TradeHistoryINVST, Transaction> {
			@Override
			public Transaction apply(TradeHistoryINVST t) {
				return null; // FIXME
			}
		}
		private static class REINVEST implements Function<TradeHistoryINVST, Transaction> {
			@Override
			public Transaction apply(TradeHistoryINVST t) {
				return null; // FIXME
			}
		}
	}
}
