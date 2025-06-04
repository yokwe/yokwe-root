package yokwe.finance.trade2.rakuten;

import static yokwe.finance.trade2.rakuten.UpdateTransaction.toLocalDate;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.stream.Collectors;

import yokwe.finance.stock.StorageStock;
import yokwe.finance.trade2.Transaction;
import yokwe.util.CSVUtil;
import yokwe.util.ToString;
import yokwe.util.UnexpectedException;

public class TradeHistoryUS {
	private static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();
	
	public static void main(String[] args) {
		logger.info("START");
		
		var date = UpdateTransaction.getFileDate();
		logger.info("date  {}", date);

		var file = UpdateTransaction.getTradeHistoryUS(date);
		var list = CSVUtil.read(TradeHistoryUS.class).file(file);
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
	
	public enum Mode {
		CASH  ("現物"),
		MARGIN("信用"),
		;
		
		public final String string;
		private Mode(String string) {
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
		USD("ＵＳドル"),
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
	
	
	//約定日,受渡日,ティッカー,銘柄名,口座,取引区分,売買区分,信用区分,弁済期限,決済通貨,数量［株］,単価［USドル］,約定代金［USドル］,為替レート,手数料［USドル］,税金［USドル］,受渡金額［USドル］,受渡金額［円］
	//"2023/9/13","2023/9/15","IYR","ISHARES REAL EST","特定","現物","買付","-","-","ＵＳドル","1","83.8500","83.85","147.550","-","-","83.85","-"
	
	@CSVUtil.ColumnName("約定日")               public String   tradeDate;         // 2025/5/20
	@CSVUtil.ColumnName("受渡日")               public String   settlementDate;    // 2025/5/20
	@CSVUtil.ColumnName("ティッカー")           public String   code;              // stock code
	@CSVUtil.ColumnName("銘柄名")               public String   name;              // stock name
	@CSVUtil.ColumnName("口座")                 public Account  accountType;       // "特定"
	@CSVUtil.ColumnName("取引区分")             public Mode     mode;              // "現物"
	@CSVUtil.ColumnName("売買区分")             public Type     type;              // "買付" "売付"
	@CSVUtil.ColumnName("信用区分")             public String   marginType;        // "-"
	@CSVUtil.ColumnName("弁済期限")             public String   marginDueDate;     // "-"
	@CSVUtil.ColumnName("決済通貨")             public Currency currency;          // "ＵＳドル"
	@CSVUtil.ColumnName("数量［株］")           public String   units;             // "2,800" "-"
	@CSVUtil.ColumnName("単価［USドル］")       public String   unitPrice;         // "83.8500" or "-"
	@CSVUtil.ColumnName("約定代金［USドル］")   public String   contractAmount;    // "83.85" "-"
	@CSVUtil.ColumnName("為替レート")           public String   fxRate;            // "143.9"
	@CSVUtil.ColumnName("手数料［USドル］")     public String   fee;               // "83.85" "-"
	@CSVUtil.ColumnName("税金［USドル］")       public String   tax;               // "83.85" "-"
	@CSVUtil.ColumnName("受渡金額［USドル］")   public String   amount;            // "83.85"
	@CSVUtil.ColumnName("受渡金額［円］")       public String   deliveryAmountJPY; // "83.85" "-"
	
	
	@Override
	public String toString() {
		return ToString.withFieldName(this);
	}
	
	
	static List<Transaction> toTransaction(List<TradeHistoryUS> list) {
		buildStockNameCode(list);
		//
		var ret = new ArrayList<Transaction>();
		for(var e: list) {
			var t = toTransaction(e);
			if (t == null) continue;
			ret.add(toTransaction(e));
		}
		logger.info("toTransaction  {}", ret.size());
		return ret;
	}
	static Transaction toTransaction(TradeHistoryUS e) {
		var function = functionMap.get(e.type);
		if (function == null) {
			logger.error("Uneexpected type");
			logger.error("  type  {}", e.type);
			throw new UnexpectedException("Uneexpected type");
		}
		return function.apply(e);
	}
	private static Map<Type, Function<TradeHistoryUS, Transaction>> functionMap = Map.ofEntries(
		Map.entry(Type.BUY,      new Functions.BUY()),
		Map.entry(Type.SELL,     new Functions.SELL())
	);
	private static class Functions {
		private static class BUY implements Function<TradeHistoryUS, Transaction> {
			@Override
			public Transaction apply(TradeHistoryUS e) {
				var ret = new Transaction();
				
				ret.settlementDate = toLocalDate(e.settlementDate);
				ret.tradeDate      = toLocalDate(e.tradeDate);
				ret.currency       = Transaction.Currency.USD;
				ret.type           = Transaction.Type.BUY;
				ret.asset          = Transaction.Asset.STOCK_US;
				ret.units          = Integer.valueOf(e.units.replace(",", ""));
				ret.amount         = new BigDecimal(e.amount.replace(",", "")).movePointRight(2).intValue();
				ret.code           = TradeHistoryUS.toStockCode(e.name);
				ret.comment        = TradeHistoryUS.toStockName(ret.code);
				
				return ret;
			}
		}
		private static class SELL implements Function<TradeHistoryUS, Transaction> {
			@Override
			public Transaction apply(TradeHistoryUS e) {
				var ret = new Transaction();
				
				ret.settlementDate = toLocalDate(e.settlementDate);
				ret.tradeDate      = toLocalDate(e.tradeDate);
				ret.currency       = Transaction.Currency.USD;
				ret.type           = Transaction.Type.SELL;
				ret.asset          = Transaction.Asset.STOCK_US;
				ret.units          = Integer.valueOf(e.units.replace(",", ""));
				ret.amount         = new BigDecimal(e.amount.replace(",", "")).movePointRight(2).intValue();
				ret.code           = TradeHistoryUS.toStockCode(e.name);
				ret.comment        = TradeHistoryUS.toStockName(ret.code);
				
				return ret;
			}
		}
	}
	
	
	private static Map<String, String> stockNameMap = new TreeMap<>();
	//                 name    code
	private static void buildStockNameCode(List<TradeHistoryUS> list) {
		for(var e: list) {
			stockNameMap.put(e.name,  e.code);
		}
	}
	public static String toStockCode(String stockName) {
		// find stock code from stockNameMap
		{
			var code = stockNameMap.get(stockName);
			if (code != null) return code;
		}
				
		// find stock code from stockCodeMap
		for(var entry: stockCodeMap.entrySet()) {
			var code = entry.getKey();
			var name = entry.getValue();
			
			if (name.equals(stockName)) {
				// save for later use
				stockNameMap.put(stockName, code);
				return code;
			}
		}
		
		logger.error("Unexpected name");
		logger.error("  name  {}!", stockName);
		throw new UnexpectedException("Unexpected name");
	}
	
	private static Map<String, String> stockCodeMap = StorageStock.StockInfoUSAll.getList().stream().collect(Collectors.toMap(o -> o.stockCode, o -> o.name));
	//                 code    name
	public static String toStockName(String code) {
		var ret = stockCodeMap.get(code);
		if (ret != null) {
			return ret;
		} else {
			logger.error("Unexpected code");
			logger.error("  code  {}!", code);
			throw new UnexpectedException("Unexpected code");
		}
	}

}
