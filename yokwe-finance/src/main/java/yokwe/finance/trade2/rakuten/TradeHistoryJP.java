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
import yokwe.finance.type.StockCodeJP;
import yokwe.util.CSVUtil;
import yokwe.util.ToString;
import yokwe.util.UnexpectedException;

public class TradeHistoryJP {
	private static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();
	
	public static void main(String[] args) {
		logger.info("START");
		
		var date = UpdateTransaction.getFileDate();
		logger.info("date  {}", date);

		var file = UpdateTransaction.getTradeHistoryJP(date);
		var list = CSVUtil.read(TradeHistoryJP.class).file(file);
		logger.info("load  {}  {}", list.size(), file.getPath());
		
		logger.info("STOP");
	}
	
	
	public enum Market {
		TSE    ("東証"),
		TOSTNET("ToSTNeT"),
		CHIX   ("Chi-X"),
		JNX    ("JNX"),
		;
		
		public final String string;
		private Market(String string) {
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
	
	public enum Mode {
		CASH  ("現物"),
		MARGIN("信用"),
		NONE  (""),
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
		BUY     ("買付"),
		SELL    ("売付"),
		DEPOSIT ("入庫"),
		WITHDRAW("出庫"),
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
	
	//約定日,受渡日,銘柄コード,銘柄名,市場名称,口座区分,取引区分,売買区分,信用区分,弁済期限,数量［株］,単価［円］,手数料［円］,税金等［円］,諸費用［円］,税区分,受渡金額［円］,建約定日,建単価［円］,建手数料［円］,建手数料消費税［円］,金利（支払）〔円〕,金利（受取）〔円〕,逆日歩／特別空売り料（支払）〔円〕,逆日歩（受取）〔円〕,貸株料,事務管理費〔円〕（税抜）,名義書換料〔円〕（税抜）
	//"2022/12/12","2023/10/12","7198","アルヒ","東証","特定","","入庫","-","-","100","987.0","0","0","0","-","0","-","0.0","0","0","0","0","0","0","0","0","0"
	//"2025/5/16","2025/5/20","8410","セブン銀行","東証","特定","現物","買付","-","-","800","254.0","0","0","0","-","203,200","-","0.0","0","0","0","0","0","0","0","0","0"
	//"2025/5/16","2025/5/20","9432","日本電信電話","ToSTNeT","特定","現物","売付","-","-","2,800","154.1","0","0","0","源徴あり","431,480","-","0.0","0","0","0","0","0","0","0","0","0"
	
	@CSVUtil.ColumnName("約定日")                              public String  tradeDate;         // 2025/5/20
	@CSVUtil.ColumnName("受渡日")                              public String  settlementDate;    // 2025/5/20
	@CSVUtil.ColumnName("銘柄コード")                          public String  code;              // stock code
	@CSVUtil.ColumnName("銘柄名")                              public String  name;              // stock nane
	@CSVUtil.ColumnName("市場名称")                            public Market  marketName;        // "東証"
	@CSVUtil.ColumnName("口座区分")                            public Account account;           // "特定"
	@CSVUtil.ColumnName("取引区分")                            public Mode    mode;              // "現物" ""
	@CSVUtil.ColumnName("売買区分")                            public Type    type;              // "買付" "売付" "入庫"
	@CSVUtil.ColumnName("信用区分")                            public String  marginType;        // "-"
	@CSVUtil.ColumnName("弁済期限")                            public String  marginDueDate;     // "-"
	@CSVUtil.ColumnName("数量［株］")                          public String  units;             // "2,800" "-"
	@CSVUtil.ColumnName("単価［円］")                          public String  unitPrice;         // "83.8500" or "-"
	@CSVUtil.ColumnName("手数料［円］")                        public String  feeA;              // "83.85" "-"
	@CSVUtil.ColumnName("税金等［円］")                        public String  feeB;              // "83.85" "-"
	@CSVUtil.ColumnName("諸費用［円］")                        public String  feeC;              // "83.85" "-"
	@CSVUtil.ColumnName("税区分")                              public String  taxWithholding;    // "源徴あり" "-"
	@CSVUtil.ColumnName("受渡金額［円］")                      public String  amount;            // "413,000"
	@CSVUtil.ColumnName("建約定日")                            public String  marginBuyDate;
	@CSVUtil.ColumnName("建単価［円］")                        public String  marginBuyUnitPrice;
	@CSVUtil.ColumnName("建手数料［円］")                      public String  marginBuyFee;
	@CSVUtil.ColumnName("建手数料消費税［円］")                public String  marginBuyTax;
	@CSVUtil.ColumnName("金利（支払）〔円〕")                  public String  marginInterestPay;
	@CSVUtil.ColumnName("金利（受取）〔円〕")                  public String  marginInterestReceive;
	@CSVUtil.ColumnName("逆日歩／特別空売り料（支払）〔円〕")  public String  marginReverseDailyRatePay;
	@CSVUtil.ColumnName("逆日歩（受取）〔円〕")                public String  marginReverseDailyRateReceive;
	@CSVUtil.ColumnName("貸株料")                              public String  marginLendingFee;
	@CSVUtil.ColumnName("事務管理費〔円〕（税抜）")            public String  marginManagementFee;
	@CSVUtil.ColumnName("名義書換料〔円〕（税抜）")            public String  marginNameChangeFee;
	
	@Override
	public String toString() {
		return ToString.withFieldName(this);
	}
	
	
	static List<Transaction> toTransaction(List<TradeHistoryJP> list) {
		buildStockNameCode(list);
		
		var ret = new ArrayList<Transaction>();
		for(var e: list) {
			var transaction = toTransaction(e);
			if (transaction == null) continue;
			ret.add(transaction);
		}
		logger.info("toTransaction  {}", ret.size());
		return ret;
	}
	static Transaction toTransaction(TradeHistoryJP e) {
		var function = functionMap.get(e.type);
		if (function == null) {
			logger.error("Uneexpected type");
			logger.error("  type  {}", e.type);
			throw new UnexpectedException("Uneexpected type");
		}
		return function.apply(e);
	}
	private static Map<Type, Function<TradeHistoryJP, Transaction>> functionMap = Map.ofEntries(
		Map.entry(Type.BUY,      new Functions.BUY()),
		Map.entry(Type.SELL,     new Functions.SELL()),
		Map.entry(Type.DEPOSIT,  new Functions.DEPOSIT()),
		Map.entry(Type.WITHDRAW, new Functions.WITHDRAW())
	);
	private static class Functions {
		private static class BUY implements Function<TradeHistoryJP, Transaction> {
			@Override
			public Transaction apply(TradeHistoryJP e) {
				var ret = new Transaction();
				
				ret.settlementDate = toLocalDate(e.settlementDate);
				ret.tradeDate      = toLocalDate(e.tradeDate);
				ret.currency       = Transaction.Currency.JPY;
				ret.type           = Transaction.Type.BUY;
				ret.asset          = Transaction.Asset.STOCK_JP;
				ret.units          = Integer.valueOf(e.units.replace(",", ""));
				ret.amount         = Integer.valueOf(e.amount.replace(",", ""));
				ret.code           = TradeHistoryJP.toStockCode(e.name);
				ret.comment        = TradeHistoryJP.toStockName(ret.code);
				
				return ret;
			}
		}
		private static class SELL implements Function<TradeHistoryJP, Transaction> {
			@Override
			public Transaction apply(TradeHistoryJP e) {
				var ret = new Transaction();
				
				ret.settlementDate = toLocalDate(e.settlementDate);
				ret.tradeDate      = toLocalDate(e.tradeDate);
				ret.currency       = Transaction.Currency.JPY;
				ret.type           = Transaction.Type.SELL;
				ret.asset          = Transaction.Asset.STOCK_JP;
				ret.units          = Integer.valueOf(e.units.replace(",", ""));
				ret.amount         = Integer.valueOf(e.amount.replace(",", ""));
				ret.code           = TradeHistoryJP.toStockCode(e.name);
				ret.comment        = TradeHistoryJP.toStockName(ret.code);
				
				return ret;
			}
		}
		private static class DEPOSIT implements Function<TradeHistoryJP, Transaction> {
			@Override
			public Transaction apply(TradeHistoryJP e) {
				var ret = new Transaction();
				
				ret.settlementDate = toLocalDate(e.settlementDate);
				ret.tradeDate      = toLocalDate(e.tradeDate);
				ret.currency       = Transaction.Currency.JPY;
				ret.type           = Transaction.Type.DEPOSIT;
				ret.asset          = Transaction.Asset.STOCK_JP;
				ret.units          = Integer.valueOf(e.units.replace(",", ""));
				ret.amount         = new BigDecimal(e.unitPrice.replace(",", "")).multiply(BigDecimal.valueOf(ret.units)).intValue();
				ret.code           = TradeHistoryJP.toStockCode(e.name);
				ret.comment        = TradeHistoryJP.toStockName(ret.code);
				
				return ret;
			}
		}
		private static class WITHDRAW implements Function<TradeHistoryJP, Transaction> {
			@Override
			public Transaction apply(TradeHistoryJP e) {
				throw new UnexpectedException(""); // FIXME
			}
		}
	}
	
	private static Map<String, String> stockNameMap = new TreeMap<>();
	//                 name    code
	private static void buildStockNameCode(List<TradeHistoryJP> list) {
		for(var e: list) {
			stockNameMap.put(e.name,  StockCodeJP.toStockCode5(e.code));
		}
	}
	public static String toStockCode(String stockName) {
		// find stock code from stockNameMap
		{
			var code = stockNameMap.get(stockName);
			if (code != null) return code;
		}
		
		// find stock code from stockNameMap
		for(var entry: stockNameMap.entrySet()) {
			var name = entry.getKey();
			var code = entry.getValue();
			
			if (stockName.replace("　", "").equals(name)) {
				// save for later use
				stockNameMap.put(stockName, code);
				return code;
			}
		}
		
		// find stock code from stockCodeMap
		for(var entry: stockCodeMap.entrySet()) {
			var code = entry.getKey();
			var name = entry.getValue();
			
			if (name.startsWith(stockName)) {
				// save for later use
				stockNameMap.put(stockName, code);
				return code;
			}
		}
		
		logger.error("Unexpected name");
		logger.error("  name  {}!", stockName);
		throw new UnexpectedException("Unexpected name");
	}
	//
	private static Map<String, String> stockCodeMap = StorageStock.StockInfoJP.getList().stream().collect(Collectors.toMap(o -> o.stockCode, o -> o.name));
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
