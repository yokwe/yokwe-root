package yokwe.finance.trade.rakuten;

import java.io.File;

import yokwe.util.CSVUtil;
import yokwe.util.ToString;

public class AdjustHistoryJP {
	private static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();
	
	public enum TradeType {
		DIVIDEND_STOCK    ("国内株式配当金"),
		DEPOSIT_CAMPAIGN  ("手数料キャンペーン"),
		DEOSIT_TRANSFER   ("振替入金"),
		WITHDRAW_TRANSFER ("振替出金"),
		SELL_STOCK        ("株式売却"),
		SELL_FUND         ("株式投信解約"),
		BUY_FUND          ("株式投信購入"),
		BUY_STOCK         ("株式購入"),
		TAX_INCOME        ("譲渡益税（所得税）"),
		TAX_LOCAL         ("譲渡益税（住民税）"),
		TAX_REFUND_INCOME ("還付金（所得税）"),
		TAX_REFUND_LOCAL  ("還付金（住民税）"),
		DEPOSIT_REALTIME  ("リアルタイム入金(三井住友銀行)"),
		DEPOSIT_POINT     ("入金(楽天証券ポイント交換)"),
		WITHDRAW          ("通常出金(三井住友銀行)");
		
		public final String string;
		private TradeType(String string) {
			this.string = string;
		}
		
		@Override
		public String toString() {
			return string;
		}
	}
	
	public enum AccountType {
		NONE   ("-"),
		SPECIAL("特定");
		
		public final String string;
		private AccountType(String string) {
			this.string = string;
		}
		
		@Override
		public String toString() {
			return string;
		}
	}


	//受渡日,約定日,取引区分,口座区分,対象証券名,単価［円/％］,数量［株/口/額面］,受渡金額（受取）,受渡金額（支払）,預り金（MRF）［円］
	//"2025/5/20","2025/5/20","譲渡益税（住民税）","-","-","-","-","-","3,395","5,103,375"
	
	@CSVUtil.ColumnName("受渡日")               public String      settlementDate;  // 2025/5/20
	@CSVUtil.ColumnName("約定日")               public String      tradeDate;       // 2025/5/20
	@CSVUtil.ColumnName("取引区分")             public TradeType   tradeType;       // 譲渡益税（住民税）
	@CSVUtil.ColumnName("口座区分")             public AccountType accountType;     // "特定" "-"
	@CSVUtil.ColumnName("対象証券名")           public String      name;            // name or "-"
	@CSVUtil.ColumnName("単価［円/％］")        public String      unitPrice;       // "3,376.00000" or "-"
	@CSVUtil.ColumnName("数量［株/口/額面］")   public String      units;           // "2,800" "-"
	@CSVUtil.ColumnName("受渡金額（受取）")     public String      amountDeposit;   // "1,012,800" "-"
	@CSVUtil.ColumnName("受渡金額（支払）")     public String      amountWithdraw;  // "286,300" "-"
	@CSVUtil.ColumnName("預り金（MRF）［円］")  public String      balance;         // "1,020,568" ""
	
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

	
	public static void main(String[] args) {
		logger.info("START");
		
		var file = new File("tmp/tradeHistory/adjusthistory(JP)_20250517.csv");
		if (!file.exists()) logger.warn("file not exists");
		var list = CSVUtil.read(AdjustHistoryJP.class).file(file);
		logger.info("load  {}  {}", list.size(), file.getPath());
		
		logger.info("STOP");
	}
}
