package yokwe.finance.provider.jpx;

import yokwe.util.StringUtil;

public class StockDetailType implements Comparable<StockDetailType> {
	public final String stockCode;
	public final String isinCode;
	public final int    tradeUnit;
	public final long   issued;
	
	public StockDetailType(String stockCode, String isinCode, int tradeUnit, long issued) {
		this.stockCode = stockCode;
		this.isinCode  = isinCode;
		this.tradeUnit = tradeUnit;
		this.issued    = issued;
	}
	
	
	@Override
	public String toString() {
		return StringUtil.toString(this);
	}
	
	@Override
	public int compareTo(StockDetailType that) {
		return this.stockCode.compareTo(that.stockCode);
	}
	@Override
	public boolean equals(Object o) {
		if (o == null) return false;
		if (o instanceof StockDetailType) {
			StockDetailType that = (StockDetailType)o;
			return 
				this.stockCode.equals(that.stockCode) &&
				this.isinCode.equals(that.isinCode) &&
				this.tradeUnit == that.tradeUnit &&
				this.issued == that.issued;
		} else {
			return false;
		}
	}
}
