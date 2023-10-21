package yokwe.finance.provider.nikkei;

import java.math.BigDecimal;

import yokwe.util.StringUtil;

public class DivScoreType implements Comparable<DivScoreType> {
	public static BigDecimal NO_VALUE = BigDecimal.ONE.negate();
	
	public static boolean hasValue(BigDecimal value) {
		return value.compareTo(NO_VALUE) != 0;
	}
	
	public String     isinCode;
	public BigDecimal score1Y;
	public BigDecimal score3Y;
	public BigDecimal score5Y;
	public BigDecimal score10Y;
	
	public DivScoreType(String isinCode, BigDecimal score1Y, BigDecimal socre3Y, BigDecimal socre5Y, BigDecimal score10Y) {
		this.isinCode = isinCode;
		this.score1Y  = score1Y;
		this.score3Y  = socre3Y;
		this.score5Y  = socre5Y;
		this.score10Y = score10Y;
	}

	@Override
	public int compareTo(DivScoreType that) {
		return this.isinCode.compareTo(that.isinCode);
	}
	
	@Override
	public String toString() {
		return StringUtil.toString(this);
	}
}
