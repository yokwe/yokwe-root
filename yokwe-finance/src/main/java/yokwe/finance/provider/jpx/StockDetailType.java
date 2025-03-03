package yokwe.finance.provider.jpx;

import java.math.BigDecimal;

import yokwe.util.StringUtil;

public class StockDetailType implements Comparable<StockDetailType> {
	public enum SimpleType {
		STOCK,
		ETF,
		ETN,
		REIT,
		INFRA,
		OTHER,
		NEW,
	}
	public static enum Type {
		// STOCK
		DOMESTIC_PRIME(SimpleType.STOCK),
		DOMESTIC_STANDARD(SimpleType.STOCK),
		DOMESTIC_GROWTH(SimpleType.STOCK),
		FOREIGN_PRIME(SimpleType.STOCK),
		FOREIGN_STANDARD(SimpleType.STOCK),
		FOREIGN_GROWTH(SimpleType.STOCK),
		// ETF
		ETF(SimpleType.ETF),
		// ETN
		ETN(SimpleType.ETN),
		// REIT
		REIT(SimpleType.REIT),
		INFRA_FUND(SimpleType.INFRA),
		// OTHER
		CERTIFICATE(SimpleType.OTHER);
		
		public final SimpleType simpleType;
		
		private Type(SimpleType simpleType) {
			this.simpleType = simpleType;
		}
	}
	
	public String     stockCode;
	public String     isinCode;
	public int        tradeUnit;
	public Type       type;
	public String     sector33;
	
	public BigDecimal issued;
	
	public String     name;
	
	public StockDetailType(
		String     stockCode,
		String     isinCode,
		int        tradeUnit,
		Type       type,
		String     sector33,
		BigDecimal issued,
		String     name
		) {
		this.stockCode = stockCode;
		this.isinCode  = isinCode;
		this.tradeUnit = tradeUnit;
		this.type      = type;
		this.sector33  = sector33;
		this.issued    = issued;
		this.name      = name;
	}
	
	public String getKey() {
		return stockCode;
	}
	@Override
	public int compareTo(StockDetailType that) {
		return this.stockCode.compareTo(that.stockCode);
	}
	@Override
	public String toString() {
		return StringUtil.toString(this);
	}
}
