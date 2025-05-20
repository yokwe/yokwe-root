package yokwe.finance.trade.rakuten;

import java.io.File;

import yokwe.util.CSVUtil;
import yokwe.util.ToString;

public class TradeHistoryUS {
	private static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();
	
	public enum AccountType {
		NONE   ("-"),
		SPECIAL("特定"),
		;
		
		public final String string;
		private AccountType(String string) {
			this.string = string;
		}
		
		@Override
		public String toString() {
			return string;
		}
	}
	
	public enum TradeMode {
		CASH  ("現物"),
		MARGIN("信用"),
		;
		
		public final String string;
		private TradeMode(String string) {
			this.string = string;
		}
		
		@Override
		public String toString() {
			return string;
		}
	}

	public enum TradeType {
		BUY ("買付"),
		SELL("売付"),
		;
		
		public final String string;
		private TradeType(String string) {
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
	
	@CSVUtil.ColumnName("約定日")               public String      tradeDate;         // 2025/5/20
	@CSVUtil.ColumnName("受渡日")               public String      settlementDate;    // 2025/5/20
	@CSVUtil.ColumnName("ティッカー")           public String      code;              // stock code
	@CSVUtil.ColumnName("銘柄名")               public String      name;              // stock name
	@CSVUtil.ColumnName("口座")                 public AccountType accountType;       // "特定"
	@CSVUtil.ColumnName("取引区分")             public TradeMode   cashOrMargin;      // "現物"
	@CSVUtil.ColumnName("売買区分")             public TradeType   buyOrSell;         // "買付" "売付"
	@CSVUtil.ColumnName("信用区分")             public String      marginType;        // "-"
	@CSVUtil.ColumnName("弁済期限")             public String      marginDueDate;           // "-"
	@CSVUtil.ColumnName("決済通貨")             public Currency    currency;          // "ＵＳドル"
	@CSVUtil.ColumnName("数量［株］")           public String      units;             // "2,800" "-"
	@CSVUtil.ColumnName("単価［USドル］")       public String      unitPrice;         // "83.8500" or "-"
	@CSVUtil.ColumnName("約定代金［USドル］")   public String      contractAmount;    // "83.85" "-"
	@CSVUtil.ColumnName("為替レート")           public String      fxRate;            // "143.9"
	@CSVUtil.ColumnName("手数料［USドル］")     public String      fee;               // "83.85" "-"
	@CSVUtil.ColumnName("税金［USドル］")       public String      tax;               // "83.85" "-"
	@CSVUtil.ColumnName("受渡金額［USドル］")   public String      deliveryAmount;    // "83.85"
	@CSVUtil.ColumnName("受渡金額［円］")       public String      deliveryAmountJPY; // "83.85" "-"
	
	
	@Override
	public String toString() {
		return ToString.withFieldName(this);
	}
	
	public static void main(String[] args) {
		logger.info("START");
		
		var file = new File("tmp/tradeHistory/tradehistory(US)_20250517.csv");
		if (!file.exists()) logger.warn("file not exists");
		var list = CSVUtil.read(TradeHistoryUS.class).file(file);
		logger.info("load  {}  {}", list.size(), file.getPath());
		
		logger.info("STOP");
	}

}
