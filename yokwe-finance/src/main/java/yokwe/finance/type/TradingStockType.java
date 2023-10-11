package yokwe.finance.type;

public class TradingStockType implements Comparable<TradingStockType>{
	public enum FeeType {
		PAID(1),
		BUY_FREE(2),
		FREE(3);
		
		public final int value;
		
		private FeeType(int value) {
			this.value = value;
		}
	}
	public enum TradeType {
		BUY_SELL(1),
		SELL(2);
		
		public final int value;
		
		private TradeType(int value) {
			this.value = value;
		}
	}
	
	public String    stockCode;
	public FeeType   feeType;
	public TradeType tradeType;
	
	public TradingStockType(String stockCode, FeeType feeType, TradeType tradeType) {
		this.stockCode = stockCode;
		this.feeType   = feeType;
		this.tradeType = tradeType;
	}
	
	@Override
	public String toString() {
		return String.format("{%s  %s  %s}", stockCode, feeType, tradeType);
	}
	@Override
	public int compareTo(TradingStockType that) {
		return this.stockCode.compareTo(that.stockCode);
	}
	@Override
	public boolean equals(Object o) {
		if (o instanceof TradingStockType) {
			TradingStockType that = (TradingStockType)o;
			return
				this.stockCode.equals(that.stockCode) &&
				this.feeType == that.feeType &&
				this.tradeType == that.tradeType;
		} else {
			return false;
		}
	}
}
