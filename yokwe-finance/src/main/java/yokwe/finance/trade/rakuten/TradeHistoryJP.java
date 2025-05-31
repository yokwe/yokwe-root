package yokwe.finance.trade.rakuten;

import java.io.File;

import yokwe.util.CSVUtil;
import yokwe.util.ToString;

public class TradeHistoryJP {
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
		NONE  (""),
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
		BUY     ("買付"),
		SELL    ("売付"),
		DEPOSIT ("入庫"),
		WITHDRAW("出庫"),
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
	
	//約定日,受渡日,銘柄コード,銘柄名,市場名称,口座区分,取引区分,売買区分,信用区分,弁済期限,数量［株］,単価［円］,手数料［円］,税金等［円］,諸費用［円］,税区分,受渡金額［円］,建約定日,建単価［円］,建手数料［円］,建手数料消費税［円］,金利（支払）〔円〕,金利（受取）〔円〕,逆日歩／特別空売り料（支払）〔円〕,逆日歩（受取）〔円〕,貸株料,事務管理費〔円〕（税抜）,名義書換料〔円〕（税抜）
	//"2022/12/12","2023/10/12","7198","アルヒ","東証","特定","","入庫","-","-","100","987.0","0","0","0","-","0","-","0.0","0","0","0","0","0","0","0","0","0"
	//"2025/5/16","2025/5/20","8410","セブン銀行","東証","特定","現物","買付","-","-","800","254.0","0","0","0","-","203,200","-","0.0","0","0","0","0","0","0","0","0","0"
	//"2025/5/16","2025/5/20","9432","日本電信電話","ToSTNeT","特定","現物","売付","-","-","2,800","154.1","0","0","0","源徴あり","431,480","-","0.0","0","0","0","0","0","0","0","0","0"
	
	@CSVUtil.ColumnName("約定日")                              public String      tradeDate;         // 2025/5/20
	@CSVUtil.ColumnName("受渡日")                              public String      settlementDate;    // 2025/5/20
	@CSVUtil.ColumnName("銘柄コード")                          public String      code;              // stock code
	@CSVUtil.ColumnName("銘柄名")                              public String      name;              // stock nane
	@CSVUtil.ColumnName("市場名称")                            public String      marketName;        // "東証"
	@CSVUtil.ColumnName("口座区分")                            public AccountType accountType;       // "特定"
	@CSVUtil.ColumnName("取引区分")                            public TradeMode   cashOrMargin;      // "現物" ""
	@CSVUtil.ColumnName("売買区分")                            public TradeType   tradeType;         // "買付" "売付" "入庫"
	@CSVUtil.ColumnName("信用区分")                            public String      marginType;        // "-"
	@CSVUtil.ColumnName("弁済期限")                            public String      marginDueDate;     // "-"
	@CSVUtil.ColumnName("数量［株］")                          public String      units;             // "2,800" "-"
	@CSVUtil.ColumnName("単価［円］")                          public String      unitPrice;         // "83.8500" or "-"
	@CSVUtil.ColumnName("手数料［円］")                        public String      feeA;              // "83.85" "-"
	@CSVUtil.ColumnName("税金等［円］")                        public String      feeB;              // "83.85" "-"
	@CSVUtil.ColumnName("諸費用［円］")                        public String      feeC;              // "83.85" "-"
	@CSVUtil.ColumnName("税区分")                              public String      taxWithholding;    // "源徴あり" "-"
	@CSVUtil.ColumnName("受渡金額［円］")                      public String      deliveryAmount;    // "413,000"
	@CSVUtil.ColumnName("建約定日")                            public String      marginBuyDate;
	@CSVUtil.ColumnName("建単価［円］")                        public String      marginBuyUnitPrice;
	@CSVUtil.ColumnName("建手数料［円］")                      public String      marginBuyFee;
	@CSVUtil.ColumnName("建手数料消費税［円］")                public String      marginBuyTax;
	@CSVUtil.ColumnName("金利（支払）〔円〕")                  public String      marginInterestPay;
	@CSVUtil.ColumnName("金利（受取）〔円〕")                  public String      marginInterestReceive;
	@CSVUtil.ColumnName("逆日歩／特別空売り料（支払）〔円〕")  public String      marginReverseDailyRatePay;
	@CSVUtil.ColumnName("逆日歩（受取）〔円〕")                public String      marginReverseDailyRateReceive;
	@CSVUtil.ColumnName("貸株料")                              public String      marginLendingFee;
	@CSVUtil.ColumnName("事務管理費〔円〕（税抜）")            public String      marginManagementFee;
	@CSVUtil.ColumnName("名義書換料〔円〕（税抜）")            public String      marginNameChangeFee;
	
	@Override
	public String toString() {
		return ToString.withFieldName(this);
	}
	
	
	public static void main(String[] args) {
		logger.info("START");
		
		var file = new File("tmp/tradeHistory/tradehistory(JP)_20250517.csv");
		if (!file.exists()) logger.warn("file not exists");
		var list = CSVUtil.read(TradeHistoryJP.class).file(file);
		logger.info("load  {}  {}", list.size(), file.getPath());
		
		logger.info("STOP");
	}

}
