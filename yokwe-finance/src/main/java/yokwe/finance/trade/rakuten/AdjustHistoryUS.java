package yokwe.finance.trade.rakuten;

import java.io.File;

import yokwe.util.CSVUtil;
import yokwe.util.ToString;

public class AdjustHistoryUS {
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
		DIVIDEND_BOND     ("外国債券利金"),
		SELL_BOND         ("外国債券売却"),
		BUY_BOND          ("外国債券購入"),
		DIVIDEND_STOCK    ("外株配当金"),
		DEOSIT_TRANSFER   ("振替入金"),
		WITHDRAW_TRANSFER ("振替出金"),
		SELL_STOCK        ("米国株式売却"),
		BUY_STOCK         ("米国株式購入"),
		SELL_MMF          ("外貨建てＭＭＦ解約"),
		BUY_MMF           ("外貨建てＭＭＦ買付"),
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
	
	//受渡日,約定日,口座区分,取引区分,対象証券名,決済通貨,単価,数量［株 /口］,受渡金額（受取）,受渡金額（受取）[円換算],受渡金額（支払）,受渡金額（支払）[円換算],為替レート,預り金
	//"2024/8/7","2024/8/5","-","振替出金","-","米ドル","-","-","-","-","1.28","184","144.531","0.00"
	
	@CSVUtil.ColumnName("受渡日")                    public String      settlementDate;    // 2025/5/20
	@CSVUtil.ColumnName("約定日")                    public String      tradeDate;         // 2025/5/20
	@CSVUtil.ColumnName("口座区分")                  public AccountType accountType;       // "特定" "-"
	@CSVUtil.ColumnName("取引区分")                  public TradeType   tradeType;         // 譲渡益税（住民税）
	@CSVUtil.ColumnName("対象証券名")                public String      name;              // name or "-"
	@CSVUtil.ColumnName("決済通貨")                  public Currency    currency;          // "米ドル" or "-"
	@CSVUtil.ColumnName("単価")                      public String      unitPrice;         // "3,376.00000" or "-"
	@CSVUtil.ColumnName("数量［株 /口］")            public String      units;             // "2,800" "-"
	@CSVUtil.ColumnName("受渡金額（受取）")          public String      amountDeposit;     // "1,012,800" "-"
	@CSVUtil.ColumnName("受渡金額（受取）[円換算]")  public String      amountDepositJPY;  // "1,012,800" "-"
	@CSVUtil.ColumnName("受渡金額（支払）")          public String      amountWithdraw;    // "1,012,800" "-"
	@CSVUtil.ColumnName("受渡金額（支払）[円換算]")  public String      amountWithdrawJPY; // "1,012,800" "-"
	@CSVUtil.ColumnName("為替レート")                public String      fxRate;            // "143.9"
	@CSVUtil.ColumnName("預り金")                    public String      balance;           // "1,020,568" ""
	
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

	public static void main(String[] args) {
		logger.info("START");
		
		var file = new File("tmp/tradeHistory/adjusthistory(US)_20250517.csv");
		if (!file.exists()) logger.warn("file not exists");
		var list = CSVUtil.read(AdjustHistoryUS.class).file(file);
		logger.info("load  {}  {}", list.size(), file.getPath());
		
		logger.info("STOP");
	}
}
