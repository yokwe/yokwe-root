package yokwe.finance.trade.rakuten;

import java.io.File;

import yokwe.util.CSVUtil;
import yokwe.util.ToString;

public class TradeHistoryFB {
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
	
	@CSVUtil.ColumnName("約定日")           public String      tradeDate;         // 2025/5/20
	@CSVUtil.ColumnName("受渡日")           public String      settlementDate;    // 2025/5/20
	@CSVUtil.ColumnName("種類")             public String      bondCategory;      // 外国債券
	@CSVUtil.ColumnName("銘柄コード")       public String      bondCode;          // A00396006
	@CSVUtil.ColumnName("銘柄名")           public String      bondName;          // "三井住友フィナンシャルグループ 米ドル建債券 3.446% 2027/01/11"
	@CSVUtil.ColumnName("口座")             public AccountType accountType;       // "特定"
	@CSVUtil.ColumnName("通貨")             public Currency    fundCurrency;      // "米ドル" or "-"
	@CSVUtil.ColumnName("決済通貨")         public Currency    tradeCurrency;     // "米ドル" or "-"
	@CSVUtil.ColumnName("償還日")           public String      redemptionDate;    // 2027/1/11
	@CSVUtil.ColumnName("年利率（税引前）") public String      interestRate;      // "3.45 %"
	@CSVUtil.ColumnName("売買区分")         public TradeType   buyOrSell;         // "買付" "売付"
	@CSVUtil.ColumnName("数量（額面）")     public String      units;             // "2,000"
	@CSVUtil.ColumnName("約定単価")         public String      unitPrice;         // "95.49 %"
	@CSVUtil.ColumnName("適用為替レート")   public String      fxRate;            // "143.88"
	@CSVUtil.ColumnName("経過利子")         public String      accuredInterest;   // "5.17"
	@CSVUtil.ColumnName("受渡金額")         public String      deliveryAmount;    // "1,914.97"
	
	@Override
	public String toString() {
		return ToString.withFieldName(this);
	}
	
	public static void main(String[] args) {
		logger.info("START");
		
		var file = new File("tmp/tradeHistory/tradehistory(FB)_20250517.csv");
		if (!file.exists()) logger.warn("file not exists");
		var list = CSVUtil.read(TradeHistoryFB.class).file(file);
		logger.info("load  {}  {}", list.size(), file.getPath());
		
		logger.info("STOP");
	}

}
