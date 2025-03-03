package yokwe.finance.provider.jpx;

import java.math.BigDecimal;

import yokwe.util.StringUtil;

public class StockDetailType implements Comparable<StockDetailType> {
	public String     stockCode;
	public String     isinCode;
	public int        tradeUnit;
	public String     type;
	public String     sector33;
	
	public BigDecimal issued;
	
	public String     name;
	
	public StockDetailType(
		String     stockCode,
		String     isinCode,
		int        tradeUnit,
		String     type,
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
