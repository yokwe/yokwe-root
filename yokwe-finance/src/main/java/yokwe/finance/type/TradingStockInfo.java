package yokwe.finance.type;

public class TradingStockInfo implements Comparable<TradingStockInfo>{
	public enum FeeType {
		PAID,
		BUY_FREE,
		FREE,
	}
	public enum TradeType {
		BUY_SELL,
		SELL,
		PROHIBIT,
	}
	
	public String    stockCode;
	public FeeType   feeType;
	public TradeType tradeType;
	
	public TradingStockInfo(String stockCode, FeeType feeType, TradeType tradeType) {
		this.stockCode = stockCode;
		this.feeType   = feeType;
		this.tradeType = tradeType;
	}
	public TradingStockInfo() {}
	
	@Override
	public String toString() {
		return String.format("{%s  %s  %s}", stockCode, feeType, tradeType);
	}
	@Override
	public int compareTo(TradingStockInfo that) {
		return this.stockCode.compareTo(that.stockCode);
	}
	@Override
	public boolean equals(Object o) {
		if (o instanceof TradingStockInfo) {
			TradingStockInfo that = (TradingStockInfo)o;
			return
				this.stockCode.equals(that.stockCode) &&
				this.feeType == that.feeType &&
				this.tradeType == that.tradeType;
		} else {
			return false;
		}
	}
}
