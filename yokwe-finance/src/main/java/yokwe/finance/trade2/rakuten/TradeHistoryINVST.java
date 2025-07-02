package yokwe.finance.trade2.rakuten;

import static yokwe.finance.trade2.rakuten.UpdateTransaction.toLocalDate;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.Function;

import yokwe.finance.fund.StorageFund;
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
	@CSVUtil.ColumnName("ファンド名")                  public String   name;              // "東京海上セレクション・物価連動国債(うんよう博士)"
	@CSVUtil.ColumnName("分配金")                      public String   fundType;          // "再投資型"
	@CSVUtil.ColumnName("口座")                        public Account  account;           // "特定"
	@CSVUtil.ColumnName("取引")                        public Type     type;              // "買付" "売付"
	@CSVUtil.ColumnName("買付方法")                    public String   buyType;           // "通常"
	@CSVUtil.ColumnName("数量［口］")                  public String   units;             // "830,358"
	@CSVUtil.ColumnName("単価")                        public String   unitPrice;         // "12,043"
	@CSVUtil.ColumnName("経費")                        public String   fee;               // "0"
	@CSVUtil.ColumnName("為替レート")                  public String   fxRate;            // "-"
	@CSVUtil.ColumnName("受付金額[現地通貨]")          public String   amount;            // "-"
	@CSVUtil.ColumnName("受渡金額/(ポイント利用)[円]") public String   amountJPY;         // "1,000,000"
	@CSVUtil.ColumnName("決済通貨")                    public Currency currency;          // "円"
	
	@Override
	public String toString() {
		return ToString.withFieldName(this);
	}
	
	
	static List<Transaction> toTransaction(List<TradeHistoryINVST> list) {
		FundPriceInfoJP.load();

		var ret = new ArrayList<Transaction>();
		for(var e: list) {
			var transaction = toTransaction(e);
			if (transaction == null) continue;
			ret.add(transaction);
		}
		logger.info("toTransaction  {}", ret.size());
		
		FundPriceInfoJP.save();
		
		return ret;
	}
	static Transaction toTransaction(TradeHistoryINVST e) {
		var function = functionMap.get(e.currency).get(e.type);
		if (function == null) {
			logger.error("Uneexpected type");
			logger.error("  type  {}", e.type);
			throw new UnexpectedException("Uneexpected type");
		}
		return function.apply(e);
	}
	
	private static Map<Currency, Map<Type, Function<TradeHistoryINVST, Transaction>>> functionMap =Map.ofEntries(
		Map.entry(Currency.JPY, Map.ofEntries(
			Map.entry(Type.BUY,      new FunctionsJPY.BUY()),
			Map.entry(Type.SELL,     new FunctionsJPY.SELL()),
			Map.entry(Type.REINVEST, new FunctionsJPY.REINVEST())
		)),
		Map.entry(Currency.USD, Map.ofEntries(
			Map.entry(Type.BUY,      new FunctionsUSD.BUY()),
			Map.entry(Type.SELL,     new FunctionsUSD.SELL()),
			Map.entry(Type.REINVEST, new FunctionsUSD.REINVEST())))
	);
	
	
	private static class FunctionsJPY {
		private static class BUY implements Function<TradeHistoryINVST, Transaction> {
			@Override
			public Transaction apply(TradeHistoryINVST e) {
				var code = toFundCode(e.name);
				var name = toFundName(code);

				FundPriceInfoJP.add(code, name, e);

				int amountJPY;
//				int amountPoint;
				{
					var string = e.amountJPY.replace(",", "");
					if (string.contains("(")) {
						var i = string.indexOf("(");
//						var j = string.indexOf(")");
						amountJPY   = Integer.valueOf(string.substring(0, i));
//						amountPoint = Integer.valueOf(string.substring(i + 1, j));
					} else {
						amountJPY   = Integer.valueOf(string);
//						amountPoint = 0;
					}
				}
				
				var ret = new Transaction();
				
				ret.settlementDate = toLocalDate(e.settlementDate);
				ret.tradeDate      = toLocalDate(e.tradeDate);
				ret.currency       = Transaction.Currency.JPY;
				ret.type           = Transaction.Type.BUY;
				ret.asset          = Transaction.Asset.FUND_JP;
				ret.units          = Integer.valueOf(e.units.replace(",", ""));
//				ret.amount         = amountJPY - amountPoint;
				ret.amount         = amountJPY; // to match the value of "預り金（MRF）［円］" in adjusthistory\(JP\)_XXXXXXXX.csv, use amountJPY
				ret.code           = code;
				ret.comment        = name;
				
				return ret;
			}
		}
		private static class SELL implements Function<TradeHistoryINVST, Transaction> {
			@Override
			public Transaction apply(TradeHistoryINVST e) {
				var ret = new Transaction();
				
				ret.settlementDate = toLocalDate(e.settlementDate);
				ret.tradeDate      = toLocalDate(e.tradeDate);
				ret.currency       = Transaction.Currency.JPY;
				ret.type           = Transaction.Type.SELL;
				ret.asset          = Transaction.Asset.FUND_JP;
				ret.units          = Integer.valueOf(e.units.replace(",", ""));
				ret.amount         = Integer.valueOf(e.amountJPY.replace(",", ""));
				ret.code           = toFundCode(e.name);
				ret.comment        = toFundName(ret.code);
				
				return ret;
			}
		}
		private static class REINVEST implements Function<TradeHistoryINVST, Transaction> {
			@Override
			public Transaction apply(TradeHistoryINVST t) {
				throw new UnexpectedException(""); // FIXME
			}
		}
	}
	private static class FunctionsUSD {
		private static final Set<String> mmfSet = new HashSet<>();
		static {
			mmfSet.add("GS米ドルファンド");
			mmfSet.add("ノーザン・トラスト・米ドル・リクイディティ・ファンド(楽天・米ドルMMF)");
		}
		private static class BUY implements Function<TradeHistoryINVST, Transaction> {
			@Override
			public Transaction apply(TradeHistoryINVST e) {
				if (!mmfSet.contains(e.name)) {
					throw new UnexpectedException("Unexpected mmf name"); // FIXME
				}
				
				var ret = new Transaction();
				
				ret.settlementDate = toLocalDate(e.settlementDate);
				ret.tradeDate      = toLocalDate(e.tradeDate);
				ret.currency       = Transaction.Currency.USD;
				ret.type           = Transaction.Type.BUY;
				ret.asset          = Transaction.Asset.MMF_US;
				ret.units          = Integer.valueOf(e.units.replace(",", ""));
				ret.amount         = new BigDecimal(e.amount.replace(",", "")).movePointRight(2).intValue();
				ret.code           = "";
				ret.comment        = e.name;
				
				return ret;
			}
		}
		private static class SELL implements Function<TradeHistoryINVST, Transaction> {
			@Override
			public Transaction apply(TradeHistoryINVST e) {
				if (!e.name.equals("GS米ドルファンド")) {
					throw new UnexpectedException(""); // FIXME
				}
				
				var ret = new Transaction();
				
				ret.settlementDate = toLocalDate(e.settlementDate);
				ret.tradeDate      = toLocalDate(e.tradeDate);
				ret.currency       = Transaction.Currency.USD;
				ret.type           = Transaction.Type.SELL;
				ret.asset          = Transaction.Asset.MMF_US;
				ret.units          = Integer.valueOf(e.units.replace(",", ""));
				ret.amount         = new BigDecimal(e.amount.replace(",", "")).movePointRight(2).intValue();
				ret.code           = "";
				ret.comment        = e.name;
				
				return ret;
			}
		}
		private static class REINVEST implements Function<TradeHistoryINVST, Transaction> {
			@Override
			public Transaction apply(TradeHistoryINVST e) {
				if (!e.name.equals("GS米ドルファンド")) {
					throw new UnexpectedException(""); // FIXME
				}
				
				var ret = new Transaction();
				
				ret.settlementDate = toLocalDate(e.settlementDate);
				ret.tradeDate      = toLocalDate(e.tradeDate);
				ret.currency       = Transaction.Currency.USD;
				ret.type           = Transaction.Type.DIVIDEND;
				ret.asset          = Transaction.Asset.MMF_US;
				ret.units          = Integer.valueOf(e.units.replace(",", ""));
				ret.amount         = ret.units;
				ret.code           = "";
				ret.comment        = e.name;
				
				return ret;
			}
		}
	}
	
	private static Map<String, String> fundNameMap = new TreeMap<>();
	//                 name    isinCode
	private static Map<String, String> fundCodeMap = new TreeMap<>();
	//                 isinCode name
	static {
		for(var e: yokwe.finance.provider.rakuten.StorageRakuten.FundCodeNameRakuten.getList()) {
			var name = e.name;
			var code = e.isinCode;
			fundNameMap.put(name, code);
		}
		for(var e: StorageFund.FundInfo.getList()) {
			var name = e.name;
			var code = e.isinCode;
			fundNameMap.put(name, code);
			fundCodeMap.put(code, name);
		}
	}
	public static String toFundCode(String name) {
		if (name.contains("/")) {
			name = name.substring(0, name.indexOf("/"));
		}
		{
			var code = fundNameMap.get(name);
			if (code != null) return code;
		}
		{
			if (name.contains("(")) {
				var code = fundNameMap.get(name.substring(0, name.indexOf("(")));
				if (code != null) return code;
			}
		}
		{
			if (name.contains("SMT")) {
				var code = fundNameMap.get(name.replace("SMT", "ＳＭＴ"));
				if (code != null) return code;
			}
		}
		{
			var code = fundNameMap.get(name.replace(" ", ""));
			if (code != null) return code;
		}
		{
			var code = fundNameMap.get(name.replace("(", "（").replace(")", "）"));
			if (code != null) return code;
		}
		logger.error("Unexpected name");
		logger.error("  name  {}!", name);
		throw new UnexpectedException("Unexpected name");
	}
	public static String toFundName(String code) {
		var ret = fundCodeMap.get(code);
		if (ret != null) return ret;
		logger.error("Unexpected code");
		logger.error("  code  {}!", code);
		throw new UnexpectedException("Unexpected code");
	}

}
