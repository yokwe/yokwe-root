package yokwe.finance.trade2;

import yokwe.util.ToString;
import yokwe.util.UnexpectedException;

public class FundPriceInfoJPType implements Comparable<FundPriceInfoJPType> {
	private static final org.slf4j.Logger logger = yokwe.util.LoggerUtil.getLogger();
	
	public final String code;
	public final String name;
	public final int    units; // how many units represents fund price. if value is 10,000, fund price represents 10,000 units of fund
	public final int    shift; // log10(units)
	
	public FundPriceInfoJPType(String code, String name, int units, int shift) {
		this.code  = code;
		this.name  = name;
		this.units = units;
		this.shift = shift;
		
		// sanity check
		{
			var units2 = (int)Math.pow(10, shift);
			if (units != units2) {
				logger.error("Unexpected shift");
				logger.error("  code    {}", code);
				logger.error("  units   {}", units);
				logger.error("  shift   {}", shift);
				logger.error("  units2  {}", units2);
				throw new UnexpectedException("Unexpected shift");
			}
		}
	}
	public FundPriceInfoJPType(String code, String name, int units) {
		this(code, name, units, (int)Math.log10(units));
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
