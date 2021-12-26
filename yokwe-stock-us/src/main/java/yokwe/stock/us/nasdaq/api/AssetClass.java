package yokwe.stock.us.nasdaq.api;

public enum AssetClass {
	STOCK("stocks"),
	ETF  ("etf");
	
	public final String value;
	AssetClass(String value) {
		this.value = value;
	}
	
	@Override
	public String toString() {
		return value;
	}
}