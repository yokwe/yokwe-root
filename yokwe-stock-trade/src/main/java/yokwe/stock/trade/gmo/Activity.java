package yokwe.stock.trade.gmo;

import java.time.format.DateTimeFormatter;

import yokwe.util.libreoffice.Sheet;

@Sheet.HeaderRow(0)
@Sheet.DataRow(1)
public class Activity extends Sheet {
	public static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy/MM/DD HH:mm:ss");
	public static final DateTimeFormatter DATE_FORMATTER      = DateTimeFormatter.ofPattern("yyyy/MM/DD");

	public static final String TRADE_CONNECT_AUTOMATIC_DEPOSIT  = "コネクト自動入金";
	public static final String TRADE_CONNECT_AUTOMATIC_WITHDRAW = "コネクト自動出金";
	public static final String TRADE_CASH_TRANSACTION           = "現物";
	public static final String TRADE_DEPOSIT                    = "入金";
	public static final String TRADE_WITHDRAW                   = "出金";
	public static final String TRADE_DIVIDEND_DEPOSIT           = "配当金（入金）";
	public static final String TRADE_TRANSFER_TAX_COLLECTION    = "譲渡益税徴収金";
	public static final String TRADE_TRANSFER_TAX_REFUND        = "譲渡益税還付金";
	
	public static final String TRANSACTION_BUY  = "買";
	public static final String TRANSACTION_SELL = "売";

//	public static enum Trade {
//		CONNECT_AUTOMATIC_DEPOSIT ("コネクト自動入金"),
//		CONNECT_AUTOMATIC_WITHDRAW("コネクト自動出金"),
//		CASH_TRANSACTION          ("現物"),
//		DEPOSIT                   ("入金"),
//		WITHDRAW                  ("出金");
//		
//		public final String value;
//		Trade(String value) {
//			this.value = value;
//		}
//		@Override
//		public String toString() {
//			return value;
//		}
//
//		private static final Trade[] VALUES = Trade.values();
//		public static Trade getInstance(String value) {
//			if (value == null || value.isEmpty()) return null;
//			for(Trade e: VALUES) {
//				if (value.equals(e.value)) return e;
//			}
//			logger.error("Unknown value {}!", value);
//			throw new UnexpectedException("Unknown value");
//		}
//	}
//	public static enum Transaction {
//		BUY ("買"),
//		SELL("売");
//		
//		public final String value;
//		Transaction(String value) {
//			this.value = value;
//		}
//		@Override
//		public String toString() {
//			return value;
//		}
//
//		private static final Transaction[] VALUES = Transaction.values();
//		public static Transaction getInstance(String value) {
//			if (value == null || value.isEmpty()) return null;
//			for(Transaction e: VALUES) {
//				if (value.equals(e.value)) return e;
//			}
//			logger.error("Unknown value {}!", value);
//			throw new UnexpectedException("Unknown value");
//		}
//	}
//
//	public static class DomesticStockCashTransaction implements Comparable<DomesticStockCashTransaction>{
//		public LocalDateTime tradeDateTime;
//		public Trade         trade;
//		public LocalDate     settlementDate;
//		public String        tradeNumber;
//		public String        stockName;
//		public String        stockCode;
//		public Transaction   transactionType;
//		public int           tradeAmount;
//		public double        tradeUnitPrice;
//		public int           fee;
//		public int           feeTax;
//		public int           tradePrice;
//		public int           realizedProfitAndLoss;
//		public int           settlementPrice;
//		
//		public DomesticStockCashTransaction(
//				String tradeDateTime,
//				String trade,
//				String settlementDate,
//				String tradeNumber,
//				String stockName,
//				String stockCode,
//				String transactionType,
//				String tradeAmount,
//				String tradeUnitPrice,
//				String fee,
//				String feeTax,
//				String tradePrice,
//				String realizedProfitAndLoss,
//				String settlementPrice
//		) {
//			this.tradeDateTime         = (LocalDateTime)DATE_TIME_FORMATTER.parse(tradeDateTime);
//			this.trade                 = Trade.getInstance(trade);
//			this.settlementDate        = (LocalDate)DATE_FORMATTER.parse(settlementDate);
//			this.tradeNumber           = tradeNumber;
//			this.stockName             = stockName;
//			this.stockCode             = stockCode;
//			this.transactionType       = Transaction.getInstance(transactionType);
//			this.tradeAmount           = Integer.parseInt(tradeAmount);
//			this.tradeUnitPrice        = Double.parseDouble(tradeUnitPrice);
//			this.fee                   = Integer.parseInt(fee);
//			this.feeTax                = Integer.parseInt(feeTax);
//			this.tradePrice            = Integer.parseInt(tradePrice);
//			this.realizedProfitAndLoss = Integer.parseInt(realizedProfitAndLoss);
//			this.settlementPrice       = Integer.parseInt(settlementPrice);
//		}
//		public DomesticStockCashTransaction(
//				LocalDateTime tradeDateTime,
//				Trade         trade,
//				LocalDate     settlementDate,
//				String        tradeNumber,
//				String        stockName,
//				String        stockCode,
//				Transaction   transactionType,
//				int           tradeAmount,
//				double        tradeUnitPrice,
//				int           fee,
//				int           feeTax,
//				int           tradePrice,
//				int           realizedProfitAndLoss,
//				int           settlementPrice
//		) {
//			this.tradeDateTime = tradeDateTime;
//			this.trade = trade;
//			this.settlementDate = settlementDate;
//			this.tradeNumber = tradeNumber;
//			this.stockName = stockName;
//			this.stockCode = stockCode;
//			this.transactionType = transactionType;
//			this.tradeAmount = tradeAmount;
//			this.tradeUnitPrice = tradeUnitPrice;
//			this.fee = fee;
//			this.feeTax = feeTax;
//			this.tradePrice = tradePrice;
//			this.realizedProfitAndLoss = realizedProfitAndLoss;
//			this.settlementPrice = settlementPrice;
//		}
//		public DomesticStockCashTransaction() {
//			this(null, null, null, null, null, null, null, 0, 0, 0, 0, 0, 0, 0);
//		}
//		@Override
//		public int compareTo(DomesticStockCashTransaction that) {
//			int ret = this.tradeDateTime.compareTo(that.tradeDateTime);
//			if (ret == 0) ret = this.tradeNumber.compareTo(that.tradeNumber);
//			if (ret == 0) ret = this.stockCode.compareTo(that.stockCode);
//			return ret;
//		}
//	}
//	
	
