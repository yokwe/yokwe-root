package yokwe.finance.trade2;

import yokwe.util.ToString;

public class FundPriceInfoJPType implements Comparable<FundPriceInfoJPType>{
	public final String code;
	public final String name;
	public final int    value;
	
	public FundPriceInfoJPType(String code, String name, int value) {
		this.code  = code;
		this.value = value;
		this.name  = name;
	}

	@Override 
	public String toString() {
		return ToString.withFieldName(this);
	}
	@Override 
	public boolean equals(Object o) {
		if (o != null && o instanceof FundPriceInfoJPType) {
			FundPriceInfoJPType that = (FundPriceInfoJPType)o;
			return this.code.equals(that.code) && this.value == that.value;
		}
		return false;
	}
	
	@Override
	public int compareTo(FundPriceInfoJPType that) {
		return this.code.compareTo(that.code);
	}
}
