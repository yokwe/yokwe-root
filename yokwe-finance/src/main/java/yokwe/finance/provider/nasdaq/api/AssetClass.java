package yokwe.finance.provider.nasdaq.api;

import yokwe.finance.type.StockInfoUS.Type;

public enum AssetClass {
	STOCK("stocks"),
	ETF  ("etf");
	
	public static AssetClass getInstance(Type type) {
		return type.isETF() ? ETF : STOCK;
	}
	
	public final String value;
	AssetClass(String value) {
		this.value = value;
	}
	
	@Override
	public String toString() {
		return value;
	}
}