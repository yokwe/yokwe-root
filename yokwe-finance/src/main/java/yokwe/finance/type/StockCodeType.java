package yokwe.finance.type;

public class StockCodeType implements Comparable<StockCodeType> {
	public String stockCode;
	
	public StockCodeType(String stockCode) {
		this.stockCode = stockCode;
	}
	public StockCodeType() {}
	
	@Override
	public String toString() {
		return String.format("%s", stockCode);
	}
	@Override
	public int compareTo(StockCodeType that) {
		return this.stockCode.compareTo(that.stockCode);
	}
	@Override
	public boolean equals(Object o) {
		if (o instanceof StockCodeType) {
			StockCodeType that = (StockCodeType)o;
			return this.stockCode.equals(that.stockCode);
		} else {
			return false;
		}
	}
}
