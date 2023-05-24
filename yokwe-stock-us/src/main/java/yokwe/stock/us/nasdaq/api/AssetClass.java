package yokwe.stock.us.nasdaq.api;

import yokwe.stock.us.Stock.SimpleType;
import yokwe.stock.us.Stock.Type;

public enum AssetClass {
	STOCK("stocks"),
	ETF  ("etf");
	
	public static AssetClass getInstance(Type type) {
		return type.simpleType == SimpleType.ETF ? ETF : STOCK;
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