	public static final String ACCOUNT_DESIGNATED = "特定";
	
	@ColumnName("約定日時")						public String tradeDateTime;                    // YYYY/MM/DD HH:MM:SS
	@ColumnName("取引区分")						public String tradeType;                        // コネクト自動入金 コネクト自動出金 現物 入金 出金 配当金（入金）
	@ColumnName("受渡日")						public String settlementDate;                   // YYYY/MM/DD
	@ColumnName("約定番号")						public String tradeNumber;
	@ColumnName("銘柄名")						public String stockName;
	@ColumnName("銘柄コード")					public String stockCode;
	@ColumnName("限月")							public String contractMonth;                    // NOT FOR DOMESTIC STOCK CASH TRANSACTION CASH TRANSACTION
	@ColumnName("コールプット区分")				public String callOrPut;                        // NOT FOR DOMESTIC STOCK CASH TRANSACTION CASH TRANSACTION
	@ColumnName("権利行使価格")					public String excercisePrice;                   // NOT FOR DOMESTIC STOCK CASH TRANSACTION CASH TRANSACTION
	@ColumnName("権利行使価格通貨")				public String excerciseCurrency;                // NOT FOR DOMESTIC STOCK CASH TRANSACTION CASH TRANSACTION
	@ColumnName("カバードワラント商品種別")	public String coveredWarrantType;               // NOT FOR DOMESTIC STOCK CASH TRANSACTION CASH TRANSACTION
	@ColumnName("売買区分")						public String transactionType;                  // 買 売
	@ColumnName("通貨")							public String currency;                         // NOT FOR DOMESTIC STOCK CASH TRANSACTION CASH TRANSACTION
	@ColumnName("市場")							public String market;                           // 東証
	@ColumnName("口座")							public String accountType;                      // 特定
	@ColumnName("信用区分")						public String marginType;                       // NOT FOR DOMESTIC STOCK CASH TRANSACTION CASH TRANSACTION
	@ColumnName("約定数量") 					public int    tradeAmount;
	@ColumnName("約定単価")						public int    tradeUnitPrice;                   // FIXME tradeUnitPrice has *fractional* value
	@ColumnName("コンバージョンレート")		public String conversionRate;                   // NOT FOR DOMESTIC STOCK CASH TRANSACTION CASH TRANSACTION
	@ColumnName("手数料")						public int    fee;
	@ColumnName("手数料消費税")					public int    feeTax;
	@ColumnName("建単価")						public String longUnitPrice;                    // NOT FOR DOMESTIC STOCK CASH TRANSACTION CASH TRANSACTION
	@ColumnName("新規手数料")					public String newFee;                           // NOT FOR DOMESTIC STOCK CASH TRANSACTION CASH TRANSACTION
	@ColumnName("新規手数料消費税")				public String newFeeTax;                        // NOT FOR DOMESTIC STOCK CASH TRANSACTION CASH TRANSACTION
	@ColumnName("管理費")						public String administrationFee;                // NOT FOR DOMESTIC STOCK CASH TRANSACTION CASH TRANSACTION
	@ColumnName("名義書換料")					public String transferFee;                      // NOT FOR DOMESTIC STOCK CASH TRANSACTION CASH TRANSACTION
	@ColumnName("金利")							public String interestRate;                     // NOT FOR DOMESTIC STOCK CASH TRANSACTION CASH TRANSACTION
	@ColumnName("貸株料")						public String stockLendingFee;                  // NOT FOR DOMESTIC STOCK CASH TRANSACTION CASH TRANSACTION
	@ColumnName("品貸料")						public String premiumFee;                       // NOT FOR DOMESTIC STOCK CASH TRANSACTION CASH TRANSACTION
	@ColumnName("前日分値洗")					public String previousDayMarkToMarket;          // NOT FOR DOMESTIC STOCK CASH TRANSACTION CASH TRANSACTION
	@ColumnName("経過利子（円貨）")				public String accruedInterestJPY;               // NOT FOR DOMESTIC STOCK CASH TRANSACTION CASH TRANSACTION
	@ColumnName("経過利子（外貨）")				public String accruedInterestForeignCurrency;   // NOT FOR DOMESTIC STOCK CASH TRANSACTION CASH TRANSACTION
	@ColumnName("経過日数（外債）")				public String elapsedDayForeignCurrency;        // NOT FOR DOMESTIC STOCK CASH TRANSACTION CASH TRANSACTION
	@ColumnName("所得税（外債）")				public String incomeTaxForeignCurrency;         // NOT FOR DOMESTIC STOCK CASH TRANSACTION CASH TRANSACTION
	@ColumnName("地方税（外債）")				public String localtaxForeignCurrency;          // NOT FOR DOMESTIC STOCK CASH TRANSACTION CASH TRANSACTION
	@ColumnName("金利・価格調整額（CFD）")		public String interestPriceAdjustmentCFD;       // NOT FOR DOMESTIC STOCK CASH TRANSACTION CASH TRANSACTION
	@ColumnName("配当金調整額（CFD）")			public String dividendAjdustmentCFD;            // NOT FOR DOMESTIC STOCK CASH TRANSACTION CASH TRANSACTION
	@ColumnName("売建単価（くりっく365）")		public String sellingUnitPriceClick365;         // NOT FOR DOMESTIC STOCK CASH TRANSACTION CASH TRANSACTION
	@ColumnName("買建単価（くりっく365）")		public String buyingUnitPriceClick365;          // NOT FOR DOMESTIC STOCK CASH TRANSACTION CASH TRANSACTION
	@ColumnName("円貨スワップ損益")				public String jpySwapProfitAndLoss;             // NOT FOR DOMESTIC STOCK CASH TRANSACTION CASH TRANSACTION
	@ColumnName("外貨スワップ損益")				public String foreignCurrentySwapProfitAndLoss; // NOT FOR DOMESTIC STOCK CASH TRANSACTION CASH TRANSACTION
	@ColumnName("約定金額（円貨）")				public int    tradePriceJPY;
	@ColumnName("約定金額（外貨）")				public String tradePriceForeignCurrency;        // NOT FOR DOMESTIC STOCK CASH TRANSACTION CASH TRANSACTION
	@ColumnName("決済金額（円貨）")				public String settlementPriceJPY;               // NOT FOR DOMESTIC STOCK CASH TRANSACTION CASH TRANSACTION
	@ColumnName("決済金額（外貨）")				public String settlementPriceForeignCurrency;   // NOT FOR DOMESTIC STOCK CASH TRANSACTION CASH TRANSACTION
	@ColumnName("実現損益")						public String realizedProfitAndLoss;
	@ColumnName("受渡金額（円貨）")				public int    settlementPrice;
	@ColumnName("備考")							public String comment;                          // 余力自動振替 証券コネクト口座から入金 コネクト自動出金 証券コネクト口座へ出金
	
