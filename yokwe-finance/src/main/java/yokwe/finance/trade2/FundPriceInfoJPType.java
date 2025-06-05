package yokwe.finance.trade2;

import yokwe.util.ToString;

public class FundPriceInfoJPType implements Comparable<FundPriceInfoJPType>{
	public final String code;
	public final String name;
	public final int    units; // how many units represents fund price. if value is 10,000, fund price represents 10,000 units of fund
	
	public FundPriceInfoJPType(String code, String name, int units) {
		this.code  = code;
		this.name  = name;
		this.units = units;
	}

	@Override 
	public String toString() {
		return ToString.withFieldName(this);
	}
	@Override 
	public boolean equals(Object o) {
		if (o != null && o instanceof FundPriceInfoJPType) {
			FundPriceInfoJPType that = (FundPriceInfoJPType)o;
			return this.code.equals(that.code) && this.units == that.units;
		}
		return false;
	}
	
	@Override
	public int compareTo(FundPriceInfoJPType that) {
		return this.code.compareTo(that.code);
	}
}
