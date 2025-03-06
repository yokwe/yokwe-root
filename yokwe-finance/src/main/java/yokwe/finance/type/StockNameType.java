package yokwe.finance.type;

public class StockNameType implements Comparable<StockNameType> {
	public String stockCode;
	public String name;
	
	public StockNameType(String stockCode, String name) {
		this.stockCode = stockCode;
		this.name      = name;
	}
	
	public String getKey() {
		return stockCode;
	}
	
	@Override
	public String toString() {
		return String.format("{%s  %s}", stockCode, name);
	}
	
	@Override
	public int compareTo(StockNameType that) {
		return this.stockCode.compareTo(that.stockCode);
	}
	@Override
	public boolean equals(Object o) {
		if (o == null) return false;
		if (o instanceof StockNameType) {
			StockNameType that = (StockNameType)o;
			return 
				this.stockCode.equals(that.stockCode) &&
				this.name.equals(that.name);
		} else {
			return false;
		}
	}
}
