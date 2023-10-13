package yokwe.finance.type;

import java.math.BigDecimal;

public class TradingFundType implements Comparable<TradingFundType> {
	public static final BigDecimal SALES_FEE_UNKNOWN = BigDecimal.valueOf(-1);
	
	public String     isinCode;
	public BigDecimal salesFee;  // 0 for no load
	
	public TradingFundType(String isinCode, BigDecimal salesFee) {
		this.isinCode = isinCode;
		this.salesFee = salesFee;
	}
	
	@Override
	public String toString() {
		return String.format("{%s  %s}", isinCode, salesFee);
	}
	@Override
	public int compareTo(TradingFundType that) {
		return this.isinCode.compareTo(that.isinCode);
	}
	@Override
	public boolean equals(Object o) {
		if (o instanceof TradingFundType) {
			TradingFundType that = (TradingFundType)o;
			return
				this.isinCode.equals(that.isinCode) &&
				this.salesFee.compareTo(that.salesFee) == 0;
		} else {
			return false;
		}
	}
}