	public Activity(
			String tradeDateTime,
			String tradeType,
			String settlementDate,
			String tradeNumber,
			String stockName,
			String stockCode,
			String contractMonth,
			String callOrPut,
			String excercisePrice,
			String excerciseCurrency,
			String coveredWarrantType,
			String transactionType,
			String currency,
			String market,
			String accountType,
			String marginType,
			int    tradeAmount,
			int    tradeUnitPrice,
			String conversionRate,
			int    fee,
			int    feeTax,
			String longUnitPrice,
			String newFee,
			String newFeeTax,
			String administrationFee,
			String transferFee,
			String interestRate,
			String stockLendingFee,
			String premiumFee,
			String previousDayMarkToMarket,
			String accruedInterestJPY,
			String accruedInterestForeignCurrency,
			String elapsedDayForeignCurrency,
			String incomeTaxForeignCurrency,
			String localtaxForeignCurrency,
			String interestPriceAdjustmentCFD,
			String dividendAjdustmentCFD,
			String sellingUnitPriceClick365,
			String buyingUnitPriceClick365,
			String jpySwapProfitAndLoss,
			String foreignCurrentySwapProfitAndLoss,
			int    tradePriceJPY,
			String tradePriceForeignCurrency,
			String settlementPriceJPY,
			String settlementPriceForeignCurrency,
			String realizedProfitAndLoss,
			int    settlementPrice,
			String comment
			) {
		this.tradeDateTime = tradeDateTime;
		this.tradeType = tradeType;
		this.settlementDate = settlementDate;
		this.tradeNumber = tradeNumber;
		this.stockName = stockName;
		this.stockCode = stockCode;
		this.contractMonth = contractMonth;
		this.callOrPut = callOrPut;
		this.excercisePrice = excercisePrice;
		this.excerciseCurrency = excerciseCurrency;
		this.coveredWarrantType = coveredWarrantType;
		this.transactionType = transactionType;
		this.currency = currency;
		this.market = market;
		this.accountType = accountType;
		this.marginType = marginType;
		this.tradeAmount = tradeAmount;
		this.tradeUnitPrice = tradeUnitPrice;
		this.conversionRate = conversionRate;
		this.fee = fee;
		this.feeTax = feeTax;
		this.longUnitPrice = longUnitPrice;
		this.newFee = newFee;
		this.newFeeTax = newFeeTax;
		this.administrationFee = administrationFee;
		this.transferFee = transferFee;
		this.interestRate = interestRate;
		this.stockLendingFee = stockLendingFee;
		this.premiumFee = premiumFee;
		this.previousDayMarkToMarket = previousDayMarkToMarket;
		this.accruedInterestJPY = accruedInterestJPY;
		this.accruedInterestForeignCurrency = accruedInterestForeignCurrency;
		this.elapsedDayForeignCurrency = elapsedDayForeignCurrency;
		this.incomeTaxForeignCurrency = incomeTaxForeignCurrency;
		this.localtaxForeignCurrency = localtaxForeignCurrency;
		this.interestPriceAdjustmentCFD = interestPriceAdjustmentCFD;
		this.dividendAjdustmentCFD = dividendAjdustmentCFD;
		this.sellingUnitPriceClick365 = sellingUnitPriceClick365;
		this.buyingUnitPriceClick365 = buyingUnitPriceClick365;
		this.jpySwapProfitAndLoss = jpySwapProfitAndLoss;
		this.foreignCurrentySwapProfitAndLoss = foreignCurrentySwapProfitAndLoss;
		this.tradePriceJPY = tradePriceJPY;
		this.tradePriceForeignCurrency = tradePriceForeignCurrency;
		this.settlementPriceJPY = settlementPriceJPY;
		this.settlementPriceForeignCurrency = settlementPriceForeignCurrency;
		this.realizedProfitAndLoss = realizedProfitAndLoss;
		this.settlementPrice = settlementPrice;
		this.comment = comment;
	}
	public Activity() {
		this(
			null, null, null, null, null, null, null, null, null, null,
			null, null, null, null, null, null,    0,    0, null,    0, 
			0,    null, null, null, null, null, null, null, null, null, 
			null, null, null, null, null, null, null, null, null, null, 
			null,    0, null, null, null, null, 0, null);
	}
	
	@Override
	public String toString() {
		return String.format("{%s!%s!%s!%s %s %d}", tradeDateTime, tradeType, settlementDate, tradeNumber, stockCode, settlementPrice);
	}
}
